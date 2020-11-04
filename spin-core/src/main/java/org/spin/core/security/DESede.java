package org.spin.core.security;

import javax.crypto.SecretKey;

/**
 * 3DES工具类
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public final class DESede extends Symmetry {

    private DESede(Mode mode, Padding padding) {
        super(Algorithm.DESEDE, mode, padding);
    }


    /**
     * 使用指定的工作模式与填充方式构造DESEDE加密工具
     *
     * @param mode    工作模式
     * @param padding 填充方式
     * @return DESEDE加密工具实例
     */
    public static DESede newInstance(Mode mode, Padding padding) {
        return new DESede(mode, padding);
    }

    /**
     * 使用DESEDE/ECB/PKCS5Padding构造DESEDE加密工具
     *
     * @return DESEDE加密工具实例
     */
    public static DESede newInstance() {
        return new DESede(null, null);
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withKey(SecretKey secretKey) {
        setKey(secretKey);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withKey(byte[] key) {
        setKey(toKey(algorithm, key));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥
     *
     * @param secretKey 密钥
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withKey(String secretKey) {
        setKey(generateKey(algorithm, secretKey, 192));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param iv        初始化向量
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withKey(SecretKey secretKey, byte[] iv) {
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
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withKey(byte[] key, byte[] iv) {
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
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withKey(String secretKey, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, 192));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定DESEDE的初始化向量
     * <pre>
     *     初始化向量的长度必须&ge;16
     * </pre>
     *
     * @param iv 初始化向量
     * @return 当前DESEDE加密工具实例
     */
    public synchronized DESede withIv(byte[] iv) {
        if (mode.isNeedIv()) {
            setIv(iv);
            initCipher();
        }
        return this;
    }
}
