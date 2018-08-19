package org.spin.core.util;


import org.junit.jupiter.api.Test;
import org.spin.core.function.SerializedLambda;
import org.spin.core.function.serializable.Function;
import org.spin.core.session.Session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

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
        java.lang.invoke.SerializedLambda serializedLambda = org.spin.core.util.LambdaUtils.resolveLambda(Session::getTimeout);
        implMethodName = serializedLambda.getImplMethodName();
        e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(BeanUtils.toFieldName(implMethodName));

        s = System.currentTimeMillis();
        serializedLambda = org.spin.core.util.LambdaUtils.resolveLambda(Session::getAttributeKeys);
        implMethodName = serializedLambda.getImplMethodName();
        e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(implMethodName);

        s = System.currentTimeMillis();
        serializedLambda = org.spin.core.util.LambdaUtils.resolveLambda(Session::getAttributeKeys);
        implMethodName = serializedLambda.getImplMethodName();
        e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(implMethodName);
    }

    public static <T> SerializedLambda resolve(Function<T, ?> func) {
        Class clazz = func.getClass();
        SerializedLambda lambda = convert(func);
        return lambda;
    }

    public static SerializedLambda convert(Function lambda) {
        byte[] bytes = SerializeUtils.serialize(lambda);
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class<?> clazz = super.resolveClass(objectStreamClass);
                return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
            }
        }) {
            Object o = objIn.readObject();
            return (SerializedLambda) o;
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }
}
