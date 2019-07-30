package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.throwable.SpinException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class ExceptionUtilsTest {

    @Test
    void getCause() {
        RuntimeException runtimeException = new RuntimeException(new SpinException(new IllegalAccessException("aaa")));

        IllegalAccessException cause = ExceptionUtils.getCause(runtimeException, IllegalAccessException.class);
        System.out.println(cause);
    }
}
