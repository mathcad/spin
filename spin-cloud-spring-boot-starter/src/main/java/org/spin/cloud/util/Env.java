package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.spin.core.util.StringUtils;
import org.spin.core.util.Util;
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
public final class Env extends Util {
    private static final String APP_NAME = "spring.application.name";

    private static Environment environment;
    private static String activeProfiles;
    private static String appName;

    private static final ThreadLocal<String> CURRENT_API_CODE = new ThreadLocal<>();

    static {
        Util.registerLatch(Env.class);
    }

    public static void init(Environment environment) {
        Env.environment = environment;
        activeProfiles = StringUtils.join(environment.getActiveProfiles(), ',').toUpperCase();
        appName = environment.getProperty(APP_NAME);
        Util.ready(Env.class);
    }

    public static boolean isDev() {
        Util.awaitUntilReady(Env.class);
        return activeProfiles.startsWith("DEV");
    }

    public static boolean isTest() {
        Util.awaitUntilReady(Env.class);
        return activeProfiles.startsWith("FAT");
    }

    public static boolean isBeta() {
        Util.awaitUntilReady(Env.class);
        return activeProfiles.startsWith("UAT");
    }

    public static boolean isProd() {
        Util.awaitUntilReady(Env.class);
        return activeProfiles.startsWith("PRO");
    }

    public static String getActiveProfiles() {
        Util.awaitUntilReady(Env.class);
        return activeProfiles;
    }

    public static String getAppName() {
        Util.awaitUntilReady(Env.class);
        return appName;
    }

    public static String getVersion() {
        Util.awaitUntilReady(Env.class);
        return environment.getProperty("app.version");
    }

    public static String getProperty(String key) {
        Util.awaitUntilReady(Env.class);
        return environment.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        Util.awaitUntilReady(Env.class);
        return environment.getProperty(key, defaultValue);
    }

    public static String getCurrentApiCode() {
        Util.awaitUntilReady(Env.class);
        return CURRENT_API_CODE.get();
    }

    public static void setCurrentApiCode(String apiCode) {
        Util.awaitUntilReady(Env.class);
        CURRENT_API_CODE.set(apiCode);
    }
}
