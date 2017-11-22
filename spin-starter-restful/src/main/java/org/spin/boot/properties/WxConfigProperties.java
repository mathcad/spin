package org.spin.boot.properties;

import org.spin.core.util.StringUtils;
import org.spin.wx.WxConfigInfo;
import org.spin.wx.WxConfigManager;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 微信配置properties
 * <p> Created by xuweinan on 2016/9/28.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.wx")
public class WxConfigProperties {
    private Map<String, WxConfigInfo> config;
    private String defaultConfig;

    public Map<String, WxConfigInfo> getConfig() {
        return config;
    }

    public void setConfig(Map<String, WxConfigInfo> config) {
        this.config = config;
    }

    public String getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(String defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    @PostConstruct
    public void init() {
        WxConfigManager.putConfig(config);
        if (StringUtils.isNotEmpty(defaultConfig)) {
            WxConfigManager.DEFAULT = defaultConfig;
        }
    }
}
