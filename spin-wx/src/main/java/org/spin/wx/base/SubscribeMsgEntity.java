package org.spin.wx.base;

/**
 * 微信订阅消息实体
 * <p>Created by xuweinan on 2020/6/22.</p>
 *
 * @author xuweinan
 */
public class SubscribeMsgEntity {

    /**
     * 接收者（用户）的 openid
     */
    private String touser;

    /**
     * 所需下发的订阅模板id
     */
    private String templateId;

    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
     */
    private String page;

    /**
     * 模板内容
     */
    private MessageData data;

    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    private String miniprogramState;

    /**
     * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    private String lang;

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public MessageData getData() {
        return data;
    }

    public void setData(MessageData data) {
        this.data = data;
    }

    public String getMiniprogramState() {
        return miniprogramState;
    }

    public void setMiniprogramState(String miniprogramState) {
        this.miniprogramState = miniprogramState;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
