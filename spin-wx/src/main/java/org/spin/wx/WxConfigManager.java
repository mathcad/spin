package org.spin.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;
import org.spin.wx.base.WxConfigInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 微信配置管理类
 * <p>Created by xuweinan on 2016/9/28.</p>
 *
 * @author xuweinan
 */
public class WxConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(WxConfigManager.class);

    private WxConfigManager() {
    }

    private static final Map<String, WxConfigInfo> infos = new HashMap<>();
    private static final Map<String, String> alias = new HashMap<>();

    public static String DEFAULT = "default";

    public static String parseAlias(String name) {
        if (infos.containsKey(Assert.notNull(name, "There is no specified wx config"))) {
            return name;
        }
        if (alias.containsKey(name)) {
            logger.info("Wechat Config {} alias to {}", name, alias.get(name));
            return alias.get(name);
        }
        throw new AssertFailException("There is no specified wx config");
    }

    public static WxConfigInfo getConfig(String name) {
        String actual = Assert.notNull(name, "There is no specified wx config");
        WxConfigInfo info = infos.get(actual);
        if (null == info) {
            throw new SimplifiedException("There is no specified wx config");
        }
        return info;
    }

    public static void putConfig(String name, WxConfigInfo configInfo) {
        Assert.notBlank(name, "微信配置名称不能为空");
        if (Objects.nonNull(configInfo)) {
            infos.put(name, configInfo);
        }
    }

    public static void putConfig(Map<String, WxConfigInfo> infos) {
        if (Objects.nonNull(infos)) {
            infos.forEach(WxConfigManager::putConfig);
        }
    }

    public static void putAlias(String aliasName, String actualName) {
        if (StringUtils.isNotEmpty(aliasName) && StringUtils.isNotEmpty(actualName)) {
            alias.put(aliasName, actualName);
        }
    }

    public static void putAlias(Map<String, String> aliasMap) {
        if (Objects.nonNull(aliasMap)) {
            aliasMap.forEach(WxConfigManager::putAlias);
        }
    }

    public static void clear() {
        WxConfigManager.infos.clear();
        WxConfigManager.alias.clear();
    }
}
