package org.spin.shiro.redis;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;
import org.spin.cache.Cache;

public class ShiroRedisCacheManager extends AbstractCacheManager {

    private Cache<Session> cached;

    @Override
    protected org.apache.shiro.cache.Cache<String, Session> createCache(String cacheName) throws CacheException {
//		return new ShiroRedisCache<Session>(cacheName, cached);
        return null;
    }

    public Cache<Session> getCached() {
        return cached;
    }

    public void setCached(Cache<Session> cached) {
        this.cached = cached;
    }

}
