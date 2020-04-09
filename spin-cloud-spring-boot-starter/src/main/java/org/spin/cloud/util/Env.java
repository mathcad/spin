package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.springframework.core.env.Environment;

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
    private static String activeProfile;
    private static String appName;

    private static final ThreadLocal<String> CURRENT_API_CODE = new ThreadLocal<>();

    public static void init(Environment environment) {
        Env.environment = environment;
        activeProfile = environment.getProperty(ACTIVE_PROFILE);
        appName = environment.getProperty(APP_NAME);
    }

    public static boolean isDev() {
        return activeProfile.toLowerCase().contains("dev");
    }

    public static boolean isTest() {
        return activeProfile.toLowerCase().contains("fat");
    }

    public static boolean isBeta() {
        return activeProfile.toLowerCase().contains("uat");
    }

    public static boolean isProd() {
        return activeProfile.toLowerCase().contains("pro");
    }

    public static String getActiveProfile() {
        return activeProfile;
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
