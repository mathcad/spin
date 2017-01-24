package org.spin.throwable;

import org.spin.sys.ErrorAndExceptionCode;

/**
 * <p>这个异常类用来简化处理异常</p>
 * <p>遇到无需明确分类处理的异常，可以统一用此异常处理，封装{@link ErrorAndExceptionCode}枚举作为参数，用于区分异常类别</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SimplifiedException extends RuntimeException {
    private static final long serialVersionUID = 3761977150343281224L;
    private ErrorAndExceptionCode exceptionType = ErrorAndExceptionCode.OTHER;

    public SimplifiedException(ErrorAndExceptionCode exceptionType, Throwable e) {
        super(e);
        this.exceptionType = exceptionType;
    }

    public SimplifiedException(ErrorAndExceptionCode exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public SimplifiedException(ErrorAndExceptionCode exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public SimplifiedException(ErrorAndExceptionCode exceptionType, String message, Throwable e) {
        super(message, e);
        this.exceptionType = exceptionType;
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
        this.exceptionType = ErrorAndExceptionCode.OTHER;
    }

    public ErrorAndExceptionCode getExceptionType() {
        return this.exceptionType;
    }

    @Override
    public String getMessage() {
        return this.exceptionType.toString() + ':' + super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return this.exceptionType.toString() + ':' + super.getLocalizedMessage();
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
