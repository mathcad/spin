package org.spin.data.delayqueue;

import org.spin.core.util.StringUtils;
import org.spin.data.redis.RedisClientWrapper;
import org.spin.data.redis.RedisConnectionWrapper;
import org.spin.data.redis.RedisPubSubConnectionWrapper;

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
    protected final String delayQueueName;
    protected final String scheduleGroupId;
    protected final boolean groupScheduleEnabled;
    protected final String delayQueueKeyPrefix;
    protected final String delayQueueTopicPrefix;
    protected final String notifierChannel;
    protected final RedisConnectionWrapper<String, String> connection;
    protected final RedisPubSubConnectionWrapper<String, String> pubsubConnection;

    public DelayQueueContext(String delayQueueName, String scheduleGroupId, RedisClientWrapper redisClientWrapper) {
        this.redisClientWrapper = redisClientWrapper;
        if (delayQueueName.indexOf('{') != -1 || delayQueueName.indexOf(':') != -1) {
            throw new IllegalArgumentException("DelayQueue name must not contains '{', '}' or ':'");
        }
        this.delayQueueName = delayQueueName;
        this.scheduleGroupId = scheduleGroupId;
        groupScheduleEnabled = StringUtils.isNotEmpty(scheduleGroupId);
        delayQueueKeyPrefix = "{" + delayQueueName + "}:";
        delayQueueTopicPrefix = delayQueueKeyPrefix + "Topic:";
        notifierChannel = delayQueueKeyPrefix + "TransferNotifier";
        connection = redisClientWrapper.connect();
        pubsubConnection = redisClientWrapper.connectPubSub();
    }
}
