package org.spin.core.function;

import java.util.Objects;

@FunctionalInterface
public interface TripleConsumer<T, U, V> {
    void accept(T t, U u, V v);

    default TripleConsumer<T, U, V> andThen(TripleConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);

        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}
