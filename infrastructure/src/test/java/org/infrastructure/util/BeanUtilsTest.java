package org.infrastructure.util;

import org.infrastructure.security.Base64;
import org.infrastructure.security.RSA;
import org.junit.Test;

import java.security.KeyPair;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arvin on 2016/8/19.
 */
public class BeanUtilsTest {
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
}