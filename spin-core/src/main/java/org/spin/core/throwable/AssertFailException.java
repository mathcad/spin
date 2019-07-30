package org.spin.core.throwable;

import org.spin.core.ErrorCode;

/**
 * <p>Created by xuweinan on 2018/1/12.</p>
 *
 * @author xuweinan
 */
public class AssertFailException extends SpinException {
    private static final long serialVersionUID = 1174360235354917591L;

    public AssertFailException() {
        super(ErrorCode.ASSERT_FAIL);
    }

    public AssertFailException(String message) {
        super(ErrorCode.ASSERT_FAIL, message);
    }

    public AssertFailException(Throwable e) {
        super(ErrorCode.ASSERT_FAIL, e);
    }

    public AssertFailException(String message, Throwable e) {
        super(ErrorCode.ASSERT_FAIL, message, e);
    }
}
