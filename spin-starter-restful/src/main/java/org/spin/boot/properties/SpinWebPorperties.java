package org.spin.boot.properties;

import org.spin.core.SpinContext;
import org.spin.core.util.SystemUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * <p>Created by xuweinan on 2017/11/8.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.web")
public class SpinWebPorperties {
    private String restfulPrefix;
    private String fileUploadDir = SystemUtils.USER_HOME + "/upload/files";
    private Long maxUploadSize = 5L;
    private Boolean devMode = false;

    @PostConstruct
    public void init() {
        SpinContext.FILE_UPLOAD_DIR = fileUploadDir;
        SpinContext.DEV_MODE = devMode;
    }

    public String getRestfulPrefix() {
        return restfulPrefix;
    }

    public void setRestfulPrefix(String restfulPrefix) {
        this.restfulPrefix = restfulPrefix;
    }

    public String getFileUploadDir() {
        return fileUploadDir;
    }

    public void setFileUploadDir(String fileUploadDir) {
        this.fileUploadDir = fileUploadDir;
    }

    public Long getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(Long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public Boolean getDevMode() {
        return devMode;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }
}
