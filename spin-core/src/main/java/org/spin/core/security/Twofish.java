package org.spin.core.security;

import javax.crypto.SecretKey;

/**
 * Twofish工具类
 * <p>Created by xuweinan on 2020/8/28.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public final class Twofish extends Symmetry {

    private Twofish(Mode mode, Padding padding) {
        super(Algorithm.TWOFISH, mode, padding);
    }


    /**
     * 使用指定的工作模式与填充方式构造Twofish加密工具
     *
     * @param mode    工作模式
     * @param padding 填充方式
     * @return Twofish加密工具实例
     */
    public static Twofish newInstance(Mode mode, Padding padding) {
        return new Twofish(mode, padding);
    }

    /**
     * 使用Twofish/ECB/PKCS5Padding构造Twofish加密工具
     *
     * @return Twofish加密工具实例
     */
    public static Twofish newInstance() {
        return new Twofish(null, null);
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(SecretKey secretKey) {
        setKey(secretKey);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(byte[] key) {
        setKey(toKey(algorithm, key));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥
     *
     * @param secretKey 密钥
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(String secretKey) {
        setKey(generateKey(algorithm, secretKey, 32));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param keyLength 密钥强度
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(String secretKey, int keyLength) {
        setKey(generateKey(algorithm, secretKey, keyLength));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param iv        初始化向量
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(SecretKey secretKey, byte[] iv) {
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
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(byte[] key, byte[] iv) {
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
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(String secretKey, byte[] iv) {
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
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withKey(String secretKey, int keyLength, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, keyLength));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定Twofish的初始化向量
     * <pre>
     *     初始化向量的长度必须&ge;16
     * </pre>
     *
     * @param iv 初始化向量
     * @return 当前Twofish加密工具实例
     */
    public synchronized Twofish withIv(byte[] iv) {
        if (setIv(iv)) {
            initCipher();
        }
        return this;
    }
}
