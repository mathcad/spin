
package org.infrastructure.security;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RSA {

    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static Map<String, String> generateKeyPair() throws Exception {

        HashMap<String, String> keysMap = new HashMap<>();
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        keysMap.put("PK", Base64.encode(publicKey.getEncoded()));
        keysMap.put("SK", Base64.encode(privateKey.getEncoded()));

        return keysMap;
    }

    /**
     * RSA签名
     *
     * @param content       待签名数据
     * @param privateKey    私钥
     * @param input_charset 编码格式
     * @return 签名值
     */
    public static String sign(String content, String privateKey, String input_charset) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(input_charset));

            byte[] signed = signature.sign();

            return Base64.encode(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * RSA验签名检查
     *
     * @param content       待签名数据
     * @param sign          签名值
     * @param public_key    公钥
     * @param input_charset 编码格式
     * @return 布尔值
     */
    public static boolean verify(String content, String sign, String public_key, String input_charset) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(public_key);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes(input_charset));

            return signature.verify(Base64.decode(sign));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 公钥加密
     */
    public static String encrypt(String content, String public_key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");

        RSAPublicKey publicKey = (RSAPublicKey) getPublicKey(public_key);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        InputStream ins = new ByteArrayInputStream(content.getBytes());
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        // 加密数据长度 <= 模长-11
        byte[] buf = new byte[117];
        int bufl;

        while ((bufl = ins.read(buf)) != -1) {
            byte[] block;
            if (buf.length == bufl) {
                block = buf;
            } else {
                block = Arrays.copyOf(buf, bufl);
            }
            writer.write(cipher.doFinal(block));
        }
        return Base64.encode(writer.toByteArray());
    }

    /**
     * 解密
     *
     * @param content       密文
     * @param private_key   私钥
     * @param input_charset 编码格式
     * @return 解密后的字符串
     */
    public static String decrypt(String content, String private_key, String input_charset) throws Exception {
        PrivateKey prikey = getPrivateKey(private_key);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);
        InputStream ins = new ByteArrayInputStream(Base64.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        // rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
        byte[] buf = new byte[128];
        int bufl;
        while ((bufl = ins.read(buf)) != -1) {
            byte[] block;
            if (buf.length == bufl) {
                block = buf;
            } else {
                block = Arrays.copyOf(buf, bufl);
            }
            writer.write(cipher.doFinal(block));
        }
        return new String(writer.toByteArray(), input_charset);
    }

    /**
     * 得到私钥
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 得到公钥
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] encodedKey = Base64.decode(key);
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }
}