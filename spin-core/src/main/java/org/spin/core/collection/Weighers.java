package org.spin.core.collection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A common set of {@link Weigher} implementations.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 * http://code.google.com/p/concurrentlinkedhashmap/</a>
 */
public final class Weighers {

    private Weighers() {
        throw new AssertionError();
    }

    /**
     * A weigher where a value has a weight of <tt>1</tt>. A map bounded with
     * this weigher will evict when the number of key-value pairs exceeds the
     * capacity.
     *
     * @param <V> value type
     * @return A weigher where a value takes one unit of capacity.
     */
    @SuppressWarnings({"unchecked"})
    public static <V> Weigher<V> singleton() {
        return (Weigher<V>) SingletonWeigher.INSTANCE;
    }

    /**
     * A weigher where the value is a byte array and its weight is the number of
     * bytes. A map bounded with this weigher will evict when the number of bytes
     * exceeds the capacity rather than the number of key-value pairs in the map.
     * This allows for restricting the capacity based on the memory-consumption
     * and is primarily for usage by dedicated caching servers that hold the
     * serialized data.
     * <p>
     * A value with a weight of <tt>0</tt> will be rejected by the map. If a value
     * with this weight can occur then the caller should eagerly evaluate the
     * value and treat it as a removal operation. Alternatively, a custom weigher
     * may be specified on the map to assign an empty value a positive weight.
     *
     * @return A weigher where each byte takes one unit of capacity.
     */
    public static Weigher<byte[]> byteArray() {
        return ByteArrayWeigher.INSTANCE;
    }

    /**
     * A weigher where the value is a {@link Iterable} and its weight is the
     * number of elements. This weigher only should be used when the alternative
     * {@link #collection()} weigher cannot be, as evaluation takes O(n) time. A
     * map bounded with this weigher will evict when the total number of elements
     * exceeds the capacity rather than the number of key-value pairs in the map.
     * <p>
     * A value with a weight of <tt>0</tt> will be rejected by the map. If a value
     * with this weight can occur then the caller should eagerly evaluate the
     * value and treat it as a removal operation. Alternatively, a custom weigher
     * may be specified on the map to assign an empty value a positive weight.
     *
     * @param <E> element type
     * @return A weigher where each element takes one unit of capacity.
     */
    public static <E> Weigher<? super Iterable<E>> iterable() {
        return IterableWeigher.INSTANCE;
    }

    /**
     * A weigher where the value is a {@link Collection} and its weight is the
     * number of elements. A map bounded with this weigher will evict when the
     * total number of elements exceeds the capacity rather than the number of
     * key-value pairs in the map.
     * <p>
     * A value with a weight of <tt>0</tt> will be rejected by the map. If a value
     * with this weight can occur then the caller should eagerly evaluate the
     * value and treat it as a removal operation. Alternatively, a custom weigher
     * may be specified on the map to assign an empty value a positive weight.
     *
     * @param <E> element type
     * @return A weigher where each element takes one unit of capacity.
     */
    public static <E> Weigher<? super Collection<E>> collection() {
        return CollectionWeigher.INSTANCE;
    }

    /**
     * A weigher where the value is a {@link List} and its weight is the number
     * of elements. A map bounded with this weigher will evict when the total
     * number of elements exceeds the capacity rather than the number of
     * key-value pairs in the map.
     * <p>
     * A value with a weight of <tt>0</tt> will be rejected by the map. If a value
     * with this weight can occur then the caller should eagerly evaluate the
     * value and treat it as a removal operation. Alternatively, a custom weigher
     * may be specified on the map to assign an empty value a positive weight.
     *
     * @param <E> element type
     * @return A weigher where each element takes one unit of capacity.
     */
    public static <E> Weigher<? super List<E>> list() {
        return ListWeigher.INSTANCE;
    }

    /**
     * A weigher where the value is a {@link Set} and its weight is the number
     * of elements. A map bounded with this weigher will evict when the total
     * number of elements exceeds the capacity rather than the number of
     * key-value pairs in the map.
     * <p>
     * A value with a weight of <tt>0</tt> will be rejected by the map. If a value
     * with this weight can occur then the caller should eagerly evaluate the
     * value and treat it as a removal operation. Alternatively, a custom weigher
     * may be specified on the map to assign an empty value a positive weight.
     *
     * @param <E> element type
     * @return A weigher where each element takes one unit of capacity.
     */
    public static <E> Weigher<? super Set<E>> set() {
        return SetWeigher.INSTANCE;
    }

    /**
     * A weigher where the value is a {@link Map} and its weight is the number of
     * entries. A map bounded with this weigher will evict when the total number of
     * entries across all values exceeds the capacity rather than the number of
     * key-value pairs in the map.
     * <p>
     * A value with a weight of <tt>0</tt> will be rejected by the map. If a value
     * with this weight can occur then the caller should eagerly evaluate the
     * value and treat it as a removal operation. Alternatively, a custom weigher
     * may be specified on the map to assign an empty value a positive weight.
     *
     * @param <A> key type
     * @param <B> value type
     * @return A weigher where each entry takes one unit of capacity.
     */
    public static <A, B> Weigher<? super Map<A, B>> map() {
        return MapWeigher.INSTANCE;
    }


    enum SingletonWeigher implements Weigher<Object> {
        INSTANCE;

        @Override
        public int weightOf(Object value) {
            return 1;
        }
    }

    enum ByteArrayWeigher implements Weigher<byte[]> {
        INSTANCE;

        @Override
        public int weightOf(byte[] value) {
            return value.length;
        }
    }

    enum IterableWeigher implements Weigher<Iterable<?>> {
        INSTANCE;

        @Override
        public int weightOf(Iterable<?> values) {
            if (values instanceof Collection<?>) {
                return ((Collection<?>) values).size();
            }
            int size = 0;
            for (Object ignored : values) {
                size++;
            }
            return size;
        }
    }

    enum CollectionWeigher implements Weigher<Collection<?>> {
        INSTANCE;

        @Override
        public int weightOf(Collection<?> values) {
            return values.size();
        }
    }

    enum ListWeigher implements Weigher<List<?>> {
        INSTANCE;

        @Override
        public int weightOf(List<?> values) {
            return values.size();
        }
    }

    enum SetWeigher implements Weigher<Set<?>> {
        INSTANCE;

        @Override
        public int weightOf(Set<?> values) {
            return values.size();
        }
    }

    enum MapWeigher implements Weigher<Map<?, ?>> {
        INSTANCE;

        @Override
        public int weightOf(Map<?, ?> values) {
            return values.size();
        }
    }
}
