package org.spin.core.util.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * 可以带RequestBody的Delete请求
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/1/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class HttpDelete extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "DELETE";

    public HttpDelete() {
        super();
    }

    public HttpDelete(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpDelete(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
