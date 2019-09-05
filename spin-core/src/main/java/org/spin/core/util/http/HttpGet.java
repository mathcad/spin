package org.spin.core.util.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * 可以带RequestBody的Get请求
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/1/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class HttpGet extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "GET";

    public HttpGet() {
        super();
    }

    public HttpGet(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpGet(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
