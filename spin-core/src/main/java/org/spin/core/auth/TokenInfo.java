package org.spin.core.auth;


import java.io.Serializable;
import java.util.Objects;

/**
 * Token信息
 * <p>
 * Created by xuweinan on 2016/10/3.
 *
 * @author xuweinan
 */
public class TokenInfo implements Serializable {
    private static final long serialVersionUID = -167149892869305494L;

    /**
     * 用户标识符
     */
    private String identifier;

    /**
     * 令牌
     */
    private String token;

    /**
     * 产生令牌的密钥
     */
    private String sourceKey;

    /**
     * 令牌产生时间
     */
    private Long generateTime;


    public TokenInfo(String identifier, String token, String sourceKey) {
        this.identifier = identifier;
        this.token = token;
        this.generateTime = System.currentTimeMillis();
        this.sourceKey = sourceKey;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getToken() {
        return token;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public Long getGenerateTime() {
        return generateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenInfo tokenInfo = (TokenInfo) o;
        return Objects.equals(identifier, tokenInfo.identifier) &&
            Objects.equals(token, tokenInfo.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, token);
    }
}
