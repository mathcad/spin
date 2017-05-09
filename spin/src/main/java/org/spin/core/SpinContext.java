package org.spin.core;

import org.spin.core.util.BeanUtils;

import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文缓存及全局变量
 * <p>集中管理缓存与常量，可以集中清空，以应对热加载后的不一致问题</p>
 * Created by xuweinan on 2016/9/5.
 *
 * @author xuweinan
 */
public final class SpinContext {
    public static final Map<String, Map<String, BeanUtils.PropertyDescriptorWrapper>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Field>> BEAN_FIELDS = new ConcurrentHashMap<>();

    /** Needed注解检查的方法参数缓存 */
    public static final Map<String, List<Integer>> CHECKED_METHOD_PARAM = new ConcurrentHashMap<>();

    /** 实体中*ToOne字段列表缓存 */
    public static final Map<String, Map<String, Field>> ENTITY_SOMETOONE_JOIN_FIELDS = new ConcurrentHashMap<>();

    /** 实体对应列名列表缓存 */
    public static final Map<String, Set<String>> ENTITY_COLUMNS = new ConcurrentHashMap<>();

    /** 线程绑定的全局公用属性 */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_PARAMETERS = ThreadLocal.withInitial(HashMap::new);

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

    /** RSA公钥 */
    public static PublicKey RSA_PUBKEY;

    /** RSA私钥 */
    public static PrivateKey RSA_PRIKEY;

    private SpinContext() {
    }

    /**
     * 注册与当前前程绑定的参数，如果参数已存在，则覆盖
     *
     * @param key 参数名
     * @param param 参数值
     */
    public static void putLocalParam(String key, Object param) {
        THREAD_LOCAL_PARAMETERS.get().put(key, param);
    }

    /**
     * 获取与当前线程绑定的参数
     *
     * @param key 参数名
     * @return 参数值，不存在则为null
     */
    public static Object getLocalParam(String key) {
        return SpinContext.THREAD_LOCAL_PARAMETERS.get().get(key);
    }

    /**
     * 移除与当前线程绑定的参数
     *
     * @param key 参数名
     * @return 被移除的参数值(不存在则为null)
     */
    public static Object removeLocalParam(String key) {
        return SpinContext.THREAD_LOCAL_PARAMETERS.get().remove(key);
    }

    public synchronized static void clearCache() {
        ENTITY_SOMETOONE_JOIN_FIELDS.clear();
        CLASS_PROPERTY_CACHE.clear();
        BEAN_FIELDS.clear();
        CHECKED_METHOD_PARAM.clear();
    }
}
