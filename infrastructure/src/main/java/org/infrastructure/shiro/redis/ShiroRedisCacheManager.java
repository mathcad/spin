package org.infrastructure.shiro.redis;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;
import org.infrastructure.redis.ICached;

public class ShiroRedisCacheManager extends AbstractCacheManager {

	private ICached<Session> cached;

	@Override
	protected Cache<String, Session> createCache(String cacheName) throws CacheException {
//		return new ShiroRedisCache<Session>(cacheName, cached);
		return null;
	}
	public ICached<Session> getCached() {
		return cached;
	}
	public void setCached(ICached<Session> cached) {
		this.cached = cached;
	}

}
