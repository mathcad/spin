package org.spin.data.cache;

import java.util.Set;

/**
 * 缓存接口
 * <p>Created by xuweinan on 2016/8/22.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface Cache<V> {

    /**
     * 获取所有满足pattern的Key
     *
     * @param pattern key的pattern
     * @return key的集合
     */
    Set<String> getKeys(final String pattern);

    /**
     * 获取所有满足pattern的缓存对象
     *
     * @param pattern key的pattern
     * @return 缓存对象集合
     */
    Set<V> getValues(final String pattern);

    /**
     * 设置缓存对象的过期时间
     *
     * @param key     对象key
     * @param seconds 超时时间
     */
    void expire(final String key, final long seconds);

    /**
     * 存入缓存
     *
     * @param key   对象key
     * @param value 对象
     */
    void put(final String key, final V value);

    /**
     * 存入对象，并设置期限
     *
     * @param key    对象key
     * @param value  对象
     * @param expire 超时时间
     */
    void put(final String key, final V value, final Long expire);

    /**
     * 获取对象
     *
     * @param key 对象key
     * @return 缓存对象
     */
    V get(final String key);

    /**
     * 更新对象
     *
     * @param key   对象key
     * @param value 对象
     */
    void update(final String key, final V value);

    /**
     * 更新对象，并设置缓存有效期(秒)
     *
     * @param key       对象key
     * @param value     对象
     * @param expireSec 有效期
     */
    void update(final String key, final V value, final Long expireSec);

    /**
     * 删除缓存
     *
     * @param keys 对象key
     */
    void delete(String... keys);

    /**
     * 获取数据库大小
     *
     * @return 数据库大小
     */
    Long getDBSize();

    /**
     * 清空数据库
     */
    void clearDB();
}
