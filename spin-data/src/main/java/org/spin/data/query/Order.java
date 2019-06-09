package org.spin.data.query;

import org.spin.core.function.serializable.Function;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.LambdaUtils;

/**
 * 排序
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Order extends org.hibernate.criterion.Order {

    /**
     * Ascending order
     *
     * @param field The property to order on
     * @return The build Order instance
     */
    public static Order asc(Function<?, ?> field) {
        return new Order(resolveLambda(field), true);
    }

    /**
     * Descending order.
     *
     * @param field The property to order on
     * @return The build Order instance
     */
    public static Order desc(Function<?, ?> field) {
        return new Order(resolveLambda(field), false);
    }

    /**
     * Ascending order
     *
     * @param propertyName The property to order on
     * @return The build Order instance
     */
    public static Order asc(String propertyName) {
        return new Order(propertyName, true);
    }

    /**
     * Descending order.
     *
     * @param propertyName The property to order on
     * @return The build Order instance
     */
    public static Order desc(String propertyName) {
        return new Order(propertyName, false);
    }

    /**
     * Constructor for Order.  Order instances are generally created by factory methods.
     *
     * @param propertyName 排序属性
     * @param ascending    是否升序
     * @see #asc
     * @see #desc
     */
    protected Order(String propertyName, boolean ascending) {
        super(propertyName, ascending);
    }

    public static String resolveLambda(Function<?, ?> lambda) {
        return BeanUtils.toFieldName(LambdaUtils.resolveLambda(lambda).getImplMethodName());
    }
}
