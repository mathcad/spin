package org.spin.cloud.vo;

import org.spin.core.Assert;
import org.spin.core.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class SmsTemplateVariableVo implements Serializable {

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
     * 模板参数, 包括手机号码等参数,每个对象代表一个独立的接收者
     */
    private List<VariableParamVo> params = new LinkedList<>();

    public static SmsTemplateVariableVo aInstance() {
        return new SmsTemplateVariableVo();
    }

    public SmsTemplateVariableVo withTemplateCode(String templateCode) {
        this.templateCode = templateCode;
        return this;
    }

    public SmsTemplateVariableVo withReport(Boolean report) {
        this.report = report;
        return this;
    }

    public SmsTemplateVariableVo withSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
        return this;
    }

    public SmsTemplateVariableVo withParams(List<VariableParamVo> params) {
        this.params = Assert.notNull(params, "发送参数不能为null");
        return this;
    }

    public SmsTemplateVariableVo addParam(VariableParamVo... params) {
        if (null != params && params.length > 0) {
            CollectionUtils.mergeArrayIntoCollection(params, this.params);
        }
        return this;
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

    public List<VariableParamVo> getParams() {
        return params;
    }

    public void setParams(List<VariableParamVo> params) {
        this.params = params;
    }
}
