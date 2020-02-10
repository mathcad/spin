package org.spin.common.util;

import org.spin.common.annotation.UtilClass;
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

    private static String activeProfile;

    public static void init(Environment environment) {
        activeProfile = environment.getProperty(ACTIVE_PROFILE);
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
}
