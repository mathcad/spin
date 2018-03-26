package org.spin.core;

import org.spin.core.throwable.AssertFailException;
import org.spin.core.util.StringUtils;

import java.util.Collection;
import java.util.Map;
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
public abstract class Assert {

    private static final String DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE = "[Assertion failed] - The value %s is not in the specified exclusive range of %s to %s";
    private static final String DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE = "[Assertion failed] - The value %s is not in the specified inclusive range of %s to %s";
    private static final String DEFAULT_MATCHES_PATTERN_EX = "[Assertion failed] - The string %s does not match the pattern %s";
    private static final String DEFAULT_IS_NULL_EX_MESSAGE = "[Assertion failed] - this argument is required; it must not be null";
    private static final String DEFAULT_IS_TRUE_EX_MESSAGE = "[Assertion failed] - this expression must be true";
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
    private static final String DEFAULT_VALID_STATE_EX_MESSAGE = "[Assertion failed] - The validated state must be true";
    private static final String DEFAULT_IS_ASSIGNABLE_EX_MESSAGE = "[Assertion failed] - Cannot assign a %s to a %s";
    private static final String DEFAULT_IS_INSTANCE_OF_EX_MESSAGE = "[Assertion failed] - Expected type: %s, actual: %s";

    private Assert() {
    }
    // isTrue
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则抛出指定的异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param exception  条件不成立时的异常, 不能为空
     */
    public static void isTrue(final boolean expression, Supplier<? extends RuntimeException> exception) {
        if (!expression) {
            throw exception.get();
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件不成立时的异常信息, 不能为空
     * @param value      条件不成立时，追加在异常信息中的long值
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean)
     * @see #isTrue(boolean, String, double)
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression, final String message, final long value) {
        if (!expression) {
            throw new AssertFailException(String.format(message, value));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;s", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件不成立时的异常信息, 不能为空
     * @param value      条件不成立时，追加在异常信息中的double值
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean)
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression, final String message, final double value) {
        if (!expression) {
            throw new AssertFailException(String.format(message, value));
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
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, double)
     */
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new AssertFailException(String.format(message, values));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0");</pre>
     *
     * @param expression 需要判断的bool表达式
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, double)
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
     * @param exception  条件成立时的异常, 不能为空
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, double)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, Supplier<? extends RuntimeException> exception) {
        if (expression) {
            throw exception.get();
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件成立时的异常信息, 不能为空
     * @param value      条件成立时，追加在异常信息中的long值
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, double)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, final String message, final long value) {
        if (expression) {
            throw new AssertFailException(String.format(message, value));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;s", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件成立时的异常信息, 不能为空
     * @param value      条件成立时，追加在异常信息中的double值
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, long)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, final String message, final double value) {
        if (expression) {
            throw new AssertFailException(String.format(message, value));
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
     * @see #notTrue(boolean, String, long)
     * @see #notTrue(boolean, String, double)
     */
    public static void notTrue(final boolean expression, final String message, final Object... values) {
        if (expression) {
            throw new AssertFailException(String.format(message, values));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0");</pre>
     *
     * @param expression 需要判断的bool表达式
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean, String, long)
     * @see #notTrue(boolean, String, double)
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
     * @param <T>    类型参数
     * @param object 待检查对象
     * @return 返回对象自身 (一定不为 {@code null})
     * @throws AssertFailException 当对象为空时抛出 {@code null}
     * @see #notNull(Object, String, Object...)
     */
    public static <T> T notNull(final T object) {
        return notNull(object, DEFAULT_IS_NULL_EX_MESSAGE);
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
            throw new AssertFailException(String.format(message, values));
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
     * @param message 异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 检查通过时返回原数组
     * @throws AssertFailException 当数组为{@code null}时抛出
     * @throws AssertFailException 当数组为空时抛出
     */
    public static <T> T[] notEmpty(final T[] array, final String message, final Object... values) {
        if (array == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (array.length == 0) {
            throw new AssertFailException(String.format(message, values));
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
        return notEmpty(array, DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE);
    }

    // notEmpty collection
    //---------------------------------------------------------------------------------

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
        if (collection == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (collection.isEmpty()) {
            throw new AssertFailException(String.format(message, values));
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
        return notEmpty(collection, DEFAULT_NOT_EMPTY_COLLECTION_EX_MESSAGE);
    }

    // notEmpty map
    //---------------------------------------------------------------------------------

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
        if (map == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (map.isEmpty()) {
            throw new AssertFailException(String.format(message, values));
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
        return notEmpty(map, DEFAULT_NOT_EMPTY_MAP_EX_MESSAGE);
    }

    // notEmpty string
    //---------------------------------------------------------------------------------

    /**
     * <p>Assert that the specified argument character sequence is
     * neither {@code null} nor a length of zero (no characters);
     * otherwise throwing an exception with the specified message.
     * </p>
     * <pre>Assert.notEmpty(myString, "The string must not be empty");</pre>
     *
     * @param <T>     the character sequence type
     * @param chars   the character sequence to check, validated not null by this method
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return the validated character sequence (never {@code null} method for chaining)
     * @throws AssertFailException if the character sequence is {@code null}
     * @throws AssertFailException if the character sequence is empty
     */
    public static <T extends CharSequence> T notEmpty(final T chars, final String message, final Object... values) {
        if (chars == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (chars.length() == 0) {
            throw new AssertFailException(String.format(message, values));
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
        return notEmpty(chars, DEFAULT_NOT_EMPTY_CHAR_SEQUENCE_EX_MESSAGE);
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
            throw new AssertFailException(String.format(message, values));
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
            throw new AssertFailException(String.format(message, values));
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
            throw new AssertFailException(String.format(message, values));
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
        return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");

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
     * @return value本身
     * @throws AssertFailException if the character sequence does not match the pattern
     * @see #matchesPattern(CharSequence, String)
     * @since 3.0
     */
    public static <T extends CharSequence> T matchesPattern(final T input, final String pattern, final String message, final Object... values) {
        if (!Pattern.matches(pattern, input)) {
            throw new AssertFailException(String.format(message, values));
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
            throw new AssertFailException(String.format(message, values));
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
            throw new AssertFailException(String.format(message, values));
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
     * <pre>Assert.isInstanceOf(OkClass.classs, object, "Wrong class, object is of class %s",
     *   object.getClass().getName());</pre>
     *
     * @param type    the class the object must be validated against, not null
     * @param obj     the object to check, null throws an exception
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return value本身
     * @throws AssertFailException if argument is not of specified class
     * @see #isInstanceOf(Class, Object)
     * @since 3.0
     */
    public static <T> T isInstanceOf(final Class<?> type, final Object obj, final String message, final Object... values) {
        if (!notNull(type).isInstance(obj)) {
            throw new AssertFailException(String.format(message, values));
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
     * @param superType the class the class must be validated against, not null
     * @param type      the class to check, not null
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
     * @param superType the class the class must be validated against, not null
     * @param type      the class to check, not null
     * @param message   the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values    the optional values for the formatted exception message, null array not recommended
     * @return type本身
     * @throws AssertFailException if argument can not be converted to the specified class
     * @see #isAssignableFrom(Class, Class)
     */
    public static <T> Class<T> isAssignableFrom(final Class<?> superType, final Class<T> type, final String message, final Object... values) {
        if (!superType.isAssignableFrom(type)) {
            throw new AssertFailException(String.format(message, values));
        }
        return type;
    }
}

