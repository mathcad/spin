package org.spin.common.throwable;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

/**
 * 业务异常
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class BizException extends SimplifiedException {
    public BizException(ErrorCode exceptionType, Throwable e) {
        super(exceptionType, e);
    }

    public BizException(ErrorCode exceptionType) {
        super(exceptionType);
    }

    public BizException(ErrorCode exceptionType, String message) {
        super(exceptionType, message);
    }

    public BizException(ErrorCode exceptionType, String message, Throwable e) {
        super(exceptionType, message, e);
    }

    public BizException(String message, Throwable e) {
        super(message, e);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Throwable e) {
        super(e);
    }

    public BizException() {
        super();
    }
}
