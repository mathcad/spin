package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.TypeIdentifier;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.HttpUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.wx.wx.base.WxUrl;

import java.util.Date;
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
    private static final TypeIdentifier<Map<String, String>> type = new TypeIdentifier<Map<String, String>>() {
    };
    private static Map<String, AccessToken> instances;
    private static Map<String, AccessToken> instancesWithCode;
    private String token;
    private String refreshToken;
    private String openId;
    private int expiresIn;
    private long expiredSince;

    private static final Object lock = new Object();
    private static final Object lockWithCode = new Object();

    private AccessToken() {
    }

    /**
     * 获取默认的AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getDefaultInstance() {
        return getInstance("default");
    }

    /**
     * 获取默认的网页授权AccessToken对象，根据生命周期对token自管理
     *
     * @param code 微信code
     */
    public static AccessToken getDefaultOAuthInstance(String... code) {
        return getOAuthInstance("default", code);
    }

    /**
     * 获取AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getInstance(String name) {
        WxConfig.ConfigInfo info = WxConfig.getConfig(name);
        return getInstance(name, info.getAppId(), info.getAppSecret(), null, false);
    }

    /**
     * 获取网页授权AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getOAuthInstance(String name, String... code) {
        WxConfig.ConfigInfo info = WxConfig.getConfig(name);
        String c = (null == code || code.length == 0) ? "" : code[0];
        return getInstance(name, info.getAppId(), info.getAppSecret(), c, true);
    }

    /**
     * 获取AccessToken的对象，根据生命周期对token自管理
     */
    public static AccessToken getInstance(String name, String appId, String appSecret, String code, boolean isOAuth) {
        logger.info("getInstance({}, {}, {}, {})", name, appId, appSecret, code);

        if (null == instances) {
            instances = new ConcurrentHashMap<>();
        }
        if (null == instancesWithCode) {
            instancesWithCode = new ConcurrentHashMap<>();
        }

        AccessToken token;
        if (isOAuth) {
            // 获取网页授权access_token
            token = instancesWithCode.get(name);
            if (StringUtils.isNotEmpty(code) || null == token || StringUtils.isEmpty(token.token) || System.currentTimeMillis() > token.getExpiredSince()) {
                synchronized (lockWithCode) {
                    String result;
                    try {
                        if (null != token && StringUtils.isNotEmpty(token.getRefreshToken()) && System.currentTimeMillis() < (token.getExpiredSince() + 2160000000L))
                            result = HttpUtils.httpGetRequest(WxUrl.RefreshTokenUrl.getUrl(appId, token.getRefreshToken()));
                        else
                            result = HttpUtils.httpGetRequest(WxUrl.OAuthTokenUrl.getUrl(appId, appSecret, code));
                    } catch (Throwable e) {
                        throw new SimplifiedException("获取网页授权access_token失败", e);
                    }
                    instancesWithCode.put(name, parseToken(result));
                    token = instancesWithCode.get(name);
                }
            }
        } else {
            // 获取普通access_token
            token = instances.get(name);
            if (null == token || StringUtils.isEmpty(token.token) || System.currentTimeMillis() > token.getExpiredSince()) {
                synchronized (lock) {
                    String result;
                    try {
                        result = HttpUtils.httpGetRequest(WxUrl.AccessTokenUrl.getUrl(appId, appSecret));
                    } catch (Throwable e) {
                        throw new SimplifiedException("获取access_token失败", e);
                    }
                    instances.put(name, parseToken(result));
                    token = instances.get(name);
                }
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
        Map<String, String> resMap = JsonUtils.fromJson(json, type);
        if (null != resMap && resMap.containsKey("access_token")) {
            tmp.setExpiresIn(Integer.parseInt(resMap.get("expires_in")));
            tmp.setToken(resMap.get("access_token"));
            tmp.setOpenId(resMap.get("openid"));
            tmp.setRefreshToken(resMap.get("refresh_token"));
            if (logger.isDebugEnabled())
                logger.debug("Current AccessToken is: {}, expired since: {}", tmp.getToken(), new Date(tmp.getExpiredSince()));
            return tmp;
        }
        throw new SimplifiedException("获取access_token失败:[" + json + "]");
    }
}
