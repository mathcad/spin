package org.spin.cloud.throwable;

import org.spin.core.ErrorCode;

/**
 * 警告提示信息异常
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class WarnTipException extends BizException {
    private static final ErrorCode WARN_TIP = ErrorCode.with(1000, "警告");

    public WarnTipException(ErrorCode exceptionType, Throwable e) {
        super(exceptionType, e);
    }

    public WarnTipException(ErrorCode exceptionType) {
        super(exceptionType);
    }

    public WarnTipException(ErrorCode exceptionType, String message) {
        super(exceptionType, message);
    }

    public WarnTipException(ErrorCode exceptionType, String message, Throwable e) {
        super(exceptionType, message, e);
    }

    public WarnTipException(String message, Throwable e) {
        super(WARN_TIP, message, e);
    }

    public WarnTipException(String message) {
        super(WARN_TIP, message);
    }

    public WarnTipException(Throwable e) {
        super(WARN_TIP, e);
    }

    public WarnTipException() {
        super(WARN_TIP);
    }
}
