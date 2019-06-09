package org.spin.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description swagger2 properties
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/15.</p>
 */
@ConfigurationProperties(prefix = "swagger2")
public class Swagger2Properties {

    /**
     * 是否开启Swagger2
     */
    private boolean enable;

    /**
     * 扫描包
     */
    private String basePackage;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 创建人
     */
    private String contactName;

    private String contactUrl;

    private String contactEmail;

    /**
     * 版本号
     */
    private String version;

    /**
     * 描述
     */
    private String description;

    /**
     * 服务URL
     */
    private String serviceUrl;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceUrl() {
        return this.serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}
