package org.spin.core.security;

import javax.crypto.SecretKey;

/**
 * DES工具类
 * <p>
 * DES加密算法已经不推荐使用
 * </p>
 * Created by xuweinan on 2016/8/15.
 *
 * @author xuweinan
 * @version 1.0
 */
public final class DES extends Symmetry {

    private DES(Mode mode, Padding padding) {
        super(Algorithm.DES, mode, padding);
    }

    /**
     * 使用指定的工作模式与填充方式构造DES加密工具
     *
     * @param mode    工作模式
     * @param padding 填充方式
     * @return DES加密工具实例
     */
    public static DES newInstance(Mode mode, Padding padding) {
        return new DES(mode, padding);
    }

    /**
     * 使用DES/ECB/PKCS5Padding构造DES加密工具55x9500h 9500g
     *
     * @return DES加密工具实例
     */
    public static DES newInstance() {
        return new DES(null, null);
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @return 当前DES加密工具实例
     */
    public synchronized DES withKey(SecretKey secretKey) {
        setKey(secretKey);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @return 当前DES加密工具实例
     */
    public synchronized DES withKey(byte[] key) {
        setKey(toKey(algorithm, key));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥
     *
     * @param secretKey 密钥
     * @return 当前DES加密工具实例
     */
    public synchronized DES withKey(String secretKey) {
        setKey(generateKey(algorithm, secretKey, 64));
        initCipher();
        return this;
    }


    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param iv        初始化向量
     * @return 当前DES加密工具实例
     */
    public synchronized DES withKey(SecretKey secretKey, byte[] iv) {
        setKey(secretKey);
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @param iv        初始化向量
     * @return 当前DES加密工具实例
     */
    public synchronized DES withKey(byte[] key, byte[] iv) {
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
     * @return 当前DES加密工具实例
     */
    public synchronized DES withKey(String secretKey, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, 64));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定DES的初始化向量
     * <pre>
     *     初始化向量的长度必须&ge;8
     * </pre>
     *
     * @param iv 初始化向量
     * @return 当前DES加密工具实例
     */
    public synchronized DES withIv(byte[] iv) {
        if (mode.isNeedIv()) {
            setIv(iv);
            initCipher();
        }
        return this;
    }
}
