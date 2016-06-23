package org.infrastructure.redis;

import java.util.Set;

/**
 * 缓存接口
 *
 * @author xuweinan
 * @version 1.0
 */
public interface ICached<K, V> {

    /**
     * 得到所有缓存的Key
     *
     * @param pattern key的pattern
     * @return key的Set
     */
    Set<K> getKeys(final String pattern) throws Exception;

    /**
     * 设置缓存对象的过期时间
     *
     * @param key     key
     * @param seconds 秒
     */
    void setExpire(final K key, final long seconds);

    /**
     * 存入缓存
     */
    void put(final K key, final V value);

    /**
     * 存入对象，并设置期限
     *
     * @param expire 过期时间
     */
    void put(final K key, final V value, final Long expire);

    /**
     * 获取对象
     */
    V get(final K key);

    /**
     * 更新对象
     */
    void update(final K key, final V value);

    /**
     * 删除缓存
     *
     * @param keys key
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    void deleteCached(K... keys) throws Exception;

}
