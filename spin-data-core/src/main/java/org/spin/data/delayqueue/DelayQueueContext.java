package org.spin.data.delayqueue;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DelayQueueContext {

    protected volatile boolean isRunning = true;
    protected RedisClientWrapper redisClientWrapper;
    protected final String delayQueueKeyPrefix;
    protected final String delayQueueTopicPrefix;

    public DelayQueueContext(RedisClientWrapper redisClientWrapper) {
        this.redisClientWrapper = redisClientWrapper;
        if (redisClientWrapper.getDelayQueueProperties().getDelayQueuePrefix().contains("{")) {
            delayQueueKeyPrefix = redisClientWrapper.getDelayQueueProperties().getDelayQueuePrefix() + ":";
        } else {
            delayQueueKeyPrefix = "{" + redisClientWrapper.getDelayQueueProperties().getDelayQueuePrefix() + "}:";
        }
        delayQueueTopicPrefix = delayQueueKeyPrefix + "Topic:";
    }
}
