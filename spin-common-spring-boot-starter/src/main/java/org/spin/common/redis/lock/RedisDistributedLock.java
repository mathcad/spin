package org.spin.common.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.concurrent.DistributedLock;
import org.spin.core.util.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.UUID;

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
    private static final String REDIS_UNLOCK_SCRIPT;

    private StringRedisTemplate redisTemplate;

    private ThreadLocal<String> lockFlag = new ThreadLocal<>();


    /*
     * 通过lua脚本释放锁,来达到释放锁的原子操作
     */
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
        boolean result = setRedis(key, expire);
        // 如果获取锁失败，按照传入的重试次数进行重试
        while ((!result) && retryTimes-- > 0) {
            try {
                logger.debug("get redisDistributeLock failed, retrying...{}", retryTimes);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                logger.warn("Interrupted!", e);
                Thread.currentThread().interrupt();
            }
            result = setRedis(key, expire);
        }
        return result;
    }


    @Override
    public boolean releaseLock(String key) {
        // 释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除
        try {
            Long result = redisTemplate.execute((RedisConnection connection) -> connection.eval(
                REDIS_UNLOCK_SCRIPT.getBytes(),
                ReturnType.INTEGER,
                1,
                StringUtils.getBytesUtf8(key),
                StringUtils.getBytesUtf8(lockFlag.get()))
            );

            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("release redisDistributeLock occured an exception", e);
        } finally {
            lockFlag.remove();
        }
        return false;
    }

    private boolean setRedis(final String key, final long expire) {
        try {
            Boolean status = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                String uuid = UUID.randomUUID().toString();
                lockFlag.set(uuid);
                return connection.set(StringUtils.getBytesUtf8(key), uuid.getBytes(),
                    Expiration.milliseconds(expire),
                    RedisStringCommands.SetOption.SET_IF_ABSENT);
            });
            return status != null && status;
        } catch (Exception e) {
            logger.error("set redisDistributeLock occured an exception", e);
        }
        return false;
    }
}
