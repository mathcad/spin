package org.infrastructure.security;

import org.infrastructure.sys.Assert;
import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.throwable.SimplifiedException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * RSA 工具类。提供加密，解密，签名，验证，生成密钥对等方法。
 * <p>
 * Created by xuweinan on 2016/8/15.
 *
 * @author xuweinan
 * @version 1.0
 */
public class RSA {
    private static final int KEY_SIZE = 1024;
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    private static final String RSA_ALGORITHMS = "RSA";

    public static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.ENCRYPT_FAIL);
        }
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    public static KeyPair readKeyPair(String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream oos = new ObjectInputStream(fis);
        KeyPair kp = (KeyPair) oos.readObject();
        oos.close();
        fis.close();
        return kp;
    }

    public static void saveKeyPair(KeyPair kp, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(kp);
        oos.close();
        fos.close();
    }

    public static PrivateKey getRSAPrivateKey(String key) throws InvalidKeySpecException {
        KeyFactory keyFactory = getRSAKeyFactory();
        Assert.notNull(keyFactory);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key)));
    }

    public static PublicKey getRSAPublicKey(String key) throws InvalidKeySpecException {
        KeyFactory keyFactory = getRSAKeyFactory();
        Assert.notNull(keyFactory);
        return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(key)));
    }

    public static PublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) throws InvalidKeySpecException {
        KeyFactory keyFactory = getRSAKeyFactory();
        Assert.notNull(keyFactory);
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
        return keyFactory.generatePublic(pubKeySpec);
    }

    public static PrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws InvalidKeySpecException {
        KeyFactory keyFactory = getRSAKeyFactory();
        Assert.notNull(keyFactory);
        RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(privateExponent));
        return keyFactory.generatePrivate(priKeySpec);
    }

    public static byte[] encrypt(PublicKey pk, byte[] data) throws NoSuchPaddingException, InvalidKeyException, BadPaddingException, ShortBufferException, IllegalBlockSizeException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        return cipher.doFinal(data);
    }

    public static String encrypt(String publicKey, String content) throws InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, ShortBufferException, NoSuchPaddingException {
        PublicKey key = getRSAPublicKey(publicKey);
        return Base64.encode(encrypt(key, getBytes(content)));
    }

    public static byte[] decrypt(PrivateKey pk, byte[] raw) throws NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
        cipher.init(Cipher.DECRYPT_MODE, pk);
        return cipher.doFinal(raw);
    }

    public static String decrypt(String privateKey, String content) throws InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        PrivateKey key = getRSAPrivateKey(privateKey);
        try {
            return new String(decrypt(key, Base64.decode(content)), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.DEENCRYPT_FAIL, e);
        }
    }

    /**
     * RSA签名
     *
     * @param content    待签名数据
     * @param privateKey 私钥
     * @return 签名值
     */
    public static String sign(String content, String privateKey) throws InvalidKeySpecException, InvalidKeyException, SignatureException {
        PrivateKey priKey = getRSAPrivateKey(privateKey);
        Signature signature;
        try {
            signature = Signature.getInstance(SIGN_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        signature.initSign(priKey);
        signature.update(getBytes(content));
        byte[] signed = signature.sign();
        return Base64.encode(signed);
    }

    /**
     * RSA验签名检查
     *
     * @param content   待签名数据
     * @param sign      签名值
     * @param publicKey 公钥
     * @return 是否匹配s
     */
    public static boolean verify(String content, String sign, String publicKey) throws InvalidKeySpecException, InvalidKeyException, SignatureException {
        PublicKey pubKey = getRSAPublicKey(publicKey);
        Signature signature;
        try {
            signature = Signature.getInstance(SIGN_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        signature.initVerify(pubKey);
        signature.update(getBytes(content));
        return signature.verify(Base64.decode(sign));
    }

    private static byte[] getBytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    private static KeyFactory getRSAKeyFactory() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(RSA_ALGORITHMS);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return keyFactory;
    }
}