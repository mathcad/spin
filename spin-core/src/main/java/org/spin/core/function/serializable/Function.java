package org.spin.core.function.serializable;

import org.spin.core.util.BeanUtils;
import org.spin.core.util.LambdaUtils;

import java.io.Serializable;

/**
 * Represents a function that accepts one argument and produces a result, serializable.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @since 1.8
 */
@FunctionalInterface
public interface Function<T, R> extends java.util.function.Function<T, R>, Serializable {
    default String name() {
        return BeanUtils.toFieldName(LambdaUtils.resolveLambda(this).getImplMethodName());
    }
}
