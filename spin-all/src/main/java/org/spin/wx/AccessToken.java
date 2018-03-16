package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.TypeIdentifier;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;

import java.util.Date;
import java.util.Map;

/**
 * 微信的AccessToken封装
 * <p>Created by xuweinan on 2016/9/28.</p>
 *
 * @author xuweinan
 */
public class AccessToken {
    private static final Logger logger = LoggerFactory.getLogger(AccessToken.class);
    private static final TypeIdentifier<Map<String, String>> type = new TypeIdentifier<Map<String, String>>() {
    };

    private String token;
    private String refreshToken;
    private String openId;
    private int expiresIn;
    private long expiredSince;

    /**
     * token类型
     */
    public enum TokenType {
        /**
         * 网页授权token
         */
        OAUTH,

        /**
         * 普通token
         */
        NORMAL
    }

    public AccessToken() {
    }

    public AccessToken(String json) {
        Map<String, String> resMap = JsonUtils.fromJson(json, type);
        if (null != resMap && resMap.containsKey("access_token")) {
            this.setExpiresIn(Integer.parseInt(resMap.get("expires_in")));
            this.setToken(resMap.get("access_token"));
            this.setOpenId(resMap.get("openid"));
            this.setRefreshToken(resMap.get("refresh_token"));
            if (logger.isDebugEnabled())
                logger.debug("Current AccessToken is: {}, expired since: {}", this.getToken(), new Date(this.getExpiredSince()));
            return;
        }
        throw new SimplifiedException("获取access_token失败:[" + json + "]");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        this.expiredSince = System.currentTimeMillis() + expiresIn * 900;
    }

    public long getExpiredSince() {
        return expiredSince;
    }
}
