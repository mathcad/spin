package org.spin.core.util;

import org.spin.core.Assert;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

/**
 * 数组工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ArrayUtils extends Util {

    private ArrayUtils() {
    }

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

    public static boolean contains(final Object[] array, final Object objectToFind) {
        return indexOf(array, objectToFind) != -1;
    }

    public static <T> int indexOf(final T[] array, final Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }

    public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
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
     * 判断数组是否不为空
     *
     * @param array 待检查数组
     * @param <T>   数组的类型参数
     * @return 是否不为空
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return (array != null && array.length != 0);
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

    public static <T> void divide(T[] array, int batchSize, CollectionUtils.ShardConsumer<T[]> consumer) {
        if (null == array) {
            return;
        }
        int shards = (int) Math.ceil(array.length * 1.0 / batchSize);
        int idx = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            consumer.accept(shards, idx, Arrays.copyOfRange(array, idx, end));
            idx += batchSize;
        }
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

    public static void divide(byte[] array, int batchSize, CollectionUtils.ShardConsumer<byte[]> consumer) {
        if (null == array) {
            return;
        }
        int shards = (int) Math.ceil(array.length * 1.0 / batchSize);
        int idx = 0;
        while (idx < array.length) {
            int end = idx + batchSize;
            end = Math.min(end, array.length);
            consumer.accept(shards, idx, Arrays.copyOfRange(array, idx, end));
            idx += batchSize;
        }
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

    public static <T> Optional<T> detect(T[] array, Predicate<T> predicate) {
        if (null == array || array.length == 0) {
            return Optional.empty();
        }
        for (T t : array) {
            if (predicate.test(t)) {
                return Optional.ofNullable(t);
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> first(T[] array) {
        if (null == array || array.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(array[0]);
    }

    @SafeVarargs
    public static <T> T[] ofArray(T... elements) {
        return elements;
    }

    public static boolean[] ofArray(boolean... elements) {
        return elements;
    }

    public static byte[] ofArray(byte... elements) {
        return elements;
    }

    public static short[] ofArray(short... elements) {
        return elements;
    }

    public static int[] ofArray(int... elements) {
        return elements;
    }

    public static long[] ofArray(long... elements) {
        return elements;
    }

    public static float[] ofArray(float... elements) {
        return elements;
    }

    public static double[] ofArray(double... elements) {
        return elements;
    }

    public static char[] ofArray(char... elements) {
        return elements;
    }

    public static boolean[] toBooleanArray(List<Boolean> booleans) {
        if (null == booleans) return null;
        boolean[] res = new boolean[booleans.size()];
        for (int i = 0; i < booleans.size(); i++) {
            res[i] = Boolean.TRUE.equals(booleans.get(i));
        }
        return res;
    }

    public static <T extends Number> byte[] toByteArray(List<T> numbers, byte nullValue) {
        if (null == numbers) return null;
        byte[] res = new byte[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            Number n = numbers.get(i);
            res[i] = null == n ? nullValue : n.byteValue();
        }
        return res;
    }

    public static <T extends Number> short[] toShortArray(List<T> numbers, short nullValue) {
        if (null == numbers) return null;
        short[] res = new short[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            Number n = numbers.get(i);
            res[i] = null == n ? nullValue : n.shortValue();
        }
        return res;
    }

    public static <T extends Number> int[] toIntArray(List<T> numbers, int nullValue) {
        if (null == numbers) return null;
        int[] res = new int[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            Number n = numbers.get(i);
            res[i] = null == n ? nullValue : n.intValue();
        }
        return res;
    }

    public static <T extends Number> long[] toLongArray(List<T> numbers, long nullValue) {
        if (null == numbers) return null;
        long[] res = new long[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            Number n = numbers.get(i);
            res[i] = null == n ? nullValue : n.longValue();
        }
        return res;
    }

    public static <T extends Number> float[] toFloatArray(List<T> numbers, float nullValue) {
        if (null == numbers) return null;
        float[] res = new float[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            Number n = numbers.get(i);
            res[i] = null == n ? nullValue : n.floatValue();
        }
        return res;
    }

    public static <T extends Number> double[] toDoubleArray(List<T> numbers, double nullValue) {
        if (null == numbers) return null;
        double[] res = new double[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            Number n = numbers.get(i);
            res[i] = null == n ? nullValue : n.doubleValue();
        }
        return res;
    }

    public static <T extends Number> byte[] toByteArray(T[] numbers, byte nullValue) {
        if (null == numbers) return null;
        byte[] res = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            res[i] = null == numbers[i] ? nullValue : numbers[i].byteValue();
        }
        return res;
    }

    public static <T extends Number> short[] toShortArray(T[] numbers, short nullValue) {
        if (null == numbers) return null;
        short[] res = new short[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            res[i] = null == numbers[i] ? nullValue : numbers[i].shortValue();
        }
        return res;
    }

    public static <T extends Number> int[] toIntArray(T[] numbers, int nullValue) {
        if (null == numbers) return null;
        int[] res = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            res[i] = null == numbers[i] ? nullValue : numbers[i].intValue();
        }
        return res;
    }

    public static <T extends Number> long[] toLongArray(T[] numbers, long nullValue) {
        if (null == numbers) return null;
        long[] res = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            res[i] = null == numbers[i] ? nullValue : numbers[i].longValue();
        }
        return res;
    }

    public static <T extends Number> float[] toFloatArray(T[] numbers, float nullValue) {
        if (null == numbers) return null;
        float[] res = new float[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            res[i] = null == numbers[i] ? nullValue : numbers[i].floatValue();
        }
        return res;
    }

    public static <T extends Number> double[] toDoubleArray(T[] numbers, double nullValue) {
        if (null == numbers) return null;
        double[] res = new double[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            res[i] = null == numbers[i] ? nullValue : numbers[i].doubleValue();
        }
        return res;
    }


    /**
     * 将枚举类型的所有枚举值包装成为一个数组
     *
     * @param enumeration 枚举
     * @param array       存放枚举常量的容器
     * @param <T>         枚举类型
     * @return 枚举常量数组
     */
    public static <T> T[] toArray(Enumeration<T> enumeration, T[] array) {
        ArrayList<T> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }
}
