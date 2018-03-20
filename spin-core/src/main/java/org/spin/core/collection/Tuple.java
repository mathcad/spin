package org.spin.core.collection;

import org.spin.core.Assert;
import org.spin.core.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface Tuple<R extends Tuple> extends Iterable<Object>, Serializable {

    /**
     * 将元组转换成列表
     *
     * @return 转换得到的列表
     */
    default List<Object> toList() {
        return CollectionUtils.ofArrayList(toArray());
    }

    /**
     * 将元组转换成数组
     *
     * @return 转换得到的数组
     */
    Object[] toArray();

    /**
     * 得到元组的大小
     *
     * @return 元组的大小
     */
    default int size() {
        return toArray().length;
    }

    /**
     * 获取元组中指定位置的元素
     *
     * @param pos  元组中的位置
     * @param <E1> 元素类型
     * @return 对应元素
     */
    @SuppressWarnings("unchecked")
    default <E1> E1 get(final int pos) {
        Object[] arr = toArray();
        return (E1) arr[(int) Assert.exclusiveBetween(-1, arr.length, pos)];
    }

    /**
     * 判断元组中是否包含某元素
     *
     * @param value 需要判定的元素
     * @return 是否包含
     */
    boolean contains(final Object value);


    /**
     * 将元组转成流
     *
     * @return 流
     */
    default Stream<Object> stream() {
        return Arrays.stream(toArray());
    }

    /**
     * 将元组转成并行流
     *
     * @return 流
     */
    default Stream<Object> parallelStream() {
        return Arrays.stream(toArray()).parallel();
    }

    /**
     * 带序号迭代元组
     *
     * @param action 带序号的迭代函数
     */
    void forEachWithIndex(final BiConsumer<Integer, Object> action);

    default Iterator<Object> iterator() {
        return toList().iterator();
    }

    /**
     * 反转元组
     * 反转后元组大小不变，子类各自实现可以达到最好性能，也可以指定返回值类型，方便使用
     *
     * @return 反转后的元组
     */
    R reverse();
}
