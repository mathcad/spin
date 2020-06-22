package org.spin.cloud.throwable;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

/**
 * 警告提示信息异常
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class InfoTipException extends BizException {
    private static final ErrorCode INFO_TIP = ErrorCode.with(2000, "提示");

    public InfoTipException(ErrorCode exceptionType, Throwable e) {
        super(exceptionType, e);
    }

    public InfoTipException(ErrorCode exceptionType) {
        super(exceptionType);
    }

    public InfoTipException(ErrorCode exceptionType, String message) {
        super(exceptionType, message);
    }

    public InfoTipException(ErrorCode exceptionType, String message, Throwable e) {
        super(exceptionType, message, e);
    }

    public InfoTipException(String message, Throwable e) {
        super(INFO_TIP, message, e);
    }

    public InfoTipException(String message) {
        super(INFO_TIP, message);
    }

    public InfoTipException(Throwable e) {
        super(INFO_TIP, e);
    }

    public InfoTipException() {
        super(INFO_TIP);
    }
}
