package org.spin.core.util;

import org.spin.core.io.Endian;

/**
 * 操纵字节与字节数组的工具类
 *
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public final class ByteUtils extends Util {

    private static final ByteConvertor BIG_CONVERTOR = new ByteConvertor(Endian.BIG);
    private static final ByteConvertor LITTLE_CONVERTOR = new ByteConvertor(Endian.LITTLE);

    private ByteUtils() {
    }

    /**
     * Compare two byte arrays (perform null checks beforehand).
     *
     * @param left  the first byte array
     * @param right the second byte array
     * @return the result of the comparison
     */
    public static boolean equals(byte[] left, byte[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }

        if (left.length != right.length) {
            return false;
        }
        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            result &= left[i] == right[i];
        }
        return result;
    }

    /**
     * Compare two two-dimensional byte arrays. No null checks are performed.
     *
     * @param left  the first byte array
     * @param right the second byte array
     * @return the result of the comparison
     */
    public static boolean equals(byte[][] left, byte[][] right) {
        if (left.length != right.length) {
            return false;
        }

        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            result &= ByteUtils.equals(left[i], right[i]);
        }

        return result;
    }

    /**
     * Compare two three-dimensional byte arrays. No null checks are performed.
     *
     * @param left  the first byte array
     * @param right the second byte array
     * @return the result of the comparison
     */
    public static boolean equals(byte[][][] left, byte[][][] right) {
        if (left.length != right.length) {
            return false;
        }

        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            if (left[i].length != right[i].length) {
                return false;
            }
            for (int j = left[i].length - 1; j >= 0; j--) {
                result &= ByteUtils.equals(left[i][j], right[i][j]);
            }
        }

        return result;
    }

    /**
     * Computes a hashcode based on the contents of a one-dimensional byte array
     * rather than its identity.
     *
     * @param array the array to compute the hashcode of
     * @return the hashcode
     */
    public static int deepHashCode(byte[] array) {
        int result = 1;
        for (int i = 0; i < array.length; i++) {
            result = 31 * result + array[i];
        }
        return result;
    }

    /**
     * Computes a hashcode based on the contents of a two-dimensional byte array
     * rather than its identity.
     *
     * @param array the array to compute the hashcode of
     * @return the hashcode
     */
    public static int deepHashCode(byte[][] array) {
        int result = 1;
        for (int i = 0; i < array.length; i++) {
            result = 31 * result + deepHashCode(array[i]);
        }
        return result;
    }

    /**
     * Computes a hashcode based on the contents of a three-dimensional byte
     * array rather than its identity.
     *
     * @param array the array to compute the hashcode of
     * @return the hashcode
     */
    public static int deepHashCode(byte[][][] array) {
        int result = 1;
        for (int i = 0; i < array.length; i++) {
            result = 31 * result + deepHashCode(array[i]);
        }
        return result;
    }


    /**
     * Return a clone of the given byte array (performs null check beforehand).
     *
     * @param array the array to clone
     * @return the clone of the given array, or <code>null</code> if the array is
     * <code>null</code>
     */
    public static byte[] clone(byte[] array) {
        if (array == null) {
            return null;
        }
        byte[] result = new byte[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    /**
     * Convert a byte array to the corresponding bit string.
     *
     * @param input the byte array to be converted
     * @return the corresponding bit string
     */
    public static String toBinaryString(byte[] input) {
        StringBuilder result = new StringBuilder();
        int i;
        for (i = 0; i < input.length; i++) {
            int e = input[i];
            for (int ii = 0; ii < 8; ii++) {
                int b = (e >>> ii) & 1;
                result.append(b);
            }
            if (i != input.length - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
     * Compute the bitwise XOR of two arrays of bytes. The arrays have to be of
     * same length. No length checking is performed.
     *
     * @param x1 the first array
     * @param x2 the second array
     * @return x1 XOR x2
     */
    public static byte[] xor(byte[] x1, byte[] x2) {
        byte[] out = new byte[x1.length];

        for (int i = x1.length - 1; i >= 0; i--) {
            out[i] = (byte) (x1[i] ^ x2[i]);
        }
        return out;
    }

    /**
     * Concatenate two byte arrays. No null checks are performed.
     *
     * @param x1 the first array
     * @param x2 the second array
     * @return (x2 | | x1) (little-endian order, i.e. x1 is at lower memory
     * addresses)
     */
    public static byte[] concatenate(byte[] x1, byte[] x2) {
        byte[] result = new byte[x1.length + x2.length];

        System.arraycopy(x1, 0, result, 0, x1.length);
        System.arraycopy(x2, 0, result, x1.length, x2.length);

        return result;
    }

    /**
     * Convert a 2-dimensional byte array into a 1-dimensional byte array by
     * concatenating all entries.
     *
     * @param array a 2-dimensional byte array
     * @return the concatenated input array
     */
    public static byte[] concatenate(byte[][] array) {
        int rowLength = array[0].length;
        byte[] result = new byte[array.length * rowLength];
        int index = 0;
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, result, index, rowLength);
            index += rowLength;
        }
        return result;
    }

    /**
     * Split a byte array <code>input</code> into two arrays at <code>index</code>,
     * i.e. the first array will have the lower <code>index</code> bytes, the
     * second one the higher <code>input.length - index</code> bytes.
     *
     * @param input the byte array to be split
     * @param index the index where the byte array is split
     * @return the separated input array as an array of two byte arrays
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is out of bounds
     */
    public static byte[][] split(byte[] input, int index)
        throws ArrayIndexOutOfBoundsException {
        if (index > input.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        byte[][] result = new byte[2][];
        result[0] = new byte[index];
        result[1] = new byte[input.length - index];
        System.arraycopy(input, 0, result[0], 0, index);
        System.arraycopy(input, index, result[1], 0, input.length - index);
        return result;
    }

    /**
     * Generate a subarray of a given byte array.
     *
     * @param input the input byte array
     * @param start the start index
     * @param end   the end index
     * @return a subarray of <code>input</code>, ranging from <code>start</code>
     * (inclusively) to <code>end</code> (exclusively)
     */
    public static byte[] subArray(byte[] input, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, end - start);
        return result;
    }

    /**
     * Generate a subarray of a given byte array.
     *
     * @param input the input byte array
     * @param start the start index
     * @return a subarray of <code>input</code>, ranging from <code>start</code> to
     * the end of the array
     */
    public static byte[] subArray(byte[] input, int start) {
        return subArray(input, start, input.length);
    }

    /**
     * Rewrite a byte array as a char array
     *
     * @param input -
     *              the byte array
     * @return char array
     */
    public static char[] toCharArray(byte[] input) {
        char[] result = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (char) input[i];
        }
        return result;
    }


    public static ByteConvertor bigEndian() {
        return BIG_CONVERTOR;
    }

    public static ByteConvertor littleEndian() {
        return LITTLE_CONVERTOR;
    }

    public static class ByteConvertor {

        private Endian endian;

        public ByteConvertor(Endian endian) {
            this.endian = endian;
        }

        public void writeByte(byte value, byte[] dest, int pos) {
            dest[pos] = value;
        }


        public byte readByte(byte[] source, int pos) {
            return source[pos];
        }

        public void writeShort(short value, byte[] dest, int pos) {
            if (endian.equals(Endian.BIG)) {
                dest[pos] = (byte) (value >>> 8);
                dest[pos + 1] = (byte) value;
            } else {
                dest[pos + 1] = (byte) (value >>> 8);
                dest[pos] = (byte) value;
            }
        }

        public short readShort(byte[] source, int pos) {
            if (endian.equals(Endian.BIG)) {
                return (short) ((source[pos] << 8) | (source[pos + 1] & 0x00ff));
            } else {
                return (short) ((source[pos + 1] << 8) | (source[pos] & 0x00ff));
            }
        }

        public void writeChar(char value, byte[] dest, int pos) {
            writeShort((short) value, dest, pos);
        }

        public char readChar(byte[] source, int pos) {
            return (char) readShort(source, pos);
        }

        public void writeInt(int value, byte[] dest, int pos) {
            if (endian.equals(Endian.BIG)) {
                dest[pos] = (byte) (value >>> 24);
                dest[pos + 1] = (byte) (value >>> 16);
                dest[pos + 2] = (byte) (value >>> 8);
                dest[pos + 3] = (byte) value;
            } else {
                dest[pos + 3] = (byte) (value >>> 24);
                dest[pos + 2] = (byte) (value >>> 16);
                dest[pos + 1] = (byte) (value >>> 8);
                dest[pos] = (byte) value;
            }
        }

        public int readInt(byte[] source, int pos) {
            if (endian.equals(Endian.BIG)) {
                return (source[pos] & 0xff) << 24 |
                    (source[pos + 1] & 0xff) << 16 |
                    (source[pos + 2] & 0xff) << 8 |
                    source[pos + 3] & 0xff;
            } else {
                return (source[pos + 3] & 0xff) << 24 |
                    (source[pos + 2] & 0xff) << 16 |
                    (source[pos + 1] & 0xff) << 8 |
                    source[pos] & 0xff;
            }
        }

        public void writeBoolean(boolean value, byte[] dest, int pos) {
            writeByte((byte) (value ? 1 : 0), dest, pos);
        }

        public boolean readBoolean(byte[] source, int pos) {
            return readByte(source, pos) != 0;
        }

        public void writeLong(long value, byte[] dest, int pos) {
            if (endian.equals(Endian.BIG)) {
                dest[pos] = (byte) (value >>> 56);
                dest[pos + 1] = (byte) (value >>> 48);
                dest[pos + 2] = (byte) (value >>> 40);
                dest[pos + 3] = (byte) (value >>> 32);
                dest[pos + 4] = (byte) (value >>> 24);
                dest[pos + 5] = (byte) (value >>> 16);
                dest[pos + 6] = (byte) (value >>> 8);
                dest[pos + 7] = (byte) value;
            } else {
                dest[pos + 7] = (byte) (value >>> 56);
                dest[pos + 6] = (byte) (value >>> 48);
                dest[pos + 5] = (byte) (value >>> 40);
                dest[pos + 4] = (byte) (value >>> 32);
                dest[pos + 3] = (byte) (value >>> 24);
                dest[pos + 2] = (byte) (value >>> 16);
                dest[pos + 1] = (byte) (value >>> 8);
                dest[pos] = (byte) value;
            }
        }

        public long readLong(byte[] source, int pos) {
            if (endian.equals(Endian.BIG)) {
                return ((long) source[pos] & 0xff) << 56 |
                    ((long) source[pos + 1] & 0xff) << 48 |
                    ((long) source[pos + 2] & 0xff) << 40 |
                    ((long) source[pos + 3] & 0xff) << 32 |
                    ((long) source[pos + 4] & 0xff) << 24 |
                    ((long) source[pos + 5] & 0xff) << 16 |
                    ((long) source[pos + 6] & 0xff) << 8 |
                    (long) source[pos + 7] & 0xff;
            } else {
                return ((long) source[pos + 7] & 0xff) << 56 |
                    ((long) source[pos + 6] & 0xff) << 48 |
                    ((long) source[pos + 5] & 0xff) << 40 |
                    ((long) source[pos + 4] & 0xff) << 32 |
                    ((long) source[pos + 3] & 0xff) << 24 |
                    ((long) source[pos + 2] & 0xff) << 16 |
                    ((long) source[pos + 1] & 0xff) << 8 |
                    (long) source[pos] & 0xff;
            }
        }

        public void writeFloat(float value, byte[] dest, int pos) {
            writeInt(Float.floatToRawIntBits(value), dest, pos);
        }

        public float readFloat(byte[] source, int pos) {
            return Float.intBitsToFloat(readInt(source, pos));
        }

        public void writeDouble(double value, byte[] dest, int pos) {
            writeLong(Double.doubleToRawLongBits(value), dest, pos);
        }

        public double readDouble(byte[] source, int pos) {
            return Double.longBitsToDouble(readLong(source, pos));
        }
    }

}
