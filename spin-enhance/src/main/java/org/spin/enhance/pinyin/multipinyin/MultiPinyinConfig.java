package org.spin.enhance.pinyin.multipinyin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多音字配置,可外挂
 * Created by 刘一波 on 16/3/4.
 * E-Mail:yibo.liu@tqmall.com
 */
public final class MultiPinyinConfig {
    /**
     * 外挂多音字路径,可以指定协议，默认为文件系统路径（file://或classpath://）
     */
    private static final Map<String, Boolean> MULTI_PINYIN_PATH = new LinkedHashMap<String, Boolean>();

    public static void addMultiPinyinPath(String path) {
        if (!MULTI_PINYIN_PATH.containsKey(path)) {
            MULTI_PINYIN_PATH.put(path, false);
        }
    }

    public static Map<String, Boolean> getMultiPinyinPath() {
        return MULTI_PINYIN_PATH;
    }
}
