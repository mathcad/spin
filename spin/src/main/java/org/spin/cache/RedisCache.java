package org.spin.cache;

import org.spin.util.SerializeUtils;
import org.spin.util.StringUtils;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisServerCommands;
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
public class RedisCache<V> implements Cache<V> {
    private int expire = -1;

    private RedisTemplate<String, V> redisTemplate;

    private RedisSerializer<V> redisSerializer;

    @Override
    public void delete(final String... keys) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            byte[][] byteKeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                byteKeys[i] = StringUtils.getBytesUtf8(keys[i]);
            }
            connection.del(byteKeys);
            return null;
        });
    }

    @Override
    public void update(final String key, final V object, final Long expireSec) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.set(StringUtils.getBytesUtf8(key), redisSerializer.serialize(object));
            if (expireSec != null) {
                connection.expire(StringUtils.getBytesUtf8(key), expireSec);
            } else {
                connection.expire(StringUtils.getBytesUtf8(key), expire);
            }
            return null;
        });
    }

    public void setString(final String key, final String value) throws Exception {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.set(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(value));
            return null;
        });
    }

    public String getString(final String key) throws Exception {
        return redisTemplate.execute((RedisCallback<String>) connection -> {
            byte[] bs = connection.get(StringUtils.getBytesUtf8(key));
            if (bs == null) {
                return null;
            }
            return StringUtils.newStringUtf8(bs);
        });
    }

    @Override
    public Set<String> getKeys(final String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<byte[]> setByte = connection.keys(StringUtils.getBytesUtf8(pattern));
            if (setByte == null) {
                return new HashSet<>();
            }
            return setByte.stream().map(StringUtils::newStringUtf8).collect(Collectors.toSet());
        });
    }

    @Override
    public Set<V> getValues(final String pattern) {
        return redisTemplate.execute((RedisCallback<Set<V>>) connection -> {
            Set<byte[]> setByte = connection.keys(StringUtils.getBytesUtf8(pattern));
            if (setByte == null) {
                return new HashSet<>();
            }
            return setByte.stream().map(connection::get).map(redisSerializer::deserialize).collect(Collectors.toSet());
        });
    }

    public Boolean updateHashValue(final String key, final String mapkey, final V value) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.hSet(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(mapkey), redisSerializer.serialize(value)));
    }

    public V getHashValue(final String key, final String mapkey) {
        return redisTemplate.execute((RedisCallback<V>) connection -> {
            byte[] hGet = connection.hGet(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(mapkey));
            return redisSerializer.deserialize(hGet);
        });
    }

    public List<V> getHashValues(final String key) {
        return redisTemplate.execute((RedisCallback<List<V>>) connection -> {
            List<byte[]> hVals = connection.hVals(StringUtils.getBytesUtf8(key));

            if (hVals == null || hVals.size() == 0) {
                return new ArrayList<>();
            }
            return hVals.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toList());
        });
    }

    public Long deleteHashValue(final String key, final String mapkey) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.hDel(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(mapkey)));
    }

    public Long getHashLength(final String key) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.hLen(StringUtils.getBytesUtf8(key)));
    }

    @Override
    public void update(final String key, final V value) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.set(StringUtils.getBytesUtf8(key), SerializeUtils.serialize(value));
            return null;
        });
    }

    /**
     * 设置过期时间
     */
    @Override
    public void expire(final String key, final long seconds) {
        redisTemplate.execute((RedisCallback<Object>) connection -> connection.expire(StringUtils.getBytesUtf8(key), seconds));
    }

    @Override
    public void put(final String key, final V value) {
        put(key, value, null);
    }

    @Override
    public void put(final String key, final V value, final Long expire) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            byte[] kb = StringUtils.getBytesUtf8(key);
            connection.openPipeline();
            connection.set(kb, SerializeUtils.serialize(value));
            if (expire != null)
                connection.expire(kb, expire);
            return connection.closePipeline();
        });
    }

    @Override
    public V get(final String key) {
        return redisTemplate.execute((RedisCallback<V>) connection -> {
            byte[] bs = connection.get(StringUtils.getBytesUtf8(key));
            if (bs == null) {
                return null;
            }
            return redisSerializer.deserialize(bs);
        });
    }

    @Override
    public Long getDBSize() {
        return redisTemplate.execute(RedisServerCommands::dbSize);
    }

    @Override
    public void clearDB() {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            connection.flushDb();
            return null;
        });
    }

    public List<V> hMGet(final String key, final Object... fields) {
        return redisTemplate.execute((RedisCallback<List<V>>) connection -> {
            List<V> result = new ArrayList<>();

            if (fields == null || fields.length == 0) {
                return null;
            }
            byte[][] byteFields = new byte[fields.length][];
            for (int i = 0; i < fields.length; i++) {
                byteFields[i] = fields[i].toString().getBytes();
            }
            List<byte[]> hmaps = connection.hMGet(StringUtils.getBytesUtf8(key), byteFields);
            if (hmaps == null || hmaps.size() == 0) {
                return Collections.EMPTY_LIST;
            }
            result.addAll(hmaps.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toList()));
            return result;
        });
    }

    public void hMSet(final String key, final Map<String, V> hashValues) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            Map<byte[], byte[]> hashes = new HashMap<>();
            for (String field : hashValues.keySet()) {
                hashes.put(field.getBytes(), redisSerializer.serialize(hashValues.get(field)));
            }
            connection.hMSet(StringUtils.getBytesUtf8(key), hashes);
            return null;
        });
    }

    public void zAdd(final String key, final LinkedHashSet<V> tuples) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            Set<Tuple> vTuples = new LinkedHashSet<>();
            double score = 0;
            for (V value : tuples) {
                vTuples.add(new DefaultTuple(redisSerializer.serialize(value), score++));
            }
            connection.zAdd(StringUtils.getBytesUtf8(key), vTuples);
            return null;
        });
    }

    public void zAddString(final String key, final LinkedHashSet<String> tuples) {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            Set<Tuple> vTuples = new LinkedHashSet<>();
            double score = 0;
            for (String value : tuples) {
                vTuples.add(new DefaultTuple(value.getBytes(), score++));
            }
            connection.zAdd(StringUtils.getBytesUtf8(key), vTuples);
            return null;
        });
    }

    public Set<V> zRange(final String key, final long begin, final long end) {
        return redisTemplate.execute((RedisCallback<Set<V>>) connection -> {
            Set<byte[]> setBytes = connection.zRange(StringUtils.getBytesUtf8(key), begin, end);
            if (setBytes == null || setBytes.size() == 0) {
                return null;
            }

            return setBytes.stream().map(bs -> redisSerializer.deserialize(bs)).collect(Collectors.toCollection(LinkedHashSet::new));
        });
    }

    public Set<String> zRangeString(final String key, final long begin, final long end) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<byte[]> setBytes = connection.zRange(StringUtils.getBytesUtf8(key), begin, end);
            if (setBytes == null || setBytes.size() == 0) {
                return null;
            }

            return setBytes.stream().map(String::new).collect(Collectors.toCollection(LinkedHashSet::new));
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
        this.redisSerializer = redisSerializer;
    }
}
