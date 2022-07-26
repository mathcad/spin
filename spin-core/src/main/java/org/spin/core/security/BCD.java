package org.spin.core.security;

public class BCD {

    private BCD() {
    }

    /**
     * ASCII码转BCD码
     *
     * @param content 内容
     * @return BCD码字节数组
     */
    public static byte[] decode(String content) {

        byte[] ascii = content.getBytes();
        int ascLen = ascii.length;
        byte[] bcd = new byte[ascLen / 2];
        int j = 0;

        for (int i = 0; i < (ascLen + 1) / 2; i++) {
            bcd[i] = ascToBcd(ascii[j++]);
            bcd[i] = (byte) (((j >= ascLen) ? 0x00 : ascToBcd(ascii[j++])) + (bcd[i] << 4));
        }

        return bcd;
    }

    /**
     * BCD转字符串
     *
     * @param bytes BCD码字节数组
     * @return 字符串
     */
    public static String encode(byte[] bytes) {

        char[] temp = new char[bytes.length * 2];
        char val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }

        return new String(temp);
    }

    private static byte ascToBcd(byte asc) {

        byte bcd;

        if ((asc >= '0') && (asc <= '9'))
            bcd = (byte) (asc - '0');
        else if ((asc >= 'A') && (asc <= 'F'))
            bcd = (byte) (asc - 'A' + 10);
        else if ((asc >= 'a') && (asc <= 'f'))
            bcd = (byte) (asc - 'a' + 10);
        else
            bcd = (byte) (asc - 48);

        return bcd;
    }
}
