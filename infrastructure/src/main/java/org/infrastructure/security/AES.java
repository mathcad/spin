package org.infrastructure.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * AES工具类
 * <p>需要JCE无限制权限策略文件
 */
public class AES {
    public static final String KEY_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final String BOUNCYCASTLE = "BC";

    /**
     * 生成密钥
     */
    public static String initkey() throws NoSuchAlgorithmException {
        return initkey(128);
    }

    /**
     * 生成密钥
     */
    public static String initkey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(keySize);
        SecretKey secretKey = kg.generateKey();
        return Base64.encode(secretKey.getEncoded());
    }

    /**
     * 转换密钥
     */
    public static Key toKey(byte[] key) throws Exception {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    public static String encrypt(String data, String key) throws Exception {
        return encrypt(data, key, null, "UTF-8");
    }

    public static String encrypt(String data, String key, String padType, String charset) throws Exception {
        Key k = toKey(Base64.decode(key));
        Cipher cipher = null;
        if (BOUNCYCASTLE.equals(padType)) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher = Cipher.getInstance(CIPHER_ALGORITHM, BOUNCYCASTLE);
        } else {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        }
        cipher.init(Cipher.ENCRYPT_MODE, k);
        return Base64.encode(cipher.doFinal(data.getBytes(charset)));
    }

    public static String decrypt(String data, String key) throws Exception {
        return decrypt(data, key, null, "UTF-8");
    }

    public static String decrypt(String data, String key, String charset) throws Exception {
        return decrypt(data, key, null, charset);
    }

    public static String decrypt(String data, String key, String padType, String charset) throws Exception {
        Key k = toKey(Base64.decode(key));
        Cipher cipher = null;
        if (BOUNCYCASTLE.equals(padType)) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher = Cipher.getInstance(CIPHER_ALGORITHM, BOUNCYCASTLE);
        } else {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        }
        cipher.init(Cipher.DECRYPT_MODE, k); // 初始化Cipher对象，设置为解密模式
        return new String(cipher.doFinal(Base64.decode(data)), charset); // 执行解密操作
    }

    public static void main(String[] args) {
        try {
            System.out.println(">>>>>>>>>>>>>encrypt>>>>>" + encrypt("8rmuhetm4we3rrqzse2sepsx38moldrx", "0NlyDhKu1HKCnZw5mSzK0Q=="));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

