package org.spin.sys;

import org.spin.util.BeanUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 框架缓存
 * <p>集中管理缓存，可以集中清空，以应对热加载后的不一致问题</p>
 * Created by xuweinan on 2016/9/5.
 *
 * @author xuweinan
 */
public final class EnvCache {
    public static final Map<String, Map<String, BeanUtils.PropertyDescriptorWrapper>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Field>> BEAN_FIELDS = new ConcurrentHashMap<>();
    public static final Map<String, List<Integer>> CHECKED_METHOD_PARAM = new ConcurrentHashMap<>();

    /** 实体中*ToOne字段列表缓存 */
    public static final Map<String, Map<String, Field>> ENTITY_SOMETOONE_JOIN_FIELDS = new ConcurrentHashMap<>();

    /** 实体对应列名列表缓存 */
    public static final Map<String, Set<String>> ENTITY_COLUMNS = new ConcurrentHashMap<>();

    /** 线程绑定的全局公用属性 */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_PARAMETERS = new ThreadLocal<>();

    /** 系统是否启用了开发模式 */
    public static boolean devMode = false;

    /** 系统是否启用了Shiro支持 */
    public static boolean shiroEnabled = false;

    /** token过期时间 */
    public static Long TokenExpireTime = 7200000L;

    /** key过期时间 */
    public static Long KeyExpireTime = 1296000000L;

    /** 文件上传路径 */
    public static String FileUploadDir;

    private EnvCache() {
    }

    public static void putLocalParam(String key, Object param) {
        if (null == THREAD_LOCAL_PARAMETERS.get())
            THREAD_LOCAL_PARAMETERS.set(new HashMap<>());
        THREAD_LOCAL_PARAMETERS.get().put(key, param);
    }

    public static Object getLocalParam(String key) {
        return THREAD_LOCAL_PARAMETERS.get() == null ? null : EnvCache.THREAD_LOCAL_PARAMETERS.get().get(key);
    }

    public static Object removeLocalParam(String key) {
        return THREAD_LOCAL_PARAMETERS.get() == null ? null : EnvCache.THREAD_LOCAL_PARAMETERS.get().remove(key);
    }

    public synchronized static void clearCache() {
        ENTITY_SOMETOONE_JOIN_FIELDS.clear();
        CLASS_PROPERTY_CACHE.clear();
        BEAN_FIELDS.clear();
    }
}
