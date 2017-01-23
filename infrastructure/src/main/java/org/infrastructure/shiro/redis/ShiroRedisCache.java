package org.infrastructure.shiro.redis;

public class ShiroRedisCache<V> {// implements Cache<String, V>    {
//
//	public Logger logger = Logger.getLogger(ShiroRedisCache.class.getName());
//
//	private String name;
//	private ICached<V> cached;
//
//	public ShiroRedisCache(String name,ICached<V> cached){
//		this.name=name;
//		this.cached=cached;
//	}
//
//	@Override
//	public V get(String key) throws CacheException {
//		logger.debug("根据key从Redis中获取对象 key [" + key + "]");
//		try {
//			if (key == null) {
//	            return null;
//	        }else{
//	        	V value= (V) cached.getHashCached(name, key);
//	        	return value;
//	        }
//		} catch (Throwable t) {
//			throw new CacheException(t);
//		}
//
//	}
//
//	@Override
//	public V put(String key, V value) throws CacheException {
//		logger.debug("根据key从存储 key [" + key + "]");
//		 try {
//			 	cached.updateHashCached(name, key, value);
//	            return value;
//	        } catch (Throwable t) {
//	            throw new CacheException(t);
//	        }
//	}
//
//	@Override
//	public V remove(String key) throws CacheException {
//		logger.debug("从redis中删除 key [" + key + "]");
//		try {
//	        V previous = get(key);
//	        cached.deleteHashCached(name, key);
//	        return previous;
//	    } catch (Throwable t) {
//	        throw new CacheException(t);
//	    }
//	}
//
//	@Override
//	public void clear() throws CacheException {
//		logger.debug("从redis中删除所有元素");
//		try {
//	        cached.deleteCached(name);
//	    } catch (Throwable t) {
//	        throw new CacheException(t);
//	    }
//	}
//
//	@Override
//	public int size() {
//		try {
//			Long longSize = new Long(cached.getHashSize(name));
//	        return longSize.intValue();
//	    } catch (Throwable t) {
//	        throw new CacheException(t);
//	    }
//	}
//
//	@Override
//	public Set<String> keys() {
//		try {
//	        Set<String> keys = cached.getHashKeys(name);
//	      return keys;
//	    } catch (Throwable t) {
//	        throw new CacheException(t);
//	    }
//	}
//
//	@Override
//	public Collection<V> values() {
//		try {
//			Collection<V> values = cached.getHashValues(name);
//	      return values;
//	    } catch (Throwable t) {
//	        throw new CacheException(t);
//	    }
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//
//	public ICached<V> getCached() {
//		return cached;
//	}
//
//
//	public void setCached(ICached<V> cached) {
//		this.cached = cached;
//	}

}
