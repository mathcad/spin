package org.spin.core.util;


import org.junit.jupiter.api.Test;
import org.spin.core.function.SerializedLambda;
import org.spin.core.session.Session;

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

    @Test
    public void testLambda() {
        long s = System.currentTimeMillis();
        SerializedLambda resolve = SerializedLambda.convert(Session::getAttributeKeys);
        String implMethodName = resolve.getImplMethodName();
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(implMethodName);

        s = System.currentTimeMillis();
        java.lang.invoke.SerializedLambda serializedLambda = LambdaUtils.resolveLambda(Session::getTimeout);
        implMethodName = serializedLambda.getImplMethodName();
        e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(BeanUtils.toFieldName(implMethodName));

        s = System.currentTimeMillis();
        serializedLambda = LambdaUtils.resolveLambda(Session::getAttributeKeys);
        implMethodName = serializedLambda.getImplMethodName();
        e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(implMethodName);

        s = System.currentTimeMillis();
        serializedLambda = LambdaUtils.resolveLambda(Session::getAttributeKeys);
        implMethodName = serializedLambda.getImplMethodName();
        e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(implMethodName);
    }
}
