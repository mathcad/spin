package org.spin.core.throwable;

import org.spin.core.ErrorCode;

/**
 * <p>HTTP请求异常</p>
 * <p>封装{@link ErrorCode}枚举作为异常的成员属性，用于区分异常类别</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class HttpRequestException extends SpinException {
    private static final long serialVersionUID = 4217844007341869973L;
    private static final ErrorCode HTTP_ERROR = ErrorCode.with(110, "Http请求异常");

    public HttpRequestException() {
        super(HTTP_ERROR);
    }

    public HttpRequestException(Throwable e) {
        super(HTTP_ERROR, e);
    }

    public HttpRequestException(String message) {
        super(HTTP_ERROR, message);
    }

    public HttpRequestException(String message, Throwable e) {
        super(HTTP_ERROR, message, e);
    }
}
