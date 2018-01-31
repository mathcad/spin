package org.spin.core.util;

import org.spin.core.Assert;
import org.spin.core.collection.MultiValueMap;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 集合工具类
 */
public abstract class CollectionUtils {

    private static long PARALLEL_FACTORY = 10000L;

    /**
     * 判断集合是否为空或{@code null}
     *
     * @param collection 待检查集合
     */
    public static boolean isEmpty(Iterable<?> collection) {
        return (collection == null || !collection.iterator().hasNext());
    }

    /**
     * 判断Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 将数组转换为List。若数组元素是基本类型，会被转换为包装类型
     * <p>若参数为{@code null}，将被转换为List</p>
     *
     * @param source the (potentially primitive) array
     * @see ObjectUtils#toObjectArray(Object)
     * @see Arrays#asList(Object[])
     */
    public static List arrayToList(Object source) {
        return Arrays.asList(ObjectUtils.toObjectArray(source));
    }

    /**
     * 将数组中的元素合并到集合中
     *
     * @param array      待合并的数组 (可以为 {@code null})
     * @param collection 合并的目标集合
     */
    @SuppressWarnings("unchecked")
    public static <E> void mergeArrayIntoCollection(Object array, Collection<E> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        Object[] arr = ObjectUtils.toObjectArray(array);
        for (Object elem : arr) {
            collection.add((E) elem);
        }
    }

    public static <T extends Collection<E>, E> T clone(T collection) {
        if (null == collection) {
            return null;
        }
        @SuppressWarnings("unchecked") T res = JsonUtils.fromJson("[]", (Class<T>) collection.getClass());
        res.addAll(collection);
        return res;
    }

    public static <T extends Collection<E>, E> T cloneWithoutValues(T collection) {
        if (null == collection) {
            return null;
        }
        @SuppressWarnings("unchecked") T res = JsonUtils.fromJson("[]", (Class<T>) collection.getClass());
        return res;
    }

    public static <T extends Collection<E>, E> E detect(T collection, Predicate<E> predicate) {
        if (null == collection || collection.isEmpty()) {
            return null;
        }
        return parallelStream(collection, PARALLEL_FACTORY).filter(predicate).findFirst().orElse(null);
    }

    public static <T extends Collection<E>, E, P> E detectWith(T collection, BiPredicate<E, P> predicate, P value) {
        if (null == collection || collection.isEmpty()) {
            return null;
        }
        return parallelStream(collection, PARALLEL_FACTORY).filter(i -> predicate.test(i, value)).findFirst().orElse(null);
    }

    public static <T extends Collection<E>, E> Optional<E> detectOptional(T collection, Predicate<E> predicate) {
        if (null == collection || collection.isEmpty()) {
            return Optional.empty();
        }
        return parallelStream(collection, PARALLEL_FACTORY).filter(predicate).findFirst();
    }

    public static <T extends Collection<E>, E, P> Optional<E> detectOptionalWith(T collection, BiPredicate<E, P> predicate, P value) {
        if (null == collection || collection.isEmpty()) {
            return Optional.empty();
        }
        return parallelStream(collection, PARALLEL_FACTORY).filter(i -> predicate.test(i, value)).findFirst();
    }

    public static <T extends Collection<E>, E> T select(T collection, Predicate<E> predicate) {
        T res = cloneWithoutValues(collection);
        if (null == res) {
            return null;
        }
        parallelStream(collection, PARALLEL_FACTORY).filter(predicate).forEach(res::add);
        return res;
    }

    public static <T extends Collection<E>, E, P> T selectWith(T collection, BiPredicate<E, P> predicate, P value) {
        T res = cloneWithoutValues(collection);
        if (null == res) {
            return null;
        }
        parallelStream(collection, PARALLEL_FACTORY).filter(i -> predicate.test(i, value)).forEach(res::add);
        return res;
    }

    public static <T extends Collection<E>, E> T reject(T collection, Predicate<E> predicate) {
        T res = cloneWithoutValues(collection);
        if (null == res) {
            return null;
        }
        parallelStream(collection, PARALLEL_FACTORY).filter(i -> !predicate.test(i)).forEach(res::add);
        return res;
    }

    public static <T extends Collection<E>, E, P> T rejectWith(T collection, BiPredicate<E, P> predicate, P value) {
        T res = cloneWithoutValues(collection);
        if (null == res) {
            return null;
        }
        parallelStream(collection, PARALLEL_FACTORY).filter(i -> !predicate.test(i, value)).forEach(res::add);
        return res;
    }

    public static <T extends Collection<E>, E, P> T take(T collection, int size) {
        T res = cloneWithoutValues(collection);
        if (null == res) {
            return null;
        }
        parallelStream(collection, PARALLEL_FACTORY).limit(size).forEach(res::add);
        return res;
    }

    /**
     * 将Properties实例中的内容合并到Map中
     *
     * @param props 待合并的Properties实例(可以为 {@code null})
     * @param map   合并的目标Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.getProperty(key);
                if (value == null) {
                    // Potentially a non-String value...
                    value = props.get(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }


    /**
     * 检查迭代器中是否含有与指定元素相同的元素(equals)
     *
     * @param iterator 待检查的目标迭代器
     * @param element  待检查的目标元素
     */
    public static <T> boolean contains(Iterator<T> iterator, T element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                T candidate = iterator.next();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查枚举中是否含有指定元素
     *
     * @param enumeration 待检查的枚举
     * @param element     待检查的枚举常量
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查集合中是否含有指定对象实例(同一实例)
     *
     * @param iterator 待检查的目标迭代器
     * @param element  待检查的目标元素
     */
    public static boolean containsInstance(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (candidate == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断两集合是否相交
     *
     * @param source     源集合
     * @param candidates 目标集合
     */
    public static boolean containsAny(Collection<?> source, Collection<?> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return false;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找目标集合中第一个与源集合相同元素
     * 如果两集合不相交，返回{@code null}
     *
     * @param source     源集合
     * @param candidates 目标集合
     */
    public static <E> E findFirstMatch(Collection<?> source, Collection<E> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        for (E candidate : candidates) {
            if (source.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 查找目标集合中，指定类型的第一个元素
     *
     * @param collection 待查找的目标集合
     * @param type       搜索的类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T findValueOfType(Collection<?> collection, Class<T> type) {
        if (isEmpty(collection)) {
            return null;
        }
        T value = null;
        for (Object element : collection) {
            if (type == null || type.isInstance(element)) {
                if (value != null) {
                    // More than one value found... no clear single value.
                    return null;
                }
                value = (T) element;
            }
        }
        return value;
    }

    /**
     * 查找目标集合中，指定类型的第一个元素：
     * <pre>
     *     查找第集合中第一个类型的元素，如果没有，查找第二个类型的元素，依次查找。
     *     返回找到的第一个元素
     * </pre>
     *
     * @param collection 待查找的目标集合
     * @param types      搜索的类型
     */
    public static Object findValueOfType(Collection<?> collection, Class<?>[] types) {
        if (isEmpty(collection) || ObjectUtils.isEmpty(types)) {
            return null;
        }
        for (Class<?> type : types) {
            Object value = findValueOfType(collection, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断集合中是否仅含有一个唯一元素(去重后唯一)
     *
     * @param collection 待检查的集合
     */
    public static boolean hasUniqueObject(Collection<?> collection) {
        if (isEmpty(collection)) {
            return false;
        }
        boolean hasCandidate = false;
        Object candidate = null;
        for (Object elem : collection) {
            if (!hasCandidate) {
                hasCandidate = true;
                candidate = elem;
            } else if (candidate != elem) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查集合中元素的类型，如果元素类型不同或集合为空，返回null
     *
     * @param collection 待检查集合
     */
    public static Class<?> findCommonElementType(Collection<?> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> candidate = null;
        for (Object val : collection) {
            if (val != null) {
                if (candidate == null) {
                    candidate = val.getClass();
                } else if (candidate != val.getClass()) {
                    return null;
                }
            }
        }
        return candidate;
    }

    /**
     * 将枚举类型的所有枚举值包装成为一个数组
     */
    public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    /**
     * 获取枚举类型的迭代器
     *
     * @param enumeration 枚举
     */
    public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
        return new EnumerationIterator<>(enumeration);
    }

    /**
     * 将Map转换为MultiValueMap
     *
     * @param map 源Map
     */
    public static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, List<V>> map) {
        return new MultiValueMapAdapter<>(map);
    }

    /**
     * 返回MultiValueMap的一个只读视图
     *
     * @param map 目标Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> MultiValueMap<K, V> unmodifiableMultiValueMap(MultiValueMap<? extends K, ? extends V> map) {
        Assert.notNull(map, "'map' must not be null");
        Map<K, List<V>> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<? extends K, ? extends List<? extends V>> entry : map.entrySet()) {
            List<? extends V> values = Collections.unmodifiableList(entry.getValue());
            result.put(entry.getKey(), (List<V>) values);
        }
        Map<K, List<V>> unmodifiableMap = Collections.unmodifiableMap(result);
        return toMultiValueMap(unmodifiableMap);
    }


    @SafeVarargs
    public static <E> E[] ofArray(E... elements) {
        return elements;
    }

    @SafeVarargs
    public static <E> List<E> ofArrayList(E... elements) {
        List<E> lst = new ArrayList<>(elements.length * 2);
        Collections.addAll(lst, elements);
        return lst;
    }

    @SafeVarargs
    public static <E> List<E> ofLinkedList(E... elements) {
        List<E> lst = new LinkedList<>();
        Collections.addAll(lst, elements);
        return lst;
    }

    @SafeVarargs
    public static <E> Set<E> ofHashSet(E... elements) {
        Set<E> set = new HashSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    @SafeVarargs
    public static <E> Set<E> ofTreeSet(E... elements) {
        Set<E> set = new TreeSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * 将集合转换为stream。当元素个数超过阈值时，将转为并行流
     *
     * @param collection     集合
     * @param parallelFactor 并行阈值
     * @param <E>            元素类型
     * @return 流
     */
    public static <E> Stream<E> parallelStream(Collection<E> collection, long parallelFactor) {
        Assert.notNull(collection, "collection must be non-null");
        return (collection.size() > parallelFactor ? collection.parallelStream() : collection.stream());
    }

    /**
     * 枚举迭代器
     */
    private static class EnumerationIterator<E> implements Iterator<E> {

        private final Enumeration<E> enumeration;

        public EnumerationIterator(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return this.enumeration.hasMoreElements();
        }

        @Override
        public E next() {
            return this.enumeration.nextElement();
        }

        @Override
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported");
        }
    }


    /**
     * Map到MultiValueMap的适配器
     */
    @SuppressWarnings("serial")
    private static class MultiValueMapAdapter<K, V> implements MultiValueMap<K, V>, Serializable {

        private final Map<K, List<V>> map;

        public MultiValueMapAdapter(Map<K, List<V>> map) {
            Assert.notNull(map, "'map' must not be null");
            this.map = map;
        }

        @Override
        public void add(K key, V value) {
            List<V> values = this.map.computeIfAbsent(key, k -> new LinkedList<>());
            values.add(value);
        }

        @Override
        public V getFirst(K key) {
            List<V> values = this.map.get(key);
            return (values != null ? values.get(0) : null);
        }

        @Override
        public void set(K key, V value) {
            List<V> values = new LinkedList<>();
            values.add(value);
            this.map.put(key, values);
        }

        @Override
        public void setAll(Map<K, V> values) {
            for (Entry<K, V> entry : values.entrySet()) {
                set(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Map<K, V> toSingleValueMap() {
            LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<>(this.map.size());
            for (Entry<K, List<V>> entry : map.entrySet()) {
                singleValueMap.put(entry.getKey(), entry.getValue().get(0));
            }
            return singleValueMap;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.map.containsValue(value);
        }

        @Override
        public List<V> get(Object key) {
            return this.map.get(key);
        }

        @Override
        public List<V> put(K key, List<V> value) {
            return this.map.put(key, value);
        }

        @Override
        public List<V> remove(Object key) {
            return this.map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends List<V>> map) {
            this.map.putAll(map);
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public Set<K> keySet() {
            return this.map.keySet();
        }

        @Override
        public Collection<List<V>> values() {
            return this.map.values();
        }

        @Override
        public Set<Entry<K, List<V>>> entrySet() {
            return this.map.entrySet();
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof Map && map.equals(other);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode();
        }

        @Override
        public String toString() {
            return this.map.toString();
        }
    }

}
