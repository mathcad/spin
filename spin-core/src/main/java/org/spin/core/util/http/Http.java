package org.spin.core.util.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SimplifiedException;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * HTTP请求
 *
 * @param <T> 请求类型
 */
public final class Http<T extends HttpRequestBase> {

    private Function<URI, T> requestSuppiler;

    public static final Http<HttpGet> GET = new Http<>(HttpGet::new);
    public static final Http<HttpPost> POST = new Http<>(HttpPost::new);
    public static final Http<HttpDelete> DELETE = new Http<>(HttpDelete::new);
    public static final Http<HttpPut> PUT = new Http<>(HttpPut::new);
    public static final Http<HttpHead> HEAD = new Http<>(HttpHead::new);
    public static final Http<HttpOptions> OPTIONS = new Http<>(HttpOptions::new);
    public static final Http<HttpTrace> TRACE = new Http<>(HttpTrace::new);
    public static final Http<HttpPatch> PATCH = new Http<>(HttpPatch::new);

    private static final String SCHEMA = "http://";

    private Http(Function<URI, T> requestSuppiler) {
        this.requestSuppiler = requestSuppiler;
    }

    // region init and getter/setter

    public static void initSync() {
        HttpExecutor.initSync(200, 40, null, null, null);
    }

    public static void initSync(InputStream certsInput, String password, String algorithm) {
        HttpExecutor.initSync(200, 40, certsInput, password, algorithm);
    }

    public static void initSync(int maxTotal, int maxPerRoute, InputStream certsInput, String password, String algorithm) {
        HttpExecutor.initSync(maxTotal, maxPerRoute, certsInput, password, algorithm);
    }

    public static void initAync() {
        HttpExecutor.initAync(200, 40);
    }

    public static void initAync(int maxTotal, int maxPerRoute) {
        HttpExecutor.initAync(maxTotal, maxPerRoute);
    }

    public static int getSocketTimeout() {
        return HttpExecutor.getSocketTimeout();
    }

    public static void setSocketTimeout(int socketTimeout) {
        HttpExecutor.setSocketTimeout(socketTimeout);
    }

    public static int getConnectTimeout() {
        return HttpExecutor.getConnectTimeout();
    }

    public static void setConnectTimeout(int connectTimeout) {
        HttpExecutor.setConnectTimeout(connectTimeout);
    }

    public static int getMaxTotal() {
        return HttpExecutor.getMaxTotal();
    }

    public static int getMaxPerRoute() {
        return HttpExecutor.getMaxPerRoute();
    }

    public static HttpRequestRetryHandler getDefaultHttpRetryHandler() {
        return HttpExecutor.getDefaultHttpRetryHandler();
    }

    public static void setDefaultHttpRetryHandler(HttpRequestRetryHandler defaultHttpRetryHandler) {
        HttpExecutor.setDefaultHttpRetryHandler(defaultHttpRetryHandler);
    }

    // endregion

    // region build

    /**
     * 从指定uri字符串构造请求
     *
     * @param uri uri地址
     * @return 请求对象
     */
    public final Request<T> withUrl(String uri) {
        URI u;
        try {
            URIBuilder uriBuilder = new URIBuilder(fixUrl(uri));
            u = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
        }
        return withUrl(u);
    }

    /**
     * 从指定uri对象构造请求
     *
     * @param uri uri地址
     * @return 请求对象
     */
    public final Request<T> withUrl(URI uri) {
        return new Request<>(requestSuppiler.apply(uri));
    }


    /**
     * 从指定uri字符串构造请求，并通过uriProc对uri对象进行自定义处理
     *
     * @param uri     uri地址
     * @param uriProc uri处理逻辑
     * @return 请求对象
     */
    public final Request<T> withUrl(String uri, FinalConsumer<URIBuilder> uriProc) {
        URI u;
        try {
            URIBuilder uriBuilder = new URIBuilder(fixUrl(uri));
            if (null != uriProc) {
                uriProc.accept(uriBuilder);
            }
            u = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
        }
        return withUrl(u);
    }

    // endregion

    private String fixUrl(String url) {
        return url.toLowerCase().startsWith("http") ? url : SCHEMA + url;
    }
}
