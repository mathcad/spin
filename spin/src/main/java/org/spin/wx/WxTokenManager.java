package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.HttpUtils;
import org.spin.core.util.StringUtils;
import org.spin.wx.base.WxUrl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AccessToken与JSAPI Ticket管理类
 * <p>实现AccessToken与JSAPI Ticket的自管理，系统自动维护相应实例集合，
 * 并保证该容器内的token与ticket始终有效</p>
 * <p>Created by xuweinan on 2016/10/20.</p>
 *
 * @author xuweinan
 */
public class WxTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(WxTokenManager.class);

    private static Map<String, AccessToken> tokenInstances;
    private static Map<String, AccessToken> oauthTokenInstances;
    private static Map<String, ApiTicket> ticketInstances;

    private static final Object tokenLock = new Object();
    private static final Object tokenLockWithCode = new Object();
    private static final Object ticketLock = new Object();


    private WxTokenManager() {
    }

    /**
     * 获取默认的AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getDefaultToken() {
        return getToken(WxConfigManager.DEFAULT);
    }

    /**
     * 获取默认的网页授权AccessToken对象，根据生命周期对token自管理
     *
     * @param code 微信code
     */
    public static AccessToken getDefaultOAuthToken(String... code) {
        return getOAuthToken(WxConfigManager.DEFAULT, code);
    }

    /**
     * 获取AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getToken(String configName) {
        WxConfigInfo info = WxConfigManager.getConfig(configName);
        return getToken(configName, info.getAppId(), info.getAppSecret(), null, AccessToken.TokenType.NORMAL);
    }

    /**
     * 获取网页授权AccessToken对象，根据生命周期对token自管理
     */
    public static AccessToken getOAuthToken(String configName, String... code) {
        WxConfigInfo info = WxConfigManager.getConfig(configName);
        String c = (null == code || code.length == 0) ? "" : code[0];
        return getToken(configName, info.getAppId(), info.getAppSecret(), c, AccessToken.TokenType.OAUTH);
    }

    /**
     * 获取AccessToken的对象，根据生命周期对token自管理
     *
     * @param configName 微信配置名称
     * @param appId      微信appid
     * @param appSecret  微信appsecret
     * @param code       访问时微信带来的code
     * @param type       需要的accessToken类型
     */
    public static AccessToken getToken(String configName, String appId, String appSecret, String code, AccessToken.TokenType type) {
        logger.info("getInstance({}, {}, {}, {})", configName, appId, appSecret, code);

        if (null == tokenInstances) {
            tokenInstances = new ConcurrentHashMap<>();
        }
        if (null == oauthTokenInstances) {
            oauthTokenInstances = new ConcurrentHashMap<>();
        }

        AccessToken token = null;
        switch (type) {
            case NORMAL:
                // 获取普通access_token
                token = tokenInstances.get(configName);
                if (null == token || StringUtils.isEmpty(token.getToken()) || System.currentTimeMillis() > token.getExpiredSince()) {
                    synchronized (tokenLock) {
                        token = tokenInstances.get(configName);
                        if (null == token || StringUtils.isEmpty(token.getToken()) || System.currentTimeMillis() > token.getExpiredSince()) {
                            String result;
                            try {
                                result = HttpUtils.get(WxUrl.AccessTokenUrl.getUrl(appId, appSecret));
                            } catch (Throwable e) {
                                throw new SimplifiedException("获取access_token失败", e);
                            }
                            token = new AccessToken(result);
                            oauthTokenInstances.put(configName, token);
                        }
                    }
                }
                break;
            case OAUTH:
                // 获取网页授权access_token
                token = oauthTokenInstances.get(configName);
                if (null == token || StringUtils.isEmpty(token.getToken()) || System.currentTimeMillis() > token.getExpiredSince()) {
                    synchronized (tokenLockWithCode) {
                        token = oauthTokenInstances.get(configName);
                        if (null == token || StringUtils.isEmpty(token.getToken()) || System.currentTimeMillis() > token.getExpiredSince()) {
                            String result;
                            try {
                                if (StringUtils.isNotEmpty(code)) {
                                    result = HttpUtils.get(WxUrl.OAuthTokenUrl.getUrl(appId, appSecret, code));
                                } else if (null != token && StringUtils.isNotEmpty(token.getRefreshToken()) && System.currentTimeMillis() < (token.getExpiredSince() + 2160000000L)) {
                                    result = HttpUtils.get(WxUrl.RefreshTokenUrl.getUrl(appId, token.getRefreshToken()));
                                } else {
                                    throw new SimplifiedException("获取网页授权access_token失败, 缺少code参数");
                                }
                            } catch (Throwable e) {
                                throw new SimplifiedException("获取网页授权access_token失败", e);
                            }
                            token = new AccessToken(result);
                            oauthTokenInstances.put(configName, token);
                        }
                    }
                }
                break;
            default:
                break;
        }
        Assert.notNull(token, "获取accessToken失败");
        return token;
    }

    /**
     * 获取默认的JSAPI ticket
     *
     * @return jsapi ticket
     */
    public static ApiTicket getDefaultTicket() {
        return getTicket(WxConfigManager.DEFAULT);
    }

    /**
     * 获取指定的JSAPI ticket
     *
     * @param configName 微信配置名称
     * @return jsapi ticket
     */
    public static ApiTicket getTicket(String configName) {
        if (null == ticketInstances) {
            ticketInstances = new ConcurrentHashMap<>();
        }
        ApiTicket ticket = ticketInstances.get(configName);
        if (ticket == null || StringUtils.isEmpty(ticket.getTicket()) || System.currentTimeMillis() > ticket.getExpiredSince()) {
            synchronized (ticketLock) {
                ticket = ticketInstances.get(configName);
                if (ticket == null || StringUtils.isEmpty(ticket.getTicket()) || System.currentTimeMillis() > ticket.getExpiredSince()) {
                    String token = getToken(configName).getToken();
                    String result;
                    try {
                        result = HttpUtils.get(WxUrl.ApiTicketUrl.getUrl(token));
                    } catch (Throwable e) {
                        throw new SimplifiedException("获取access_token失败", e);
                    }
                    ticket = new ApiTicket(result);
                    ticketInstances.put(configName, ticket);
                }
            }
        }
        Assert.notNull(ticket, "获取jsapi ticket失败");
        return ticket;
    }
}