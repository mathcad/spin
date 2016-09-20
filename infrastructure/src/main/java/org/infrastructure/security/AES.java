package org.infrastructure.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.throwable.SimplifiedException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;

/**
 * AES工具类
 * <p>使用强度超过{@link KeyLength#WEAK}的密钥需要JCE无限制权限策略文件
 */
public class AES {
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String BOUNCYCASTLE = "BC";

    /**
     * 密钥强度，WEAK为128bit，MEDIAM为192bit，STRONG为256bit
     */
    public enum KeyLength {
        WEAK(128),
        MEDIAM(192),
        STRONG(256);
        private int _value;

        KeyLength(int value) {
            this._value = value;
        }

        public int getValue() {
            return this._value;
        }
    }

    /**
     * 生成密钥，默认使用最低强度
     */
    public static SecretKey generateKey(String keySeed) {
        return generateKey(keySeed, KeyLength.WEAK);
    }

    /**
     * 生成指定强度的密钥
     */
    public static SecretKey generateKey(String keySeed, KeyLength keySize) {
        KeyGenerator kg;
        SecureRandom secureRandom;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.KEY_FAIL, e);
        }
        kg.init(keySize.getValue(), secureRandom);
        return kg.generateKey();
    }

    public static Key toKey(byte[] key) throws Exception {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    public static String encrypt(SecretKey key, String data) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        try {
            return encrypt(key, data.getBytes("UTF-8"), null);
        } catch (UnsupportedEncodingException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.ENCRYPT_FAIL, e);
        }
    }

    public static String encrypt(SecretKey key, byte[] data, String padType) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = null;
        try {
            if (BOUNCYCASTLE.equals(padType)) {
                Security.addProvider(new BouncyCastleProvider());
                cipher = Cipher.getInstance(CIPHER_ALGORITHM, BOUNCYCASTLE);
            } else
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.ENCRYPT_FAIL, e);
        }
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encode(cipher.doFinal(data));
    }

    public static String decrypt(SecretKey key, String data) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        return decrypt(key, data, null, "UTF-8");
    }


    public static String decrypt(SecretKey key, String data, String padType, String charset) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = null;
        try {
            if (BOUNCYCASTLE.equals(padType)) {
                Security.addProvider(new BouncyCastleProvider());
                cipher = Cipher.getInstance(CIPHER_ALGORITHM, BOUNCYCASTLE);
            } else
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorAndExceptionCode.DEENCRYPT_FAIL, e);
        }
        cipher.init(Cipher.DECRYPT_MODE, key);
        try {
            return new String(cipher.doFinal(Base64.decode(data)), charset);
        } catch (UnsupportedEncodingException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.ENCRYPT_FAIL, e);
        }
    }
}

