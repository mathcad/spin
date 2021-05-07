package org.spin.core.security;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.collection.Pair;
import org.spin.core.collection.Tuple;
import org.spin.core.io.FastByteBuffer;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.SerializeUtils;
import org.spin.core.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * ECC 工具类。提供加密，解密，签名，验证，生成密钥对等方法。
 * <p>Created by xuweinan on 2021/4/13.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ECC extends ProviderDetector {

    public static final int DEFAULT_KEY_SIZE = 256;
    private static final String ECC_ALGORITHM = "EC";
    private static final String SIGN_ALGORITHMS = "SHA256withECDSA";
    private static final String KEY_INVALID = "密钥不合法";
    private static final String NO_SUCH_ALGORITHM = "加密算法不存在";


    /**
     * 生成随机密钥对
     *
     * @param keySize 密钥长度
     * @return 密钥对
     */
    public static KeyPair generateKeyPair(int keySize) {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(ECC_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL);
        }
        keyPairGen.initialize(keySize, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    /**
     * 生成序列化的随机密钥对(公钥/私钥)
     *
     * @return BASE64格式的公钥与私钥对
     */
    public static Pair<String, String> generateSerializedKeyPair() {
        KeyPair keyPair = generateKeyPair(DEFAULT_KEY_SIZE);
        return Tuple.of(Base64.encode(keyPair.getPublic().getEncoded()), Base64.encode(keyPair.getPrivate().getEncoded()));
    }

    /**
     * 从文件中读取密钥对
     *
     * @param filePath 密钥文件路径
     * @return 密钥对
     * @throws IOException 文件读写异常
     */
    public static KeyPair readKeyPair(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return SerializeUtils.deserialize(() -> fis);
        }
    }

    /**
     * 将密钥对存储到文件
     *
     * @param kp       密钥对
     * @param filePath 存储路径
     * @throws IOException 文件读写异常
     */
    public static void saveKeyPair(KeyPair kp, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            SerializeUtils.serialize(kp, () -> fos);
        }
    }

    /**
     * 将字符串解析为私钥
     *
     * @param key 私钥字符串
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(String key) {
        KeyFactory keyFactory = getECCKeyFactory();
        try {
            return Assert.notNull(keyFactory).generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key)));
        } catch (InvalidKeySpecException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALID, e);
        }
    }

    /**
     * 将字符串解析为公钥
     *
     * @param key 公钥字符串
     * @return 公钥
     */
    public static PublicKey getPublicKey(String key) {
        KeyFactory keyFactory = getECCKeyFactory();
        try {
            return Assert.notNull(keyFactory).generatePublic(new X509EncodedKeySpec(Base64.decode(key)));
        } catch (InvalidKeySpecException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALID, e);
        }
    }

    /**
     * 加密
     *
     * @param pk   公钥
     * @param data 数据
     * @return 密文数据
     */
    public static byte[] encrypt(PublicKey pk, byte[] data) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ECC_ALGORITHM + "IES");
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            FastByteBuffer byteBuffer = new FastByteBuffer();
            byteBuffer.append(cipher.doFinal(data));
            return byteBuffer.toArray();
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "加密失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALID, e);
        }
    }

    /**
     * 加密
     *
     * @param publicKey 公钥字符串
     * @param content   明文字符串
     * @return 密文字符串
     */
    public static String encrypt(String publicKey, String content) {
        PublicKey key = getPublicKey(publicKey);
        return Base64.encode(encrypt(key, StringUtils.getBytesUtf8(content)));
    }

    /**
     * 加密
     *
     * @param publicKey 公钥
     * @param content   明文字符串
     * @return 密文字符串
     */
    public static String encrypt(PublicKey publicKey, String content) {
        return Base64.encode(encrypt(publicKey, StringUtils.getBytesUtf8(content)));
    }

    /**
     * 解密
     *
     * @param pk  私钥
     * @param raw 密文数据
     * @return 明文数据
     */
    public static byte[] decrypt(PrivateKey pk, byte[] raw) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ECC_ALGORITHM + "IES");
            cipher.init(Cipher.DECRYPT_MODE, pk);

            FastByteBuffer byteBuffer = new FastByteBuffer();
            byteBuffer.append(cipher.doFinal(raw));
            return byteBuffer.toArray();
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "解密失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALID, e);
        }
    }

    /**
     * 解密
     *
     * @param privateKey 私钥字符串
     * @param content    密文字符串
     * @return 明文字符串
     */
    public static String decrypt(String privateKey, String content) {
        PrivateKey key = getPrivateKey(privateKey);
        return new String(decrypt(key, Base64.decode(content)), StandardCharsets.UTF_8);
    }

    /**
     * 解密
     *
     * @param privateKey 私钥
     * @param content    密文字符串
     * @return 明文字符串
     */
    public static String decrypt(PrivateKey privateKey, String content) {
        return StringUtils.newStringUtf8(decrypt(privateKey, Base64.decode(content)));
    }

    /**
     * RSA签名
     *
     * @param content    待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String content, PrivateKey privateKey) {
        Signature signature;
        try {
            signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(privateKey);
            signature.update(StringUtils.getBytesUtf8(content));
            byte[] signed = signature.sign();
            return Base64.encode(signed);
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "签名算法不存在: " + SIGN_ALGORITHMS, e);
        } catch (SignatureException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "签名失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALID, e);
        }
    }

    /**
     * RSA验签名检查
     *
     * @param content   待签名数据
     * @param sign      签名
     * @param publicKey 公钥字符串
     * @return 签名是否有效
     */
    public static boolean verify(String content, String sign, String publicKey) {
        PublicKey pubKey = getPublicKey(publicKey);
        return verify(content, sign, pubKey);
    }

    /**
     * RSA验签名检查
     *
     * @param content   待签名数据
     * @param sign      签名
     * @param publicKey 公钥字符串
     * @return 签名是否有效
     */
    public static boolean verify(String content, String sign, PublicKey publicKey) {
        Signature signature;
        try {
            signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(publicKey);
            signature.update(StringUtils.getBytesUtf8(content));
            return signature.verify(Base64.decode(sign));
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "签名算法不存在: " + SIGN_ALGORITHMS, e);
        } catch (SignatureException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "签名校验失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALID, e);
        }
    }

    private static KeyFactory getECCKeyFactory() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(ECC_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        }
        return keyFactory;
    }
}
