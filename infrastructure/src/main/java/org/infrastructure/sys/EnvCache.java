package org.infrastructure.sys;

import org.infrastructure.util.BeanUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 框架缓存
 * <p>集中管理缓存，可以集中清空，以应对热加载后的不一致问题</p>
 * Created by xuweinan on 2016/9/5.
 * @author xuweinan
 */
public final class EnvCache {
    public static final Map<String, Map<String, Field>> REFER_JOIN_FIELDS = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, BeanUtils.PropertyDescriptorWrapper>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();

    private EnvCache() {
    }

    public static void clearCache() {
        REFER_JOIN_FIELDS.clear();
        CLASS_PROPERTY_CACHE.clear();
    }
}