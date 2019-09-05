package org.spin.core.function;

/**
 * 可能会抛出checked exception的supplier
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/9/30.</p>
 *
 * @author xuweinan
 */
public interface ExceptionalSupplier<T, E extends Exception> {
    T get() throws E;
}
