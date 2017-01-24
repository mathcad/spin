package org.spin.cache;

import java.util.Set;

/**
 * 缓存接口
 *
 * @author xuweinan
 * @version 1.0
 */
public interface Cache<V> {

    /**
     * 获取所有满足pattern的Key
     */
    Set<String> getKeys(final String pattern);

    /**
     * 获取所有满足pattern的缓存对象
     */
    Set<V> getValues(final String pattern);

    /**
     * 设置缓存对象的过期时间
     */
    void expire(final String key, final long seconds);

    /**
     * 存入缓存
     */
    void put(final String key, final V value);

    /**
     * 存入对象，并设置期限
     */
    void put(final String key, final V value, final Long expire);

    /**
     * 获取对象
     */
    V get(final String key);

    /**
     * 更新对象
     */
    void update(final String key, final V value);

    /**
     * 更新对象，并设置缓存有效期(秒)
     */
    void update(final String key, final V object, final Long expireSec);

    /**
     * 删除缓存
     */
    void delete(String... keys);

    /**
     * 获取数据库大小
     */
    Long getDBSize();

    /**
     * 清空数据库
     */
    void clearDB();
}