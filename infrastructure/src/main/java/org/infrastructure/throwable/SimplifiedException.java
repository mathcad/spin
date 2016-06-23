package org.infrastructure.throwable;

import org.infrastructure.sys.ErrorAndExceptionCode;

/**
 * <p>这个异常类用来简化处理异常</p>
 * <p>遇到无需明确分类处理的异常，可以统一用此异常处理，封装{@code ErrorAndExceptionCode}枚举作为参数，用于区分异常类别</p>
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

	public SimplifiedException() {
		super();
		this.exceptionType = ErrorAndExceptionCode.OTHER;
	}

	public ErrorAndExceptionCode getExceptionType() {
		return this.exceptionType;
	}
}
