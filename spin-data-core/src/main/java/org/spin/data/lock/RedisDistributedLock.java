package org.spin.data.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.concurrent.DistributedLock;
import org.spin.core.concurrent.Uninterruptibles;
import org.spin.core.util.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的分布式锁实现
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/8/10</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RedisDistributedLock implements DistributedLock {
    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);
    private static final String SPIN_REDIS_LOCKS = "SPIN_REDIS_LOCKS:";
    private static final String REDIS_UNLOCK_SCRIPT;

    private final StringRedisTemplate redisTemplate;

    private final ThreadLocal<String> lockFlag = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    static {
        REDIS_UNLOCK_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
            "then " +
            "    return redis.call(\"del\",KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end ";
    }

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        super();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        String lockKey = SPIN_REDIS_LOCKS + key;
        boolean result = setRedis(lockKey, expire);
        while ((!result) && retryTimes-- > 0) {
            logger.debug("get redisDistributeLock failed, retrying...{}", retryTimes);
            Uninterruptibles.sleepUninterruptibly(sleepMillis, TimeUnit.MILLISECONDS);
            result = setRedis(lockKey, expire);
        }
        return result;
    }


    @Override
    public boolean releaseLock(String key) {
        String lockKey = SPIN_REDIS_LOCKS + key;
        try {
            Long result = redisTemplate.execute((RedisConnection connection) -> connection.eval(
                REDIS_UNLOCK_SCRIPT.getBytes(),
                ReturnType.INTEGER,
                1,
                StringUtils.getBytesUtf8(lockKey),
                StringUtils.getBytesUtf8(lockFlag.get()))
            );

            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("release redisDistributeLock occured an exception", e);
        }
        return false;
    }

    private boolean setRedis(final String key, final long expire) {
        try {
            Boolean status = redisTemplate.opsForValue().setIfAbsent(key, lockFlag.get(), expire, TimeUnit.MILLISECONDS);
            return status != null && status;
        } catch (Exception e) {
            logger.error("set redisDistributeLock occured an exception", e);
        }
        return false;
    }
}
