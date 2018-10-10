package org.spin.core.function.serializable;

import java.io.Serializable;

/**
 * 可能会抛出checked exception的supplier
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/9/30.</p>
 *
 * @author xuweinan
 */
public interface ExceptionalSupplier<T, E extends Exception> extends Serializable {
    T get() throws E;
}
