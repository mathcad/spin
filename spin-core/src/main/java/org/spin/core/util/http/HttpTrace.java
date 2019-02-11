package org.spin.core.util.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * 可以带RequestBody的Trace请求
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/1/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class HttpTrace extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "TRACE";

    public HttpTrace() {
        super();
    }

    public HttpTrace(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpTrace(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
