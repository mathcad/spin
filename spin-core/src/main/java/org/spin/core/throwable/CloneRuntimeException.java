package org.spin.core.throwable;

import org.spin.core.ErrorCode;

public class CloneRuntimeException extends SpinException {
    private static final long serialVersionUID = 6774837422188798989L;

    public CloneRuntimeException() {
        super(ErrorCode.CLONE_EXCEPTION);
    }

    public CloneRuntimeException(String message) {
        super(ErrorCode.CLONE_EXCEPTION, message);
    }

    public CloneRuntimeException(Throwable e) {
        super(ErrorCode.CLONE_EXCEPTION, e);
    }

    public CloneRuntimeException(String message, Throwable e) {
        super(ErrorCode.CLONE_EXCEPTION, message, e);
    }
}
