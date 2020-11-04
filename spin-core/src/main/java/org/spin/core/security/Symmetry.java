package org.spin.core.security;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.function.serializable.Consumer;
import org.spin.core.throwable.SpinException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 对称加密算法的抽象类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/8/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
abstract class Symmetry extends ProviderDetector {
    protected static final int BUFFER_SIZE = 2048;

    protected Mode mode = Mode.ECB;
    protected Padding padding = Padding.PKCS5Padding;
    protected byte[] iv;
    protected SecretKey secretKey;
    protected int keyLength;

    protected final Algorithm algorithm;
    protected final Cipher enCipher;
    protected final Cipher deCipher;

    enum Algorithm {
        AES("AES", 16, l -> Assert.isTrue(l == 128 || l == 192 || l == 256, "密钥长度必须为128位, 192位或256位")),
        DES("DES", 8, l -> Assert.isTrue(l == 64, "密钥长度必须为64位")),
        DESEDE("DESede", 8, l -> Assert.isTrue(l == 192, "密钥长度必须为192位")),
        BLOWFISH("Blowfish", 8, l -> Assert.isTrue(l % 8 == 0, "密钥长度不合法")),
        TWOFISH("Twofish", 16, l -> Assert.isTrue(l % 8 == 0 && l >= 64 && l <= 312, "密钥长度必须在57位至312位之间"));

        private final String algorithmName;
        private final int ivLength;
        private final Consumer<Integer> keyLengthChecker;

        Algorithm(String algorithmName, int ivLength, Consumer<Integer> keyLengthChecker) {
            this.algorithmName = algorithmName;
            this.ivLength = ivLength;
            this.keyLengthChecker = keyLengthChecker;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public int getIvLength() {
            return ivLength;
        }
    }

    protected Symmetry(Algorithm algorithm, Mode mode, Padding padding) {
        this.algorithm = Assert.notNull(algorithm, "请指定加密算法");
        if (null != mode) {
            this.mode = mode;
        }
        if (null != padding) {
            this.padding = padding;
        }
        String cipherAlgorithm = algorithm.getAlgorithmName() + "/" + this.mode.getValue() + "/" + this.padding.getValue();
        if (this.mode.isNeedIv()) {
            iv = new byte[algorithm.ivLength];
            new Random().nextBytes(iv);
        }
        try {
            enCipher = Cipher.getInstance(cipherAlgorithm);
            deCipher = Cipher.getInstance(cipherAlgorithm);
        } catch (Exception e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, algorithm.name() + "密码构造失败", e);
        }
    }

    /**
     * 将字节数组转换为密钥
     *
     * @param algorithm 算法
     * @param key       密钥字节数据
     * @return 密钥
     */
    public static SecretKey toKey(Algorithm algorithm, byte[] key) {
        if (null == algorithm || null == key) {
            return null;
        }
        return new SecretKeySpec(key, algorithm.algorithmName);
    }

    /**
     * 为特定加密算法生成指定长度的密钥
     *
     * @param algorithm 加密算法
     * @param keySeed   密钥种子
     * @param keySize   密钥长度
     * @return 密钥
     */
    public static SecretKey generateKey(Algorithm algorithm, String keySeed, int keySize) {
        if (null == algorithm || null == keySeed) {
            return null;
        }
        Assert.gt(keySize, 0, "密钥长度必须大于0");
        KeyGenerator kg;
        SecureRandom secureRandom;
        try {
            kg = KeyGenerator.getInstance(algorithm.algorithmName);
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(keySeed.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new SpinException(ErrorCode.KEY_FAIL, e);
        }
        kg.init(keySize, secureRandom);
        //获取密匙对象
        return kg.generateKey();
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
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, algorithm.name() + "加密失败", e);
        }
    }

    /**
     * 流式加密，适用于大文件
     *
     * @param input  明文输入流
     * @param output 密文输出流
     */
    public void encrypte(InputStream input, OutputStream output) {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            doEncode(input, output, buffer, enCipher);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, algorithm.name() + "加密失败, IO异常", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, algorithm.name() + "加密失败", e);
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
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, algorithm.name() + "解密失败", e);
        }
    }

    /**
     * 流式解密，适用于大文件
     *
     * @param input  密文输入流
     * @param output 明文输出流
     */
    public void decrypt(InputStream input, OutputStream output) {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            doEncode(input, output, buffer, deCipher);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, algorithm.name() + "解密失败, IO异常", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, algorithm.name() + "解密失败", e);
        }
    }

    public Mode getMode() {
        return mode;
    }

    public Padding getPadding() {
        return padding;
    }

    protected void initCipher() {
        try {
            if (mode.isNeedIv()) {
                enCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
                deCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            } else {
                enCipher.init(Cipher.ENCRYPT_MODE, secretKey);
                deCipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, algorithm.name() + "密码初始化失败", e);
        }
    }

    protected void doEncode(InputStream input, OutputStream output, byte[] buffer, Cipher enCipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        int bytesRead;
        boolean finish = false;
        while ((bytesRead = input.read(buffer)) > 0) {
            if (bytesRead == BUFFER_SIZE) {
                output.write(enCipher.update(buffer));
            } else {
                output.write(enCipher.doFinal(buffer, 0, bytesRead));
                finish = true;
            }
        }

        if (!finish) {
            output.write(enCipher.doFinal());
        }
        output.flush();
    }

    /**
     * 指定加密密钥与密钥强度
     *
     * @param secretKey 密钥
     */
    protected void setKey(SecretKey secretKey) {
        this.secretKey = Assert.notNull(secretKey, "密钥不能为空");
        Assert.isTrue(secretKey.getAlgorithm().equalsIgnoreCase(algorithm.getAlgorithmName()), "密钥算法与加密算法不匹配, 密钥算法: " + secretKey.getAlgorithm());
        keyLength = secretKey.getEncoded().length * 8;
        algorithm.keyLengthChecker.accept(keyLength);
    }

    protected boolean setIv(byte[] iv) {
        if (mode.isNeedIv()) {
            Assert.isTrue(iv != null && iv.length >= algorithm.getIvLength(), "初始化向量的长度不能小于" + algorithm.getIvLength());
            System.arraycopy(iv, 0, this.iv, 0, algorithm.getIvLength());
            return true;
        }
        return false;
    }
}
