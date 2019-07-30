package org.spin.core.security;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;

/**
 * DES工具类
 * <p>
 * Created by xuweinan on 2016/8/15.
 *
 * @author xuweinan
 * @version 1.0
 * @deprecated DES算法已经不建议使用
 */
@Deprecated
public class DES {
    private static final String DES_ALGORITHM = "DES/ECB/PKCS5Padding";

    private DES() {
    }

    public static Key generateKey(String keySeed) {
        KeyGenerator keyGenerator;
        SecureRandom secureRandom;
        try {
            keyGenerator = KeyGenerator.getInstance("DES");
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new SpinException(ErrorCode.KEY_FAIL, e);
        }
        keyGenerator.init(56, secureRandom);
        return keyGenerator.generateKey();
    }

    public static String encrypt(Key key, String plainText) {
        try {
            return Base64.encode(encrypt(key, plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, e);
        }
    }

    public static String decrypt(Key key, String cipherText) {
        try {
            return new String(decrypt(key, Base64.decode(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SpinException(ErrorCode.DEENCRYPT_FAIL, e);
        }
    }

    public static byte[] encrypt(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(DES_ALGORITHM);
            cipher.init(1, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, e);
        }
    }

    public static byte[] decrypt(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(DES_ALGORITHM);
            cipher.init(2, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new SpinException(ErrorCode.DEENCRYPT_FAIL, e);
        }
    }
}
