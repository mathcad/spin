package org.spin.core.auth;

import java.io.Serializable;
import java.util.Objects;

/**
 * 密钥信息
 * Created by xuweinan on 2017/3/2.
 *
 * @author xuweinan
 */
public class KeyInfo implements Serializable {
    private static final long serialVersionUID = 6398212613135892288L;

    /**
     * 用户标识符
     */
    private String identifier;

    /**
     * 密钥
     */
    private String key;

    /**
     * 用户私钥
     */
    private String secret;

    /**
     * 私钥类型
     */
    private String secretType;

    /**
     * 密钥产生时间
     */
    private Long generateTime;

    public KeyInfo(String identifier, String key, String secret, String secretType, Long generateTime) {
        this.identifier = identifier;
        this.key = key;
        this.secret = secret;
        this.secretType = secretType;
        this.generateTime = generateTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public String getSecretType() {
        return secretType;
    }

    public Long getGenerateTime() {
        return generateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyInfo keyInfo = (KeyInfo) o;
        return Objects.equals(key, keyInfo.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
