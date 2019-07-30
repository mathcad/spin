package org.spin.core.throwable;

/**
 * 当无法克隆一个对象时，抛出该异常. 与
 * {@link CloneNotSupportedException} 对应。但该异常是 {@link RuntimeException}.
 */
public class CloneFailedException extends SpinException {

    private static final long serialVersionUID = 20091223L;

    public CloneFailedException(final String message) {
        super(message);
    }

    public CloneFailedException(final Throwable cause) {
        super(cause);
    }

    public CloneFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
