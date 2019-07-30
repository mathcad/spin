package org.spin.core.throwable;

import org.spin.core.ErrorCode;

/**
 * <p>spin框架的根异常，框架中所有异常均从此类派生</p>
 * <p>封装{@link ErrorCode}枚举作为异常的成员属性，用于区分异常类别</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SimplifiedException extends SpinException {
    private static final long serialVersionUID = 3761977150343281224L;

    public SimplifiedException(ErrorCode exceptionType) {
        super(exceptionType);
    }

    public SimplifiedException(ErrorCode exceptionType, Throwable e) {
        super(exceptionType, e);
    }

    public SimplifiedException(ErrorCode exceptionType, String message) {
        super(exceptionType, message);
    }

    public SimplifiedException(int errorCode, String message, Throwable e) {
        super(errorCode, message, e);
    }

    public SimplifiedException(int errorCode, String message) {
        super(errorCode, message);
    }

    public SimplifiedException(ErrorCode exceptionType, String message, Throwable e) {
        super(exceptionType, message, e);
    }

    public SimplifiedException(String message, Throwable e) {
        super(message, e);
    }

    public SimplifiedException(String message) {
        super(message);
    }

    public SimplifiedException(Throwable e) {
        super(e);
    }

    public SimplifiedException() {
        super();
    }
}
