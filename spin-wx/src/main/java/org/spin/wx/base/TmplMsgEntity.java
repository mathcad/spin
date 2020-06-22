package org.spin.wx.base;

import java.util.Map;

/**
 * 微信模板消息实体
 * <p>Created by xuweinan on 2017/5/16.</p>
 *
 * @author xuweinan
 */
public class TmplMsgEntity {

    /**
     * 发送目标OPENID
     */
    private String touser;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * url
     */
    private String url;

    /**
     * 模板数据
     */
    private Map<String, ?> data;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, ?> getData() {
        return data;
    }

    public void setData(Map<String, ?> data) {
        this.data = data;
    }
}
