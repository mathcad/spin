package org.spin.core.function;

import org.spin.core.Assert;

public interface ExceptionalConsumer<T, E extends Exception> {
    /**
     * 在指定的对象上执行操作（原始对象不可更改）
     *
     * @param value 需要操作的对象
     */
    void accept(final T value) throws E;

    default ExceptionalConsumer<T, E> andThen(ExceptionalConsumer<T, E> after) {
        Assert.notNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
