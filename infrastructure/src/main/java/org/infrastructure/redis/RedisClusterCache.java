package org.infrastructure.redis;

import java.util.Set;

/**
 * 缓存实现
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年5月29日 上午1:09:27
 * @version V1.0
 */
public class RedisClusterCache<V>  implements ICached<V> {

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#getKeys(java.lang.String)
	 */
	@Override
	public Set<V> getKeys(String pattern) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#setExpire(java.lang.String, long)
	 */
	@Override
	public void setExpire(String key, long seconds) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public void put(String key, Object value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#put(java.lang.String, java.lang.Object, java.lang.Long)
	 */
	@Override
	public void put(String key, Object value, Long expire) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#update(java.lang.String, java.lang.Object)
	 */
	@Override
	public void update(String key, Object value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.gsh56.infrastructure.redis.ICached#deleteCached(java.lang.String[])
	 */
	@Override
	public void deleteCached(String... keys) throws Exception {
		// TODO Auto-generated method stub

	}



}