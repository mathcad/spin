package org.infrastructure.wx;

import org.infrastructure.security.Hex;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.DigestUtils;
import org.infrastructure.util.HttpUtils;
import org.infrastructure.util.JSONUtils;
import org.infrastructure.util.RandomStringUtils;
import org.infrastructure.wx.wx.base.WxUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 将字符串数组按字典序排序后拼接，计算SHA1值(小写16进制表示)
     */
    public static String sha1(String... param) {
        StringBuilder sbuilder = new StringBuilder();
        Arrays.stream(param).sorted().forEach(sbuilder::append);
        return DigestUtils.sha1Hex(sbuilder.toString()).toLowerCase();
    }

    /**
     * 将字符串数组按字典序排序后拼接，计算MD5值(大写16进制表示)
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
//        Arrays.stream(param).sorted().forEach(sbuilder::append);
        sbuilder.append("&key=").append(key);
        return DigestUtils.md5Hex(sbuilder.substring(1, sbuilder.length()));
    }

    /**
     * 验证消息签名
     */
    public static boolean verifySign(String signature, String token, String timestamp, String nonce) {
        return signature.equals(sha1(token, timestamp, nonce));
    }

    /**
     * 根据code获取微信用户信息
     */
    public static WxUserInfo getUserInfo(String code) {
        AccessToken access_token = AccessToken.getDefaultInstance(code);
        return getUserInfo(access_token.getToken(), access_token.getOpenId());
    }

    /**
     * 获取微信用户信息
     */
    public static WxUserInfo getUserInfo(String accessToken, String openId) {
        String tmp;
        try {
            tmp = HttpUtils.httpGetRequest("https://api.weixin.qq.com/sns/userinfo?access_token={}&openid={}&lang=zh_CN", accessToken, openId);
        } catch (Exception e) {
            throw new SimplifiedException("获取用户信息失败");
        }
        logger.debug("获取用户信息: {}", tmp);
        WxUserInfo userInfo = JSONUtils.fromJson(tmp, WxUserInfo.class);
        if (null == userInfo.getOpenid()) {
            throw new SimplifiedException("Can not fetch userInfo use this code" + tmp);
        } else
            return userInfo;
    }

    /**
     * 根据jsapi_ticket对url生成签名
     */
    public static Map<String, String> signature(String url) {
        Map<String, String> ret = new HashMap<>();
        String nonce_str = RandomStringUtils.randomAlphanumeric(16);
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String jsapi_ticket = ApiTicket.getInstance().getTicket();
        String string1 = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonce_str + "&timestamp=" + timestamp + "&url=" + url;
        String signature = Hex.encodeHexStringL(DigestUtils.sha1(string1));
        ret.put("url", url);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
        return ret;
    }

    public static String sign4Parameters(SortedMap<String, Object> parameters) {

        String stringA = "";

        for (String key : parameters.keySet()) {
            stringA += key + "=" + parameters.get(key) + "&";
        }

        logger.info("微信签名参数：stringA:" + stringA);

        return DigestUtils.md5Hex(stringA + "key=" + WxConfig.mchKey).toUpperCase();
    }

    /**
     * 转换微信传入的时间
     *
     * @param date yyyyMMddHHmmss格式的时间
     */
    public static Date transDateForWxDate(String date) {

        SimpleDateFormat fullDay = new SimpleDateFormat("yyyyMMddHHmmss");

        Date d = null;
        try {
            d = fullDay.parse(date);
        } catch (ParseException e) {
            logger.error("日期转换错误", e);
        }
        return d;
    }
}