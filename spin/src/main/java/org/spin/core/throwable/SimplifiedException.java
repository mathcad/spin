package org.spin.core.throwable;

import org.spin.core.ErrorCode;
import org.spin.core.util.StringUtils;

/**
 * <p>这个异常类用来简化处理异常</p>
 * <p>遇到无需明确分类处理的异常，可以统一用此异常处理，封装{@link ErrorCode}枚举作为参数，用于区分异常类别</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SimplifiedException extends RuntimeException {
    private static final long serialVersionUID = 3761977150343281224L;
    private ErrorCode exceptionType = ErrorCode.OTHER;

    public SimplifiedException(ErrorCode exceptionType, Throwable e) {
        super(e);
        this.exceptionType = exceptionType;
    }

    public SimplifiedException(ErrorCode exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public SimplifiedException(ErrorCode exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public SimplifiedException(ErrorCode exceptionType, String message, Throwable e) {
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
        this.exceptionType = ErrorCode.OTHER;
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
