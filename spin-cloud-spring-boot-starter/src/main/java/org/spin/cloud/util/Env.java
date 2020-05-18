package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.spin.core.util.StringUtils;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * 环境信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/12/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public abstract class Env {
    private static final String ACTIVE_PROFILE = "spring.profiles.active";
    private static final String APP_NAME = "spring.application.name";

    private static Environment environment;
    private static Set<String> activeProfiles;
    private static String appName;

    private static final ThreadLocal<String> CURRENT_API_CODE = new ThreadLocal<>();

    public static void init(Environment environment) {
        Env.environment = environment;
        activeProfiles = StringUtils.splitToSet(StringUtils.trimToEmpty(environment.getProperty(ACTIVE_PROFILE)).toLowerCase(), ",");
        appName = environment.getProperty(APP_NAME);
    }

    public static boolean isDev() {
        return activeProfiles.contains("dev");
    }

    public static boolean isTest() {
        return activeProfiles.contains("fat");
    }

    public static boolean isBeta() {
        return activeProfiles.contains("uat");
    }

    public static boolean isProd() {
        return activeProfiles.contains("pro");
    }

    public static Set<String> getActiveProfiles() {
        return activeProfiles;
    }

    public static String getAppName() {
        return appName;
    }

    public static String getVersion() {
        return environment.getProperty("app.version");
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    public static String getCurrentApiCode() {
        return CURRENT_API_CODE.get();
    }

    public static void setCurrentApiCode(String apiCode) {
        CURRENT_API_CODE.set(apiCode);
    }
}
