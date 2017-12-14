package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.security.Base64;
import org.spin.core.security.RSA;
import org.spin.data.core.Dict;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.security.KeyPair;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created by Arvin on 2016/8/19.
 */
public class BeanUtilsTest<E> {
    @Test
    public void wrapperMapToBean() throws Exception {
    }

    @Test
    public void testPackageUtil() {
        List<String> res = PackageUtils.getClassName("org");
        assertTrue(null != res && res.size() > 0);
    }

    @Test
    public void testRSA() throws Exception {
        KeyPair keyPair = RSA.generateKeyPair();
        String pubKey = Base64.encode(keyPair.getPublic().getEncoded());
        String prvKey = Base64.encode(keyPair.getPrivate().getEncoded());
        System.out.println("公匙：" + pubKey);
        System.out.println("私匙：" + prvKey);

        String test = "RSA加密测试字符串";
        String encry = RSA.encrypt(pubKey, test);
        System.out.println("加密后字符串：" + RSA.encrypt(pubKey, test));
        System.out.println("解密后字符串：" + RSA.decrypt(prvKey, encry));
    }


    public void testType(List<MultipartFile> files) {
    }

    @Test
    public void test() throws NoSuchMethodException {
        Method[] methods = BeanUtilsTest.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("testType")) {
                Parameter[] params = method.getParameters();
                for (Parameter param : params) {
                    if (param.getParameterizedType() instanceof ParameterizedType) {
                        ParameterizedType type = (ParameterizedType) param.getParameterizedType();
                        Class<?> paramClass = type.getActualTypeArguments().length > 0 ? (Class<?>) type.getActualTypeArguments()[0] : Object.class;
                        System.out.println(paramClass.toString());
                    }
                }
                assertTrue(true);
            }
        }

    }

    @Test
    public void testVarargs() {
        BeanUtilsTest<Double> aa = new BeanUtilsTest<>();
        ReflectionUtils.doWithMethods(aa.getClass(), method -> {
                if (method.getName().equals("aaa")) {
                    boolean ext = MethodUtils.containsGenericArg(method);
                    String[] names = MethodUtils.getMethodParamNames(method);
                    System.out.println(1);
                }
            }
        );
    }

    @Test
    public void testGetFields() {
        Field field = ReflectionUtils.findField(Dict.class, "parent.parent.id");
        assertTrue(field.getType().getName().equals(Long.class.getName()));

    }

    public <T extends CharSequence> void aaa(List<String> a,
                                             String[] b,
                                             Class<String> d,
                                             List<List<String>> f,
                                             T g,
                                             List<T> h,
                                             E i, T[] j,
                                             List<T>[] k,
                                             String... z) {
    }

    @Test
    public void testPackage() {
        List<String> ls = PackageUtils.getClassNameByJar("file:/H:/Projects/ideaWorkspace/fundamental/test/build/libs/test-1.0-SNAPSHOT.jar!/BOOT-INF/classes!/com/shipping/domain/enums", true);
        System.out.println(ls);
        ls = PackageUtils.getClassNameByJar("file:/D:/mysql/mysql-connector-java-6.0.6.jar!/com/mysql/cj/api/x/core", false);
        assertTrue(true);
    }
}
