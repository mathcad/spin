package org.spin.web;

import org.spin.core.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 内部访问白名单
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class InternalWhiteList {
    private static final Set<String> whiteList = new HashSet<>();

    public static void refreshList(String hosts) {
        synchronized (whiteList) {
            whiteList.clear();
            Arrays.stream(StringUtils.trimToEmpty(hosts).split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::toLowerCase)
                .forEach(whiteList::add);
        }
    }

    public static Set<String> getWhiteList() {
        return whiteList;
    }

    public static boolean contains(String host) {
        return whiteList.contains(StringUtils.trimToEmpty(host).toLowerCase());
    }

    public static boolean containsOne(String... hosts) {
        for (String host : hosts) {
            if (whiteList.contains(StringUtils.trimToEmpty(host).toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
