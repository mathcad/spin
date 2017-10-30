package org.spin.core.function;

import org.spin.core.Assert;
import org.spin.core.trait.Order;

/**
 * 无参数，无返回值的handler
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface Handler extends Order {
    void handle();

    /**
     * Returns a composed {@code IntConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code IntConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default Handler andThen(Handler after) {
        Assert.notNull(after);
        return () -> {
            handle();
            after.handle();
        };
    }
}
