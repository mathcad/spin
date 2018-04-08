package org.spin.data.throwable;

import org.spin.core.throwable.SimplifiedException;

public class SQLException extends SimplifiedException {
    private static final long serialVersionUID = -6315329503841905147L;

    public SQLException(SQLError exceptionType, Throwable e) {
        super(exceptionType, e);
    }

    public SQLException(SQLError exceptionType) {
        super(exceptionType);
    }

    public SQLException(SQLError exceptionType, String message) {
        super(exceptionType, message);
    }

    public SQLException(SQLError exceptionType, String message, Throwable e) {
        super(exceptionType, message, e);
    }
}

