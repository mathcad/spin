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
public class RedisClusterCache<K, V> implements ICached<K, V> {

	@Override
	public Set<K> getKeys(String pattern) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExpire(K key, long seconds) {
		// TODO Auto-generated method stub
	}

	@Override
	public void put(K key, V value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void put(K key, V value, Long expire) {
		// TODO Auto-generated method stub
	}

	@Override
	public V get(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(K key, V value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteCached(@SuppressWarnings("unchecked") K... keys) {
		// TODO Auto-generated method stub
	}
}
