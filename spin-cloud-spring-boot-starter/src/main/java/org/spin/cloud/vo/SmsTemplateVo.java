package org.spin.cloud.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SmsTemplateVo implements Serializable {

    private static final long serialVersionUID = 8510216555815564281L;

    /**
     * 需要发送的手机号,多个用英文逗号隔开
     */
    private String phone;

    /**
     * 模板CODE
     */
    private String templateCode;

    /**
     * 是否需要状态报告
     */
    private Boolean report = false;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime = LocalDateTime.now();

    /**
     * 模板参数
     */
    private Map<String, Object> param;

    public static SmsTemplateVo aSmsTemplateVo(String phone) {
        return new SmsTemplateVo().withPhone(phone);
    }

    private SmsTemplateVo withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public SmsTemplateVo withTemplateCode(String templateCode) {
        this.templateCode = templateCode;
        return this;
    }

    public SmsTemplateVo withReport(Boolean report) {
        this.report = report;
        return this;
    }

    public SmsTemplateVo withSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
        return this;
    }

    public SmsTemplateVo withParams(Map<String, Object> param) {
        this.param = param;
        return this;
    }

    public SmsTemplateVo withParam(String key, Object value) {
        if (null == param) {
            param = new HashMap<>();
        }
        param.put(key, value);
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Boolean getReport() {
        return report;
    }

    public void setReport(Boolean report) {
        this.report = report;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

}
