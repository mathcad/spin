package org.spin.core.util.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.utils.URIBuilder;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SimplifiedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * HTTP请求方式
 *
 * @param <T> 请求类型
 */
public final class HttpMethod<T extends HttpRequestBase> {
    public static final HttpMethod<HttpGet> GET = new HttpMethod<>(HttpGet::new);
    public static final HttpMethod<HttpPost> POST = new HttpMethod<>(HttpPost::new);
    public static final HttpMethod<HttpDelete> DELETE = new HttpMethod<>(HttpDelete::new);
    public static final HttpMethod<HttpPut> PUT = new HttpMethod<>(HttpPut::new);
    public static final HttpMethod<HttpHead> HEAD = new HttpMethod<>(HttpHead::new);
    public static final HttpMethod<HttpOptions> OPTIONS = new HttpMethod<>(HttpOptions::new);
    public static final HttpMethod<HttpTrace> TRACE = new HttpMethod<>(HttpTrace::new);
    public static final HttpMethod<HttpPatch> PATCH = new HttpMethod<>(HttpPatch::new);

    private Function<URI, T> requestSuppiler;

    private HttpMethod(Function<URI, T> requestSuppiler) {
        this.requestSuppiler = requestSuppiler;
    }

    /**
     * 从指定uri字符串构造请求
     *
     * @param uri uri地址
     * @return 请求对象
     */
    public final T withUrl(String uri) {
        URI u;
        try {
            URIBuilder uriBuilder = new URIBuilder(HttpUtils.fixUrl(uri));
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
    public final T withUrl(URI uri) {
        return requestSuppiler.apply(uri);
    }

    /**
     * 从指定uri字符串构造请求
     *
     * @param uri         uri地址
     * @param requestProc request后处理
     * @return 请求对象
     */
    public final T withUrl(String uri, FinalConsumer<T> requestProc) {
        return withUrl(uri, null, requestProc);
    }

    /**
     * 从指定uri字符串构造请求，并通过uriProc对uri对象进行自定义处理
     *
     * @param uri         uri地址
     * @param uriProc     uri处理逻辑
     * @param requestProc request后处理
     * @return 请求对象
     */
    public final T withUrl(String uri, FinalConsumer<URIBuilder> uriProc, FinalConsumer<T> requestProc) {
        URI u;
        try {
            URIBuilder uriBuilder = new URIBuilder(HttpUtils.fixUrl(uri));
            if (null != uriProc) {
                uriProc.accept(uriBuilder);
            }
            u = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "url格式错误: " + uri + e.getMessage());
        }
        return withUrl(u, requestProc);
    }

    /**
     * 从指定uri对象构造请求
     *
     * @param uri         uri地址
     * @param requestProc request后处理
     * @return 请求对象
     */
    public final T withUrl(URI uri, FinalConsumer<T> requestProc) {
        T request = requestSuppiler.apply(uri);
        if (null != requestProc) {
            requestProc.accept(request);
        }
        return request;
    }
}
