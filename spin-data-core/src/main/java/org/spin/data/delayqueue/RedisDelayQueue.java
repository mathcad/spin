package org.spin.data.delayqueue;

import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.security.Base64;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.SerializeUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.redis.RedisClientWrapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RedisDelayQueue implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RedisDelayQueue.class);
    private static final String PUSH_MSG_SCRIPT = "redis.call(\"HSET\", KEYS[1] .. \"Data\", KEYS[2], ARGV[1])\n" +
        "local now = redis.call(\"TIME\")\n" +
        "local millis = now[1] * 1000 + math.ceil(now[2] / 1000)\n" +
        "local cnt = redis.call(\"ZADD\", KEYS[1] .. \"PriorityQueue\", millis + ARGV[2], KEYS[2] .. KEYS[3])\n" +
        "redis.call(\"PUBLISH\", KEYS[1] .. \"TransferNotifier\", \"WAKE UP\")\n" +
        "return cnt";

    private final TopicListener topicListener;
    private final QueueTransfer transfer;
    private final DelayQueueContext delayQueueContext;

    public RedisDelayQueue(String delayQueueName,
                           String scheduleGroupId,
                           RedisClientWrapper clientWrapper,
                           List<DelayMessageHandler> handlerList,
                           int corePoolSize,
                           int maxPoolSize,
                           long keepAliveTimeInMs,
                           int queueSize) {
        delayQueueContext = new DelayQueueContext(delayQueueName, scheduleGroupId, clientWrapper);
        topicListener = new TopicListener(delayQueueContext, handlerList, corePoolSize, maxPoolSize, keepAliveTimeInMs, queueSize);
        transfer = new QueueTransfer(delayQueueContext);
    }

    @Override
    public void close() {
        delayQueueContext.isRunning = false;
        transfer.unPark();
        delayQueueContext.pubsubConnection.close();
        delayQueueContext.connection.close();
        topicListener.close();
    }

    public String scheduledInGroup(String message, LocalDateTime scheduleAt, GroupScheduledTask task) {
        Assert.notNull(scheduleAt, "Delay Message's schedule time must not be null!!");
        if (scheduleAt.isBefore(LocalDateTime.now())) {
            throw new SimplifiedException("Delay Message's schedule time must not before now!!");
        }

        long delayTime = scheduleAt.toInstant(ZoneId.systemDefault().getRules().getOffset(scheduleAt)).toEpochMilli() - System.currentTimeMillis();
        return publishInternal(delayQueueContext.scheduleGroupId, message, delayTime, task);
    }

    public String scheduledInGroup(String message, long delayTimeInSeconds, GroupScheduledTask task) {
        return publishInternal(delayQueueContext.scheduleGroupId, message, delayTimeInSeconds * 1000L, task);
    }

    public String publish(String topic, String message, LocalDateTime scheduleAt) {
        Assert.notNull(scheduleAt, "Delay Message's schedule time must not be null!!");
        if (scheduleAt.isBefore(LocalDateTime.now())) {
            throw new SimplifiedException("Delay Message's schedule time must not before now!!");
        }

        long delayTime = scheduleAt.toInstant(ZoneId.systemDefault().getRules().getOffset(scheduleAt)).toEpochMilli() - System.currentTimeMillis();
        return publishInternal(topic, message, delayTime, null);
    }

    public String publish(String topic, String message, long delayTimeInSeconds) {
        return publishInternal(topic, message, delayTimeInSeconds * 1000L, null);
    }

    private String publishInternal(String topic, String message, long delayTimeInMillis, GroupScheduledTask task) {
        long triggerTime = System.currentTimeMillis();
        String msgId = UUID.randomUUID().toString();
        if (delayTimeInMillis < 1000L) {
            throw new SimplifiedException("Message Delay Time must grate than 1s");
        }

        DelayMessage delayMessage = new DelayMessage(msgId, topic, delayTimeInMillis, triggerTime, message);
        if (null != task) {
            String taskStr = Base64.encode(SerializeUtils.serialize(task));
            delayMessage.setHandler(taskStr);
        }

        Long cnt = delayQueueContext.connection.syncEval(PUSH_MSG_SCRIPT, ScriptOutputType.INTEGER,
            ArrayUtils.ofArray(delayQueueContext.delayQueueKeyPrefix, msgId, topic, delayQueueContext.notifierChannel),
            delayMessage.toString(),
            StringUtils.toString(Math.max(delayTimeInMillis - System.currentTimeMillis() + triggerTime - topicListener.getOffset(), 0)));

        if (!Objects.equals(1L, cnt)) {
            throw new SimplifiedException("Delay message delivery failed");
        }

        return msgId;
    }

}
