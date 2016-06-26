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
     *
     * @param pattern
     * @return
     * @version 1.0
     */
    Set<V> getKeys(final String pattern) throws Exception;

    /**
     * 设置缓存对象的过期时间
     *
     * @param key
     * @param seconds
     */
    public void setExpire(final String key,final long seconds);

    /**
     * 存入缓存
     *
     * @param key
     * @param value
     * @version 1.0
     */
    public void put(final String key, final Object value);

    /**
     * 存入对象，并设置期限
     *
     * @param key
     * @param value
     * @param expire
     * @version 1.0
     */
    public void put(final String key, final Object value, final Long expire);

    /**
     * 获取对象
     *
     * @param key
     * @return
     * @version 1.0
     */
    public Object get(final String key);

    /**
     * 更新对象
     *
     * @param key
     * @param value
     * @version 1.0
     */
    public void update(final String key, final Object value);

    /**
     * 删除缓存
     *
     * @param keys
     * @throws Exception
     * @version 1.0
     */
    public void deleteCached(String... keys) throws Exception;

}