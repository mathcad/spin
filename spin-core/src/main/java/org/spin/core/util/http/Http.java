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
import org.spin.core.throwable.SpinException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * HTTP请求
 *
 * @param <T> 请求类型
 */
public final class Http<T extends HttpRequestBase> {

    private final Function<URI, T> requestSuppiler;
    private final String method;

    public static final Http<HttpGet> GET = new Http<>(HttpGet::new, "GET");
    public static final Http<HttpPost> POST = new Http<>(HttpPost::new, "POST");
    public static final Http<HttpDelete> DELETE = new Http<>(HttpDelete::new, "DELETE");
    public static final Http<HttpPut> PUT = new Http<>(HttpPut::new, "PUT");
    public static final Http<HttpHead> HEAD = new Http<>(HttpHead::new, "HEAD");
    public static final Http<HttpOptions> OPTIONS = new Http<>(HttpOptions::new, "OPTIONS");
    public static final Http<HttpTrace> TRACE = new Http<>(HttpTrace::new, "TRACE");
    public static final Http<HttpPatch> PATCH = new Http<>(HttpPatch::new, "PATCH");

    private static final String SCHEMA = "http://";

    private Http(Function<URI, T> requestSuppiler, String method) {
        this.requestSuppiler = requestSuppiler;
        this.method = method;

    }

    // region init and getter/setter

    public static HttpExecutor.HttpInitializer configure() {
        return HttpExecutor.configure();
    }

    public static int getSocketTimeout() {
        return HttpExecutor.getSocketTimeout();
    }

    public static int getConnectTimeout() {
        return HttpExecutor.getConnectTimeout();
    }

    public static int getMaxTotal() {
        return HttpExecutor.getDefaultMaxTotal();
    }

    public static int getMaxPerRoute() {
        return HttpExecutor.getDefaultMaxPerRoute();
    }

    public static HttpRequestRetryHandler getDefaultHttpRetryHandler() {
        return HttpExecutor.getDefaultHttpRetryHandler();
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
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
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
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
        }
        return withUrl(u);
    }

    // endregion


    public String getMethod() {
        return method;
    }

    private String fixUrl(String url) {
        return url.toLowerCase().startsWith("http") ? url : SCHEMA + url;
    }
}
