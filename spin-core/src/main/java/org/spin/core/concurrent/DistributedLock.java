package org.spin.core.concurrent;

/**
 * 分布式锁顶级接口
 * 例如：
 * RETRY_TIMES=100，SLEEP_MILLIS=100
 * RETRY_TIMES * SLEEP_MILLIS = 10000 意味着如果一直获取不了锁，最长会等待10秒后抛超时异常
 */
public abstract class DistributedLock {

    /**
     * 默认超时时间
     */
    private static final long TIMEOUT_MILLIS = 600_000L;

    /**
     * 重试次数
     */
    private static final int RETRY_TIMES = 10;

    /**
     * 每次重试后等待的时间
     */
    private static final long SLEEP_MILLIS = 100;

    /**
     * 获取锁
     * <p>
     * 锁超时时间5s, 重试10次, 重试等待100ms
     * </p>
     *
     * @param key key
     * @return 锁凭据
     */
    public LockTicket lock(String key) {
        return lock(key, TIMEOUT_MILLIS, RETRY_TIMES, SLEEP_MILLIS);
    }

    /**
     * 获取锁
     * <p>
     * 锁超时时间5s, 重试指定次数, 重试等待100ms
     * </p>
     *
     * @param key        key
     * @param retryTimes 重试次数
     * @return 锁凭据
     */
    public LockTicket lock(String key, int retryTimes) {
        return lock(key, TIMEOUT_MILLIS, retryTimes, SLEEP_MILLIS);
    }

    /**
     * 获取锁
     * <p>
     * 锁超时时间5s, 重试{@code retryTimes}次, 重试等待{@code sleepMillis}秒
     * </p>
     *
     * @param key         key
     * @param retryTimes  重试次数
     * @param sleepMillis 获取锁失败的重试间隔
     * @return 锁凭据
     */
    public LockTicket lock(String key, int retryTimes, long sleepMillis) {
        return lock(key, TIMEOUT_MILLIS, retryTimes, sleepMillis);
    }

    /**
     * 获取锁
     *
     * <p>
     * 锁超时时间{@code expire}毫秒, 重试10次, 重试等待100毫秒
     * </p>
     *
     * @param key    key
     * @param expire 获取锁超时时间
     * @return 锁凭据
     */
    public LockTicket lock(String key, long expire) {
        return lock(key, expire, RETRY_TIMES, SLEEP_MILLIS);
    }

    /**
     * 获取锁
     *
     * <p>
     * 锁超时时间{@code expire}毫秒, 重试{@code retryTimes}次, 重试等待100毫秒
     * </p>
     *
     * @param key        key
     * @param expire     获取锁超时时间
     * @param retryTimes 重试次数
     * @return 锁凭据
     */
    public LockTicket lock(String key, long expire, int retryTimes) {
        return lock(key, expire, retryTimes, SLEEP_MILLIS);
    }

    /**
     * 获取锁
     *
     * <p>
     * 锁超时时间{@code expire}毫秒, 重试{@code retryTimes}次, 重试等待{@code sleepMillis}毫秒
     * </p>
     *
     * @param key         key
     * @param expire      锁的超时时间
     * @param retryTimes  重试次数
     * @param sleepMillis 获取锁失败的重试间隔
     * @return 锁凭据
     */
    public abstract LockTicket lock(String key, long expire, int retryTimes, long sleepMillis);

    /**
     * 尝试获取锁，如果失败不会重试
     *
     * @param key key
     * @return 锁凭据
     */
    public LockTicket tryLock(String key) {
        return lock(key, TIMEOUT_MILLIS, 0, 0);
    }

    /**
     * 尝试获取锁，如果失败不会重试
     *
     * @param key           key
     * @param timeoutMillis 超时时间
     * @return 锁凭据
     */
    public LockTicket tryLock(String key, long timeoutMillis) {
        return lock(key, timeoutMillis, 0, 0);
    }

    /**
     * 释放锁
     *
     * @param key    key值
     * @param ticket 票据
     * @return 释放结果
     */
    protected abstract boolean releaseLock(String key, String ticket);
}
