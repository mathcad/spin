package org.spin.core.throwable;

import org.spin.core.ErrorCode;
import org.spin.core.util.StringUtils;

/**
 * <p>spin框架的根异常，框架中所有异常均从此类派生</p>
 * <p>封装{@link ErrorCode}枚举作为异常的成员属性，用于区分异常类别</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SpinException extends RuntimeException {
    private static final long serialVersionUID = 3761977150343281224L;
    private ErrorCode exceptionType = ErrorCode.OTHER;

    public SpinException(ErrorCode exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public SpinException(ErrorCode exceptionType, Throwable e) {
        super(e);
        this.exceptionType = exceptionType;
    }

    public SpinException(ErrorCode exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public SpinException(int errorCode, String message, Throwable e) {
        super(message, e);
        this.exceptionType = new ErrorCode(errorCode, message);
    }

    public SpinException(int errorCode, String message) {
        super(message);
        this.exceptionType = new ErrorCode(errorCode, message);
    }

    public SpinException(ErrorCode exceptionType, String message, Throwable e) {
        super(message, e);
        this.exceptionType = exceptionType;
    }

    public SpinException(String message, Throwable e) {
        super(message, e);
    }

    public SpinException(String message) {
        super(message);
    }

    public SpinException(Throwable e) {
        super(e);
    }

    public SpinException() {
        super();
    }

    public ErrorCode getExceptionType() {
        return this.exceptionType;
    }

    @Override
    public String getMessage() {
        return StringUtils.isEmpty(super.getMessage()) ? this.exceptionType.toString() : this.exceptionType.toString() + ':' + super.getMessage();
    }

    public String getSimpleMessage() {
        return StringUtils.isEmpty(super.getMessage()) ? this.exceptionType.toString() : super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return StringUtils.isEmpty(super.getLocalizedMessage()) ? this.exceptionType.toString() : this.exceptionType.toString() + ':' + super.getLocalizedMessage();
    }

    @Override
    public void printStackTrace() {
        synchronized (System.err) {
            System.err.println(this.exceptionType.toString());
        }
        super.printStackTrace();
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
