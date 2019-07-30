package org.spin.core.throwable;

import org.spin.core.ErrorCode;

/**
 * <p>当参数为空 <code>null</code>时抛出</p>
 * <p><code>NullArgumentException</code> 表时当某个必须非空的参数，传入了空值<code>null</code>.</p>
 * <pre>
 * public void foo(String str) {
 *   if (str == null) {
 *     throw new NullArgumentException("str");
 *   }
 *   // do something with the string
 * }
 * </pre>
 */
public class NullArgumentException extends SpinException {

    private static final long serialVersionUID = 1174360235354917591L;

    public NullArgumentException(String argName) {
        super(ErrorCode.INVALID_PARAM, (argName == null ? "Argument" : argName) + " must not be null.");
    }

}
