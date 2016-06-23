package org.infrastructure.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 
 * redis缓存实现
 * 
 * @author xuweinan
 * @version V1.0
 */
public class RedisCacheSupport<K, V> implements ICached<K, V> {
	/**
	 * -1: never expire
	 */
	private int expire = -1;

	private RedisTemplate<String, V> redisTemplate;

	private RedisSerializer<K> redisKeySerializer;
	private RedisSerializer<V> redisVlaueSerializer;

	public RedisCacheSupport() {
	}

	// @Override
	public Boolean updateHashCached(final String key, final String mapkey, final V value) throws Exception {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				Boolean hSet = connection.hSet(key.getBytes(), mapkey.getBytes(),
						redisVlaueSerializer.serialize(value));
				return hSet;
			}
		});
	}

	// @Override
	public V getHashCached(final String key, final String mapkey) throws Exception {
		return redisTemplate.execute(new RedisCallback<V>() {
			@Override
			public V doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] hGet = connection.hGet(key.getBytes(), mapkey.getBytes());
				return redisVlaueSerializer.deserialize(hGet);

			}
		});
	}

	// @Override
	public Long deleteHashCached(final String key, final String mapkey) throws Exception {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long hDel = connection.hDel(key.getBytes(), mapkey.getBytes());
				return hDel;

			}
		});
	}

	// @Override
	public Long getHashSize(final String key) throws Exception {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long len = connection.hLen(key.getBytes());
				return len;

			}
		});
	}

	// @Override
	public Long getDBSize() throws Exception {
		return redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long len = connection.dbSize();

				return len;

			}
		});
	}

	// @Override
	public void clearDB() throws Exception {
		redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushDb();
				return null;

			}
		});
	}

	// @Override
	public String deleteCached(final String key) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {

			// @Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.del(key.getBytes());
				return null;
			}

		});
		return null;
	}

	// @Override
	public void updateCached(final String key, final V value) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {

			// @Override
			public V doInRedis(RedisConnection connection) throws DataAccessException {
				connection.set(key.getBytes(), redisVlaueSerializer.serialize(value));
				return null;
			}

		});
	}

	// @Override
	public List<V> getHashValues(final String key) throws Exception {
		return redisTemplate.execute(new RedisCallback<List<V>>() {
			public List<V> doInRedis(RedisConnection connection) throws DataAccessException {
				List<byte[]> hVals = connection.hVals(key.getBytes());

				if (hVals == null || hVals.size() == 0) {
					return null;
				}
				List<V> list = new ArrayList<V>();

				for (byte[] bs : hVals) {
					list.add(redisVlaueSerializer.deserialize(bs));
				}
				return list;

			}
		});
	}

	private List<byte[]> _mGet(final Object... keys) {
		return redisTemplate.execute(new RedisCallback<List<byte[]>>() {
			public List<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
				if (keys == null || keys.length == 0) {
					return null;
				}
				byte[][] byteKeys = new byte[keys.length][];
				for (int i = 0; i < keys.length; i++) {
					byteKeys[i] = keys[i].toString().getBytes();
				}
				List<byte[]> result = connection.mGet(byteKeys);
				if (result == null || result.size() == 0) {
					return null;
				}
				return result;
			}
		});
	}

	private void _mSet(final Map<byte[], byte[]> tuple) {
		redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.mSet(tuple);
				return null;
			}
		});
	}

	// @Override
	public List<V> mGet(final Object... keys) {
		List<byte[]> byteList = _mGet(keys);
		if (byteList == null || byteList.size() == 0) {
			return null;
		}

		List<V> result = new ArrayList<V>();
		for (byte[] bs : byteList) {
			result.add(redisVlaueSerializer.deserialize(bs));
		}
		return result;
	}

	// @Override
	public List<String> mGetString(final Object... keys) {
		List<byte[]> byteList = _mGet(keys);
		if (byteList == null || byteList.size() == 0) {
			return null;
		}

		List<String> result = new ArrayList<String>();
		for (byte[] bs : byteList) {
			if (bs == null) {
				result.add(null);
			} else {
				result.add(new String(bs));
			}
		}
		return result;
	}

	// @Override
	public void mSet(final Map<String, V> tuple) {
		Map<byte[], byte[]> vTuple = new HashMap<byte[], byte[]>();
		for (String key : tuple.keySet()) {
			vTuple.put(key.getBytes(), redisVlaueSerializer.serialize(tuple.get(key)));
		}
		_mSet(vTuple);
	}

	// @Override
	public void mSetString(final Map<String, String> tuple) {
		Map<byte[], byte[]> vTuple = new HashMap<byte[], byte[]>();
		for (String key : tuple.keySet()) {
			vTuple.put(key.getBytes(), tuple.get(key).getBytes());
		}
		_mSet(vTuple);
	}

	// @Override
	public List<V> hMGet(final String key, final Object... fields) {
		return redisTemplate.execute(new RedisCallback<List<V>>() {
			@SuppressWarnings("unchecked")
			public List<V> doInRedis(RedisConnection connection) throws DataAccessException {
				List<V> result = new ArrayList<V>();

				if (fields == null || fields.length == 0) {
					return null;
				}
				byte[][] byteFields = new byte[fields.length][];
				for (int i = 0; i < fields.length; i++) {
					byteFields[i] = fields[i].toString().getBytes();
				}
				List<byte[]> hmaps = connection.hMGet(key.getBytes(), byteFields);
				if (hmaps == null || hmaps.size() == 0) {
					return Collections.EMPTY_LIST;
				}
				for (byte[] bs : hmaps) {
					result.add(redisVlaueSerializer.deserialize(bs));
				}
				return result;
			}
		});
	}

	// @Override
	public void hMSet(final String key, final Map<String, V> hashValues) {
		redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
				for (String field : hashValues.keySet()) {
					hashes.put(field.getBytes(), redisVlaueSerializer.serialize(hashValues.get(field)));
				}
				connection.hMSet(key.getBytes(), hashes);
				return null;
			}
		});
	}

	// @Override
	public void zAdd(final String key, final LinkedHashSet<V> tuples) {
		redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				Set<Tuple> vTuples = new LinkedHashSet<Tuple>();
				double score = 0;
				for (V value : tuples) {
					vTuples.add(new SimpleTuple(redisVlaueSerializer.serialize(value), score++));
				}
				connection.zAdd(key.getBytes(), vTuples);
				return null;
			}
		});
	}

	// @Override
	public void zAddString(final String key, final LinkedHashSet<String> tuples) {
		redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				Set<Tuple> vTuples = new LinkedHashSet<Tuple>();
				double score = 0;
				for (String value : tuples) {
					vTuples.add(new SimpleTuple(value.getBytes(), score++));
				}
				connection.zAdd(key.getBytes(), vTuples);
				return null;
			}
		});
	}

	// @Override
	public Set<V> zRange(final String key, final long begin, final long end) {
		return redisTemplate.execute(new RedisCallback<Set<V>>() {
			public Set<V> doInRedis(RedisConnection connection) throws DataAccessException {
				Set<byte[]> setBytes = connection.zRange(key.getBytes(), begin, end);
				if (setBytes == null || setBytes.size() == 0) {
					return null;
				}

				Set<V> result = new LinkedHashSet<V>();
				for (byte[] bs : setBytes) {
					result.add(redisVlaueSerializer.deserialize(bs));
				}
				return result;
			}
		});
	}

	// @Override
	public Set<String> zRangeString(final String key, final long begin, final long end) {
		return redisTemplate.execute(new RedisCallback<Set<String>>() {
			public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
				Set<byte[]> setBytes = connection.zRange(key.getBytes(), begin, end);
				if (setBytes == null || setBytes.size() == 0) {
					return null;
				}

				Set<String> result = new LinkedHashSet<String>();
				for (byte[] bs : setBytes) {
					result.add(new String(bs));
				}
				return result;
			}
		});
	}

	public RedisTemplate<String, V> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, V> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public void setRedisSerializer(RedisSerializer<V> redisSerializer) {
		this.redisVlaueSerializer = redisSerializer;
	}

	@Override
	public Set<K> getKeys(final String pattern){
		return redisTemplate.execute(new RedisCallback<Set<K>>() {
			@Override
			public Set<K> doInRedis(RedisConnection connection) throws DataAccessException {
				Set<byte[]> setByte = connection.keys(pattern.getBytes());
				if (setByte == null || setByte.size() < 1) {
					return null;
				}
				Set<K> set = new HashSet<K>();
				for (byte[] key : setByte) {
					byte[] bs = connection.get(key);
					set.add(redisKeySerializer.deserialize(bs));
				}

				return set;

			}
		});
	}

	public String[] getStringKeys(final String pattern) {
		return redisTemplate.execute(new RedisCallback<String[]>() {
			@Override
			public String[] doInRedis(RedisConnection connection) throws DataAccessException {
				Set<byte[]> setByte = connection.keys(pattern.getBytes());
				if (setByte == null || setByte.size() < 1) {
					return null;
				}
				String[] keys = new String[setByte.size()];
				int index = 0;
				for (byte[] key : setByte) {
					keys[index++] = new String(key);
				}
				return keys;
			}
		});
	}

	public Set<String> getHashKeys(final String key) throws Exception {
		return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
				Set<byte[]> hKeys = connection.hKeys(key.getBytes());
				if (hKeys == null || hKeys.size() == 0) {
					return null;
				}
				Set<String> set = new HashSet<String>();
				for (byte[] bs : hKeys) {
					set.add(new String(bs));
				}
				return set;
			}
		});
	}

	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param seconds
	 */
	@Override
	public void setExpire(final K key, final long seconds) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.expire(redisKeySerializer.serialize(key), seconds);
			}
		});
	}

	@Override
	public void put(final K key, final V value) {
		put(key, value, null);
	}

	@Override
	public void put(final K key, final V value, final Long expire) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] kb = redisKeySerializer.serialize(key);
				connection.openPipeline();
				connection.set(kb, redisVlaueSerializer.serialize(value));
				if (expire != null)
					connection.expire(kb, expire);
				return connection.closePipeline();
			}

		});
	}

	public void putString(final String key, final String value) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(final RedisConnection connection) throws DataAccessException {
				connection.set(key.getBytes(), value.getBytes());
				return null;
			}
		});
	}

	@Override
	public V get(final K key) {
		return redisTemplate.execute(new RedisCallback<V>() {
			@Override
			public V doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bs = connection.get(redisKeySerializer.serialize(key));
				if (bs == null) {
					return null;
				}
				return redisVlaueSerializer.deserialize(bs);
			}
		});
	}

	public String getString(final String key) throws Exception {
		return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bs = connection.get(key.getBytes());
				if (bs == null) {
					return null;
				}
				return new String(bs);
			}
		});
	}

	@Override
	public void update(final K key, final V value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(final RedisConnection connection) throws DataAccessException {
				connection.set(redisKeySerializer.serialize(key), redisVlaueSerializer.serialize(value));
				return null;
			}
		});
	}

	public void update(final String key, final V object, final Long expireSec) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(final RedisConnection connection) throws DataAccessException {
				connection.set(key.getBytes(), redisVlaueSerializer.serialize(object));
				if (expireSec != null) {
					connection.expire(key.getBytes(), expireSec);
				} else {
					connection.expire(key.getBytes(), expire);
				}
				return null;
			}
		});

	}

	@Override
	public void deleteCached(@SuppressWarnings("unchecked") final K... keys) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) {
				byte[][] byteKeys = new byte[keys.length][];
				for (int i = 0; i < keys.length; i++) {
					byteKeys[i] = redisKeySerializer.serialize(keys[i]);
				}
				connection.del(byteKeys);
				return null;
			}
		});
	}
}
