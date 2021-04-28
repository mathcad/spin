package org.spin.data.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.concurrent.DistributedLock;
import org.spin.core.concurrent.LockTicket;
import org.spin.core.concurrent.Uninterruptibles;
import org.spin.core.util.StringUtils;
import org.spin.data.throwable.DistributedLockException;
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
public class RedisDistributedLock extends DistributedLock {
    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);
    private static final String SPIN_REDIS_LOCKS = "SPIN_REDIS_LOCKS:";
    private static final String REDIS_UNLOCK_SCRIPT;

    private final StringRedisTemplate redisTemplate;

    static {
        REDIS_UNLOCK_SCRIPT = "local val = redis.call(\"get\", KEYS[1])\n" +
            "if val then\n" +
            "    if val == ARGV[1] then\n" +
            "        return redis.call(\"del\", KEYS[1])\n" +
            "    else\n" +
            "        return -1\n" +
            "    end\n" +
            "else\n" +
            "    return 0\n" +
            "end";
    }

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        super();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public LockTicket lock(String key, long expire, int retryTimes, long sleepMillis) {
        String lockKey = SPIN_REDIS_LOCKS + key;
        String ticket = UUID.randomUUID().toString();
        boolean result = setRedisLock(lockKey, ticket, expire);
        while ((!result) && retryTimes-- > 0) {
            logger.debug("Get RedisDistributeLock failed, retrying...{}", retryTimes);
            Uninterruptibles.sleepUninterruptibly(sleepMillis, TimeUnit.MILLISECONDS);
            result = setRedisLock(lockKey, ticket, expire);
        }
        return new LockTicket(result, ticket, key, this);
    }

    @Override
    protected boolean releaseLock(String key, String ticket) {
        String lockKey = SPIN_REDIS_LOCKS + key;
        Long result;
        try {
            result = redisTemplate.execute((RedisConnection connection) -> connection.eval(
                REDIS_UNLOCK_SCRIPT.getBytes(),
                ReturnType.INTEGER,
                1,
                StringUtils.getBytesUtf8(lockKey),
                StringUtils.getBytesUtf8(ticket))
            );
        } catch (Exception e) {
            logger.error("Release RedisDistributeLock [" + key + "] occurred an exception", e);
            return false;
        }

        if (null == result) {
            logger.error("Release RedisDistributeLock [" + key + "] error, return null");
            return false;
        }
        if (result == -1L) {
            throw new DistributedLockException("Release RedisDistributeLock [" + key + "] error, ticket not correct");
        }
        return result > 0;
    }

    private boolean setRedisLock(final String key, final String ticket, final long expire) {
        try {
            Boolean status = redisTemplate.opsForValue().setIfAbsent(key, ticket, expire, TimeUnit.MILLISECONDS);
            return status != null && status;
        } catch (Exception e) {
            logger.error("Operate RedisDistributeLock [" + key + "] occurred an exception", e);
        }
        return false;
    }
}
