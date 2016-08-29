package org.zibra.common;

import java.io.IOException;

public class ZibraException extends IOException {

    private final static long serialVersionUID = -6146544906159301857L;

    public ZibraException() {
        super();
    }

    public ZibraException(String msg) {
        super(msg);
    }

    public ZibraException(Throwable e) {
        super(e.getMessage());
        setStackTrace(e.getStackTrace());
    }

    public ZibraException(String msg, Throwable e) {
        super(msg);
        setStackTrace(e.getStackTrace());
    }
}
