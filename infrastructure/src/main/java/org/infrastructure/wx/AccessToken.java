package org.infrastructure.wx;


import org.infrastructure.sys.TypeIdentifier;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.HttpUtils;
import org.infrastructure.util.JSONUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信的AccessToken封装
 * <p>
 * 实现AccessToken的自管理，系统自动维护access_token实例集合，并保证该容器内的access_token始终有效<br>
 * Created by xuweinan on 2016/9/28.
 *
 * @author xuweinna
 */
public class AccessToken {
    private static final Logger logger = LoggerFactory.getLogger(AccessToken.class);
    private static final TypeIdentifier<HashMap<String, String>> type = new TypeIdentifier<HashMap<String, String>>() {
    };
    private static Map<String, AccessToken> instances;
    private String token;
    private String refreshToken;
    private String openId;
    private int expiresIn;
    private long expiredSince;

    private AccessToken() {
    }

    /**
     * 获取默认的AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getDefaultInstance(String... code) {
        return getInstance("default", WxConfig.appId, WxConfig.appSecret, code);
    }

    /**
     * 获取AccessToken的对象，根据生命周期对token自管理
     */
    public static AccessToken getInstance(String name, String appId, String appSecret, String... code) {
        if (code != null && code.length != 0 && StringUtils.isNotEmpty(code[0])) {
            try {
                String result = HttpUtils.httpGetRequest("https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code", appId, appSecret, code[0]);
                return parseToken(result);
            } catch (URISyntaxException e) {
                throw new SimplifiedException("获取access_token失败", e);
            }
        }
        if (null == instances)
            instances = new ConcurrentHashMap<>();
        AccessToken token = instances.get(name);
        if (null == token || StringUtils.isEmpty(token.token) || System.currentTimeMillis() > token.getExpiredSince()) {
            synchronized (AccessToken.class) {
                String result;
                try {
                    result = HttpUtils.httpGetRequest("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}", appId, appSecret);
                } catch (Throwable e) {
                    throw new SimplifiedException("获取access_token失败", e);
                }
                instances.put(name, parseToken(result));
                return instances.get(name);
            }
        }
        return token;
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

    private static AccessToken parseToken(String json) {
        AccessToken tmp = new AccessToken();
        Map<String, String> resMap = JSONUtils.fromJson(json, type);
        if (null != resMap && resMap.containsKey("access_token")) {
            tmp.setExpiresIn(Integer.parseInt(resMap.get("expires_in")));
            tmp.setToken(resMap.get("access_token"));
            tmp.setOpenId(resMap.get("openid"));
            if (logger.isDebugEnabled())
                logger.debug("Current AccessToken is: {}, expired since: {}", tmp.getToken(), new Date(tmp.getExpiredSince()));
            return tmp;
        }
        throw new SimplifiedException("获取access_token失败:[" + json + "]");
    }
}