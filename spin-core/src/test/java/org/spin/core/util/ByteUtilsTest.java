package org.spin.core.util;


import org.junit.jupiter.api.Test;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/8/18.</p>
 *
 * @author xuweinan
 */
class ByteUtilsTest {

    @Test
    public void testEndian() {
        byte[] r = new byte[8];
//        (int)1434418902, r, 0
        ByteUtils.littleEndian().writeDouble(1434418.902D, r, 0);
        System.out.println(HexUtils.encodeHexStringL(r));
        double a = ByteUtils.littleEndian().readDouble(r, 0);
        System.out.println(a);

    }
}
