package org.spin.core.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/6/12.</p>
 *
 * @author xuweinan
 */
class AESTest {
    private String key = "ecb55d9d3501e902";

    @Test
    public void testAes() {
        AES aes = AES.newInstance(AES.Mode.ECB, AES.Padding.PKCS7Padding).withKey(key, AES.KeyLength.STRONG);
        String encrypt = aes.encrypt("message你1");
        System.out.println(encrypt);
        System.out.println(aes.decrypt(encrypt));

        encrypt = aes.encrypt("message你2");
        System.out.println(encrypt);
        System.out.println(aes.decrypt(encrypt));

        aes.withIv(new byte[16]);
        encrypt = aes.encrypt("message你2");
        System.out.println(encrypt);
        System.out.println(aes.decrypt(encrypt));
        assertTrue(true);
    }

}
