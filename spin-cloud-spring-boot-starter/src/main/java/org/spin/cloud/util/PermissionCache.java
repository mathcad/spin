package org.spin.cloud.util;

import org.spin.cloud.annotation.UtilClass;
import org.spin.core.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

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
public abstract class PermissionCache {

    private static final String FIELD_PERMISSION_CACHE_KEY = "ALL_FIELD_PERMISSION";

    private static StringRedisTemplate redisTemplate;

    public static void init(StringRedisTemplate redisTemplate) {
        PermissionCache.redisTemplate = redisTemplate;
    }


    /**
     * 获取指定API接口上的字段权限定义
     *
     * @param apiCode api编码
     * @return 需要控制的字段列表
     */
    public static Set<String> getFieldPermDefByApi(String apiCode) {
        return StringUtils.splitToSet(redisTemplate.<String, String>opsForHash().get(FIELD_PERMISSION_CACHE_KEY, apiCode), ",");
    }
}
