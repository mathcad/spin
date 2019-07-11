package org.spin.core.util.http;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.Handler;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.IOUtils;
import org.spin.core.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * 利用Apache HttpClient完成请求
 * <p>支持异步请求与连接池</p>
 * <p>Created by xuweinan on 2018/4/9.</p>
 *
 * @author xuweinan
 * @version V1.2
 */
public abstract class HttpExecutor {
    private static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);
    private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();
    private static final HttpInitializer INITIALIZER = new HttpInitializer();
    private static final int DEFAULT_MAX_TOTAL = 200;
    private static final int DEFAULT_MAX_PER_ROUTE = 40;

    private static int socketTimeout = 60000;
    private static int connectTimeout = 60000;

    private static int maxTotal = DEFAULT_MAX_TOTAL;
    private static int maxPerRoute = DEFAULT_MAX_PER_ROUTE;

    private static CloseableHttpAsyncClient httpAsyncClient;
    private static CloseableHttpClient httpClient;

    private static volatile byte[] certificate;
    private static volatile String password;
    private static volatile String algorithm;
    private static volatile boolean needReloadSync = true;
    private static volatile boolean needReloadAsync = true;

    /**
     * 默认重试机制
     */
    private static HttpRequestRetryHandler defaultHttpRetryHandler = (exception, executionCount, context) -> {
        if (executionCount >= 5) {// 如果已经重试了5次，就放弃
            return false;
        }
        if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
            return true;
        }
        if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
            return false;
        }
        if (exception instanceof InterruptedIOException) {// 超时
            return false;
        }
        if (exception instanceof UnknownHostException) {// 目标服务器不可达
            return false;
        }
        if (exception instanceof SSLException) {// SSL握手异常
            return false;
        }

        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        // 如果请求是幂等的，就再次尝试
        return !(request instanceof HttpEntityEnclosingRequest);
    };


    public static class HttpInitializer {

        private volatile Thread currentThread;
        private boolean changed = false;

        public HttpInitializer withSocketTimeout(int socketTimeout) {
            checkThread();
            changed = changed || HttpExecutor.socketTimeout != socketTimeout;
            HttpExecutor.socketTimeout = socketTimeout;
            return this;
        }

        public HttpInitializer withConnectTimeout(int connectTimeout) {
            checkThread();
            changed = changed || HttpExecutor.connectTimeout != connectTimeout;
            HttpExecutor.connectTimeout = connectTimeout;
            return this;
        }

        public HttpInitializer withMaxTotal(int maxTotal) {
            checkThread();
            changed = changed || HttpExecutor.maxTotal != maxTotal;
            HttpExecutor.maxTotal = maxTotal;
            return this;
        }

        public HttpInitializer withMaxPerRoute(int maxPerRoute) {
            checkThread();
            changed = changed || HttpExecutor.maxPerRoute != maxPerRoute;
            HttpExecutor.maxPerRoute = maxPerRoute;
            return this;
        }

        public HttpInitializer withCertificate(InputStream certsInput, String password, String algorithm) {
            checkThread();
            changed = true;
            try {
                HttpExecutor.certificate = IOUtils.copyToByteArray(certsInput);
            } catch (IOException e) {
                throw new SimplifiedException("读取证书内容失败");
            }
            HttpExecutor.password = password;
            HttpExecutor.algorithm = algorithm;
            return this;
        }

        public HttpInitializer withRetryHandler(HttpRequestRetryHandler retryHandler) {
            checkThread();
            changed = changed || HttpExecutor.defaultHttpRetryHandler != retryHandler;
            HttpExecutor.defaultHttpRetryHandler = retryHandler;
            return this;
        }

        public void finishConfigure() {
            if (changed) {
                needReloadSync = true;
                needReloadAsync = true;
            }
            reset();
        }

        private void checkThread() {
            Assert.isTrue(currentThread == Thread.currentThread(), "HttpExecutor禁止跨线程配置");
        }

        private void reset() {
            currentThread = null;
            changed = false;
        }
    }

    // region init and getter/setter

    public static HttpInitializer configure() {
        synchronized (INITIALIZER) {
            Assert.isTrue(INITIALIZER.currentThread == null, "同时只能有一个客户端配置HttpExecutor");
            INITIALIZER.currentThread = Thread.currentThread();
            return INITIALIZER;
        }
    }

    public static int getSocketTimeout() {
        return socketTimeout;
    }

    public static int getConnectTimeout() {
        return connectTimeout;
    }

    public static int getDefaultMaxTotal() {
        return maxTotal;
    }

    public static int getDefaultMaxPerRoute() {
        return maxPerRoute;
    }

    public static HttpRequestRetryHandler getDefaultHttpRetryHandler() {
        return defaultHttpRetryHandler;
    }

    // endregion

    // region executor

    /**
     * 执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request    请求对象，可以通过Method枚举构造
     * @param entityProc 请求结果处理器
     * @param <T>        处理后的返回类型
     * @return 处理后的请求结果
     */
    public static <T> T executeRequest(HttpUriRequest request, EntityProcessor<T> entityProc) {
        T res;
        initSync();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int code = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (code != 200) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "\n错误状态码:" + code + "\n响应:" + toStringProc(entity));
            }
            res = Assert.notNull(entityProc, "请求结果处理器不能为空").process(entity);
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage());
        }
        return res;
    }


    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request           请求对象，可以通过Method枚举构造
     * @param entityProc        请求结果处理器
     * @param completedCallback 请求成功时的回调
     * @param failedCallback    请求失败时的回调
     * @param cancelledCallback 请求取消后的回调
     * @param <T>               处理后的返回类型
     * @return 包含请求结果的Future对象
     */
    public static <T> Future<HttpResponse> executeRequestAsync(HttpUriRequest request, EntityProcessor<T> entityProc,
                                                               FinalConsumer<T> completedCallback,
                                                               FinalConsumer<Exception> failedCallback,
                                                               Handler cancelledCallback) {
        try {
            initAync();
            return httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    int code = result.getStatusLine().getStatusCode();
                    HttpEntity entity = result.getEntity();
                    if (code != 200) {
                        failed(new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "\n错误状态码:" + code + "\n响应:" + toStringProc(entity)));
                    }
                    if (null != completedCallback) {
                        try {
                            T res = entityProc.process(entity);
                            completedCallback.accept(res);
                        } catch (Exception e) {
                            failedCallback.accept(e);
                        }
                    } else {
                        logger.info("请求[{}]执行成功:\n{}", request.getURI(), entity);
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
                    if (null != failedCallback) {
                        cancelledCallback.handle();
                    } else {
                        logger.error("请求[{}]被取消", request.getURI());
                    }
                }
            });
        } catch (Exception e) {
            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage());
        }
    }
    // endregion

    // region common method
    public static String toStringProc(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, getContentCharSet(entity));
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
        }
    }

    public static Map<String, String> downloadProc(HttpEntity entity, String savePath) {
        Map<String, String> map = new HashMap<>();
        String saveFile = savePath;
        String contentType = entity.getContentType().getValue();
        String extention = contentType.substring(contentType.indexOf('/') + 1);
        if (StringUtils.isNotBlank(savePath))
            saveFile = savePath + "." + extention;
        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            byte[] bytes = EntityUtils.toByteArray(entity);
            fos.write(bytes);
            map.put("extention", StringUtils.isBlank(extention) ? "" : "." + extention);
            map.put("bytes", Integer.toString(bytes.length));
        } catch (IOException e) {
            throw new SimplifiedException("无法保存文件:[" + saveFile + "]", e);
        }
        return map;
    }

    private static String getContentCharSet(final HttpEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("schema entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement[] values = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }

        if (StringUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }

    private static SSLContext buildSSLContext(InputStream certsInput, String password, String algorithm) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance(algorithm);
        keyStore.load(certsInput, StringUtils.trimToEmpty(password).toCharArray());

        keyManagerFactory.init(keyStore, StringUtils.trimToEmpty(password).toCharArray());

        SSLContext sslContext = SSLContexts.custom()
            .setProtocol("TLS")
            .loadTrustMaterial((chain, authType) -> true)
            .build();
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        return sslContext;
    }

    private static void initSync() {
        if (needReloadSync) {
            synchronized (HttpExecutor.class) {
                if (needReloadSync) {
                    needReloadSync = false;
                    SSLConnectionSocketFactory sslConnectionSocketFactory = null;
                    if (null != certificate && StringUtils.isNotEmpty(algorithm)) {
                        try (InputStream certInput = new ByteArrayInputStream(certificate)) {
                            SSLContext sslContext = buildSSLContext(certInput, password, algorithm);
                            sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                                sslContext.getSocketFactory(),
                                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
                        } catch (Exception e) {
                            throw new SimplifiedException(e);
                        }
                    }
                    PoolingHttpClientConnectionManager connectionManager = null == sslConnectionSocketFactory ? new PoolingHttpClientConnectionManager() : new PoolingHttpClientConnectionManager(
                        RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslConnectionSocketFactory)
                            .build());
                    connectionManager.setMaxTotal(maxTotal);
                    connectionManager.setDefaultMaxPerRoute(maxPerRoute);
                    httpClient = HttpClients.custom().setRetryHandler(defaultHttpRetryHandler).setConnectionManager(connectionManager)
                        .build();
                }
            }
        }
    }

    private static void initAync() {
        if (needReloadAsync) {
            synchronized (HttpExecutor.class) {
                if (needReloadAsync) {
                    needReloadAsync = false;
                    if (null != certificate && StringUtils.isNotEmpty(algorithm)) {
                        try (InputStream certInput = new ByteArrayInputStream(certificate)) {
                            SSLContext sslContext = buildSSLContext(certInput, password, algorithm);
                            SchemeIOSessionStrategy sslStrategy = new SSLIOSessionStrategy(sslContext,
                                new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault()));
                            final ConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT, THREAD_FACTORY);
                            final PoolingNHttpClientConnectionManager poolingmgr = new PoolingNHttpClientConnectionManager(
                                ioreactor,
                                RegistryBuilder.<SchemeIOSessionStrategy>create()
                                    .register("http", NoopIOSessionStrategy.INSTANCE)
                                    .register("https", sslStrategy)
                                    .build());
                            httpAsyncClient = HttpAsyncClients.custom().setConnectionManager(poolingmgr).setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
                            httpAsyncClient.start();
                            return;
                        } catch (Exception e) {
                            throw new SimplifiedException("构建SSL安全上下文失败", e);
                        }
                    }

                    httpAsyncClient = HttpAsyncClients.custom().setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
                    httpAsyncClient.start();
                }
            }
        }
    }
    // endregion
}
