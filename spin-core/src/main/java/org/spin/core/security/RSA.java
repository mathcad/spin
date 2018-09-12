package org.spin.core.security;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.SerializeUtils;
import org.spin.core.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * RSA 工具类。提供加密，解密，签名，验证，生成密钥对等方法。
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RSA {
    private static final int KEY_SIZE = 1024;
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    private static final String RSA_ALGORITHMS = "RSA";
    private static final String KEY_INVALIE = "密钥不合法";
    private static final String NO_SUCH_ALGORITHM = "加密算法不存在";
    private static boolean hasBouncyProvider = true;
    private static Constructor<Provider> providerConstructor;

    private RSA() {
    }

    /**
     * 生成随机密钥对
     *
     * @return 密钥对
     */
    public static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL);
        }
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGen.generateKeyPair();
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
    public static PrivateKey getRSAPrivateKey(String key) {
        KeyFactory keyFactory = getRSAKeyFactory();
        try {
            return Assert.notNull(keyFactory).generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key)));
        } catch (InvalidKeySpecException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        }
    }

    /**
     * 将字符串解析为公钥
     *
     * @param key 公钥字符串
     * @return 公钥
     */
    public static PublicKey getRSAPublicKey(String key) {
        KeyFactory keyFactory = getRSAKeyFactory();
        try {
            return Assert.notNull(keyFactory).generatePublic(new X509EncodedKeySpec(Base64.decode(key)));
        } catch (InvalidKeySpecException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        }
    }

    public static PublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) throws InvalidKeySpecException {
        KeyFactory keyFactory = getRSAKeyFactory();
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
        return Assert.notNull(keyFactory).generatePublic(pubKeySpec);
    }

    public static PrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws
        InvalidKeySpecException {
        KeyFactory keyFactory = getRSAKeyFactory();
        RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(privateExponent));
        return Assert.notNull(keyFactory).generatePrivate(priKeySpec);
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
        Provider provider = null;
        if (hasBouncyProvider) {
            try {
                if (null == providerConstructor) {
                    //noinspection unchecked
                    providerConstructor = (Constructor<Provider>) ClassUtils.getClass("org.bouncycastle.jce.provider.BouncyCastleProvider").getDeclaredConstructor();
                }
                provider = providerConstructor.newInstance();
            } catch (Exception ignore) {
                // no bouncyCastle provider
                hasBouncyProvider = false;
            }
        }
        try {
            if (null != provider) {
                cipher = Cipher.getInstance(RSA_ALGORITHMS, provider);
            } else {
                cipher = Cipher.getInstance(RSA_ALGORITHMS);
            }
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "加密失败", e);
        } catch (InvalidKeyException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
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
        PublicKey key = getRSAPublicKey(publicKey);
        return Base64.encode(encrypt(key, getBytes(content)));
    }

    /**
     * 加密
     *
     * @param publicKey 公钥
     * @param content   明文字符串
     * @return 密文字符串
     */
    public static String encrypt(PublicKey publicKey, String content) {
        return Base64.encode(encrypt(publicKey, getBytes(content)));
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
        Provider provider = null;
        if (hasBouncyProvider) {
            try {
                if (null == providerConstructor) {
                    //noinspection unchecked
                    providerConstructor = (Constructor<Provider>) ClassUtils.getClass("org.bouncycastle.jce.provider.BouncyCastleProvider").getDeclaredConstructor();
                }
                provider = providerConstructor.newInstance();
            } catch (Exception ignore) {
                // no bouncyCastle provider
                hasBouncyProvider = false;
            }
        }
        try {
            if (null != provider) {
                cipher = Cipher.getInstance(RSA_ALGORITHMS, provider);
            } else {
                cipher = Cipher.getInstance(RSA_ALGORITHMS);
            }
            cipher.init(Cipher.DECRYPT_MODE, pk);
            return cipher.doFinal(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "解密失败", e);
        } catch (InvalidKeyException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
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
        PrivateKey key = getRSAPrivateKey(privateKey);
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
     * @param privateKey 私钥字符串
     * @return 签名
     */
    public static String sign(String content, String privateKey) {
        PrivateKey priKey = getRSAPrivateKey(privateKey);
        Signature signature;
        try {
            signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(priKey);
            signature.update(getBytes(content));
            byte[] signed = signature.sign();
            return Base64.encode(signed);
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "签名算法不存在: " + SIGN_ALGORITHMS, e);
        } catch (SignatureException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "签名失败", e);
        } catch (InvalidKeyException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
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
        PublicKey pubKey = getRSAPublicKey(publicKey);
        Signature signature;
        try {
            signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(pubKey);
            signature.update(getBytes(content));
            return signature.verify(Base64.decode(sign));
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "签名算法不存在: " + SIGN_ALGORITHMS, e);
        } catch (SignatureException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "签名校验失败", e);
        } catch (InvalidKeyException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        }
    }

    private static byte[] getBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static KeyFactory getRSAKeyFactory() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        }
        return keyFactory;
    }
}
