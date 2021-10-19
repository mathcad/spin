package org.spin.data.delayqueue;

import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
        "local now = redis.call(\"TIME\")[1]\n" +
        "return redis.call(\"ZADD\", KEYS[1] .. \"PriorityQueue\", now + ARGV[2], KEYS[2] .. KEYS[3])\n";

    private final ConcurrentHashMap<String, TopicListener> TOPIC_LISTENERS = new ConcurrentHashMap<>();
    private final QueueTransfer transfer;
    private final DelayQueueContext delayQueueContext;

    public RedisDelayQueue(RedisDelayQueueProperties queueProperties) {
        delayQueueContext = new DelayQueueContext(new RedisClientWrapper(queueProperties));
        transfer = new QueueTransfer(delayQueueContext);
    }

    @Override
    public void close() {
        delayQueueContext.isRunning = false;
        transfer.unPark();
        delayQueueContext.redisClientWrapper.shutdown();
    }

    public void publish(String topic, String message, long delayTimeInSeconds) {
        String msgId = UUID.randomUUID().toString();
        if (delayTimeInSeconds < 1) {
            throw new SimplifiedException("Message Delay Time must grate than 1s");
        }
        Long cnt = delayQueueContext.redisClientWrapper.syncEval(PUSH_MSG_SCRIPT, ScriptOutputType.INTEGER,
            ArrayUtils.ofArray(delayQueueContext.delayQueueKeyPrefix, msgId, topic),
            message, StringUtils.toString(delayTimeInSeconds));

        if (!Objects.equals(1L, cnt)) {
            throw new SimplifiedException("Delay message delivery failed");
        }
        transfer.unPark();
    }

    public void setMessageHandlers(List<DelayMessageHandler> delayMessageHandlers) {
        TOPIC_LISTENERS.clear();
        if (null == delayMessageHandlers) {
            return;
        }
        for (DelayMessageHandler handler : delayMessageHandlers) {
            addMessageHandlers(handler);
        }
    }

    public void addMessageHandlers(DelayMessageHandler handler) {
        TopicListener topicListener = new TopicListener(delayQueueContext, handler);
        if (TOPIC_LISTENERS.containsKey(handler.getTopic())) {
            TOPIC_LISTENERS.get(handler.getTopic()).stopListen();
        }
        TOPIC_LISTENERS.put(handler.getTopic(), topicListener);
        topicListener.startListen();
    }
}
