package org.spin.sys.auth;

/**
 * Token信息
 * <p>
 * Created by xuweinan on 2016/10/3.
 *
 * @author xuweinan
 */
public class TokenInfo {
    private Long generateTime = 0L;
    private String userId;
    private String token;

    public TokenInfo(String userId, String token) {
        this.userId = userId;
        this.setToken(token);
    }

    public Long getGenerateTime() {
        return generateTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        this.generateTime = System.currentTimeMillis();
    }
}