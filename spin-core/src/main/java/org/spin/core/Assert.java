package org.spin.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 断言工具
 * <p>异常抛出规则：</p>
 * <ul>
 * <li>断言失败将抛出 {@link AssertFailException}.</li>
 * </ul>
 * <p>线程安全</p>
 */
public final class Assert {

    private static final String DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE = "[Assertion failed] - The value %s is not in the specified exclusive range of %s to %s";
    private static final String DEFAULT_GT_EX_MESSAGE = "[Assertion failed] - The value %s must greater than specified value %s";
    private static final String DEFAULT_GE_EX_MESSAGE = "[Assertion failed] - The value %s must greater than or equals specified value %s";
    private static final String DEFAULT_LT_EX_MESSAGE = "[Assertion failed] - The value %s must less than specified value %s";
    private static final String DEFAULT_LE_EX_MESSAGE = "[Assertion failed] - The value %s must less than or equals specified value %s";
    private static final String DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE = "[Assertion failed] - The value %s is not in the specified inclusive range of %s to %s";
    private static final String DEFAULT_MATCHES_PATTERN_EX = "[Assertion failed] - The string %s does not match the pattern %s";
    private static final String DEFAULT_IS_NULL_EX_MESSAGE = "[Assertion failed] - this argument is required; it must not be null";
    private static final String DEFAULT_IS_TRUE_EX_MESSAGE = "[Assertion failed] - this expression must be true";
    private static final String DEFAULT_IS_EQUALS_EX_MESSAGE = "[Assertion failed] - these two objects must be equals";
    private static final String DEFAULT_NOT_EQUALS_EX_MESSAGE = "[Assertion failed] - these two objects must not equals";
    private static final String DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE = "[Assertion failed] - The validated array contains null element at index: %d";
    private static final String DEFAULT_NO_NULL_ELEMENTS_COLLECTION_EX_MESSAGE = "[Assertion failed] - The validated collection contains null element at index: %d";
    private static final String DEFAULT_NOT_BLANK_EX_MESSAGE = "[Assertion failed] - The validated character sequence is blank";
    private static final String DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE = "[Assertion failed] - The validated array is empty";
    private static final String DEFAULT_NOT_EMPTY_CHAR_SEQUENCE_EX_MESSAGE = "[Assertion failed] - The validated character sequence is empty";
    private static final String DEFAULT_NOT_EMPTY_COLLECTION_EX_MESSAGE = "[Assertion failed] - The validated collection is empty";
    private static final String DEFAULT_NOT_EMPTY_MAP_EX_MESSAGE = "[Assertion failed] - The validated map is empty";
    private static final String DEFAULT_VALID_INDEX_ARRAY_EX_MESSAGE = "[Assertion failed] - The validated array index is invalid: %d";
    private static final String DEFAULT_VALID_INDEX_CHAR_SEQUENCE_EX_MESSAGE = "[Assertion failed] - The validated character sequence index is invalid: %d";
    private static final String DEFAULT_VALID_INDEX_COLLECTION_EX_MESSAGE = "[Assertion failed] - The validated collection index is invalid: %d";
    private static final String DEFAULT_IS_ASSIGNABLE_EX_MESSAGE = "[Assertion failed] - Cannot assign a %s to a %s";
    private static final String DEFAULT_IS_INSTANCE_OF_EX_MESSAGE = "[Assertion failed] - Expected type: %s, actual: %s";

    private Assert() {
    }

    public static void isEquals(@Nullable final Object o1, @Nullable final Object o2, Supplier<String> message) {
        if (!Objects.equals(o1, o2)) {
            throw new AssertFailException(message.get());
        }
    }

    public static void isEquals(final Object o1, final Object o2, final String message, final Object... values) {
        if (!Objects.equals(o1, o2)) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
    }

    public static void isEquals(final Object o1, final Object o2) {
        if (!Objects.equals(o1, o2)) {
            throw new AssertFailException(DEFAULT_IS_EQUALS_EX_MESSAGE);
        }
    }

    public static void notEquals(final Object o1, final Object o2, Supplier<String> message) {
        if (Objects.equals(o1, o2)) {
            throw new AssertFailException(message.get());
        }
    }

    public static void notEquals(final Object o1, final Object o2, final String message, final Object... values) {
        if (Objects.equals(o1, o2)) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
    }

    public static void notEquals(final Object o1, final Object o2) {
        if (!Objects.equals(o1, o2)) {
            throw new AssertFailException(DEFAULT_NOT_EQUALS_EX_MESSAGE);
        }
    }

    // isTrue
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则抛出指定的异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    条件不成立时的异常消息, 不能为空
     */
    @Contract(value = "false,_ -> fail")
    public static void isTrue(final boolean expression, @NotNull Supplier<String> message) {
        if (!expression) {
            throw new AssertFailException(message.get());
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d, &#37;s", i, 0.0);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件不成立时的异常信息, 不能为空
     * @param values     条件不成立时，追加在异常信息中的参数
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean)
     */
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0");</pre>
     *
     * @param expression 需要判断的bool表达式
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression) {
        if (!expression) {
            throw new AssertFailException(DEFAULT_IS_TRUE_EX_MESSAGE);
        }
    }

    // notTrue
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则抛出指定的异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    条件成立时的异常消息, 不能为空
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, Supplier<String> message) {
        if (expression) {
            throw new AssertFailException(message.get());
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;d, &#37;s", i, 0.0);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件成立时的异常信息, 不能为空
     * @param values     条件成立时，追加在异常信息中的参数
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean)
     */
    public static void notTrue(final boolean expression, final String message, final Object... values) {
        if (expression) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0");</pre>
     *
     * @param expression 需要判断的bool表达式
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression) {
        if (expression) {
            throw new AssertFailException(DEFAULT_IS_TRUE_EX_MESSAGE);
        }
    }

    // notNull
    //---------------------------------------------------------------------------------

    /**
     * 断言指定对象不为 {@code null}，并返回该对象。否则抛出异常
     * <pre>Assert.notNull(myObject);</pre>
     *
     * <p>异常信息为:  &quot;[Assertion failed] - this argument is required; it must not be null&quot;.</p>
     *
     * @param <T>     类型参数
     * @param object  待检查对象
     * @param message 对象为null时的异常信息
     * @return 返回对象自身 (一定不为 {@code null})
     * @throws AssertFailException 当对象为空时抛出 {@code null}
     * @see #notNull(Object, String, Object...)
     */
    @Contract(value = "!null,_ -> param1;null,_ -> fail")
    @NotNull
    public static <T> T notNull(@Nullable final T object, @NotNull Supplier<String> message) {
        if (object == null) {
            throw new AssertFailException(message.get());
        }
        return object;
    }

    /**
     * 断言指定对象不为 {@code null}，并返回该对象。否则抛出异常
     * <pre>Assert.notNull(myObject, "对象不能为空");</pre>
     *
     * @param <T>     类型参数
     * @param object  待检查对象
     * @param message 对象为null时的异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 返回对象自身 (一定不为 {@code null})
     * @throws AssertFailException 当对象为空时抛出 {@code null}
     * @see #notNull(Object)
     */
    public static <T> T notNull(final T object, final String message, final Object... values) {
        if (object == null) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return object;
    }

    /**
     * 断言指定对象不为 {@code null}，并返回该对象。否则抛出异常
     * <pre>Assert.notNull(myObject);</pre>
     *
     * <p>异常信息为:  &quot;[Assertion failed] - this argument is required; it must not be null&quot;.</p>
     *
     * @param <T>    类型参数
     * @param object 待检查对象
     * @return 返回对象自身 (一定不为 {@code null})
     * @throws AssertFailException 当对象为空时抛出 {@code null}
     * @see #notNull(Object, String, Object...)
     */
    public static <T> T notNull(final T object) {
        if (object == null) {
            throw new AssertFailException(DEFAULT_IS_NULL_EX_MESSAGE);
        }
        return object;
    }

    // notEmpty array
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的数组不为null，且长度&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myArray, "The array must not be empty");</pre>
     *
     * @param <T>     数组元素类型
     * @param array   待检查的数组
     * @param message 异常信息
     * @return 检查通过时返回原数组
     * @throws AssertFailException 当数组为{@code null}时抛出
     * @throws AssertFailException 当数组为空时抛出
     */
    public static <T> T[] notEmpty(final T[] array, final Supplier<String> message) {
        if (array == null || array.length == 0) {
            throw new AssertFailException(message.get());
        }
        return array;
    }

    /**
     * 断言指定的数组不为null，且长度&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myArray, "The array must not be empty");</pre>
     *
     * @param <T>     数组元素类型
     * @param array   待检查的数组
     * @param message 异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 检查通过时返回原数组
     * @throws AssertFailException 当数组为{@code null}时抛出
     * @throws AssertFailException 当数组为空时抛出
     */
    public static <T> T[] notEmpty(final T[] array, final String message, final Object... values) {
        if (array == null || array.length == 0) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return array;
    }

    /**
     * 断言指定的数组不为null，且长度&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myArray);</pre>
     *
     * <p>异常信息为 &quot;The validated array is
     * empty&quot;.</p>
     *
     * @param <T>   数组元素类型
     * @param array 待检查的数组
     * @return 检查通过时返回原数组
     * @throws AssertFailException 当数组为{@code null}时抛出
     * @throws AssertFailException 当数组为空时抛出
     */
    public static <T> T[] notEmpty(final T[] array) {
        if (array == null || array.length == 0) {
            throw new AssertFailException(DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE);
        }
        return array;
    }

    // notEmpty collection
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的集合不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myCollection, "The collection must not be empty");</pre>
     *
     * @param <T>        集合参数类型
     * @param collection 待检查的集合
     * @param message    异常信息
     * @return 检查通过时返回原集合
     * @throws AssertFailException 当集合为{@code null}时抛出
     * @throws AssertFailException 当集合为空时抛出
     */
    public static <T extends Collection<?>> T notEmpty(final T collection, final Supplier<String> message) {
        if (collection == null || collection.isEmpty()) {
            throw new AssertFailException(message.get());
        }
        return collection;
    }

    /**
     * 断言指定的集合不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myCollection, "The collection must not be empty");</pre>
     *
     * @param <T>        集合参数类型
     * @param collection 待检查的集合
     * @param message    异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values     异常信息的填充参数
     * @return 检查通过时返回原集合
     * @throws AssertFailException 当集合为{@code null}时抛出
     * @throws AssertFailException 当集合为空时抛出
     */
    public static <T extends Collection<?>> T notEmpty(final T collection, final String message, final Object... values) {
        if (collection == null || collection.isEmpty()) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return collection;
    }

    /**
     * 断言指定的集合不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myCollection);</pre>
     *
     * <p>异常信息为 &quot;[Assertion failed] - The validated collection is
     * empty&quot;.</p>
     *
     * @param <T>        集合参数类型
     * @param collection 待检查的集合
     * @return 检查通过时返回原集合
     * @throws AssertFailException 当集合为{@code null}时抛出
     * @throws AssertFailException 当集合为空时抛出
     */
    public static <T extends Collection<?>> T notEmpty(final T collection) {
        if (collection == null || collection.isEmpty()) {
            throw new AssertFailException(DEFAULT_NOT_EMPTY_COLLECTION_EX_MESSAGE);
        }
        return collection;
    }

    // notEmpty map
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的Map不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myMap, "The map must not be empty");</pre>
     *
     * @param <T>     Map参数类型
     * @param map     待检查的Map
     * @param message 异常信息
     * @return 检查通过时返回原Map
     * @throws AssertFailException 当Map为{@code null}时抛出
     * @throws AssertFailException 当Map为空时抛出
     */
    public static <T extends Map<?, ?>> T notEmpty(final T map, final Supplier<String> message) {
        if (map == null || map.isEmpty()) {
            throw new AssertFailException(message.get());
        }
        return map;
    }

    /**
     * 断言指定的Map不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myMap, "The map must not be empty");</pre>
     *
     * @param <T>     Map参数类型
     * @param map     待检查的Map
     * @param message 异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 检查通过时返回原Map
     * @throws AssertFailException 当Map为{@code null}时抛出
     * @throws AssertFailException 当Map为空时抛出
     */
    public static <T extends Map<?, ?>> T notEmpty(final T map, final String message, final Object... values) {
        if (map == null || map.isEmpty()) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return map;
    }

    /**
     * 断言指定的Map不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myMap);</pre>
     *
     * <p>异常信息为 &quot;The validated map is
     * empty&quot;.</p>
     *
     * @param <T> Map参数类型
     * @param map 待检查的Map
     * @return 检查通过时返回原Map
     * @throws AssertFailException 当Map为{@code null}时抛出
     * @throws AssertFailException 当Map为空时抛出
     */
    public static <T extends Map<?, ?>> T notEmpty(final T map) {
        if (map == null || map.isEmpty()) {
            throw new AssertFailException(DEFAULT_NOT_EMPTY_MAP_EX_MESSAGE);
        }
        return map;
    }

    // notEmpty string
    //---------------------------------------------------------------------------------

    /**
     * <p>断言指定的字符串一定非空（不为null且长度大于0）
     * </p>
     * <pre>Assert.notEmpty(myString, "The string must not be empty");</pre>
     *
     * @param <T>     字符串类型
     * @param chars   待检查的字符串
     * @param message 检查失败时的信息
     * @return 检查通过时返回原字符串
     * @throws AssertFailException 当字符串为{@code null} 或长度为0时抛出
     */
    public static <T extends CharSequence> T notEmpty(final T chars, final Supplier<String> message) {
        if (chars == null || chars.length() == 0) {
            throw new AssertFailException(message.get());
        }
        return chars;
    }

    /**
     * <p>断言指定的字符串一定非空（不为null且长度大于0）
     * </p>
     * <pre>Assert.notEmpty(myString, "The string must not be empty");</pre>
     *
     * @param <T>     字符串类型
     * @param chars   待检查的字符串
     * @param message {@link String#format(String, Object...)} 检查失败时的信息
     * @param values  填充失败信息的参数
     * @return 检查通过时返回原字符串
     * @throws AssertFailException 当字符串为{@code null} 或长度为0时抛出
     */
    public static <T extends CharSequence> T notEmpty(final T chars, final String message, final Object... values) {
        if (chars == null || chars.length() == 0) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return chars;
    }

    /**
     * <p>Assert that the specified argument character sequence is
     * neither {@code null} nor a length of zero (no characters);
     * otherwise throwing an exception with the specified message.</p>
     * <pre>Assert.notEmpty(myString);</pre>
     *
     * <p>The message in the exception is &quot;The validated
     * character sequence is empty&quot;.</p>
     *
     * @param <T>   the character sequence type
     * @param chars the character sequence to check, validated not null by this method
     * @return the validated character sequence (never {@code null} method for chaining)
     * @throws AssertFailException if the character sequence is {@code null}
     * @throws AssertFailException if the character sequence is empty
     */
    public static <T extends CharSequence> T notEmpty(final T chars) {
        if (chars == null || chars.length() == 0) {
            throw new AssertFailException(DEFAULT_NOT_EMPTY_CHAR_SEQUENCE_EX_MESSAGE);
        }
        return chars;
    }

    // validIndex array
    //---------------------------------------------------------------------------------

    /**
     * <p>Validates that the index is within the bounds of the argument
     * array; otherwise throwing an exception with the specified message.</p>
     * <pre>Assert.validIndex(myArray, 2, "The array index is invalid: ");</pre>
     *
     * <p>If the array is {@code null}, then the message of the exception
     * is &quot;The validated object is null&quot;.</p>
     *
     * @param <T>     the array type
     * @param array   the array to check, validated not null by this method
     * @param index   the index to check
     * @param message the exception message if invalid, not null
     * @return the validated array (never {@code null} for method chaining)
     * @throws AssertFailException if the array is {@code null}
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(Object[], int)
     * @since 3.0
     */
    public static <T> T[] validIndex(final T[] array, final int index, Supplier<String> message) {
        Assert.notNull(array);
        if (index < 0 || index >= array.length) {
            throw new AssertFailException(message.get());
        }
        return array;
    }

    /**
     * <p>Validates that the index is within the bounds of the argument
     * array; otherwise throwing an exception with the specified message.</p>
     * <pre>Assert.validIndex(myArray, 2, "The array index is invalid: ");</pre>
     *
     * <p>If the array is {@code null}, then the message of the exception
     * is &quot;The validated object is null&quot;.</p>
     *
     * @param <T>     the array type
     * @param array   the array to check, validated not null by this method
     * @param index   the index to check
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return the validated array (never {@code null} for method chaining)
     * @throws AssertFailException if the array is {@code null}
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(Object[], int)
     * @since 3.0
     */
    public static <T> T[] validIndex(final T[] array, final int index, final String message, final Object... values) {
        Assert.notNull(array);
        if (index < 0 || index >= array.length) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return array;
    }

    /**
     * <p>Validates that the index is within the bounds of the argument
     * array; otherwise throwing an exception.</p>
     * <pre>Assert.validIndex(myArray, 2);</pre>
     *
     * <p>If the array is {@code null}, then the message of the exception
     * is &quot;The validated object is null&quot;.</p>
     *
     * <p>If the index is invalid, then the message of the exception is
     * &quot;The validated array index is invalid: &quot; followed by the
     * index.</p>
     *
     * @param <T>   the array type
     * @param array the array to check, validated not null by this method
     * @param index the index to check
     * @return the validated array (never {@code null} for method chaining)
     * @throws AssertFailException if the array is {@code null}
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(Object[], int, String, Object...)
     * @since 3.0
     */
    public static <T> T[] validIndex(final T[] array, final int index) {
        return validIndex(array, index, DEFAULT_VALID_INDEX_ARRAY_EX_MESSAGE, index);
    }

    /**
     * 验证数组中没有null元素
     *
     * @param array 待判断数组
     * @param <T>   数组元素类型
     * @return 原数组
     */
    public static <T> T[] doesNotContainsNull(final T[] array) {
        Assert.notNull(array);
        for (int i = 0; i < array.length; i++) {
            if (null == array[i]) {
                throw new AssertFailException(String.format(DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE, i));
            }
        }
        return array;
    }

    // validIndex collection
    //---------------------------------------------------------------------------------

    /**
     * <p>Validates that the index is within the bounds of the argument
     * collection; otherwise throwing an exception with the specified message.</p>
     * <pre>Assert.validIndex(myCollection, 2, "The collection index is invalid: ");</pre>
     *
     * <p>If the collection is {@code null}, then the message of the
     * exception is &quot;The validated object is null&quot;.</p>
     *
     * @param <T>        the collection type
     * @param collection the collection to check, validated not null by this method
     * @param index      the index to check
     * @param message    the  exception message if invalid, not null
     * @return the validated collection (never {@code null} for chaining)
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(Collection, int)
     * @since 3.0
     */
    public static <T extends Collection<?>> T validIndex(final T collection, final int index, final Supplier<String> message) {
        Assert.notNull(collection);
        if (index < 0 || index >= collection.size()) {
            throw new AssertFailException(message.get());
        }
        return collection;
    }

    /**
     * <p>Validates that the index is within the bounds of the argument
     * collection; otherwise throwing an exception with the specified message.</p>
     * <pre>Assert.validIndex(myCollection, 2, "The collection index is invalid: ");</pre>
     *
     * <p>If the collection is {@code null}, then the message of the
     * exception is &quot;The validated object is null&quot;.</p>
     *
     * @param <T>        the collection type
     * @param collection the collection to check, validated not null by this method
     * @param index      the index to check
     * @param message    the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values     the optional values for the formatted exception message, null array not recommended
     * @return the validated collection (never {@code null} for chaining)
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(Collection, int)
     * @since 3.0
     */
    public static <T extends Collection<?>> T validIndex(final T collection, final int index, final String message, final Object... values) {
        Assert.notNull(collection);
        if (index < 0 || index >= collection.size()) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return collection;
    }

    /**
     * <p>Validates that the index is within the bounds of the argument
     * collection; otherwise throwing an exception.</p>
     * <pre>Assert.validIndex(myCollection, 2);</pre>
     *
     * <p>If the index is invalid, then the message of the exception
     * is &quot;The validated collection index is invalid: &quot;
     * followed by the index.</p>
     *
     * @param <T>        the collection type
     * @param collection the collection to check, validated not null by this method
     * @param index      the index to check
     * @return the validated collection (never {@code null} for method chaining)
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(Collection, int, String, Object...)
     * @since 3.0
     */
    public static <T extends Collection<?>> T validIndex(final T collection, final int index) {
        return validIndex(collection, index, DEFAULT_VALID_INDEX_COLLECTION_EX_MESSAGE, index);
    }

    /**
     * 验证集合中没有null元素
     *
     * @param collection 待判断集合
     * @param <T>        集合类型
     * @return 原集合
     */
    public static <T extends Collection<?>> T doesNotContainsNull(final T collection) {
        Assert.notNull(collection);

        int i = 0;
        for (Object o : collection) {
            if (null == o) {
                throw new AssertFailException(String.format(DEFAULT_NO_NULL_ELEMENTS_COLLECTION_EX_MESSAGE, i));
            }
            ++i;
        }
        return collection;
    }

    // validIndex string
    //---------------------------------------------------------------------------------

    /**
     * <p>Validates that the index is within the bounds of the argument
     * character sequence; otherwise throwing an exception with the
     * specified message.</p>
     * <pre>Assert.validIndex(myStr, 2, "The string index is invalid: ");</pre>
     *
     * <p>If the character sequence is {@code null}, then the message
     * of the exception is &quot;The validated object is null&quot;.</p>
     *
     * @param <T>     the character sequence type
     * @param chars   the character sequence to check, validated not null by this method
     * @param index   the index to check
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return the validated character sequence (never {@code null} for method chaining)
     * @throws AssertFailException if the index is invalid
     * @see #validIndex(CharSequence, int)
     * @since 3.0
     */
    public static <T extends CharSequence> T validIndex(final T chars, final int index, final String message, final Object... values) {
        Assert.notNull(chars);
        if (index < 0 || index >= chars.length()) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return chars;
    }

    /**
     * <p>Validates that the index is within the bounds of the argument
     * character sequence; otherwise throwing an exception.</p>
     * <pre>Assert.validIndex(myStr, 2);</pre>
     *
     * <p>If the character sequence is {@code null}, then the message
     * of the exception is &quot;The validated object is
     * null&quot;.</p>
     *
     * <p>If the index is invalid, then the message of the exception
     * is &quot;The validated character sequence index is invalid: &quot;
     * followed by the index.</p>
     *
     * @param <T>   the character sequence type
     * @param chars the character sequence to check, validated not null by this method
     * @param index the index to check
     * @return the validated character sequence (never {@code null} for method chaining)
     * @throws AssertFailException if the character sequence is {@code null}
     * @see #validIndex(CharSequence, int, String, Object...)
     * @since 3.0
     */
    public static <T extends CharSequence> T validIndex(final T chars, final int index) {
        return validIndex(chars, index, DEFAULT_VALID_INDEX_CHAR_SEQUENCE_EX_MESSAGE, index);
    }

    /**
     * Assert that the given String has valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     *
     * @param text    the String to check
     * @param message the exception message to use if the assertion fails
     * @return text本身
     * @throws AssertFailException if the text does not contain valid text content
     */
    public static String notBlank(String text, Supplier<String> message) {
        if (StringUtils.isBlank(text)) {
            throw new AssertFailException(message.get());
        }
        return text;
    }

    /**
     * Assert that the given String has valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     *
     * @param text    the String to check
     * @param message the exception message to use if the assertion fails
     * @return text本身
     * @throws AssertFailException if the text does not contain valid text content
     */
    public static String notBlank(String text, String message) {
        if (StringUtils.isBlank(text)) {
            throw new AssertFailException(message);
        }
        return text;
    }

    /**
     * Assert that the given String has valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     *
     * @param text the String to check
     * @return text本身
     * @throws AssertFailException if the text does not contain valid text content
     */
    public static String notBlank(String text) {
        return notBlank(text, DEFAULT_NOT_BLANK_EX_MESSAGE);

    }

    /**
     * Assert that the given text does not contain the given substring.
     * <pre class="code">Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");</pre>
     *
     * @param textToSearch the text to search
     * @param substring    the substring to find within the text
     * @param message      the exception message to use if the assertion fails
     * @return textToSearch本身
     * @throws AssertFailException if the text contains the substring
     */
    public static String doesNotContain(String textToSearch, String substring, String message) {
        if (StringUtils.isNotEmpty(textToSearch) && StringUtils.isNotEmpty(substring) &&
            textToSearch.contains(substring)) {
            throw new AssertFailException(message);
        }
        return textToSearch;
    }

    /**
     * Assert that the given text does not contain the given substring.
     * <pre class="code">Assert.doesNotContain(name, "rod");</pre>
     *
     * @param textToSearch the text to search
     * @param substring    the substring to find within the text
     * @return textToSearch本身
     * @throws AssertFailException if the text contains the substring
     */
    public static String doesNotContain(String textToSearch, String substring) {
        return doesNotContain(textToSearch, substring,
            "[Assertion failed] - this String argument must not contain the substring [" + substring + "]");
    }

    // matchesPattern
    //---------------------------------------------------------------------------------

    /**
     * <p>Assert that the specified argument character sequence matches the specified regular
     * expression pattern; otherwise throwing an exception.</p>
     * <pre>Assert.matchesPattern("hi", "[a-z]*");</pre>
     *
     * <p>The syntax of the pattern is the one used in the {@link Pattern} class.</p>
     *
     * @param input   the character sequence to validate, not null
     * @param pattern the regular expression pattern, not null
     * @param <T>     字符序列泛型
     * @return input本身
     * @throws AssertFailException if the character sequence does not match the pattern
     * @see #matchesPattern(CharSequence, String, String, Object...)
     * @since 3.0
     */
    public static <T extends CharSequence> T matchesPattern(final T input, final String pattern) {
        if (!Pattern.matches(pattern, input)) {
            throw new AssertFailException(String.format(DEFAULT_MATCHES_PATTERN_EX, input, pattern));
        }
        return input;
    }

    /**
     * <p>Assert that the specified argument character sequence matches the specified regular
     * expression pattern; otherwise throwing an exception with the specified message.</p>
     * <pre>Assert.matchesPattern("hi", "[a-z]*", "%s does not match %s", "hi" "[a-z]*");</pre>
     *
     * <p>The syntax of the pattern is the one used in the {@link Pattern} class.</p>
     *
     * @param input   the character sequence to validate, not null
     * @param pattern the regular expression pattern, not null
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @param <T>     class 泛型
     * @return value本身
     * @throws AssertFailException if the character sequence does not match the pattern
     * @see #matchesPattern(CharSequence, String)
     * @since 3.0
     */
    public static <T extends CharSequence> T matchesPattern(final T input, final String pattern, final String message, final Object... values) {
        if (!Pattern.matches(pattern, input)) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return input;
    }

    // inclusiveBetween
    //---------------------------------------------------------------------------------

    /**
     * <p>Assert that the specified argument object fall between the two
     * inclusive values specified; otherwise, throws an exception.</p>
     * <pre>Assert.inclusiveBetween(0, 2, 1);</pre>
     *
     * @param <T>   the type of the argument object
     * @param start the inclusive start value, not null
     * @param end   the inclusive end value, not null
     * @param value the object to validate, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.0
     */
    public static <T extends Comparable<T>> T inclusiveBetween(final T start, final T end, final T value) {
        if (value.compareTo(start) < 0 || value.compareTo(end) > 0) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * <p>Assert that the specified argument object fall between the two
     * inclusive values specified; otherwise, throws an exception with the
     * specified message.</p>
     * <pre>Assert.inclusiveBetween(0, 2, 1, "Not in boundaries");</pre>
     *
     * @param <T>     the type of the argument object
     * @param start   the inclusive start value, not null
     * @param end     the inclusive end value, not null
     * @param value   the object to validate, not null
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.0
     */
    public static <T extends Comparable<T>> T inclusiveBetween(final T start, final T end, final T value, final String message, final Object... values) {
        if (value.compareTo(start) < 0 || value.compareTo(end) > 0) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception.
     * <pre>Assert.inclusiveBetween(0, 2, 1);</pre>
     *
     * @param start the inclusive start value
     * @param end   the inclusive end value
     * @param value the value to validate
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries (inclusive)
     * @since 3.3
     */
    public static int inclusiveBetween(final int start, final int end, final int value) {
        if (value < start || value > end) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception with the
     * specified message.
     * <pre>Assert.inclusiveBetween(0, 2, 1, "Not in range");</pre>
     *
     * @param start   the inclusive start value
     * @param end     the inclusive end value
     * @param value   the value to validate
     * @param message the exception message if invalid, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.3
     */
    public static int inclusiveBetween(final int start, final int end, final int value, final String message) {
        if (value < start || value > end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception.
     * <pre>Assert.inclusiveBetween(0, 2, 1);</pre>
     *
     * @param start the inclusive start value
     * @param end   the inclusive end value
     * @param value the value to validate
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries (inclusive)
     * @since 3.3
     */
    public static long inclusiveBetween(final long start, final long end, final long value) {
        if (value < start || value > end) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception with the
     * specified message.
     * <pre>Assert.inclusiveBetween(0, 2, 1, "Not in range");</pre>
     *
     * @param start   the inclusive start value
     * @param end     the inclusive end value
     * @param value   the value to validate
     * @param message the exception message if invalid, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.3
     */
    public static long inclusiveBetween(final long start, final long end, final long value, final String message) {
        if (value < start || value > end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception.
     * <pre>Assert.inclusiveBetween(0.1, 2.1, 1.1);</pre>
     *
     * @param start the inclusive start value
     * @param end   the inclusive end value
     * @param value the value to validate
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries (inclusive)
     * @since 3.3
     */
    public static double inclusiveBetween(final double start, final double end, final double value) {
        if (value < start || value > end) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception with the
     * specified message.
     * <pre>Assert.inclusiveBetween(0.1, 2.1, 1.1, "Not in range");</pre>
     *
     * @param start   the inclusive start value
     * @param end     the inclusive end value
     * @param value   the value to validate
     * @param message the exception message if invalid, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.3
     */
    public static double inclusiveBetween(final double start, final double end, final double value, final String message) {
        if (value < start || value > end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    // exclusiveBetween
    //---------------------------------------------------------------------------------

    /**
     * <p>Assert that the specified argument object fall between the two
     * exclusive values specified; otherwise, throws an exception.</p>
     * <pre>Assert.exclusiveBetween(0, 2, 1);</pre>
     *
     * @param <T>   the type of the argument object
     * @param start the exclusive start value, not null
     * @param end   the exclusive end value, not null
     * @param value the object to validate, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.0
     */
    public static <T extends Comparable<T>> T exclusiveBetween(final T start, final T end, final T value) {
        if (value.compareTo(start) <= 0 || value.compareTo(end) >= 0) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * <p>Assert that the specified argument object fall between the two
     * exclusive values specified; otherwise, throws an exception with the
     * specified message.</p>
     * <pre>Assert.exclusiveBetween(0, 2, 1, "Not in boundaries");</pre>
     *
     * @param <T>     the type of the argument object
     * @param start   the exclusive start value, not null
     * @param end     the exclusive end value, not null
     * @param value   the object to validate, not null
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.0
     */
    public static <T extends Comparable<T>> T exclusiveBetween(final T start, final T end, final T value, final String message, final Object... values) {
        if (value.compareTo(start) <= 0 || value.compareTo(end) >= 0) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * exclusive values specified; otherwise, throws an exception.
     * <pre>Assert.exclusiveBetween(0, 2, 1);</pre>
     *
     * @param start the exclusive start value
     * @param end   the exclusive end value
     * @param value the value to validate
     * @return value本身
     * @throws AssertFailException if the value falls out of the boundaries
     * @since 3.3
     */
    public static int exclusiveBetween(final int start, final int end, final int value) {
        if (value <= start || value >= end) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * exclusive values specified; otherwise, throws an exception with the
     * specified message.
     * <pre>Assert.exclusiveBetween(0, 2, 1, "Not in range");</pre>
     *
     * @param start   the exclusive start value
     * @param end     the exclusive end value
     * @param value   the value to validate
     * @param message the exception message if invalid, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.3
     */
    public static int exclusiveBetween(final int start, final int end, final int value, final String message) {
        if (value <= start || value >= end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * exclusive values specified; otherwise, throws an exception.
     * <pre>Assert.exclusiveBetween(0, 2, 1);</pre>
     *
     * @param start the exclusive start value
     * @param end   the exclusive end value
     * @param value the value to validate
     * @return value本身
     * @throws AssertFailException if the value falls out of the boundaries
     * @since 3.3
     */
    public static long exclusiveBetween(final long start, final long end, final long value) {
        if (value <= start || value >= end) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * exclusive values specified; otherwise, throws an exception with the
     * specified message.
     * <pre>Assert.exclusiveBetween(0, 2, 1, "Not in range");</pre>
     *
     * @param start   the exclusive start value
     * @param end     the exclusive end value
     * @param value   the value to validate
     * @param message the exception message if invalid, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.3
     */
    public static long exclusiveBetween(final long start, final long end, final long value, final String message) {
        if (value <= start || value >= end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * exclusive values specified; otherwise, throws an exception.
     * <pre>Assert.exclusiveBetween(0.1, 2.1, 1.1);</pre>
     *
     * @param start the exclusive start value
     * @param end   the exclusive end value
     * @param value the value to validate
     * @return value本身
     * @throws AssertFailException if the value falls out of the boundaries
     * @since 3.3
     */
    public static double exclusiveBetween(final double start, final double end, final double value) {
        if (value <= start || value >= end) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    /**
     * Assert that the specified primitive value falls between the two
     * exclusive values specified; otherwise, throws an exception with the
     * specified message.
     * <pre>Assert.exclusiveBetween(0.1, 2.1, 1.1, "Not in range");</pre>
     *
     * @param start   the exclusive start value
     * @param end     the exclusive end value
     * @param value   the value to validate
     * @param message the exception message if invalid, not null
     * @return value本身
     * @throws AssertFailException if the value falls outside the boundaries
     * @since 3.3
     */
    public static double exclusiveBetween(final double start, final double end, final double value, final String message) {
        if (value <= start || value >= end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    public static void gt(final int value, final int floor, final Supplier<String> message) {
        if (value <= floor) {
            throw new AssertFailException(message.get());
        }
    }

    public static void gt(final int value, final int floor, final String message) {
        if (value <= floor) {
            throw new AssertFailException(message);
        }
    }

    public static void gt(final int value, final int floor) {
        if (value <= floor) {
            throw new AssertFailException(String.format(DEFAULT_GT_EX_MESSAGE, floor, value));
        }
    }

    public static void gt(final long value, final long floor, final Supplier<String> message) {
        if (value <= floor) {
            throw new AssertFailException(message.get());
        }
    }

    public static void gt(final long value, final long floor, final String message) {
        if (value <= floor) {
            throw new AssertFailException(message);
        }
    }

    public static void gt(final long value, final long floor) {
        if (value <= floor) {
            throw new AssertFailException(String.format(DEFAULT_GT_EX_MESSAGE, floor, value));
        }
    }

    public static void gt(final double value, final double floor, final Supplier<String> message) {
        if (value <= floor) {
            throw new AssertFailException(message.get());
        }
    }

    public static void gt(final double value, final double floor, final String message) {
        if (value <= floor) {
            throw new AssertFailException(message);
        }
    }

    public static void gt(final double value, final double floor) {
        if (value <= floor) {
            throw new AssertFailException(String.format(DEFAULT_GT_EX_MESSAGE, floor, value));
        }
    }

    public static <T extends Comparable<T>> T gt(final T value, final T floor, final Supplier<String> message) {
        if (value.compareTo(floor) <= 0) {
            throw new AssertFailException(message.get());
        }
        return value;
    }

    public static <T extends Comparable<T>> T gt(final T value, final T floor, final String message, final Object... params) {
        if (value.compareTo(floor) <= 0) {
            throw new AssertFailException(null != params && params.length > 0 ? String.format(message, params) : message);
        }
        return value;
    }

    public static <T extends Comparable<T>> T gt(final T floor, final T value) {
        if (value.compareTo(floor) <= 0) {
            throw new AssertFailException(String.format(DEFAULT_GT_EX_MESSAGE, value, floor));
        }
        return value;
    }

    public static void ge(final int value, final int floor, final Supplier<String> message) {
        if (value < floor) {
            throw new AssertFailException(message.get());
        }
    }

    public static void ge(final int value, final int floor, final String message) {
        if (value < floor) {
            throw new AssertFailException(message);
        }
    }

    public static void ge(final int value, final int floor) {
        if (value < floor) {
            throw new AssertFailException(String.format(DEFAULT_GE_EX_MESSAGE, value, floor));
        }
    }

    public static void ge(final long value, final long floor, final Supplier<String> message) {
        if (value < floor) {
            throw new AssertFailException(message.get());
        }
    }

    public static void ge(final long value, final long floor, final String message) {
        if (value < floor) {
            throw new AssertFailException(message);
        }
    }

    public static void ge(final long value, final long floor) {
        if (value < floor) {
            throw new AssertFailException(String.format(DEFAULT_GE_EX_MESSAGE, value, floor));
        }
    }

    public static void ge(final double value, final double floor, final Supplier<String> message) {
        if (value < floor) {
            throw new AssertFailException(message.get());
        }
    }

    public static void ge(final double value, final double floor, final String message) {
        if (value < floor) {
            throw new AssertFailException(message);
        }
    }

    public static void ge(final double value, final double floor) {
        if (value < floor) {
            throw new AssertFailException(String.format(DEFAULT_GE_EX_MESSAGE, value, floor));
        }
    }

    public static <T extends Comparable<T>> T ge(final T value, final T floor, final Supplier<String> message) {
        if (value.compareTo(floor) < 0) {
            throw new AssertFailException(message.get());
        }
        return value;
    }

    public static <T extends Comparable<T>> T ge(final T value, final T floor, final String message, final Object... params) {
        if (value.compareTo(floor) < 0) {
            throw new AssertFailException(null != params && params.length > 0 ? String.format(message, params) : message);
        }
        return value;
    }

    public static <T extends Comparable<T>> T ge(final T value, final T floor) {
        if (value.compareTo(floor) < 0) {
            throw new AssertFailException(String.format(DEFAULT_GE_EX_MESSAGE, value, floor));
        }
        return value;
    }

    public static void lt(final int value, final int ceil, final Supplier<String> message) {
        if (value >= ceil) {
            throw new AssertFailException(message.get());
        }
    }

    public static void lt(final int value, final int ceil, final String message) {
        if (value >= ceil) {
            throw new AssertFailException(message);
        }
    }

    public static void lt(final int value, final int ceil) {
        if (value >= ceil) {
            throw new AssertFailException(String.format(DEFAULT_LT_EX_MESSAGE, ceil, value));
        }
    }

    public static void lt(final long value, final long ceil, final Supplier<String> message) {
        if (value >= ceil) {
            throw new AssertFailException(message.get());
        }
    }

    public static void lt(final long value, final long ceil, final String message) {
        if (value >= ceil) {
            throw new AssertFailException(message);
        }
    }

    public static void lt(final long value, final long ceil) {
        if (value >= ceil) {
            throw new AssertFailException(String.format(DEFAULT_LT_EX_MESSAGE, ceil, value));
        }
    }

    public static void lt(final double value, final double ceil, final Supplier<String> message) {
        if (value >= ceil) {
            throw new AssertFailException(message.get());
        }
    }

    public static void lt(final double value, final double ceil, final String message) {
        if (value >= ceil) {
            throw new AssertFailException(message);
        }
    }

    public static void lt(final double value, final double ceil) {
        if (value >= ceil) {
            throw new AssertFailException(String.format(DEFAULT_LT_EX_MESSAGE, ceil, value));
        }
    }

    public static <T extends Comparable<T>> T lt(final T value, final T ceil, final Supplier<String> message) {
        if (value.compareTo(ceil) >= 0) {
            throw new AssertFailException(message.get());
        }
        return value;
    }

    public static <T extends Comparable<T>> T lt(final T value, final T ceil, final String message, final Object... params) {
        if (value.compareTo(ceil) >= 0) {
            throw new AssertFailException(null != params && params.length > 0 ? String.format(message, params) : message);
        }
        return value;
    }

    public static <T extends Comparable<T>> T lt(final T value, final T ceil) {
        if (value.compareTo(ceil) >= 0) {
            throw new AssertFailException(String.format(DEFAULT_LT_EX_MESSAGE, ceil, value));
        }
        return value;
    }

    public static void le(final int value, final int ceil, final Supplier<String> message) {
        if (value > ceil) {
            throw new AssertFailException(message.get());
        }
    }

    public static void le(final int value, final int ceil, final String message) {
        if (value > ceil) {
            throw new AssertFailException(message);
        }
    }

    public static void le(final int value, final int ceil) {
        if (value > ceil) {
            throw new AssertFailException(String.format(DEFAULT_LE_EX_MESSAGE, ceil, value));
        }
    }

    public static void le(final long value, final long ceil, final Supplier<String> message) {
        if (value > ceil) {
            throw new AssertFailException(message.get());
        }
    }

    public static void le(final long value, final long ceil, final String message) {
        if (value > ceil) {
            throw new AssertFailException(message);
        }
    }

    public static void le(final long value, final long ceil) {
        if (value > ceil) {
            throw new AssertFailException(String.format(DEFAULT_LE_EX_MESSAGE, ceil, value));
        }
    }

    public static void le(final double value, final double ceil, final Supplier<String> message) {
        if (value > ceil) {
            throw new AssertFailException(message.get());
        }
    }

    public static void le(final double value, final double ceil, final String message) {
        if (value > ceil) {
            throw new AssertFailException(message);
        }
    }

    public static void le(final double value, final double ceil) {
        if (value > ceil) {
            throw new AssertFailException(String.format(DEFAULT_LE_EX_MESSAGE, ceil, value));
        }
    }

    public static <T extends Comparable<T>> T le(final T value, final T ceil, final Supplier<String> message) {
        if (value.compareTo(ceil) > 0) {
            throw new AssertFailException(message.get());
        }
        return value;
    }

    public static <T extends Comparable<T>> T le(final T value, final T ceil, final String message, final Object... params) {
        if (value.compareTo(ceil) > 0) {
            throw new AssertFailException(null != params && params.length > 0 ? String.format(message, params) : message);
        }
        return value;
    }

    public static <T extends Comparable<T>> T le(final T value, final T ceil) {
        if (value.compareTo(ceil) > 0) {
            throw new AssertFailException(String.format(DEFAULT_LE_EX_MESSAGE, ceil, value));
        }
        return value;
    }
    // isInstanceOf
    //---------------------------------------------------------------------------------

    /**
     * Validates that the argument is an instance of the specified class, if not throws an exception.
     * <p>This method is useful when validating according to an arbitrary class</p>
     * <pre>Assert.isInstanceOf(OkClass.class, object);</pre>
     *
     * <p>The message of the exception is &quot;Expected type: {type}, actual: {obj_type}&quot;</p>
     *
     * @param type the class the object must be validated against, not null
     * @param obj  the object to check, null throws an exception
     * @param <T>  class 泛型
     * @return value本身
     * @throws AssertFailException if argument is not of specified class
     * @see #isInstanceOf(Class, Object, String, Object...)
     * @since 3.0
     */
    public static <T> T isInstanceOf(final Class<T> type, final Object obj) {
        return isInstanceOf(type, obj, DEFAULT_IS_INSTANCE_OF_EX_MESSAGE, type.getName());
    }

    /**
     * <p>Assert that the argument is an instance of the specified class; otherwise
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary class</p>
     * <pre>Assert.isInstanceOf(OkClass.class, object, "Wrong class, object is of class %s",
     *   object.getClass().getName());</pre>
     *
     * @param type    the class the object must be validated against, not null
     * @param obj     the object to check, null throws an exception
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @param <T>     class 泛型
     * @return value本身
     * @throws AssertFailException if argument is not of specified class
     * @see #isInstanceOf(Class, Object)
     * @since 3.0
     */
    public static <T> T isInstanceOf(final Class<?> type, final Object obj, final String message, final Object... values) {
        if (!notNull(type).isInstance(obj)) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        //noinspection unchecked
        return (T) obj;
    }

    // isAssignableFrom
    //---------------------------------------------------------------------------------

    /**
     * Validates that the argument can be converted to the specified class, if not, throws an exception.
     * <p>This method is useful when validating that there will be no casting errors.</p>
     * <pre>Assert.isAssignableFrom(SuperClass.class, object.getClass());</pre>
     *
     * <p>The message format of the exception is &quot;Cannot assign {type} to {superType}&quot;</p>
     *
     * @param superType the class must be validated against, not null
     * @param type      the class to check, not null
     * @param <T>       class 泛型
     * @return value本身
     * @throws AssertFailException if type argument is not assignable to the specified superType
     * @see #isAssignableFrom(Class, Class, String, Object...)
     * @since 3.0
     */
    public static <T> Class<T> isAssignableFrom(final Class<?> superType, final Class<T> type) {
        return isAssignableFrom(superType, type, DEFAULT_IS_ASSIGNABLE_EX_MESSAGE, type.getName(),
            superType.getName());
    }

    /**
     * Validates that the argument can be converted to the specified class, if not throws an exception.
     * <p>This method is useful when validating if there will be no casting errors.</p>
     * <pre>Assert.isAssignableFrom(SuperClass.class, object.getClass());</pre>
     *
     * <p>The message of the exception is &quot;The validated object can not be converted to the&quot;
     * followed by the name of the class and &quot;class&quot;</p>
     *
     * @param superType the class must be validated against, not null
     * @param type      the class to check, not null
     * @param message   the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values    the optional values for the formatted exception message, null array not recommended
     * @param <T>       class 泛型
     * @return type本身
     * @throws AssertFailException if argument can not be converted to the specified class
     * @see #isAssignableFrom(Class, Class)
     */
    public static <T> Class<T> isAssignableFrom(final Class<?> superType, final Class<T> type, final String message, final Object... values) {
        if (!superType.isAssignableFrom(type)) {
            throw new AssertFailException(null == values || values.length == 0 ? message : String.format(message, values));
        }
        return type;
    }
}
