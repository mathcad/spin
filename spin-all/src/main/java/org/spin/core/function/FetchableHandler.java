package org.spin.core.function;

import org.spin.core.Assert;
import org.spin.core.trait.Order;

/**
 * 无参数，有返回结果的handler
 * <p>Created by xuweinan on 2017/10/27.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface FetchableHandler<T> extends Order {
    T handle();

    default FetchableHandler<?> andThen(FetchableHandler<?> after) {
        Assert.notNull(after);
        return () -> {
            handle();
            return after.handle();
        };
    }

    default FetchableHandler<T> andThen(Handler after) {
        Assert.notNull(after);
        return () -> {
            T res = handle();
            after.handle();
            return res;
        };
    }
}
