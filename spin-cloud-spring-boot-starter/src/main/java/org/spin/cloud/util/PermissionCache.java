package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.spin.cloud.vo.CurrentUser;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.Util;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public final class PermissionCache extends Util {
    public static final TypeToken<Map<String, String>> STRING_MAP_TYPE_TOKEN = new TypeToken<Map<String, String>>() {
    };
    private static final String FIELD_PERMISSION_CACHE_KEY = "ALL_FIELD_PERMISSION";

    private static StringRedisTemplate redisTemplate;

    public static void init(StringRedisTemplate redisTemplate) {
        PermissionCache.redisTemplate = redisTemplate;
        Util.ready(PermissionCache.class);
    }

    static {
        Util.registerLatch(PermissionCache.class);
    }

    /**
     * 获取指定编码的字段权限定义
     *
     * @param fieldPermCode 字段权限编码
     * @return 需要控制的字段列表(fieldKey - fieldName)
     */
    public static Map<String, String> getFieldPermDefByCode(String fieldPermCode) {
        if (StringUtils.isBlank(fieldPermCode)) {
            return Collections.emptyMap();
        }

        Util.awaitUntilReady(PermissionCache.class);
        return Optional.ofNullable(JsonUtils.fromJson(StringUtils.trimToNull(redisTemplate.<String, String>opsForHash()
            .get(FIELD_PERMISSION_CACHE_KEY, fieldPermCode)), STRING_MAP_TYPE_TOKEN))
            .orElse(Collections.emptyMap());
    }

    /**
     * 获取指定API接口上的字段权限定义
     *
     * @param apiCode api编码
     * @return 需要控制的字段列表(fieldKey - fieldName)
     */
    public static Map<String, String> getFieldPermDefByApi(String apiCode) {
        if (StringUtils.isBlank(apiCode)) {
            return Collections.emptyMap();
        }

        String fieldPermCode = "FIELD" + apiCode.substring(3);
        Util.awaitUntilReady(PermissionCache.class);
        return Optional.ofNullable(JsonUtils.fromJson(StringUtils.trimToNull(redisTemplate.<String, String>opsForHash()
            .get(FIELD_PERMISSION_CACHE_KEY, fieldPermCode)), STRING_MAP_TYPE_TOKEN))
            .orElse(Collections.emptyMap());
    }

    /**
     * 获取当前用户在指定api上需要清洗的字段列表
     *
     * @param fieldPermCode 字段权限编码
     * @return 需要清洗的字段列表
     */
    public static Map<String, String> getFieldsToClean(String fieldPermCode) {
        CurrentUser current = CurrentUser.getCurrent();

        // 超级管理员不控制
        if (null != current && current.isSuperAdmin()) {
            return Collections.emptyMap();
        }

        Util.awaitUntilReady(PermissionCache.class);
        // 获取定义
        Map<String, String> allFiedls = getFieldPermDefByCode(fieldPermCode);

        if (allFiedls.isEmpty()) {
            return allFiedls;
        }


        // 如果为null，没有任何字段权限
        if (null == current) {
            return allFiedls;
        }

        // 获取用户在接口上的字段权限
        Set<String> fieldPermInfo = current.getFieldPermInfo(fieldPermCode);

        if (null == fieldPermInfo) {
            // null表示无需控制，拥有所有权限
            return Collections.emptyMap();
        } else {
            // 计算需要清洗的字段
            Map<String, String> toClean = new HashMap<>();
            fieldPermInfo.forEach(it -> {
                if (allFiedls.containsKey(it)) {
                    toClean.put(it, allFiedls.get(it));
                }
            });
            return toClean;
        }
    }
}
