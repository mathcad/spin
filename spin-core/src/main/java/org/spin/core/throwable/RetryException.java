package org.spin.core.throwable;

import org.spin.core.ErrorCode;

/**
 * <p>Created by xuweinan on 2018/1/12.</p>
 *
 * @author xuweinan
 */
public class RetryException extends SpinException {
    private static final long serialVersionUID = 1174360235354917591L;

    public RetryException() {
        super(ErrorCode.OTHER);
    }

    public RetryException(String message) {
        super(ErrorCode.OTHER, message);
    }

    public RetryException(Throwable e) {
        super(ErrorCode.OTHER, e);
    }

    public RetryException(String message, Throwable e) {
        super(ErrorCode.OTHER, message, e);
    }
}
