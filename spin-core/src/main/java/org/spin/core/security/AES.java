package org.spin.core.security;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;
import org.spin.core.trait.Evaluatable;
import org.spin.core.trait.IntEvaluatable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Random;

/**
 * AES工具类
 * <p>使用强度超过 {@link KeyLength#WEAK} 的密钥需要JCE无限制权限策略文件(jdk 9以上不需要)</p>
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public class AES extends ProviderDetector {
    private static final String ALGORITHM = "AES";

    private Mode mode = Mode.ECB;
    private Padding padding = Padding.PKCS5Padding;
    private byte[] iv;
    private SecretKey secretKey;
    private KeyLength keyLength;

    private Cipher enCipher;
    private Cipher deCipher;

    /**
     * 密钥强度，WEAK为128bit，MEDIAM为192bit，STRONG为256bit
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
        private int value;

        KeyLength(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return this.value;
        }
    }

    /**
     * AES的工作模式，未引用其他Security Provider的情况下只能使用ECB模式(默认ECB)
     */
    public enum Mode implements Evaluatable<String> {

        /**
         * 电子密码本模式：Electronic codebook
         */
        ECB("ECB", false),

        /**
         * 密码分组链接：Cipher-block chaining
         */
        CBC("CBC", true),

        /**
         * 填充密码块链接：Propagating Cipher-block chaining
         */
        PCBC("PCBC", true),

        /**
         * 密文反馈:Cipher feedback
         */
        CFB("CFB", true),

        /**
         * 输出反馈：Output feedback
         */
        OFB("OFB", true),

        /**
         * 计数器模式：Counter
         */
        CTR("CTR", true),

        /**
         * Galois/Counter Mode
         */
        GCM("GCM", true);

        private String value;
        private boolean needIv;

        Mode(String value, boolean needIv) {
            this.value = value;
            this.needIv = needIv;
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }

    /**
     * AES的填充方式(默认PKCS5Padding)
     */
    public enum Padding implements Evaluatable<String> {

        /**
         * NoPadding
         */
        NoPadding("NoPadding"),

        /**
         * ISO10126Padding
         */
        ISO10126Padding("ISO10126Padding"),

        /**
         * PKCS5Padding
         */
        PKCS5Padding("PKCS5Padding"),

        /**
         * PKCS7Padding
         */
        PKCS7Padding("PKCS7Padding");

        private String value;

        Padding(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }

    private AES(Mode mode, Padding padding) {
        if (null != mode) {
            this.mode = mode;
        }
        if (null != padding) {
            this.padding = padding;
        }
        String cipherAlgorithm = ALGORITHM + "/" + this.mode.value + "/" + this.padding.value;
        if (this.mode.needIv) {
            iv = new byte[16];
            new Random().nextBytes(iv);
        }
        try {
            enCipher = Cipher.getInstance(cipherAlgorithm);
            deCipher = Cipher.getInstance(cipherAlgorithm);
        } catch (Exception e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "AES密码构造失败", e);
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
            kg = KeyGenerator.getInstance(ALGORITHM);
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new SpinException(ErrorCode.KEY_FAIL, e);
        }
        kg.init(keySize.intValue(), secureRandom);
        //获取密匙对象
        SecretKey skey = kg.generateKey();
        //获取随机密匙
        byte[] raw = skey.getEncoded();
        return new SecretKeySpec(raw, "AES");
    }

    /**
     * 将字节数组转换为密钥
     *
     * @param key 密钥字节数据
     * @return 密钥
     */
    public static Key toKey(byte[] key) {
        return new SecretKeySpec(key, ALGORITHM);
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
     * @param keyLength 密钥强度
     * @return 当前AES加密工具实例
     */
    public synchronized AES withKey(SecretKey secretKey, KeyLength keyLength) {
        this.secretKey = Assert.notNull(secretKey, "密钥不能为空");
        this.keyLength = Assert.notNull(keyLength, "密钥强度不能为空");

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
        keyLength = KeyLength.WEAK;
        this.secretKey = generateKey(Assert.notNull(secretKey, "密钥不能为空"), keyLength);

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
        this.keyLength = Assert.notNull(keyLength, "密钥强度不能为空");
        this.secretKey = generateKey(Assert.notNull(secretKey, "密钥不能为空"), keyLength);

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
        if (mode.needIv) {
            Assert.isTrue(iv != null && iv.length >= 16, "初始化向量的长度不能小于16");
            System.arraycopy(iv, 0, this.iv, 0, 16);

            initCipher();
        }
        return this;
    }

    /**
     * 加密并将加密结果表示为Base64字符串
     *
     * @param data 待加密数据
     * @return 密文
     */
    public String encrypt(String data) {
        return Base64.encode(encrypt(data.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @return 密文
     */
    public byte[] encrypt(byte[] data) {
        try {
            return enCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "加密失败", e);
        }
    }

    /**
     * 使用指定的Key生成最低强度的密钥进行解密
     *
     * @param data 待解密数据
     * @return 明文
     */
    public String decrypt(String data) {
        return decrypt(data, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定的密钥与字符集进行解密
     *
     * @param data    待解密数据
     * @param charset 明文字符集
     * @return 明文
     */
    public String decrypt(String data, Charset charset) {
        return new String(decrypt(Base64.decode(data)), charset);
    }

    /**
     * 使用指定的密钥与字符集进行解密
     *
     * @param data 待解密数据
     * @return 明文
     */
    public byte[] decrypt(byte[] data) {
        try {
            return deCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "解密失败", e);
        }
    }

    public Mode getMode() {
        return mode;
    }

    public Padding getPadding() {
        return padding;
    }

    private void initCipher() {
        try {
            if (mode.needIv) {
                enCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
                deCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            } else {
                enCipher.init(Cipher.ENCRYPT_MODE, secretKey);
                deCipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "AES密码初始化失败", e);
        }
    }
}
