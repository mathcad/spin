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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.Handler;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.io.StreamProgress;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.IOUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.Util;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 利用Apache HttpClient完成请求
 * <p>支持异步请求与连接池</p>
 * <p>Created by xuweinan on 2018/4/9.</p>
 *
 * @author xuweinan
 * @version V1.2
 */
public final class HttpExecutor extends Util {
    private static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);
    private static final HttpInitializer INITIALIZER = new HttpInitializer();
    private static final int DEFAULT_MAX_TOTAL = 200;
    private static final int DEFAULT_MAX_PER_ROUTE = 40;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final StreamProgress DEFAULT_PROGRESS = (p, r) -> System.out.println("Download Progress: " + p + "%");

    private static int socketTimeout = 60000;
    private static int connectTimeout = 60000;

    private static int maxTotal = DEFAULT_MAX_TOTAL;
    private static int maxPerRoute = DEFAULT_MAX_PER_ROUTE;


    private static volatile byte[] keyStore;
    private static volatile String keyStorePass;
    private static final Map<String, KeyStore.ProtectionParameter> keysPass = new HashMap<>();
    private static volatile KeyStoreType keyStoreType;

    private static volatile byte[] trustStore;
    private static volatile String trustStorePass;
    private static volatile KeyStoreType trustStoreType;

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

        /**
         * 配置HTTPS证书KeyStore
         *
         * <pre>
         * keystoreType 可用的值
         *     JCEKS
         *     JKS
         *     DKS
         *     PKCS11
         *     PKCS12
         *     Windows-MY
         *     BKS
         * </pre>
         *
         * @param keyStoreInput keyStore 输入流
         * @param keyStorePass  keyStore 密码
         * @param keyStoreType  keyStore 类型
         * @param keysPass      keyStore 中私钥的密码
         * @return HttpInitializer
         */
        public HttpInitializer withKeyStore(InputStream keyStoreInput, String keyStorePass, KeyStoreType keyStoreType, Map<String, String> keysPass) {
            checkThread();
            changed = true;
            try {
                HttpExecutor.keyStore = IOUtils.copyToByteArray(keyStoreInput);
            } catch (IOException e) {
                throw new SpinException("读取证书内容失败");
            }
            HttpExecutor.keyStorePass = keyStorePass;
            HttpExecutor.keyStoreType = keyStoreType;

            HttpExecutor.keysPass.clear();
            if (null != keysPass) {
                keysPass.forEach((k, v) -> HttpExecutor.keysPass.put(k, new KeyStore.PasswordProtection(v.toCharArray())));
            }
            return this;
        }

        /**
         * 配置HTTPS证书TrustStore
         *
         * <pre>
         * keystoreType 可用的值
         *     JCEKS
         *     JKS
         *     DKS
         *     PKCS11
         *     PKCS12
         *     Windows-MY
         *     BKS
         * </pre>
         *
         * @param trustStoreInput trustStore 输入流
         * @param trustStorePass  trustStore 密码
         * @param trustStoreType  trustStore 类型
         * @return HttpInitializer
         */
        public HttpInitializer withTrustStore(InputStream trustStoreInput, String trustStorePass, KeyStoreType trustStoreType) {
            checkThread();
            changed = true;
            try {
                HttpExecutor.trustStore = IOUtils.copyToByteArray(trustStoreInput);
            } catch (IOException e) {
                throw new SpinException("读取证书内容失败");
            }
            HttpExecutor.trustStorePass = trustStorePass;
            HttpExecutor.trustStoreType = trustStoreType;
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
     * @param request             请求对象，可以通过Method枚举构造
     * @param entityProc          请求结果处理器
     * @param checkResponseStatus 是否检查响应状态
     * @param <T>                 处理后的返回类型
     * @return 处理后的请求结果
     */
    public static <T> T executeRequest(HttpUriRequest request, EntityProcessor<T> entityProc, boolean checkResponseStatus) {
        initSync();

        CloseableHttpResponse response = null;
        HttpEntity entity;
        int code;

        try {
            response = HttpExecutorSyncHolder.getClient().execute(request);
            code = response.getStatusLine().getStatusCode();
            entity = response.getEntity();
        } catch (Exception e) {
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable e2) {
                    e.addSuppressed(e2);
                }
            }

            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage(), e);
        }

        try {
            if ((2 != code / 100) && checkResponseStatus) {
                throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "\n错误状态码:" + code + "\n响应:" + toStringProc(entity));
            }
            return Assert.notNull(entityProc, "请求结果处理器不能为空").process(entity);
        } finally {
            try {
                EntityUtils.consume(entity);
                response.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }


    /**
     * 异步执行自定义请求，并通过自定义方式转换请求结果
     *
     * @param request             请求对象，可以通过Method枚举构造
     * @param entityProc          请求结果处理器
     * @param completedCallback   请求成功时的回调
     * @param failedCallback      请求失败时的回调
     * @param cancelledCallback   请求取消后的回调
     * @param checkResponseStatus 是否检查响应状态
     * @param <T>                 处理后的返回类型
     * @return 包含请求结果的Future对象
     */
    public static <T> Future<HttpResponse> executeRequestAsync(HttpUriRequest request, EntityProcessor<T> entityProc,
                                                               FinalConsumer<T> completedCallback,
                                                               FinalConsumer<Exception> failedCallback,
                                                               Handler cancelledCallback,
                                                               boolean checkResponseStatus) {
        try {
            initAync();
            return HttpExecutorAsyncHolder.getClient().execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    int code = result.getStatusLine().getStatusCode();
                    HttpEntity entity = result.getEntity();
                    if ((2 != code / 100) && checkResponseStatus) {
                        failed(new SpinException(ErrorCode.NETWORK_EXCEPTION, "\n错误状态码:" + code + "\n响应:" + toStringProc(entity)));
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
        } catch (SpinException e) {
            throw e;
        } catch (Exception e) {
            logger.error("远程连接到" + request.getURI() + "，发生错误:", e);
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "远程连接到"
                + request.getURI()
                + "，发生错误: "
                + e.getMessage(), e);
        }
    }
    // endregion

    // region common method
    public static String toStringProc(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, getContentCharSet(entity));
        } catch (IOException e) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "转换请求结果发生错误", e);
        }
    }

    public static <T> T toObjectProc(HttpEntity entity, Class<T> clazz) {
        return JsonUtils.fromJson(toStringProc(entity), clazz);
    }

    public static <T> T toObjectProc(HttpEntity entity, Type type) {
        return JsonUtils.fromJson(toStringProc(entity), type);
    }

    public static <T> T toObjectProc(HttpEntity entity, TypeToken<T> typeToken) {
        return JsonUtils.fromJson(toStringProc(entity), typeToken);
    }

    public static Map<String, String> downloadProc(HttpEntity entity, String savePath, StreamProgress progress) {
        Map<String, String> map = new HashMap<>();
        String saveFile = savePath;
        String contentType = entity.getContentType().getValue();
        long contentLength = entity.getContentLength();
        long inProgress = 0L;
        String extention = contentType.substring(contentType.indexOf('/') + 1);
        if (StringUtils.isNotBlank(savePath)) {
            saveFile = savePath + "." + extention;
        }
        StreamProgress sp = null == progress ? DEFAULT_PROGRESS : progress;
        sp.start();
        try (InputStream inStream = entity.getContent(); FileOutputStream fos = new FileOutputStream(saveFile)) {
            if (inStream != null) {
                final byte[] tmp = new byte[DEFAULT_BUFFER_SIZE];
                int l;
                while ((l = inStream.read(tmp)) != -1) {
                    fos.write(tmp, 0, l);
                    inProgress += l;
                    sp.progress(inProgress / contentLength);
                }
            }
            fos.flush();

            map.put("extention", StringUtils.isBlank(extention) ? "" : "." + extention);
            map.put("bytes", Long.toString(contentLength));
        } catch (IOException e) {
            throw new SpinException("无法保存文件:[" + saveFile + "]", e);
        }
        sp.finish();
        return map;
    }

    public static void downloadProc(HttpEntity entity, StreamSliceConsumer slice) {
        String contentType = entity.getContentType().getValue();
        long contentLength = entity.getContentLength();
        double inProgress = 0L;
        slice.start(contentType, contentLength);
        try (InputStream inStream = entity.getContent()) {
            if (inStream != null) {
                final byte[] tmp = new byte[DEFAULT_BUFFER_SIZE];
                int l;
                while ((l = inStream.read(tmp)) != -1) {
                    inProgress += l;
                    slice.accept(tmp, l, inProgress / contentLength);
                }
            }

        } catch (IOException e) {
            throw new SpinException("下载文件失败", e);
        }
        slice.finish();
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

    private static void initSync() {
        if (INITIALIZER.currentThread != null) {
            throw new SpinException("Http客户端尚未配置完成，请先完成配置再使用");
        }
        if (needReloadSync) {
            synchronized (HttpExecutor.class) {
                if (needReloadSync) {
                    try {
                        HttpExecutorSyncHolder.initSync(maxTotal, maxPerRoute, defaultHttpRetryHandler,
                            keyStore, keyStorePass, keyStoreType, keysPass,
                            trustStore, trustStorePass, trustStoreType);
                        needReloadSync = false;
                    } catch (Exception e) {
                        logger.error("Http客户端初始化失败", e);
                        needReloadSync = true;
                    }
                }
            }
        }
    }

    private static void initAync() {
        if (INITIALIZER.currentThread != null) {
            throw new SpinException("Http异步客户端尚未配置完成，请先完成配置再使用");
        }
        if (needReloadAsync) {
            synchronized (HttpExecutor.class) {
                if (needReloadAsync) {
                    try {
                        HttpExecutorAsyncHolder.initAsync(maxTotal, maxPerRoute,
                            keyStore, keyStorePass, keyStoreType, keysPass,
                            trustStore, trustStorePass, trustStoreType);
                        needReloadAsync = false;
                    } catch (Exception e) {
                        logger.error("Http异步客户端初始化失败", e);
                        needReloadSync = true;
                    }
                }
            }
        }
    }
    // endregion
}
