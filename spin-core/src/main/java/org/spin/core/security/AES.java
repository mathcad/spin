package org.spin.core.security;

import org.spin.core.Assert;
import org.spin.core.trait.IntEvaluatable;

import javax.crypto.SecretKey;

/**
 * AES工具类
 * <p>使用强度超过 {@link KeyLength#WEAK} 的密钥需要JCE无限制权限策略文件(jdk 9以上不需要)</p>
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public final class AES extends Symmetry {

    /**
     * 密钥强度，WEAK为128bit，MEDIAM为192bit，STRONG为256bit
     * <p>使用强度超过 {@link org.spin.core.security.AES.KeyLength#WEAK} 的密钥需要JCE无限制权限策略文件(jdk 9以上不需要)</p>
     * <p>Created by xuweinan on 2016/8/15.</p>
     *
     * @author xuweinan
     * @version 1.1
     */
    public enum KeyLength implements IntEvaluatable {
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
        private final int value;

        KeyLength(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return this.value;
        }
    }

    private AES(Mode mode, Padding padding) {
        super(Algorithm.AES, mode, padding);
    }


    /**
     * 使用指定的工作模式与填充方式构造AES加密工具
     *
     * @param mode    工作模式
     * @param padding 填充方式
     * @return AES加密工具实例
     */
    public static AES newInstance(Mode mode, Padding padding) {
        return new AES(mode, padding);
    }

    /**
     * 使用AES/ECB/PKCS5Padding构造AES加密工具
     *
     * @return AES加密工具实例
     */
    public static AES newInstance() {
        return new AES(null, null);
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(SecretKey secretKey) {
        setKey(secretKey);
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param key 密钥
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(byte[] key) {
        setKey(toKey(algorithm, key));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥
     *
     * @param secretKey 密钥
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(String secretKey) {
        setKey(generateKey(algorithm, secretKey, KeyLength.WEAK.intValue()));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param keyLength 密钥强度
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(String secretKey, KeyLength keyLength) {
        setKey(generateKey(algorithm, secretKey, Assert.notNull(keyLength, "密钥强度不能为空").intValue()));
        initCipher();
        return this;
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     * @param iv        初始化向量
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(SecretKey secretKey, byte[] iv) {
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
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(byte[] key, byte[] iv) {
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
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(String secretKey, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, 112));
        setIv(iv);
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
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(String secretKey, KeyLength keyLength, byte[] iv) {
        setKey(generateKey(algorithm, secretKey, Assert.notNull(keyLength, "密钥强度不能为空").intValue()));
        setIv(iv);
        initCipher();
        return this;
    }

    /**
     * 指定AES的初始化向量
     * <pre>
     *     初始化向量的长度必须&ge;16
     * </pre>
     *
     * @param iv 初始化向量
     * @return 当前AES加密工具实例
     */
    public synchronized AES withIv(byte[] iv) {
        if (setIv(iv)) {
            initCipher();
        }
        return this;
    }
}
