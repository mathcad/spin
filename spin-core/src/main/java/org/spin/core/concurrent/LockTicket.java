package org.spin.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.function.Handler;
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

    private final boolean success;
    private final String ticket;
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

    public BooleanExt.NoThen ifSuccess(Handler handler) {
        return BooleanExt.of(success).yes(handler);
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
