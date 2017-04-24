package org.spin.sys.auth;


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
    private String identifier;
    private String token;
    private String sourceKey;
    private Long generateTime = 0L;


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
