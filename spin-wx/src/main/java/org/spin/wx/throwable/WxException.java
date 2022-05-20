package org.spin.wx.throwable;

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
public class WxException extends SimplifiedException {
    public WxException(ErrorCode exceptionType, Throwable e) {
        super(exceptionType, e);
    }

    public WxException(ErrorCode exceptionType) {
        super(exceptionType);
    }

    public WxException(ErrorCode exceptionType, String message) {
        super(exceptionType, message);
    }

    public WxException(ErrorCode exceptionType, String message, Throwable e) {
        super(exceptionType, message, e);
    }

    public WxException(String message, Throwable e) {
        super(message, e);
    }

    public WxException(String message) {
        super(message);
    }

    public WxException(Throwable e) {
        super(e);
    }

    public WxException() {
        super();
    }
}
