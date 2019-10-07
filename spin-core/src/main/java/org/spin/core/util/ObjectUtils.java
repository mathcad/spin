package org.spin.core.util;

import org.spin.core.annotation.UserEnum;
import org.spin.core.trait.IntEvaluatable;
import org.spin.core.trait.IntegerEvaluatable;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * object 工具类
 *
 * @author xuweinan
 * @see ClassUtils
 * @see CollectionUtils
 * @see StringUtils
 */
public abstract class ObjectUtils {

    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;

    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = "";
    private static final String ARRAY_START = "{";
    private static final String ARRAY_END = "}";
    private static final String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";

    private ObjectUtils() {
    }

    /**
     * 判断一个异常对象是否是检查异常
     *
     * @param ex 待检查异常
     * @return 异常是否是检查异常
     * @see Exception
     * @see RuntimeException
     * @see Error
     */
    public static boolean isCheckedException(Throwable ex) {
        return !(ex instanceof RuntimeException || ex instanceof Error);
    }

    /**
     * 判断指定的异常是否与指定类型匹配
     *
     * @param ex                 待检查异常
     * @param declaredExceptions 待判断的异常类型
     * @return 是否匹配
     */
    public static boolean isCompatibleWithThrowsClause(Throwable ex, Class<?>... declaredExceptions) {
        if (!isCheckedException(ex)) {
            return true;
        }
        if (declaredExceptions != null) {
            for (Class<?> declaredException : declaredExceptions) {
                if (declaredException.isInstance(ex)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断一个对象是否是数组类型
     * <p>不关心元素类型，无论其是基本类型还是对象类型</p>
     *
     * @param obj 待判断的对象
     * @return 是否是数组
     */
    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * 判断一个数组是否为空
     * i.e. {@code null} 或者长度为0
     *
     * @param array 待判断的数组
     * @return 是否为空
     * @see #isEmpty(Object)
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * 判断一个对象是否为“空”
     * <p>支持以下参数类型</p>
     * <ul>
     * <li>{@code Array}: 为null或者长度为0</li>
     * <li>{@link CharSequence}: 为null或者长度为0</li>
     * <li>{@link Collection}: 与{@link Collection#isEmpty()}相同</li>
     * <li>{@link Map}: 与{@link Map#isEmpty()}相同</li>
     * </ul>
     * <p>如果给定的对象不为null且不是上述几种类型中的一种，将返回{@code false}.
     *
     * @param obj 待检查的对象
     * @return {@code true} 如果对象不为 {@code null} 或 <em>empty</em>
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }

        // else
        return false;
    }

    /**
     * 将指定对象具象化(优先)或转换为指定类型
     * <p>1.如果目标类型位于{@code target}的实际类型的的继承树上(上级或同级)，直接转换(具象化)。</p>
     * <p>2.如果目标类型与{@code target}实际类型没有关系，则判断是否是基本类型之间的转换。<br>
     * <i>2.1</i> 如果是，则进行相应转换(可以相容)，如果不相容(如整形转换为byte时的超出范围等),转换失败。<br>
     * <i>2.2</i> 如果不是基本类型间互转，转换失败</p>
     * <p>3.如果目标类型是{@code String}，则与{@link ObjectUtils#toString(Object)}效果相同。</p>
     * <p>4.如果将{@code null}转换为基本类型，转换失败；否则，将返回{@code null}。</p>
     *
     * @param type   转换的目标类型
     * @param target 待转换对象
     * @param <T>    目标类型参数
     * @return 转换类型后的对象
     * @throws ClassCastException 转换失败，抛出异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Class<T> type, Object target) {
        if (null == type)
            throw new IllegalArgumentException("The type witch convert to can not be null");
        if (target != null && (type.isInstance(target) || ClassUtils.isAssignable(type, target.getClass(), true))) {
            return (T) target;
        } else if (BigDecimal.class.equals(type)) {
            return target == null ? null : (T) new BigDecimal(target.toString());
        } else if ((type.getAnnotation(UserEnum.class) != null || IntEvaluatable.class.isAssignableFrom(type) || IntegerEvaluatable.class.isAssignableFrom(type)) && target != null) {
            return (T) EnumUtils.getEnum((Class<Enum>) type, Integer.valueOf(target.toString()));
        } else {
            Class<?> typePrimitive = ClassUtils.wrapperToPrimitive(type);
            if (null == target) {
                if (type.isPrimitive())
                    throw new ClassCastException("Can not cast null to base type:" + type.getName());
                else
                    return null;
            }
            if (null != typePrimitive) {
                if (typePrimitive.equals(short.class))
                    return (T) Short.valueOf(target.toString());
                else if (typePrimitive.equals(int.class))
                    return (T) Integer.valueOf(target.toString());
                else if (typePrimitive.equals(long.class))
                    return (T) Long.valueOf(target.toString());
                else if (typePrimitive.equals(float.class))
                    return (T) Float.valueOf(target.toString());
                else if (typePrimitive.equals(double.class))
                    return (T) Double.valueOf(target.toString());
                else if (typePrimitive.equals(byte.class))
                    return (T) Byte.valueOf(target.toString());
                else if (typePrimitive.equals(boolean.class))
                    return (T) Boolean.valueOf(target.toString());
                else if (typePrimitive.equals(char.class))
                    return (T) Character.valueOf((char) Integer.parseInt(target.toString()));
                else if (CharSequence.class.isAssignableFrom(typePrimitive))
                    return (T) toString(target, null);
            }
        }
        throw new ClassCastException("Can not cast target:" + target + "[" + target.getClass().getName() + "] to type:" + type.getName());
    }

    /**
     * 判断数组中是否含有指定元素
     *
     * @param array   待检查的数组(可以为 {@code null}, 此时永远返回 {@code false})
     * @param element 待检查元素
     * @return element是否包含于array中
     */
    public static boolean containsElement(Object[] array, Object element) {
        if (array == null) {
            return false;
        }
        for (Object arrayEle : array) {
            if (nullSafeEquals(arrayEle, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 向数组中添加元素，总是返回一个新数组，长度+1，最后一个元素为刚添加的元素
     *
     * @param array 添加元素的数组(可以为 {@code null})
     * @param obj   待添加的元素
     * @param <A>   数组元素泛型
     * @param <O>   obj的类型
     * @return 新的数组 (不为 {@code null})
     */
    public static <A, O extends A> A[] addObjectToArray(A[] array, O obj) {
        Class<?> compType = Object.class;
        if (array != null) {
            compType = array.getClass().getComponentType();
        } else if (obj != null) {
            compType = obj.getClass();
        }
        int newArrLength = (array != null ? array.length + 1 : 1);
        @SuppressWarnings("unchecked")
        A[] newArr = (A[]) Array.newInstance(compType, newArrLength);
        if (array != null) {
            System.arraycopy(array, 0, newArr, 0, array.length);
        }
        newArr[newArr.length - 1] = obj;
        return newArr;
    }

    //---------------------------------------------------------------------
    // Convenience methods for content-based equality/hash-code handling
    //---------------------------------------------------------------------

    /**
     * 对{@code null}安全的equals方法
     * <p>如果两个对象相等或均为{@code null}，或都是false，或equals，返回{@code true}</p>
     * <p>通过Arrays.equals判断数组是否相等（基于数组元素的比较）</p>
     *
     * @param o1 第一个待比较对象
     * @param o2 第二个待比较对象
     * @return 两个对象是否相等
     * @see Arrays#equals
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            if (o1 instanceof Object[] && o2 instanceof Object[]) {
                return Arrays.equals((Object[]) o1, (Object[]) o2);
            }
            if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
                return Arrays.equals((boolean[]) o1, (boolean[]) o2);
            }
            if (o1 instanceof byte[] && o2 instanceof byte[]) {
                return Arrays.equals((byte[]) o1, (byte[]) o2);
            }
            if (o1 instanceof char[] && o2 instanceof char[]) {
                return Arrays.equals((char[]) o1, (char[]) o2);
            }
            if (o1 instanceof double[] && o2 instanceof double[]) {
                return Arrays.equals((double[]) o1, (double[]) o2);
            }
            if (o1 instanceof float[] && o2 instanceof float[]) {
                return Arrays.equals((float[]) o1, (float[]) o2);
            }
            if (o1 instanceof int[] && o2 instanceof int[]) {
                return Arrays.equals((int[]) o1, (int[]) o2);
            }
            if (o1 instanceof long[] && o2 instanceof long[]) {
                return Arrays.equals((long[]) o1, (long[]) o2);
            }
            if (o1 instanceof short[] && o2 instanceof short[]) {
                return Arrays.equals((short[]) o1, (short[]) o2);
            }
        }
        return false;
    }

    /**
     * 计算对象的hashcode，如果是{@code null},返回0
     *
     * @param obj 对象
     * @return 对象的hashcode
     * @see #nullSafeHashCode(Object[])
     * @see #nullSafeHashCode(boolean[])
     * @see #nullSafeHashCode(byte[])
     * @see #nullSafeHashCode(char[])
     * @see #nullSafeHashCode(double[])
     * @see #nullSafeHashCode(float[])
     * @see #nullSafeHashCode(int[])
     * @see #nullSafeHashCode(long[])
     * @see #nullSafeHashCode(short[])
     */
    public static int nullSafeHashCode(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return nullSafeHashCode((Object[]) obj);
            }
            if (obj instanceof boolean[]) {
                return nullSafeHashCode((boolean[]) obj);
            }
            if (obj instanceof byte[]) {
                return nullSafeHashCode((byte[]) obj);
            }
            if (obj instanceof char[]) {
                return nullSafeHashCode((char[]) obj);
            }
            if (obj instanceof double[]) {
                return nullSafeHashCode((double[]) obj);
            }
            if (obj instanceof float[]) {
                return nullSafeHashCode((float[]) obj);
            }
            if (obj instanceof int[]) {
                return nullSafeHashCode((int[]) obj);
            }
            if (obj instanceof long[]) {
                return nullSafeHashCode((long[]) obj);
            }
            if (obj instanceof short[]) {
                return nullSafeHashCode((short[]) obj);
            }
        }
        return obj.hashCode();
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(Object[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (Object element : array) {
            hash = MULTIPLIER * hash + nullSafeHashCode(element);
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(boolean[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (boolean element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(byte[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (byte element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(char[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (char element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(double[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (double element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(float[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (float element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(int[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (int element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(long[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (long element : array) {
            hash = MULTIPLIER * hash + hashCode(element);
        }
        return hash;
    }

    /**
     * 计算数组的hashcode
     *
     * @param array 数组
     * @return hashcode
     */
    public static int nullSafeHashCode(short[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (short element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * 计算Boolean的hashcode {@link Boolean#hashCode()}.
     *
     * @param bool bool值
     * @return hashcode
     */
    public static int hashCode(boolean bool) {
        return (bool ? 1231 : 1237);
    }

    /**
     * 计算hashcode
     *
     * @param dbl bool值
     * @return hashcode
     */
    public static int hashCode(double dbl) {
        return hashCode(Double.doubleToLongBits(dbl));
    }

    public static int hashCode(float flt) {
        return Float.floatToIntBits(flt);
    }

    public static int hashCode(long lng) {
        return (int) (lng ^ (lng >>> 32));
    }

    public static String nullSafeClassName(Object obj) {
        return (obj != null ? obj.getClass().getName() : NULL_STRING);
    }

    public static String nullSafeToString(Object obj) {
        if (obj == null) {
            return NULL_STRING;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Object[]) {
            return nullSafeToString((Object[]) obj);
        }
        if (obj instanceof boolean[]) {
            return nullSafeToString((boolean[]) obj);
        }
        if (obj instanceof byte[]) {
            return nullSafeToString((byte[]) obj);
        }
        if (obj instanceof char[]) {
            return nullSafeToString((char[]) obj);
        }
        if (obj instanceof double[]) {
            return nullSafeToString((double[]) obj);
        }
        if (obj instanceof float[]) {
            return nullSafeToString((float[]) obj);
        }
        if (obj instanceof int[]) {
            return nullSafeToString((int[]) obj);
        }
        if (obj instanceof long[]) {
            return nullSafeToString((long[]) obj);
        }
        if (obj instanceof short[]) {
            return nullSafeToString((short[]) obj);
        }
        String str = obj.toString();
        return (str != null ? str : EMPTY_STRING);
    }

    public static String nullSafeToString(Object[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }
            sb.append(String.valueOf(array[i]));
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(boolean[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }

            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(byte[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }
            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(char[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }
            sb.append("'").append(array[i]).append("'");
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(double[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }

            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(float[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }

            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(int[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }
            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(long[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }
            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public static String nullSafeToString(short[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(ARRAY_START);
            } else {
                sb.append(ARRAY_ELEMENT_SEPARATOR);
            }
            sb.append(array[i]);
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    // ToString
    //-----------------------------------------------------------------------

    /**
     * <p>Gets the {@code toString} of an {@code Object} returning
     * an empty string ("") if {@code null} input.</p>
     * <pre>
     * ObjectUtils.toString(null)         = ""
     * ObjectUtils.toString("")           = ""
     * ObjectUtils.toString("bat")        = "bat"
     * ObjectUtils.toString(Boolean.TRUE) = "true"
     * </pre>
     *
     * @param obj the Object to {@code toString}, may be null
     * @return the passed in Object's toString, or {@code ""} if {@code null} input
     * @see String#valueOf(Object)
     * @since 2.0
     */
    public static String toString(final Object obj) {
        return obj == null ? NULL_STRING : obj.toString();
    }

    /**
     * <p>Gets the {@code toString} of an {@code Object} returning
     * a specified text if {@code null} input.</p>
     * <pre>
     * ObjectUtils.toString(null, null)           = null
     * ObjectUtils.toString(null, "null")         = "null"
     * ObjectUtils.toString("", "null")           = ""
     * ObjectUtils.toString("bat", "null")        = "bat"
     * ObjectUtils.toString(Boolean.TRUE, "null") = "true"
     * </pre>
     *
     * @param obj     the Object to {@code toString}, may be null
     * @param nullStr the String to return if {@code null} input, may be null
     * @return the passed in Object's toString, or {@code nullStr} if {@code null} input
     * @see String#valueOf(Object)
     * @since 2.0
     */
    public static String toString(final Object obj, final String nullStr) {
        return obj == null ? nullStr : obj.toString();
    }

    /**
     * 计算对象长度，如果是字符串调用其length函数，集合类调用其size函数，数组调用其length属性，其他可遍历对象遍历计算长度<br>
     * 支持的类型包括：
     * <ul>
     * <li>CharSequence</li>
     * <li>Map</li>
     * <li>Iterator</li>
     * <li>Enumeration</li>
     * <li>Array</li>
     * </ul>
     *
     * @param obj 被计算长度的对象
     * @return 长度
     */
    public static int length(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }

        int count;
        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            count = 0;
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            count = 0;
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        return -1;
    }
}
