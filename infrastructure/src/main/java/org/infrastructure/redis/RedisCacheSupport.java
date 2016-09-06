package org.infrastructure.redis;

import org.infrastructure.util.SerializeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis缓存实现
 *
 * @author xuweinan
 * @version V1.0
 */
public class RedisCacheSupport<V> implements ICached<V> {
    private int expire = -1;

    private RedisTemplate<String, V> redisTemplate;

    private RedisSerializer<V> redisSerializer;

    @Override
    public void deleteCached(final String... keys) throws Exception {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[][] byteKeys = new byte[keys.length][];
                for (int i = 0; i < keys.length; i++) {
                    byteKeys[i] = keys[i].getBytes();
                }
                connection.del(byteKeys);
                return null;
            }
        });
    }

    public void updateCached(final String key, final V object, final Long expireSec)
            throws Exception {
        redisTemplate.execute(new RedisCallback<Object>() {
            public String doInRedis(final RedisConnection connection) throws DataAccessException {
                connection.set(key.getBytes(), redisSerializer.serialize(object));
                if (expireSec != null) {
                    connection.expire(key.getBytes(), expireSec);
                } else {
                    connection.expire(key.getBytes(), expire);
                }
                return null;
            }
        });
    }

    public void setString(final String key, final String value) throws Exception {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public String doInRedis(final RedisConnection connection) throws DataAccessException {
                connection.set(key.getBytes(), value.getBytes());
                return null;
            }
        });
    }

    public V getCached(final String key) throws Exception {
        return redisTemplate.execute(new RedisCallback<V>() {
            @Override
            public V doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bs = connection.get(key.getBytes());
                if (bs == null) {
                    return null;
                }
                return redisSerializer.deserialize(bs);
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
    public Set<V> getKeys(final String pattern) throws Exception {
        return redisTemplate.execute(new RedisCallback<Set<V>>() {
            @Override
            public Set<V> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> setByte = connection.keys(pattern.getBytes());
                if (setByte == null || setByte.size() < 1) {
                    return null;
                }
                Set<V> set = new HashSet<>();
                for (byte[] key : setByte) {
                    byte[] bs = connection.get(key);
                    set.add(redisSerializer.deserialize(bs));
                }
                return set;
            }
        });
    }

    public String[] keys(final String pattern) {
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
                return hKeys.stream().map(String::new).collect(Collectors.toSet());
            }
        });
    }

    public Boolean updateHashCached(final String key, final String mapkey, final V value)
            throws Exception {

        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.hSet(key.getBytes(), mapkey.getBytes(), redisSerializer.serialize(value));
            }
        });
    }

    public V getHashCached(final String key, final String mapkey) throws Exception {
        return redisTemplate.execute(new RedisCallback<V>() {
            @Override
            public V doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] hGet = connection.hGet(key.getBytes(), mapkey.getBytes());
                return redisSerializer.deserialize(hGet);
            }
        });
    }

    public Long deleteHashCached(final String key, final String mapkey) throws Exception {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.hDel(key.getBytes(), mapkey.getBytes());

            }
        });
    }

    public Long getHashSize(final String key) throws Exception {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.hLen(key.getBytes());
            }
        });
    }

    public Long getDBSize() throws Exception {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize();
            }
        });
    }

    public void clearDB() throws Exception {
        redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return null;
            }
        });
    }

    public String deleteCached(final String key) throws Exception {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.del(key.getBytes());
                return null;
            }
        });
        return null;
    }

    public void updateCached(final String key, final V value) throws Exception {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public V doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.set(key.getBytes(), SerializeUtils.serialize(value));
                return null;
            }
        });
    }

    public List<V> getHashValues(final String key) throws Exception {
        return redisTemplate.execute(new RedisCallback<List<V>>() {
            @Override
            public List<V> doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> hVals = connection.hVals(key.getBytes());

                if (hVals == null || hVals.size() == 0) {
                    return null;
                }
                return hVals.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toList());
            }
        });
    }

    public List<V> mGet(final Object... keys) {
        List<byte[]> byteList = _mGet(keys);
        if (byteList == null || byteList.size() == 0) {
            return null;
        }
        return byteList.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toList());
    }

    public List<String> mGetString(final Object... keys) {
        List<byte[]> byteList = _mGet(keys);
        if (byteList == null || byteList.size() == 0) {
            return null;
        }

        List<String> result = new ArrayList<>();
        for (byte[] bs : byteList) {
            if (bs == null) {
                result.add(null);
            } else {
                result.add(new String(bs));
            }
        }
        return result;
    }

    public void mSet(final Map<String, V> tuple) {
        Map<byte[], byte[]> vTuple = new HashMap<>();
        for (String key : tuple.keySet()) {
            vTuple.put(key.getBytes(), redisSerializer.serialize(tuple.get(key)));
        }
        _mSet(vTuple);
    }

    public void mSetString(final Map<String, String> tuple) {
        Map<byte[], byte[]> vTuple = new HashMap<>();
        for (String key : tuple.keySet()) {
            vTuple.put(key.getBytes(), tuple.get(key).getBytes());
        }
        _mSet(vTuple);
    }

    public List<V> hMGet(final String key, final Object... fields) {
        return redisTemplate.execute(new RedisCallback<List<V>>() {
            @SuppressWarnings("unchecked")
            @Override
            public List<V> doInRedis(RedisConnection connection) throws DataAccessException {
                List<V> result = new ArrayList<>();

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
                result.addAll(hmaps.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toList()));
                return result;
            }
        });
    }

    public void hMSet(final String key, final Map<String, V> hashValues) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException {
                Map<byte[], byte[]> hashes = new HashMap<>();
                for (String field : hashValues.keySet()) {
                    hashes.put(field.getBytes(), redisSerializer.serialize(hashValues.get(field)));
                }
                connection.hMSet(key.getBytes(), hashes);
                return null;
            }
        });
    }

    public void zAdd(final String key, final LinkedHashSet<V> tuples) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Set<Tuple> vTuples = new LinkedHashSet<>();
                double score = 0;
                for (V value : tuples) {
                    vTuples.add(new DefaultTuple(redisSerializer.serialize(value), score++));
                }
                connection.zAdd(key.getBytes(), vTuples);
                return null;
            }
        });
    }

    public void zAddString(final String key, final LinkedHashSet<String> tuples) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Set<Tuple> vTuples = new LinkedHashSet<>();
                double score = 0;
                for (String value : tuples) {
                    vTuples.add(new DefaultTuple(value.getBytes(), score++));
                }
                connection.zAdd(key.getBytes(), vTuples);
                return null;
            }
        });
    }

    public Set<V> zRange(final String key, final long begin, final long end) {
        return redisTemplate.execute(new RedisCallback<Set<V>>() {
            @Override
            public Set<V> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> setBytes = connection.zRange(key.getBytes(), begin, end);
                if (setBytes == null || setBytes.size() == 0) {
                    return null;
                }

                return setBytes.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
        });
    }

    public Set<String> zRangeString(final String key, final long begin, final long end) {
        return redisTemplate.execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> setBytes = connection.zRange(key.getBytes(), begin, end);
                if (setBytes == null || setBytes.size() == 0) {
                    return null;
                }

                return setBytes.stream().map(String::new).collect(Collectors.toCollection(LinkedHashSet::new));
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

    /**
     * 设置过期时间
     */
    @Override
    public void setExpire(final String key, final long seconds) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.expire(key.getBytes(), seconds);
            }
        });
    }

    public void setRedisSerializer(RedisSerializer<V> redisSerializer) {
        this.redisSerializer = redisSerializer;
    }

    @Override
    public void put(final String key, final Object value) {
        put(key, value, null);
    }

    @Override
    public void put(final String key, final Object value, final Long expire) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] kb = key.getBytes();
                connection.openPipeline();
                connection.set(kb, SerializeUtils.serialize(value));
                if (expire != null)
                    connection.expire(kb, expire);
                return connection.closePipeline();
            }

        });
    }

    @Override
    public Object get(final String key) {
        return redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bs = connection.get(key.getBytes());
                if (bs == null) {
                    return null;
                }
                return redisSerializer.deserialize(bs);
            }
        });
    }

    @Override
    public void update(final String key, final Object value) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public String doInRedis(final RedisConnection connection) throws DataAccessException {
                connection.set(key.getBytes(), SerializeUtils.serialize(value));
                return null;
            }
        });
    }

    private List<byte[]> _mGet(final Object... keys) {
        return redisTemplate.execute(new RedisCallback<List<byte[]>>() {
            @Override
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
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.mSet(tuple);
                return null;
            }
        });
    }
}