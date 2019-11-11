package org.spin.core.function.serializable;

import java.io.Serializable;

/**
 * Represents a supplier of results.
 *
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #get()}.
 *
 * @param <T> the type of results supplied by this supplier
 * @since 1.8
 */
@FunctionalInterface
public interface Supplier<T> extends java.util.function.Supplier<T>, Serializable {
}