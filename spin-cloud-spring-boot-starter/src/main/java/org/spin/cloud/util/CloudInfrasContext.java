package org.spin.cloud.util;

import org.spin.core.collection.Pair;

import java.util.Map;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class CloudInfrasContext {
    private static final ThreadLocal<Pair<String, Map<String, String>>> CUSTOMIZE_ROUTE = new ThreadLocal<>();

    private static final ThreadLocal<Pair<String, Map<String, String>>> GRAY_INFO = new ThreadLocal<>();

    private static final ThreadLocal<String> IDEMPOTENT_INFO = new ThreadLocal<>();

    public static Pair<String, Map<String, String>> getCustomizeRoute() {
        return CUSTOMIZE_ROUTE.get();
    }

    public static void setCustomizeRoute(Pair<String, Map<String, String>> customizeRoute) {
        CUSTOMIZE_ROUTE.set(customizeRoute);
    }

    public static void removeCustomizeRoute() {
        CUSTOMIZE_ROUTE.remove();
    }

    public static Pair<String, Map<String, String>> getGrayInfo() {
        return GRAY_INFO.get();
    }

    public static void setGrayInfo(Pair<String, Map<String, String>> grayInfo) {
        GRAY_INFO.set(grayInfo);
    }

    public static void removeGrayInfo() {
        GRAY_INFO.remove();
    }

    public static String getIdempotentInfo() {
        return IDEMPOTENT_INFO.get();
    }

    public static void setIdempotentInfo(String idempotentInfo) {
        IDEMPOTENT_INFO.set(idempotentInfo);
    }

    public static void removeIdempotentInfo() {
        IDEMPOTENT_INFO.remove();
    }
}
