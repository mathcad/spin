package org.spin.enhance.ip;

/**
 * util class
 */
public class Util {
    /**
     * write specfield bytes to a byte array start from offset
     *
     * @param b      字节数组
     * @param offset 偏移
     * @param v      数值
     * @param bytes  字节数
     */
    public static void write(byte[] b, int offset, long v, int bytes) {
        for (int i = 0; i < bytes; i++) {
            b[offset++] = (byte) ((v >>> (8 * i)) & 0xFF);
        }
    }

    /**
     * write a int to a byte array
     *
     * @param b      字节数组
     * @param offset 偏移
     * @param v      数值
     */
    public static void writeIntLong(byte[] b, int offset, long v) {
        b[offset++] = (byte) ((v) & 0xFF);
        b[offset++] = (byte) ((v >> 8) & 0xFF);
        b[offset++] = (byte) ((v >> 16) & 0xFF);
        b[offset] = (byte) ((v >> 24) & 0xFF);
    }

    /**
     * get a int from a byte array start from the specifiled offset
     *
     * @param b      字节数组
     * @param offset 偏移
     * @return 转换后的数值
     */
    public static long getIntLong(byte[] b, int offset) {
        return (
            ((b[offset++] & 0x000000FFL)) |
                ((b[offset++] << 8) & 0x0000FF00L) |
                ((b[offset++] << 16) & 0x00FF0000L) |
                ((b[offset] << 24) & 0xFF000000L)
        );
    }

    public static int getInt3(byte[] b, int offset) {
        return (
            (b[offset++] & 0x000000FF) |
                (b[offset++] & 0x0000FF00) |
                (b[offset] & 0x00FF0000)
        );
    }

    public static int getInt2(byte[] b, int offset) {
        return (
            (b[offset++] & 0x000000FF) |
                (b[offset] & 0x0000FF00)
        );
    }

    public static int getInt1(byte[] b, int offset) {
        return (
            (b[offset] & 0x000000FF)
        );
    }

    /**
     * string ip to long ip
     *
     * @param ip 点分十加近制的ip字符串
     * @return 数值格式的ip地址
     */
    public static long ip2long(String ip) {
        String[] p = ip.split("\\.");
        if (p.length != 4) return 0;

        int p1 = ((Integer.valueOf(p[0]) << 24) & 0xFF000000);
        int p2 = ((Integer.valueOf(p[1]) << 16) & 0x00FF0000);
        int p3 = ((Integer.valueOf(p[2]) << 8) & 0x0000FF00);
        int p4 = ((Integer.valueOf(p[3])) & 0x000000FF);

        return ((p1 | p2 | p3 | p4) & 0xFFFFFFFFL);
    }

    /**
     * int to ip string
     *
     * @param ip 数值类型的ip地址
     * @return 点分十加近制的ip字符串
     */
    public static String long2ip(long ip) {
        return String.valueOf((ip >> 24) & 0xFF) + '.' +
            ((ip >> 16) & 0xFF) + '.' +
            ((ip >> 8) & 0xFF) + '.' +
            ((ip) & 0xFF);
    }

    /**
     * check the validate of the specifeld ip address
     *
     * @param ip ip地址
     * @return ip是否合法
     */
    public static boolean isIpAddress(String ip) {
        String[] p = ip.split("\\.");
        if (p.length != 4) return false;

        for (String pp : p) {
            if (pp.length() > 3) return false;
            int val = Integer.valueOf(pp);
            if (val > 255) return false;
        }

        return true;
    }
}
