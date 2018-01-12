package org.spin.core.throwable;

import org.spin.core.ErrorCode;

/**
 * <p>Created by xuweinan on 2018/1/12.</p>
 *
 * @author xuweinan
 */
public class AssertFailException extends SimplifiedException {
    private static final long serialVersionUID = 1174360235354917591L;

    public AssertFailException(String argName) {
        super(ErrorCode.INVALID_PARAM, (argName == null ? "Argument" : argName) + " must not be null.");
    }
}
