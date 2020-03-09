package org.spin.core.util;

import org.spin.core.Assert;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SpinException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 集合工具类
 */
public abstract class CollectionUtils {

    private static final long PARALLEL_FACTORY = 10000L;

    private CollectionUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // region 集合检查

    /**
     * 判断集合是否为空或{@code null}
     *
     * @param collection 待检查集合
     * @return 是否为空
     */
    public static boolean isEmpty(Iterable<?> collection) {
        return (collection == null || !collection.iterator().hasNext());
    }

    /**
     * 判断数组是否为空或{@code null}
     *
     * @param array 待检查数组
     * @param <T>   数组的类型参数
     * @return 是否为空
     */
    public static <T> boolean isEmpty(T[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * 判断Map是否为空或{@code null}
     *
     * @param map 待检查Map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }


    /**
     * 判断集合是否不为空
     *
     * @param collection 待检查集合
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Iterable<?> collection) {
        return (collection != null && collection.iterator().hasNext());
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 待检查数组
     * @param <T>   数组的类型参数
     * @return 是否不为空
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return (array != null && array.length != 0);
    }

    /**
     * 判断Map是否不为空
     *
     * @param map 待检查Map
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return (map != null && !map.isEmpty());
    }


    /**
     * 判断一个对象是否是集合(List, Set, 数组, Tuple等)
     *
     * @param target 对象
     * @return 是否是集合
     */
    public static boolean isCollection(Object target) {

        if (target instanceof Iterable) {
            return true;
        }

        return target.getClass().isArray();
    }

    /**
     * 判断一个对象是否是数组
     *
     * @param target 对象
     * @return 是否是数组
     */
    public static boolean isArray(Object target) {
        return null != target && target.getClass().isArray();
    }

    // endregion

    // region 集合构造

    /**
     * 克隆指定集合(深克隆)
     *
     * @param collection 源集合
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @return 源集合的拷贝
     */
    public static <T extends Collection<E>, E> T clone(T collection) {
        if (null == collection) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T res = JsonUtils.fromJson("[]", (Class<T>) collection.getClass());
        res.addAll(collection);
        return res;
    }

    /**
     * 克隆指定集合(仅克隆集合本身，返回同类型的空集合)
     *
     * @param collection 源集合
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @return 源集合的拷贝(无任何元素的空集合)
     */
    public static <T extends Collection<E>, E> T cloneWithoutValues(T collection) {
        if (null == collection) {
            return null;
        }
        //noinspection unchecked
        return JsonUtils.fromJson("[]", (Class<T>) collection.getClass());
    }


    /**
     * 将枚举类型的所有枚举值包装成为一个数组
     *
     * @param enumeration 枚举
     * @param array       存放枚举常量的容器
     * @param <E>         枚举类型
     * @return 枚举常量数组
     */
    public static <E> E[] toArray(Enumeration<E> enumeration, E[] array) {
        ArrayList<E> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    /**
     * 获取枚举类型的迭代器
     *
     * @param enumeration 枚举
     * @param <E>         枚举类型
     * @return 枚举迭代器
     */
    public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
        return new EnumerationIterator<>(enumeration);
    }

    @SafeVarargs
    public static <E> E[] ofArray(E... elements) {
        return elements;
    }

    @SafeVarargs
    public static <E> ArrayList<E> ofArrayList(E... elements) {
        ArrayList<E> lst = new ArrayList<>(elements.length * 2);
        Collections.addAll(lst, elements);
        return lst;
    }

    public static ArrayList<Byte> ofByteList(byte... elements) {
        ArrayList<Byte> lst = new ArrayList<>(elements.length * 2);
        for (byte element : elements) {
            lst.add(element);
        }
        return lst;
    }

    public static ArrayList<Character> ofCharList(char... elements) {
        ArrayList<Character> lst = new ArrayList<>(elements.length * 2);
        for (char element : elements) {
            lst.add(element);
        }
        return lst;
    }

    public static ArrayList<Short> ofShortList(short... elements) {
        ArrayList<Short> lst = new ArrayList<>(elements.length * 2);
        for (short element : elements) {
            lst.add(element);
        }
        return lst;
    }

    public static ArrayList<Integer> ofIntList(int... elements) {
        ArrayList<Integer> lst = new ArrayList<>(elements.length * 2);
        for (int element : elements) {
            lst.add(element);
        }
        return lst;
    }

    public static ArrayList<Long> ofLongList(long... elements) {
        ArrayList<Long> lst = new ArrayList<>(elements.length * 2);
        for (long element : elements) {
            lst.add(element);
        }
        return lst;
    }

    public static ArrayList<Float> ofFloatList(float... elements) {
        ArrayList<Float> lst = new ArrayList<>(elements.length * 2);
        for (float element : elements) {
            lst.add(element);
        }
        return lst;
    }

    public static ArrayList<Double> ofDoubleList(double... elements) {
        ArrayList<Double> lst = new ArrayList<>(elements.length * 2);
        for (double element : elements) {
            lst.add(element);
        }
        return lst;
    }

    @SafeVarargs
    public static <E> LinkedList<E> ofLinkedList(E... elements) {
        LinkedList<E> lst = new LinkedList<>();
        Collections.addAll(lst, elements);
        return lst;
    }

    @SafeVarargs
    public static <E> HashSet<E> ofHashSet(E... elements) {
        HashSet<E> set = new HashSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    @SafeVarargs
    public static <E extends Comparable<E>> TreeSet<E> ofTreeSet(E... elements) {
        TreeSet<E> set = new TreeSet<>();
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
     * 判断一个对象是否是集合(List, Set, 数组, Tuple等)，如果是，将对象以List的形式返回
     *
     * @param target 对象
     * @param <T>    元素类型
     * @return 转换后的List
     */
    public static <T> List<T> asList(Object target) {
        if (null == target) {
            return null;
        }

        if (target instanceof List) {
            //noinspection unchecked
            return (List<T>) target;
        }

        if (target instanceof Collection) {
            //noinspection unchecked
            return new ArrayList<>((Collection<T>) target);
        }

        if (target instanceof Iterable) {
            List<T> res = new LinkedList<>();
            //noinspection unchecked
            ((Iterable<T>) target).forEach(res::add);
            return res;
        }

        if (target instanceof Iterator) {
            List<T> res = new LinkedList<>();
            //noinspection unchecked
            Iterator<T> iter = (Iterator<T>) target;
            while (iter.hasNext()) {
                res.add(iter.next());
            }
            return res;
        }

        if (target instanceof Enumeration) {
            List<T> res = new LinkedList<>();
            //noinspection rawtypes
            while (((Enumeration) target).hasMoreElements()) {
                //noinspection rawtypes,unchecked
                res.add((T) ((Enumeration) target).nextElement());
            }

            return res;
        }

        if (target.getClass().isArray()) {
            //noinspection unchecked
            return ofLinkedList((T[]) target);
        }

        throw new SpinException("目标对象不是集合类型:" + target.getClass().getName());
    }

    /**
     * 判断一个对象是否是集合(List, Set, 数组, Tuple等)，如果是，将对象以Set的形式返回
     *
     * @param target 对象
     * @param <T>    元素类型
     * @return 转换后的Set
     */
    public static <T> Set<T> asSet(Object target) {
        if (null == target) {
            return null;
        }

        if (target instanceof Set) {
            //noinspection unchecked
            return (Set<T>) target;
        }

        if (target instanceof Collection) {
            //noinspection unchecked
            return new HashSet<>((Collection<T>) target);
        }

        if (target instanceof Iterable) {
            HashSet<T> res = new HashSet<>();
            //noinspection unchecked
            ((Iterable<T>) target).forEach(res::add);
            return res;
        }

        if (target instanceof Iterator) {
            HashSet<T> res = new HashSet<>();
            //noinspection unchecked
            Iterator<T> iter = (Iterator<T>) target;
            while (iter.hasNext()) {
                res.add(iter.next());
            }
            return res;
        }

        if (target instanceof Enumeration) {
            HashSet<T> res = new HashSet<>();
            //noinspection rawtypes
            while (((Enumeration) target).hasMoreElements()) {
                //noinspection rawtypes,unchecked
                res.add((T) ((Enumeration) target).nextElement());
            }

            return res;
        }

        if (target.getClass().isArray()) {
            //noinspection unchecked
            return ofHashSet((T[]) target);
        }

        throw new SpinException("目标对象不是集合类型:" + target.getClass().getName());
    }

    // endregion

    // region 元素筛选

    public static <E, C extends Iterable<E>> E first(C collection) {
        Iterator<E> iterator;
        if (collection != null && (iterator = collection.iterator()).hasNext()) {
            return iterator.next();
        }
        return null;
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

    public static <T extends Iterable<E>, E> E detectWithIndex(T collection, BiPredicate<E, Integer> predicate) {
        if (null == collection) {
            return null;
        }

        int i = 0;
        for (E next : collection) {
            if (predicate.test(next, i++)) {
                return next;
            }
        }

        return null;
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

    public static <T extends Collection<E>, E> T take(T collection, int size) {
        T res = cloneWithoutValues(collection);
        if (null == res) {
            return null;
        }
        parallelStream(collection, PARALLEL_FACTORY).limit(size).forEach(res::add);
        return res;
    }

    /**
     * 查找目标集合中第一个与源集合相同元素
     * 如果两集合不相交，返回{@code null}
     *
     * @param source     源集合
     * @param candidates 目标集合
     * @param <E>        元素类型参数
     * @return 查找到的元素
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
     * @param <T>        元素类型参数
     * @return 查找到的元素
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
     * @return 查找到的元素
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

    // endregion

    // region 集合元素检查

    /**
     * 检查迭代器中是否含有与指定元素相同的元素(equals)
     *
     * @param iterator 待检查的目标迭代器
     * @param element  待检查的目标元素
     * @param <T>      元素类型参数
     * @return 是否含有类型相同的元素
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
     * @return 是否含有指定元素
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
     * @return 是否含有指定元素
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
     * @return 是否相交
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
     * 判断集合中是否仅含有一个唯一元素(去重后唯一)
     *
     * @param collection 待检查的集合
     * @return 是否唯一
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
     * @return 元素类型
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

    // endregion

    // region 集合操作
    public static <T> void forEach(Iterable<T> collection, BiConsumer<Integer, T> consumer) {
        if (null == collection || null == consumer) {
            return;
        }

        int i = 0;
        for (T t : collection) {
            consumer.accept(i, t);
            ++i;
        }
    }


    public static <T> void forEach(Iterable<T> collection, BiConsumer<Integer, T> consumer, Predicate<T> filter) {
        if (null == collection || null == consumer) {
            return;
        }

        int i = 0;
        for (T t : collection) {
            if (null == filter || filter.test(t)) {
                consumer.accept(i, t);
            }
            ++i;
        }
    }

    public static <T> T[][] divide(T[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T[][] res = (T[][]) Array.newInstance(array.getClass(), (int) Math.ceil(array.length * 1.0 / batchSize));
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static byte[][] divide(byte[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        byte[][] res = new byte[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static short[][] divide(short[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        short[][] res = new short[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static int[][] divide(int[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        int[][] res = new int[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static char[][] divide(char[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        char[][] res = new char[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static float[][] divide(float[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        float[][] res = new float[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static double[][] divide(double[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        double[][] res = new double[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static long[][] divide(long[] array, int batchSize) {
        if (null == array) {
            return null;
        }
        long[][] res = new long[(int) Math.ceil(array.length * 1.0 / batchSize)][];
        int idx = 0;
        int group = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            res[group] = Arrays.copyOfRange(array, idx, end);
            idx += batchSize;
            ++group;
        }
        return res;
    }

    public static <T> List<List<T>> divide(List<T> list, int batchSize) {
        Assert.isTrue(batchSize > 0, "分组容量必须大于0");
        if (null == list) {
            return null;
        }
        List<List<T>> res = new ArrayList<>(list.size() / batchSize + 1);
        int i = batchSize;
        for (; i < list.size(); i += batchSize) {
            res.add(list.subList(i - batchSize, i));
        }
        res.add(list.subList(i - batchSize, list.size()));
        return res;
    }

    public static <T> void divide(List<T> list, int batchSize, FinalConsumer<List<T>> consumer) {
        Assert.isTrue(batchSize > 0, "分组容量必须大于0");
        if (null == list) {
            return;
        }
        int i = batchSize;
        for (; i < list.size(); i += batchSize) {
            consumer.accept(list.subList(i - batchSize, i));
        }
        consumer.accept(list.subList(i - batchSize, list.size()));
    }

    public static <T> List<List<T>> divide(Iterable<T> iterable, int batchSize) {
        if (iterable instanceof List) {
            return divide((List<T>) iterable, batchSize);
        }

        List<List<T>> res = new LinkedList<>();
        List<T> work = new ArrayList<>(batchSize);
        for (T it : iterable) {
            if (work.size() == batchSize) {
                res.add(work);
                work = new ArrayList<>(batchSize);
            }
            work.add(it);
        }
        return res;
    }

    /**
     * 将数组中的元素合并到集合中
     *
     * @param array      待合并的数组 (可以为 {@code null})
     * @param collection 合并的目标集合
     * @param <E>        元素类型参数
     */
    @SuppressWarnings("unchecked")
    public static <E> void mergeArrayIntoCollection(Object[] array, Collection<E> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        if (null == array) {
            return;
        }
        for (Object elem : array) {
            collection.add((E) elem);
        }
    }

    /**
     * 将两个集合中的所有元素合并到第一个集合中
     *
     * @param left  第一个集合
     * @param right 第二个集合
     * @param <C>   集合类型
     * @param <E>   集合元素类型
     * @return 合并后的第一个集合
     */
    public static <C extends Collection<E>, E> C mergeIntoLeft(C left, C right) {
        if (null != left && null != right) {
            left.addAll(right);
        }
        return left;
    }

    public static <C extends Collection<E>, E> C removeFromLeft(C left, C right) {
        if (null != left && null != right) {
            left.removeAll(right);
        }
        return left;
    }

    // endregion

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
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
