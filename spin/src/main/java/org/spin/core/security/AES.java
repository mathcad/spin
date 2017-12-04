package org.spin.core.security;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

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

/**
 * AES工具类
 * <p>使用强度超过 {@link KeyLength#WEAK} 的密钥需要JCE无限制权限策略文件</p>
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class AES {
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * 密钥强度，WEAK为128bit，MEDIAM为192bit，STRONG为256bit
     */
    public enum KeyLength {
        /**
         * 最低强度(128bit)
         */
        WEAK(128),

        /**
         * 中等强度(192bit)
         */
        MEDIAM(192),

        /**
         * 最高强度(256bit)
         */
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
     *
     * @param keySeed 密钥种子
     * @return 密钥
     */
    public static SecretKey generateKey(String keySeed) {
        return generateKey(keySeed, KeyLength.WEAK);
    }

    /**
     * 生成指定强度的密钥
     *
     * @param keySeed 密钥种子
     * @param keySize 密钥长度
     * @return 密钥
     */
    public static SecretKey generateKey(String keySeed, KeyLength keySize) {
        KeyGenerator kg;
        SecureRandom secureRandom;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.KEY_FAIL, e);
        }
        kg.init(keySize.getValue(), secureRandom);
        return kg.generateKey();
    }

    /**
     * 将字节数组转换为密钥
     *
     * @param key 密钥字节数据
     * @return 密钥
     */
    public static Key toKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
     * 使用指定的Key生成最低强度的密钥进行加密
     *
     * @param key  密钥种子
     * @param data 待加密数据
     * @return 密文
     */
    public static String encrypt(String key, String data) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        return encrypt(generateKey(key), data);
    }

    /**
     * 使用指定的密钥进行加密
     *
     * @param key  密钥
     * @param data 待加密数据
     * @return 密文
     */
    public static String encrypt(SecretKey key, String data) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        try {
            return encrypt(key, data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, e);
        }
    }

    /**
     * 使用指定的Key生成最低强度的密钥进行加密
     *
     * @param key  密钥种子
     * @param data 待加密数据
     * @return 密文
     */
    public static String encrypt(SecretKey key, byte[] data) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, e);
        }
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encode(cipher.doFinal(data));
    }

    /**
     * 使用指定的Key生成最低强度的密钥进行解密
     *
     * @param key  密钥种子
     * @param data 待解密数据
     * @return 明文
     */
    public static String decrypt(String key, String data) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        return decrypt(generateKey(key), data, "UTF-8");
    }

    /**
     * 使用指定的密钥进行解密
     *
     * @param key  密钥
     * @param data 待解密数据
     * @return 明文
     */
    public static String decrypt(SecretKey key, String data) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        return decrypt(key, data, "UTF-8");
    }

    /**
     * 使用指定的密钥与字符集进行解密
     *
     * @param key     密钥
     * @param data    待解密数据
     * @param charset 明文字符集
     * @return 明文
     */
    public static String decrypt(SecretKey key, String data, String charset) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.DEENCRYPT_FAIL, e);
        }
        cipher.init(Cipher.DECRYPT_MODE, key);
        try {
            return new String(cipher.doFinal(Base64.decode(data)), charset);
        } catch (UnsupportedEncodingException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, e);
        }
    }
}
