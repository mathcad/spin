package org.spin.sys.auth;

import org.spin.security.RSA;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 密钥信息
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public class KeyInfo {
    private static final String SEPARATOR = "~~~";
    private String identifier;
    private String secret;
    private Long generateTime;

    private static PublicKey RSA_PUBKEY;
    private static PrivateKey RSA_PRIKEY;

    public KeyInfo(String key) {
        String info[];
        try {
            info = RSA.decrypt(RSA_PRIKEY, key).split(SEPARATOR);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID, "无效的密钥");
        }
        if (info.length != 3)
            throw new SimplifiedException(ErrorCode.SECRET_INVALID, "无效的密钥");
        this.identifier = info[0];
        this.secret = info[1];
        this.generateTime = Long.parseLong(info[2]);
    }

    public KeyInfo(String identifier, String secret) {
        this.identifier = identifier;
        this.secret = secret;
        this.generateTime = System.currentTimeMillis();
    }

    public String encode() {
        this.generateTime = System.currentTimeMillis();
        String ecodeStr = identifier + SEPARATOR + secret + SEPARATOR + generateTime;
        String key;
        try {
            key = RSA.encrypt(RSA_PUBKEY, ecodeStr);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.ENCRYPT_FAIL, "生成密钥失败");
        }
        return key;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getGenerateTime() {
        return generateTime;
    }
}
