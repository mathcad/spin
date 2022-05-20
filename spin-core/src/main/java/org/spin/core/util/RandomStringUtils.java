package org.spin.core.util;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>随机字符串工具类</p>
 *
 * <p><em>线程安全</em></p>
 *
 * @since 1.0
 */
public final class RandomStringUtils extends Util {

    private RandomStringUtils() {
    }

    // Random
    //-----------------------------------------------------------------------

    /**
     * <p>创建指定长度的随机字符串</p>
     *
     * <p>将会从所有字符中选取随机字符</p>
     *
     * @param count 需要生成的随机字符个数
     * @return 随机字符串
     */
    public static String random(final int count) {
        return random(count, false, false);
    }

    /**
     * <p>创建指定长度的随机字符串</p>
     *
     * <p>将会从ASCII码{@code 32} 到 {@code 126} 之间随机选取字符</p>
     *
     * @param count 需要生成的随机字符个数
     * @return 随机字符串
     */
    public static String randomAscii(final int count) {
        return random(count, 32, 127, false, false);
    }

    /**
     * <p>创建指定长度的随机字符串</p>
     *
     * <p>将会从小写英文字母中随机选取字符</p>
     *
     * @param count 需要生成的随机字符个数
     * @return 随机字符串
     */
    public static String randomAlphabeticL(final int count) {
        return random(count, 97, 123, false, false);
    }

    /**
     * <p>创建指定长度的随机字符串</p>
     *
     * <p>将会从大写英文字母中随机选取字符</p>
     *
     * @param count 需要生成的随机字符个数
     * @return 随机字符串
     */
    public static String randomAlphabeticU(final int count) {
        return random(count, 65, 91, false, false);
    }

    /**
     * <p>创建指定长度的随机字符串</p>
     *
     * <p>将会从所有大、小写英文字母中随机选取字符</p>
     *
     * @param count 需要生成的随机字符个数
     * @return 随机字符串
     */
    public static String randomAlphabetic(final int count) {
        return random(count, true, false);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters.</p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomAlphanumeric(final int count) {
        return random(count, true, true);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of numeric
     * characters.</p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomNumeric(final int count) {
        return random(count, false, true);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.</p>
     *
     * @param count   the length of random string to create
     * @param letters if {@code true}, generated string may include
     *                alphabetic characters
     * @param numbers if {@code true}, generated string may include
     *                numeric characters
     * @return the random string
     */
    public static String random(final int count, final boolean letters, final boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.</p>
     *
     * @param count   the length of random string to create
     * @param start   the position in set of chars to start at
     * @param end     the position in set of chars to end before
     * @param letters if {@code true}, generated string may include
     *                alphabetic characters
     * @param numbers if {@code true}, generated string may include
     *                numeric characters
     * @return the random string
     */
    public static String random(final int count, final int start, final int end, final boolean letters, final boolean numbers) {
        return random(count, start, end, letters, numbers, null, ThreadLocalRandom.current());
    }

    /**
     * <p>Creates a random string based on a variety of options, using
     * default source of randomness.</p>
     *
     * <p>This method has exactly the same semantics as
     * {@link #random(int, int, int, boolean, boolean, char[], Random)}, but
     * instead of using an externally supplied source of randomness, it uses
     * the internal static {@link Random} instance.</p>
     *
     * @param count   the length of random string to create
     * @param start   the position in set of chars to start at
     * @param end     the position in set of chars to end before
     * @param letters only allow letters?
     * @param numbers only allow numbers?
     * @param chars   the set of chars to choose randoms from.
     *                If {@code null}, then it will use the set of all chars.
     * @return the random string
     * @throws ArrayIndexOutOfBoundsException if there are not
     *                                        {@code (end - start) + 1} characters in the set array.
     */
    public static String random(final int count, final int start, final int end, final boolean letters, final boolean numbers, final char... chars) {
        return random(count, start, end, letters, numbers, chars, ThreadLocalRandom.current());
    }

    /**
     * <p>Creates a random string based on a variety of options, using
     * supplied source of randomness.</p>
     *
     * <p>If start and end are both {@code 0}, start and end are set
     * to {@code ' '} and {@code 'z'}, the ASCII printable
     * characters, will be used, unless letters and numbers are both
     * {@code false}, in which case, start and end are set to
     * {@code 0} and {@code Integer.MAX_VALUE}.
     *
     * <p>If set is not {@code null}, characters between start and
     * end are chosen.</p>
     *
     * <p>This method accepts a user-supplied {@link Random}
     * instance to use as a source of randomness. By seeding a single
     * {@link Random} instance with a fixed seed and using it for each call,
     * the same random sequence of strings can be generated repeatedly
     * and predictably.</p>
     *
     * @param count   the length of random string to create
     * @param start   the position in set of chars to start at
     * @param end     the position in set of chars to end before
     * @param letters only allow letters?
     * @param numbers only allow numbers?
     * @param chars   the set of chars to choose randoms from, must not be empty.
     *                If {@code null}, then it will use the set of all chars.
     * @param random  a source of randomness.
     * @return the random string
     * @throws ArrayIndexOutOfBoundsException if there are not
     *                                        {@code (end - start) + 1} characters in the set array.
     * @throws IllegalArgumentException       if {@code count} &lt; 0 or the provided chars array is empty.
     * @since 2.0
     */
    public static String random(int count, int start, int end, final boolean letters, final boolean numbers,
                                final char[] chars, final Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if (chars != null && chars.length == 0) {
            throw new IllegalArgumentException("The chars array must not be empty");
        }

        if (start == 0 && end == 0) {
            if (chars != null) {
                end = chars.length;
            } else {
                if (!letters && !numbers) {
                    end = Integer.MAX_VALUE;
                } else {
                    end = 'z' + 1;
                    start = ' ';
                }
            }
        } else {
            if (end <= start) {
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than start (" + start + ")");
            }
        }

        final char[] buffer = new char[count];
        final int gap = end - start;

        while (count-- != 0) {
            char ch;
            if (chars == null) {
                ch = (char) (random.nextInt(gap) + start);
            } else {
                ch = chars[random.nextInt(gap) + start];
            }
            if (letters && Character.isLetter(ch)
                || numbers && Character.isDigit(ch)
                || !letters && !numbers) {
                if (ch >= 56320 && ch <= 57343) {
                    if (count == 0) {
                        count++;
                    } else {
                        // low surrogate, insert high surrogate after putting it in
                        buffer[count] = ch;
                        count--;
                        buffer[count] = (char) (55296 + random.nextInt(128));
                    }
                } else if (ch >= 55296 && ch <= 56191) {
                    if (count == 0) {
                        count++;
                    } else {
                        // high surrogate, insert low surrogate before putting it in
                        buffer[count] = (char) (56320 + random.nextInt(128));
                        count--;
                        buffer[count] = ch;
                    }
                } else if (ch >= 56192 && ch <= 56319) {
                    // private high surrogate, no effing clue, so skip it
                    count++;
                } else {
                    buffer[count] = ch;
                }
            } else {
                count++;
            }
        }
        return new String(buffer);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of characters
     * specified by the string, must not be empty.
     * If null, the set of all characters is used.</p>
     *
     * @param count the length of random string to create
     * @param chars the String containing the set of characters to use,
     *              may be null, but must not be empty
     * @return the random string
     * @throws IllegalArgumentException if {@code count} &lt; 0 or the string is empty.
     */
    public static String random(final int count, final String chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, ThreadLocalRandom.current());
        }
        return random(count, chars.toCharArray());
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of characters specified.</p>
     *
     * @param count the length of random string to create
     * @param chars the character array containing the set of characters to use,
     *              may be null
     * @return the random string
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public static String random(final int count, final char... chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, ThreadLocalRandom.current());
        }
        return random(count, 0, chars.length, false, false, chars, ThreadLocalRandom.current());
    }

    /**
     * 生成不带分隔符的UUID
     *
     * @return UUID
     */
    public static String randomShortUUID() {
        UUID uuid = UUID.randomUUID();
        byte[] buf = new byte[32];
        formatUnsignedLong0(uuid.getLeastSignificantBits(), buf, 20, 12);
        formatUnsignedLong0(uuid.getLeastSignificantBits() >>> 48, buf, 16, 4);
        formatUnsignedLong0(uuid.getMostSignificantBits(), buf, 12, 4);
        formatUnsignedLong0(uuid.getMostSignificantBits() >>> 16, buf, 8, 4);
        formatUnsignedLong0(uuid.getMostSignificantBits() >>> 32, buf, 0, 8);
        return new String(buf);
    }

    private static void formatUnsignedLong0(long val, byte[] buf, int offset, int len) {
        int charPos = offset + len;
        int mask = 15;
        do {
            buf[--charPos] = (byte) digits[((int) val) & mask];
            val >>>= 4;
        } while (charPos > offset);
    }

    private static final char[] digits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };
}
