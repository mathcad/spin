package org.spin.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.function.Handler;
import org.spin.core.function.serializable.Supplier;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.util.BooleanExt;

/**
 * 分布式锁的凭据
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/10/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LockTicket implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(LockTicket.class);

    /**
     * 是否成功获得锁
     */
    private final boolean success;

    /**
     * 锁凭据
     */
    private final String ticket;

    /**
     * 锁的唯一标识
     */
    private final String key;
    private final DistributedLock lock;

    public LockTicket(boolean success, String ticket, String key, DistributedLock lock) {
        this.success = success;
        this.ticket = ticket;
        this.key = key;
        this.lock = lock;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTicket() {
        return ticket;
    }

    public String getKey() {
        return key;
    }

    /**
     * 如果成功获得锁时执行
     *
     * @param handler 执行逻辑
     * @return 否定情况
     */
    public BooleanExt.NoThen ifSuccess(Handler handler) {
        return BooleanExt.of(success).yes(handler);
    }

    /**
     * 如果成功获得锁时执行并返回结果
     *
     * @param handler 执行逻辑
     * @param <T>     成功时返回的数据
     * @return 否定情况
     */
    public <T> BooleanExt.NoMoreThen<T> ifSuccess(Supplier<T> handler) {
        return BooleanExt.ofAny(success).yes(handler);
    }

    /**
     * 如果成功获得锁时执行, 否则抛出指定异常
     *
     * @param handler           执行逻辑
     * @param exceptionSupplier 异常对象提供者
     */
    public void ensureSuccess(Handler handler, Supplier<RuntimeException> exceptionSupplier) {
        BooleanExt.of(success).yes(handler).otherwise(() -> {
            throw null == exceptionSupplier ? new AssertFailException("分布式锁获取失败: " + key) : exceptionSupplier.get();
        });
    }

    /**
     * 如果成功获得锁时执行并返回结果, 否则抛出指定异常
     *
     * @param handler           执行逻辑
     * @param exceptionSupplier 异常对象提供者
     * @param <T>               成功时返回的数据
     * @return 返回数据
     */
    public <T> T ensureSuccess(Supplier<T> handler, Supplier<RuntimeException> exceptionSupplier) {
        return BooleanExt.ofAny(success).yes(handler).otherwise(() -> {
            throw null == exceptionSupplier ? new AssertFailException("分布式锁获取失败: " + key) : exceptionSupplier.get();
        });
    }

    @Override
    public void close() {
        if (success) {
            if (lock.releaseLock(key, ticket)) {
                logger.debug("锁[" + key + "]释放成功");
            } else {
                logger.warn("锁[" + key + "]释放失败");
            }
        }
    }
}
