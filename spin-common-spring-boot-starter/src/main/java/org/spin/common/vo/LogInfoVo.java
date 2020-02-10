package org.spin.common.vo;

import org.spin.core.gson.annotation.PreventOverflow;

import java.time.LocalDateTime;

/**
 * 日志VO
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/12/26</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LogInfoVo {

    /**
     * appName
     */
    private String appName;

    /**
     * 用户ID
     */
    @PreventOverflow
    private Long userId;

    /**
     * 用户姓名
     */
    private String realName;

    /**
     * 企业ID
     */
    @PreventOverflow
    private Long enterpriseId;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 访问IP
     */
    private String accessIp;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作行为
     */
    private String operation;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getAccessIp() {
        return accessIp;
    }

    public void setAccessIp(String accessIp) {
        this.accessIp = accessIp;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
}
