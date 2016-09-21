package org.infrastructure.security;

import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.throwable.SimplifiedException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;

/**
 * DES工具类
 * <p>
 * Created by xuweinan on 2016/8/15.
 *
 * @author xuweinan
 * @version 1.0
 */
public class DES {
    public static Key generateKey(String keySeed) {
        KeyGenerator keyGenerator;
        SecureRandom secureRandom;
        try {
            keyGenerator = KeyGenerator.getInstance("DES");
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.KEY_FAIL, e);
        }
        keyGenerator.init(56, secureRandom);
        return keyGenerator.generateKey();
    }

    public static String encrypt(Key key, String plainText) {
        try {
            return Base64.encode(encrypt(key, plainText.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.ENCRYPT_FAIL, e);
        }
    }

    public static String decrypt(Key key, String cipherText) {
        try {
            return new String(decrypt(key, Base64.decode(cipherText)), "UTF-8");
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.DEENCRYPT_FAIL, e);
        }
    }

    public static byte[] encrypt(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(1, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.ENCRYPT_FAIL, e);
        }
    }

    public static byte[] decrypt(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(2, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.DEENCRYPT_FAIL, e);
        }
    }
}
