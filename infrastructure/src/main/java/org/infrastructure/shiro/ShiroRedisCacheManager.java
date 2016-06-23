package org.infrastructure.shiro;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;
import org.infrastructure.redis.ICached;

public class ShiroRedisCacheManager extends AbstractCacheManager {

	private ICached<String, Session> cached;

	@Override
	protected Cache<String, Session> createCache(String cacheName) throws CacheException {
		return null;
	}

	public ICached<String, Session> getCached() {
		return cached;
	}

	public void setCached(ICached<String, Session> cached) {
		this.cached = cached;
	}

}
