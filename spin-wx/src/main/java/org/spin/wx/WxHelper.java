package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.DigestUtils;
import org.spin.core.util.HexUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.RandomStringUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.http.Http;
import org.spin.wx.base.SubscribeMsgEntity;
import org.spin.wx.base.TmplMsgEntity;
import org.spin.wx.base.WxUrl;
import org.spin.wx.base.WxUserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

/**
 * 微信接口帮助类
 * <p>
 * Created by Arvin on 2016/9/29.
 *
 * @author xuweinan
 */
public class WxHelper {
    private static final Logger logger = LoggerFactory.getLogger(WxHelper.class);

    private static final TypeToken<Map<String, String>> type = new TypeToken<Map<String, String>>() {
    };

    /**
     * 将字符串数组按字典序排序后拼接，计算SHA1值(小写16进制表示)
     *
     * @param param 参数
     * @return sha1签名
     */
    public static String sha1(String... param) {
        StringBuilder sbuilder = new StringBuilder();
        Arrays.stream(param).sorted().forEach(sbuilder::append);
        return DigestUtils.sha1Hex(sbuilder.toString()).toLowerCase();
    }

    /**
     * 将字符串数组按字典序排序后拼接，计算MD5值(大写16进制表示)
     *
     * @param key   key
     * @param param 参数
     * @return md5签名
     */
    public static String md5Sign(String key, String... param) {
        List<String> list = new ArrayList<>();
        if (param.length % 2 != 0)
            throw new IllegalArgumentException("键值对必须为偶数个");
        for (int i = 0; i < param.length; ) {
            list.add(param[i] + "=" + param[i + 1]);
            i += 2;
        }

        StringBuilder sbuilder = new StringBuilder();
        list.stream().sorted().forEach(s -> sbuilder.append("&").append(s));
        sbuilder.append("&key=").append(key);
        return DigestUtils.md5Hex(sbuilder.substring(1, sbuilder.length()));
    }

    public static boolean verifySign(String signature, String token, String timestamp, String nonce) {
        return signature.equals(sha1(token, timestamp, nonce));
    }

    /**
     * 根据code获取微信用户信息
     *
     * @param code       用户同意授权后，带的code参数
     * @param configName 配置名称
     * @return 微信用户信息
     */
    public static WxUserInfo getUserInfo(String code, String... configName) {
        AccessToken access_token = WxTokenManager.getOAuthToken(extractConfigName(configName), code);
        return getUserInfo(access_token.getToken(), access_token.getOpenId());
    }

    /**
     * 获取模板消息的模板ID
     *
     * @param code       短code
     * @param configName 配置名称
     * @return 模板ID
     */
    public static String getTmplId(String code, String... configName) {
        AccessToken accessToken = WxTokenManager.getToken(extractConfigName(configName));
        try {
            String res = Http.POST.withUrl(WxUrl.TMPL_ID.getUrl(accessToken.getToken())).withForm(MapUtils.ofMap("template_id_short", code)).execute();
            Map<String, String> resMap = JsonUtils.fromJson(res, type);
            if (null != resMap && "0".equals(resMap.get("errcode"))) {
                return resMap.get("template_id");
            } else {
                logger.error("getTmplId Returned Message: {}", res);
                throw new SimplifiedException("获取模板ID失败");
            }
        } catch (Exception e) {
            logger.error("获取模板id失败", e);
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "获取模板ID失败");
        }
    }

    /**
     * 发送模板消息
     *
     * @param msg        消息实体
     * @param configName 配置名称
     * @return 发送结果
     */
    public static String postTmplMsg(TmplMsgEntity msg, String... configName) {
        AccessToken accessToken = WxTokenManager.getToken(extractConfigName(configName));
        String res = Http.POST.withUrl(WxUrl.POST_TMPL_MSG.getUrl(accessToken.getToken())).withJsonBody(JsonUtils.toJsonWithUnderscore(msg)).execute();
        Map<String, String> resMap = JsonUtils.fromJson(res, type);
        if (null != resMap && "0".equals(resMap.get("errcode"))) {
            return resMap.get("msgid");
        } else {
            logger.error("发送模板消息失败: {}", res);
            throw new SimplifiedException("发送模板消息失败");
        }
    }

    /**
     * 发送订阅消息
     *
     * @param msg        消息实体
     * @param configName 配置名称
     * @return 发送结果
     */
    public static String sendSubscribeMsg(SubscribeMsgEntity msg, String... configName) {
        AccessToken accessToken = WxTokenManager.getToken(extractConfigName(configName));
        String res = Http.POST.withUrl(WxUrl.SEND_SUBSCRIBE_MESSAGE.getUrl(accessToken.getToken())).withJsonBody(JsonUtils.toJsonWithUnderscore(msg)).execute();
        Map<String, String> resMap = JsonUtils.fromJson(res, type);
        if (null != resMap && "0".equals(resMap.get("errcode"))) {
            return resMap.get("msgid");
        } else {
            logger.error("发送订阅消息失败: {}", res);
            throw new SimplifiedException(StringUtils.render("发送订阅消息失败: To=${touser}, Tmpl=${templateId}", msg));
        }
    }

    /**
     * 获取微信用户信息
     *
     * @param accessToken 网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
     * @param openId      用户的唯一标识
     * @return 微信用户信息
     */
    public static WxUserInfo getUserInfo(String accessToken, String openId) {
        String tmp;
        try {
            tmp = Http.GET.withUrl(WxUrl.USER_INFO.getUrl(accessToken, openId)).execute();
        } catch (Exception e) {
            throw new SimplifiedException("获取用户信息失败");
        }
        logger.debug("获取用户信息: {}", tmp);
        WxUserInfo userInfo = JsonUtils.fromJson(tmp, WxUserInfo.class);
        if (null == userInfo.getOpenid()) {
            throw new SimplifiedException("Can not fetch userInfo use this code" + tmp);
        } else
            return userInfo;
    }

    /**
     * 根据jsapi_ticket对url生成签名
     *
     * @param url        需要调用jsapi的url
     * @param configName 配置名称
     * @return 签名
     */
    public static Map<String, String> signature(String url, String... configName) {
        Map<String, String> ret = new HashMap<>();
        String nonce_str = RandomStringUtils.randomAlphanumeric(16);
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String jsapi_ticket = WxTokenManager.getTicket(extractConfigName(configName)).getTicket();
        String string1 = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonce_str + "&timestamp=" + timestamp + "&url=" + url;
        String signature = HexUtils.encodeHexStringL(DigestUtils.sha1(string1));
        ret.put("url", url);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
        return ret;
    }

    public static String sign4Parameters(SortedMap<String, Object> parameters, String... configName) {

        StringBuilder stringA = new StringBuilder();

        for (String key : parameters.keySet()) {
            stringA.append(key).append("=").append(parameters.get(key)).append("&");
        }

        logger.info("微信签名参数：stringA:" + stringA);

        return DigestUtils.md5Hex(stringA + "key=" + WxConfigManager.getConfig(extractConfigName(configName)).getMchKey()).toUpperCase();
    }

    public static String extractConfigName(String[] args) {
        if (Objects.isNull(args) || args.length == 0) {
            return WxConfigManager.DEFAULT;
        } else if (args.length == 1) {
            return args[0];
        } else {
            throw new SimplifiedException(ErrorCode.INVALID_PARAM, "微信配置名称只能有一个");
        }
    }
}
