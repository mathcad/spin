package org.spin.core.security;

import javax.crypto.SecretKey;

/**
 * Blowfish工具类
 * <p>Created by xuweinan on 2020/8/28.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public final class Blowfish extends Symmetry {

    private Blowfish(Mode mode, Padding padding) {
        super(Algorithm.BLOWFISH, mode, padding);
    }


    /**
     * 使用指定的工作模式与填充方式构造Blowfish加密工具
     *
     * @param mode    工作模式
     * @param padding 填充方式
     * @return Blowfish加密工具实例
     */
    public static Blowfish newInstance(Mode mode, Padding padding) {
        return new Blowfish(mode, padding);
    }

    /**
     * 使用Blowfish/ECB/PKCS5Padding构造Blowfish加密工具
     *
     * @return Blowfish加密工具实例
     */
    public static Blowfish newInstance() {
        return new Blowfish(null, null);
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(SecretKey secretKey) {
        setKey(secretKey);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(byte[] key) {
        setKey(toKey(algorithm, key));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥
     *
     * @param secretKey 密钥
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(String secretKey) {
        setKey(generateKey(algorithm, secretKey, 32));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param keyLength 密钥强度
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(String secretKey, int keyLength) {
        setKey(generateKey(algorithm, secretKey, keyLength));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param iv        初始化向量
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(SecretKey secretKey, byte[] iv) {
        setKey(secretKey);
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @param iv  初始化向量
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(byte[] key, byte[] iv) {
        setKey(toKey(algorithm, key));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥
     *
     * @param secretKey 密钥
     * @param iv        初始化向量
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(String secretKey, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, 32));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param keyLength 密钥强度
     * @param iv        初始化向量
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withKey(String secretKey, int keyLength, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, keyLength));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定Blowfish的初始化向量
     * <pre>
     *     初始化向量的长度必须&ge;16
     * </pre>
     *
     * @param iv 初始化向量
     * @return 当前Blowfish加密工具实例
     */
    public synchronized Blowfish withIv(byte[] iv) {
        if (setIv(iv)) {
            initCipher();
        }
        return this;
    }
}
