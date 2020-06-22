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
import javax.crypto.ShortBufferException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Set;


/**
 * RSA 工具类。提供加密，解密，签名，验证，生成密钥对等方法。
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RSA extends ProviderDetector {
    private static final int DEFAULT_KEY_SIZE = 1024;
    private static final String SIGN_ALGORITHMS = "SHA256withRSA";
    private static final String RSA_ALGORITHMS = "RSA";
    private static final String KEY_INVALIE = "密钥不合法";
    private static final String NO_SUCH_ALGORITHM = "加密算法不存在";

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
            throw new SpinException(ErrorCode.ENCRYPT_FAIL);
        }
        keyPairGen.initialize(DEFAULT_KEY_SIZE, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    /**
     * 生成序列化的随机密钥对(公钥/私钥)
     *
     * @return BASE64格式的公钥与私钥对
     */
    public static Pair<String, String> generateSerializedKeyPair() {
        KeyPair keyPair = generateKeyPair();
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
    public static PrivateKey getRSAPrivateKey(String key) {
        KeyFactory keyFactory = getRSAKeyFactory();
        try {
            return Assert.notNull(keyFactory).generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key)));
        } catch (InvalidKeySpecException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
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
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
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
        try {
            cipher = Cipher.getInstance(RSA_ALGORITHMS);
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            FastByteBuffer byteBuffer = new FastByteBuffer();
            int offset = 0;
            while (offset < data.length) {
                byteBuffer.append(cipher.doFinal(data, offset, Math.min(117, data.length - offset)));
                offset += 117;
            }
            return byteBuffer.toArray();
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "加密失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        }
    }

    /**
     * 加密
     *
     * @param pk              公钥
     * @param rawInput        数据输入流
     * @param encryptedOutput 密文输入流
     */
    public static void encrypt(PublicKey pk, InputStream rawInput, OutputStream encryptedOutput) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(RSA_ALGORITHMS);
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            byte[] work = new byte[128];

            int len;
            int resLen;
            while ((len = rawInput.read(work, 0, 117)) != -1) {
                resLen = cipher.doFinal(work, 0, len, work);
                encryptedOutput.write(work, 0, resLen);
            }
            encryptedOutput.flush();
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "加密失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "IO异常, 加密失败", e);
        } catch (ShortBufferException ignore) {
            // donothing
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
            cipher = Cipher.getInstance(RSA_ALGORITHMS);
            cipher.init(Cipher.DECRYPT_MODE, pk);

            FastByteBuffer byteBuffer = new FastByteBuffer();
            int offset = 0;
            while (offset < raw.length) {
                byteBuffer.append(cipher.doFinal(raw, offset, Math.min(128, raw.length - offset)));
                offset += 128;
            }
            return byteBuffer.toArray();
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "解密失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        }
    }

    /**
     * 解密
     *
     * @param pk            私钥
     * @param encrypteInput 密文数据流
     * @param rawOutput     明文数据输出流
     */
    public static void decrypt(PrivateKey pk, InputStream encrypteInput, OutputStream rawOutput) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(RSA_ALGORITHMS);
            cipher.init(Cipher.DECRYPT_MODE, pk);

            byte[] work = new byte[128];

            int len;
            int resLen;
            while ((len = encrypteInput.read(work)) != -1) {
                resLen = cipher.doFinal(work, 0, len, work);
                rawOutput.write(work, 0, resLen);
            }
            rawOutput.flush();
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, "解密失败", e);
        } catch (InvalidKeyException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "IO异常, 解密失败", e);
        } catch (ShortBufferException ignore) {
            // donothing
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
        return sign(content, priKey);
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
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
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
        return verify(content, sign, pubKey);
    }

    /**
     * RSA验签名检查
     *
     * @param content     待签名数据
     * @param sign        签名
     * @param certificate 公钥字符串
     * @return 签名是否有效
     */
    public static boolean verify(String content, String sign, Certificate certificate) {
        return verify(content, sign, getPublicKeyFromCert(certificate));
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
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, KEY_INVALIE, e);
        }
    }

    private static KeyFactory getRSAKeyFactory() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            throw new SpinException(ErrorCode.ENCRYPT_FAIL, NO_SUCH_ALGORITHM, e);
        }
        return keyFactory;
    }

    private static PublicKey getPublicKeyFromCert(Certificate cert) {
        // If the certificate is of type X509Certificate,
        // we should check whether it has a Key Usage
        // extension marked as critical.
        //if (cert instanceof java.security.cert.X509Certificate) {
        if (cert instanceof X509Certificate) {
            // Check whether the cert has a key usage extension
            // marked as a critical extension.
            // The OID for KeyUsage extension is 2.5.29.15.
            X509Certificate c = (X509Certificate) cert;
            try {
                c.checkValidity();
            } catch (CertificateExpiredException e) {
                throw new SpinException(ErrorCode.ENCRYPT_FAIL, "数字证书已经过期");
            } catch (CertificateNotYetValidException e) {
                throw new SpinException(ErrorCode.ENCRYPT_FAIL, "数字证书尚未生效");
            }
            Set<String> critSet = c.getCriticalExtensionOIDs();

            if (critSet != null && !critSet.isEmpty()
                && critSet.contains("2.5.29.15")) {
                boolean[] keyUsageInfo = c.getKeyUsage();
                // keyUsageInfo[0] is for digitalSignature.
                if ((keyUsageInfo != null) && (!keyUsageInfo[0]))
                    throw new SpinException(ErrorCode.ENCRYPT_FAIL, "证书不正确");
            }
        }
        return cert.getPublicKey();
    }

}
