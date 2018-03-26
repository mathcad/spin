package org.spin.core.collection;

import org.spin.core.Assert;
import org.spin.core.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * 元组，最多支持22个元素
 * <p>Created by xuweinan on 2018/3/20.</p>
 *
 * @author xuweinan
 */
public interface Tuple<R extends Tuple> extends Iterable<Object>, Serializable {

    static <A, B> Pair<A, B> of(final A first, final B second) {
        return Pair.of(first, second);
    }

    static <A, B, C> Triple<A, B, C> of(final A first, final B second, final C third) {
        return Triple.of(first, second, third);
    }

    static <A, B, C, D> Tuple4<A, B, C, D> of(final A c1, final B c2, final C c3, final D c4) {
        return Tuple4.of(c1, c2, c3, c4);
    }

    static <A, B, C, D, E> Tuple5<A, B, C, D, E> of(final A c1, final B c2, final C c3, final D c4, final E c5) {
        return Tuple5.of(c1, c2, c3, c4, c5);
    }

    static <A, B, C, D, E, F>
    Tuple6<A, B, C, D, E, F> of(final A c1, final B c2, final C c3, final D c4,
                                final E c5, final F c6) {
        return Tuple6.of(c1, c2, c3, c4, c5, c6);
    }

    static <A, B, C, D, E, F, G>
    Tuple7<A, B, C, D, E, F, G> of(final A c1, final B c2, final C c3, final D c4,
                                   final E c5, final F c6, final G c7) {
        return Tuple7.of(c1, c2, c3, c4, c5, c6, c7);
    }

    static <A, B, C, D, E, F, G, H>
    Tuple8<A, B, C, D, E, F, G, H> of(final A c1, final B c2, final C c3, final D c4,
                                      final E c5, final F c6, final G c7, final H c8) {
        return Tuple8.of(c1, c2, c3, c4, c5, c6, c7, c8);
    }

    static <A, B, C, D, E, F, G, H, I>
    Tuple9<A, B, C, D, E, F, G, H, I> of(final A c1, final B c2, final C c3, final D c4,
                                         final E c5, final F c6, final G c7, final H c8,
                                         final I c9) {
        return Tuple9.of(c1, c2, c3, c4, c5, c6, c7, c8, c9);
    }

    static <A, B, C, D, E, F, G, H, I, J>
    Tuple10<A, B, C, D, E, F, G, H, I, J> of(final A c1, final B c2, final C c3, final D c4,
                                             final E c5, final F c6, final G c7, final H c8,
                                             final I c9, final J c10) {
        return Tuple10.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
    }

    static <A, B, C, D, E, F, G, H, I, J, K>
    Tuple11<A, B, C, D, E, F, G, H, I, J, K> of(final A c1, final B c2, final C c3, final D c4,
                                                final E c5, final F c6, final G c7, final H c8,
                                                final I c9, final J c10, final K c11) {
        return Tuple11.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L>
    Tuple12<A, B, C, D, E, F, G, H, I, J, K, L> of(final A c1, final B c2, final C c3, final D c4,
                                                   final E c5, final F c6, final G c7, final H c8,
                                                   final I c9, final J c10, final K c11, final L c12) {
        return Tuple12.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M>
    Tuple13<A, B, C, D, E, F, G, H, I, J, K, L,
        M> of(final A c1, final B c2, final C c3, final D c4,
              final E c5, final F c6, final G c7, final H c8,
              final I c9, final J c10, final K c11, final L c12,
              final M c13) {
        return Tuple13.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N>
    Tuple14<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N> of(final A c1, final B c2, final C c3, final D c4,
                 final E c5, final F c6, final G c7, final H c8,
                 final I c9, final J c10, final K c11, final L c12,
                 final M c13, final N c14) {
        return Tuple14.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O>
    Tuple15<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O> of(final A c1, final B c2, final C c3, final D c4,
                    final E c5, final F c6, final G c7, final H c8,
                    final I c9, final J c10, final K c11, final L c12,
                    final M c13, final N c14, final O c15) {
        return Tuple15.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P>
    Tuple16<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P> of(final A c1, final B c2, final C c3, final D c4,
                       final E c5, final F c6, final G c7, final H c8,
                       final I c9, final J c10, final K c11, final L c12,
                       final M c13, final N c14, final O c15, final P c16) {
        return Tuple16.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q>
    Tuple17<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q> of(final A c1, final B c2, final C c3, final D c4,
                          final E c5, final F c6, final G c7, final H c8,
                          final I c9, final J c10, final K c11, final L c12,
                          final M c13, final N c14, final O c15, final P c16,
                          final Q c17) {
        return Tuple17.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R>
    Tuple18<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q, R> of(final A c1, final B c2, final C c3, final D c4,
                             final E c5, final F c6, final G c7, final H c8,
                             final I c9, final J c10, final K c11, final L c12,
                             final M c13, final N c14, final O c15, final P c16,
                             final Q c17, final R c18) {
        return Tuple18.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S>
    Tuple19<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q, R, S> of(final A c1, final B c2, final C c3, final D c4,
                                final E c5, final F c6, final G c7, final H c8,
                                final I c9, final J c10, final K c11, final L c12,
                                final M c13, final N c14, final O c15, final P c16,
                                final Q c17, final R c18, final S c19) {
        return Tuple19.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T>
    Tuple20<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q, R, S, T> of(final A c1, final B c2, final C c3, final D c4,
                                   final E c5, final F c6, final G c7, final H c8,
                                   final I c9, final J c10, final K c11, final L c12,
                                   final M c13, final N c14, final O c15, final P c16,
                                   final Q c17, final R c18, final S c19, final T c20) {
        return Tuple20.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U>
    Tuple21<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q, R, S, T, U> of(final A c1, final B c2, final C c3, final D c4,
                                      final E c5, final F c6, final G c7, final H c8,
                                      final I c9, final J c10, final K c11, final L c12,
                                      final M c13, final N c14, final O c15, final P c16,
                                      final Q c17, final R c18, final S c19, final T c20,
                                      final U c21) {
        return Tuple21.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, c21);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V>
    Tuple22<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q, R, S, T, U, V> of(final A c1, final B c2, final C c3, final D c4,
                                         final E c5, final F c6, final G c7, final H c8,
                                         final I c9, final J c10, final K c11, final L c12,
                                         final M c13, final N c14, final O c15, final P c16,
                                         final Q c17, final R c18, final S c19, final T c20,
                                         final U c21, final V c22) {
        return Tuple22.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, c21, c22);
    }

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
        return (E1) arr[Assert.exclusiveBetween(-1, arr.length, pos)];
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
