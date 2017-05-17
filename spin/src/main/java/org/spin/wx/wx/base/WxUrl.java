package org.spin.wx.wx.base;

import org.spin.core.util.StringUtils;

/**
 * 微信接口url
 * <p>Created by xuweinan on 2017/5/16.</p>
 *
 * @author xuweinan
 */
public enum WxUrl {
    /**
     * 获取普通access_token
     */
    AccessTokenUrl("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}"),

    /**
     * 获取网页授权access_token
     */
    OAuthTokenUrl("https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code"),

    /**
     * 通过refresh_token刷新网页授权access_tokne
     */
    RefreshTokenUrl("https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={}&grant_type=refresh_token&refresh_token={}"),

    /**
     * 获取微信用户基本信息
     */
    UserInfoUrl("https://api.weixin.qq.com/sns/userinfo?access_token={}&openid={}&lang=zh_CN"),

    /**
     * 获取jssdk的api_ticket
     */
    ApiTicketUrl("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={}&type=jsapi"),

    /**
     * 获取模板ID
     */
    TmplIdUrl("https://api.weixin.qq.com/cgi-bin/template/api_add_template?access_token={}"),

    /**
     * 发送模板消息
     */
    PostTmplMsgUrl("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={}"),

    /**
     * 获取微信服务器地址
     */
    WxServerAddr("https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token={}");

    String url;

    WxUrl(String url) {
        this.url = url;
    }

    public String getUrl(String... params) {
        return StringUtils.plainFormat(url, params);
    }
}
