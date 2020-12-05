package org.spin.core.util.http;

import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.Handler;
import org.spin.core.throwable.SpinException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * 异步Http客户端持有者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/7/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class HttpExecutorAsyncHolder {
    private static final Logger logger = LoggerFactory.getLogger(HttpExecutorAsyncHolder.class);

    private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();
    private static volatile CloseableHttpAsyncClient httpAsyncClient;

    static void initAsync(int maxTotal, int maxPerRoute,
                          byte[] keyStore, String keyStorePass, KeyStoreType keyStoreType,
                          Map<String, KeyStore.ProtectionParameter> keysPass,
                          byte[] trustStore, String trustStorePass, KeyStoreType trustStoreType,
                          HostnameVerifier hostnameVerifier) {
        if (null != keyStore && null != keyStoreType) {
            try {
                SSLContext sslContext = HttpExecutorSyncHolder.buildSSLContext(keyStore, keyStorePass, keyStoreType, keysPass,
                    trustStore, trustStorePass, trustStoreType);
                SSLIOSessionStrategy sslStrategy = new SSLIOSessionStrategy(sslContext, null == hostnameVerifier ? SSLConnectionSocketFactory.getDefaultHostnameVerifier() : hostnameVerifier);
                final org.apache.http.nio.reactor.ConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT, THREAD_FACTORY);
                final PoolingNHttpClientConnectionManager poolingmgr =
                    new PoolingNHttpClientConnectionManager(
                        ioreactor,
                        RegistryBuilder.<org.apache.http.nio.conn.SchemeIOSessionStrategy>create()
                            .register("http", NoopIOSessionStrategy.INSTANCE)
                            .register("https", sslStrategy)
                            .build());
                httpAsyncClient = HttpAsyncClients.custom().setConnectionManager(poolingmgr).setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
                httpAsyncClient.start();
                return;
            } catch (Exception e) {
                throw new SpinException("构建SSL安全上下文失败", e);
            }
        }

        httpAsyncClient = org.apache.http.impl.nio.client.HttpAsyncClients.custom().setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
        httpAsyncClient.start();
    }

    static <T> Future<T> execute(
        HttpUriRequest request,
        EntityProcessor<T> entityProc,
        FinalConsumer<T> completedCallback,
        FinalConsumer<Exception> failedCallback,
        Handler cancelledCallback,
        boolean checkResponseStatus) {

        return httpAsyncClient.execute(HttpAsyncMethods.create(determineTarget(request), request),
            new BaseAsyncResponseConsumer<>(entityProc, checkResponseStatus),
            new BasicFutureCallback<>(request, completedCallback, failedCallback, cancelledCallback));
    }

    private static HttpHost determineTarget(final HttpUriRequest request) {
        Args.notNull(request, "HTTP request");
        // A null target may be acceptable if there is a default target.
        // Otherwise, the null target is detected in the director.
        HttpHost target = null;

        final URI requestURI = request.getURI();
        if (requestURI.isAbsolute()) {
            target = URIUtils.extractHost(requestURI);
            if (target == null) {
                throw new SpinException(
                    "URI does not specify a valid host name: " + requestURI);
            }
        }
        return target;
    }

    private static class BasicFutureCallback<T> implements FutureCallback<T> {
        private final HttpUriRequest request;
        private final FinalConsumer<T> completedCallback;
        private final FinalConsumer<Exception> failedCallback;
        private final Handler cancelledCallback;

        private BasicFutureCallback(HttpUriRequest request, FinalConsumer<T> completedCallback, FinalConsumer<Exception> failedCallback, Handler cancelledCallback) {
            this.request = request;
            this.completedCallback = completedCallback;
            this.failedCallback = failedCallback;
            this.cancelledCallback = cancelledCallback;
        }

        @Override
        public void completed(T result) {
            if (null != completedCallback) {
                completedCallback.accept(result);
            }
        }

        @Override
        public void failed(Exception ex) {
            if (null != failedCallback) {
                failedCallback.accept(ex);
            } else {
                logger.error(String.format("请求[%s]执行失败", request.getURI()), ex);
            }
        }

        @Override
        public void cancelled() {
            if (null != cancelledCallback) {
                cancelledCallback.handle();
            } else {
                logger.error("请求[{}]被取消", request.getURI());
            }
        }
    }

    private static class BaseAsyncResponseConsumer<T> extends AbstractAsyncResponseConsumer<T> {
        private static final int MAX_INITIAL_BUFFER_SIZE = 256 * 1024;

        private volatile HttpResponse response;
        private volatile SimpleInputBuffer buf;
        private final boolean checkResponseStatus;
        private final EntityProcessor<T> entityProc;

        public BaseAsyncResponseConsumer(EntityProcessor<T> entityProc, boolean checkResponseStatus) {
            this.checkResponseStatus = checkResponseStatus;
            this.entityProc = entityProc;
        }

        @Override
        protected void onResponseReceived(HttpResponse response) {
            this.response = response;
        }

        @Override
        protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
            Asserts.notNull(this.buf, "Content buffer");
            this.buf.consumeContent(decoder);
        }

        @Override
        protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
            long len = entity.getContentLength();
            if (len > Integer.MAX_VALUE) {
                throw new ContentTooLongException("Entity content is too long: " + len);
            }
            if (len < 0) {
                len = 4096;
            }
            final int initialBufferSize = Math.min((int) len, MAX_INITIAL_BUFFER_SIZE);
            this.buf = new SimpleInputBuffer(initialBufferSize, new HeapByteBufferAllocator());
            this.response.setEntity(new ContentBufferEntity(entity, this.buf));
        }

        @Override
        protected T buildResult(HttpContext context) {
            int code = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if ((2 != code / 100) && checkResponseStatus) {
                throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "\n错误状态码:" + code + "\n响应:" + HttpExecutor.toStringProc(entity));
            }
            return entityProc.process(entity);
        }

        @Override
        protected void releaseResources() {
            this.response = null;
            this.buf = null;
        }
    }
}
