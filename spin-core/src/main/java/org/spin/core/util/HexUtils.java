package org.spin.core.util;

import org.spin.core.throwable.DecoderException;
import org.spin.core.throwable.EncoderException;

import java.nio.charset.Charset;

/**
 * 16进制工具类(线程安全)
 *
 * @author xuweinan
 */
public abstract class HexUtils {

    /**
     * Used to build output as HexUtils
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as HexUtils
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private HexUtils() {
    }

    /**
     * 将字符数组表示的16进制数值转换为等值字节数组。返回的字节数组长度应该是参数数组长度的一半(两个16进制字符转换出一个字节)
     * 如果参数数组的长度为奇数，则抛出异常
     *
     * @param data 表示16进制数据的字符数组
     * @return 由参数数组转换得到字节数组
     * @throws DecoderException 如果字符数组含有非法字符或长度为奇数，抛出异常
     */
    public static byte[] decodeHex(final char[] data) throws DecoderException {

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new DecoderException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将输入的字节数组转换为通过16进制字符按顺序等值表示的字符数组。返回字符数组长度会是输入数组的两倍（一个byte需要两个16进制字符表示）
     *
     * @param data 待转换的字节数组
     * @return 16进制字符的字符数组(默认小写形式表示)
     */
    public static char[] encodeHex(final byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将输入的字节数组转换为通过16进制字符按顺序等值表示的字符数组。返回字符数组长度会是输入数组的两倍（一个byte需要两个16进制字符表示）
     *
     * @param data        待转换的字节数组
     * @param toLowerCase <code>true</code> 结果转换为小写形式, <code>false</code> 大写形式
     * @return 16进制字符的字符数组
     * @since 1.4
     */
    public static char[] encodeHex(final byte[] data, final boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将输入的字节数组转换为通过16进制字符按顺序等值表示的字符数组。返回字符数组长度会是输入数组的两倍（一个byte需要两个16进制字符表示）
     *
     * @param data     待转换的字节数组
     * @param toDigits 数字字符对应表
     * @return 16进制字符的字符数组
     * @since 1.4
     */
    public static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 将输入的字节数组转换为通过16进制字符按顺序等值表示的字符串。返回字符串长度会是输入数组长度的两倍（一个byte需要两个16进制字符表示）
     *
     * @param data 待转换的字节数组
     * @return 16进制字符组成的字符串(小写形式)
     * @since 1.4
     */
    public static String encodeHexStringL(final byte[] data) {
        return new String(encodeHex(data));
    }

    /**
     * 将输入的字节数组转换为通过16进制字符按顺序等值表示的字符串。返回字符串长度会是输入数组长度的两倍（一个byte需要两个16进制字符表示）
     *
     * @param data 待转换的字节数组
     * @return 16进制字符组成的字符串(大写形式)
     * @since 1.4
     */
    public static String encodeHexStringU(final byte[] data) {
        return new String(encodeHex(data, false));
    }

    /**
     * 将输入的字节数组转换为通过16进制字符按顺序等值表示的字符串。返回字符串长度会是输入数组长度的两倍（一个byte需要两个16进制字符表示）
     *
     * @param data        待转换的字节数组
     * @param toLowerCase <code>true</code> 结果转换为小写形式, <code>false</code> 大写形式
     * @return 16进制字符组成的字符串
     * @since 1.4
     */
    public static String encodeHexString(final byte[] data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase));
    }

    /**
     * 将16进制字符，转换为int
     *
     * @param ch    待转换字符
     * @param index 字符在源集合中的位置
     * @return int数值
     * @throws DecoderException 如果是非法的16进制字符，抛出该异常
     */
    public static int toDigit(final char ch, final int index) throws DecoderException {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new DecoderException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * 将字节数组按指定编码解释为字符串后，将该字符串表示的16进制数值转换为等值字节数组。
     * 如果参数数组的长度为奇数，则抛出异常
     *
     * @param array   表示16进制数据的字符串指定编码下的字节数组
     * @param charset 字符集编码
     * @return 由参数数组转换得到字节数组
     * @throws DecoderException 如果字符数组含有非法字符或长度为奇数，抛出异常
     * @see #decodeHex(char[])
     */
    public static byte[] decode(final byte[] array, Charset charset) throws DecoderException {
        return decodeHex(new String(array, charset).toCharArray());
    }

    /**
     * Converts a String or an array of character bytes representing hexadecimal values into an array of bytes of those
     * same values. The returned array will be half the length of the passed String or array, as it takes two characters
     * to represent any given byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * @param object A String or, an array of character bytes containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied byte array (representing characters).
     * @throws DecoderException Thrown if an odd number of characters is supplied to this function or the object is not a String or
     *                          char[]
     * @see #decodeHex(char[])
     */
    public static Object decode(final Object object) throws DecoderException {
        try {
            final char[] charArray = object instanceof String ? ((String) object).toCharArray() : (char[]) object;
            return decodeHex(charArray);
        } catch (final ClassCastException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    /**
     * Converts an array of bytes into an array of bytes for the characters representing the hexadecimal values of each
     * byte in order. The returned array will be double the length of the passed array, as it takes two characters to
     * represent any given byte.
     *
     * @param array   a byte[] to convert to HexUtils characters
     * @param charset charset
     * @return A byte[] containing the bytes of the hexadecimal characters
     * @see #encodeHex(byte[])
     * @since 1.7 No longer throws IllegalStateException if the charsetName is invalid.
     */
    public static byte[] encode(final byte[] array, Charset charset) {
        return encodeHexStringL(array).getBytes(charset);
    }

    /**
     * Converts a String or an array of bytes into an array of characters representing the hexadecimal values of each
     * byte in order. The returned array will be double the length of the passed String or array, as it takes two
     * characters to represent any given byte.
     *
     * @param object  a String, or byte[] to convert to HexUtils characters
     * @param charset charset
     * @return A char[] containing hexadecimal characters
     * @throws EncoderException Thrown if the given object is not a String or byte[]
     * @see #encodeHex(byte[])
     */
    public static Object encode(final Object object, Charset charset) throws EncoderException {
        try {
            final byte[] byteArray = object instanceof String ?
                ((String) object).getBytes(charset) : (byte[]) object;
            return encodeHex(byteArray);
        } catch (final ClassCastException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }
}
