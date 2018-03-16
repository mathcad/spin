package org.spin.wx;

import org.spin.core.throwable.SimplifiedException;
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

    private WxConfigManager() {
    }

    private static final Map<String, WxConfigInfo> infos = new HashMap<>();

    public static String DEFAULT = "default";

    public static WxConfigInfo getConfig(String name) {
        WxConfigInfo info = infos.get(name);
        if (null == info) {
            throw new SimplifiedException("There is no specified wx config");
        }
        return info;
    }

    public static void putConfig(String name, WxConfigInfo configInfo) {
        if (Objects.nonNull(configInfo)) {
            infos.put(name, configInfo);
        }
    }

    public static void putConfig(Map<String, WxConfigInfo> infos) {
        if (Objects.nonNull(infos)) {
            WxConfigManager.infos.putAll(infos);
        }
    }
}
