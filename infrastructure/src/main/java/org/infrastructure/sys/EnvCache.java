package org.infrastructure.sys;

import org.infrastructure.util.BeanUtils;

import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
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
    public static final Map<String, Map<String, Field>> INSTANT_FIELDS = new ConcurrentHashMap<>();
    public static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_PARAMETERS = new ThreadLocal<>();
    public static final Map<String, List<Integer>> CHECKED_METHOD_PARAM = new ConcurrentHashMap<>();

    /** 实体对应列名列表缓存 */
    public static final Map<String, List<String>> ENTITY_COLUMNS = new ConcurrentHashMap<>();

    /** 对订单付款行为进行同步控制 */
    public static final Map<Long, Long> PAY_SYNC_LOCK = new ConcurrentHashMap<>();

    public static final Map<String, TokenInfo> TOKEN_INFO_CACHE = new ConcurrentHashMap<>();
    public static final Map<Long, String> USERID_TOKEN_CACHE = new ConcurrentHashMap<>();
    public static final Map<String, Long> KEY_USERID_CACHE = new ConcurrentHashMap<>();
    public static final Map<Long, String> USERID_KEY_CACHE = new ConcurrentHashMap<>();

    public static PublicKey RSA_PUBKEY;
    public static PrivateKey RSA_PRIKEY;
    public static boolean devMode;
    public static Long TokenExpireTime;
    public static String FileUploadDir;
    public static String TemplateDir;

    private EnvCache() {
    }

    public synchronized static void clearCache() {
        REFER_JOIN_FIELDS.clear();
        CLASS_PROPERTY_CACHE.clear();
        INSTANT_FIELDS.clear();
    }
}