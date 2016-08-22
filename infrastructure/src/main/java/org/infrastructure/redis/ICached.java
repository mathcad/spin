package org.infrastructure.redis;

import java.util.Set;

/**
 * 缓存接口
 *
 * @author xuweinan
 * @version 1.0
 */
public interface ICached<V> {

    /**
     * 得到所有缓存的Key
     */
    Set<V> getKeys(final String pattern) throws Exception;

    /**
     * 设置缓存对象的过期时间
     */
    void setExpire(final String key, final long seconds);

    /**
     * 存入缓存
     */
    void put(final String key, final Object value);

    /**
     * 存入对象，并设置期限
     */
    void put(final String key, final Object value, final Long expire);

    /**
     * 获取对象
     */
    Object get(final String key);

    /**
     * 更新对象
     */
    void update(final String key, final Object value);

    /**
     * 删除缓存
     */
    void deleteCached(String... keys) throws Exception;
}