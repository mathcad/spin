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

    private String identifier;
    private String key;
    private String secret;
    private Long generateTime = 0L;

    public KeyInfo(String identifier, String key, String secret, Long generateTime) {
        this.identifier = identifier;
        this.key = key;
        this.secret = secret;
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
