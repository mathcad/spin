package org.spin.core.function;

import org.spin.core.Assert;

public interface FinalConsumer<T> {
    /**
     * 在指定的对象上执行操作（原始对象不可更改）
     *
     * @param value 需要操作的对象
     */
    void accept(final T value);

    default FinalConsumer<T> andThen(FinalConsumer<T> after) {
        Assert.notNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
