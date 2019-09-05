package org.spin.core.function;

import org.spin.core.Assert;

import java.util.function.Consumer;

public interface FinalConsumer<T> extends Consumer<T> {
    /**
     * 在指定的对象上执行操作（原始对象不可更改）
     *
     * @param value 需要操作的对象
     */
    @Override
    void accept(final T value);

    default FinalConsumer<T> andThen(FinalConsumer<T> after) {
        Assert.notNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
