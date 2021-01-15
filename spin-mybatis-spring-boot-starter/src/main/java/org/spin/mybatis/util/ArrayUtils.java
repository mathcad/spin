package org.spin.mybatis.util;

import org.spin.core.Assert;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ArrayUtils {

    @SuppressWarnings("unchecked")
    public static <T> Class<T> resolveArrayCompType(T[] array) {
        Assert.notNull(array, "类型参数不能为null");
        Class<T> type;
        if (array.length > 0) {
            type = (Class<T>) array[0].getClass();
        } else {
            type = (Class<T>) array.getClass().getComponentType();
        }
        return type;
    }
}
