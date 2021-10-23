package org.spin.data.delayqueue;

import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.redis.RedisClientWrapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

    private final ConcurrentHashMap<String, TopicListener> TOPIC_LISTENERS = new ConcurrentHashMap<>();
    private final QueueTransfer transfer;
    private final DelayQueueContext delayQueueContext;

    public RedisDelayQueue(String delayQueueName, RedisClientWrapper clientWrapper) {
        delayQueueContext = new DelayQueueContext(delayQueueName, clientWrapper);

        transfer = new QueueTransfer(delayQueueContext);
    }

    @Override
    public void close() {
        delayQueueContext.isRunning = false;
        transfer.unPark();
        delayQueueContext.pubsubConnection.close();
        delayQueueContext.connection.close();
        TOPIC_LISTENERS.forEach((k, v) -> v.close());
    }

    public void publish(String topic, String message, LocalDateTime scheduleAt) {
        publish(topic, message, scheduleAt, null);
    }

    public void publish(String topic, String message, LocalDateTime scheduleAt, Consumer<DelayMessage> delayHandler) {
        if (scheduleAt.isBefore(LocalDateTime.now())) {
            throw new SimplifiedException("Delay Message's schedule time must not before now!!");
        }

        long delayTime = scheduleAt.toInstant(ZoneId.systemDefault().getRules().getOffset(scheduleAt)).toEpochMilli() - System.currentTimeMillis();
        publishInternal(topic, message, delayTime, delayHandler);
    }

    public void publish(String topic, String message, long delayTimeInSeconds) {
        publishInternal(topic, message, delayTimeInSeconds * 1000L, null);
    }

    public void publish(String topic, String message, long delayTimeInSeconds, Consumer<DelayMessage> delayHandler) {
        publishInternal(topic, message, delayTimeInSeconds * 1000L, delayHandler);
    }

    private void publishInternal(String topic, String message, long delayTimeInMillis, Consumer<DelayMessage> delayHandler) {
        long triggerTime = System.currentTimeMillis();
        String msgId = UUID.randomUUID().toString();
        if (delayTimeInMillis < 1000L) {
            throw new SimplifiedException("Message Delay Time must grate than 1s");
        }

        TOPIC_LISTENERS.computeIfAbsent(topic, t -> {
            TopicListener l = new TopicListener(topic, delayQueueContext);
            l.startListen();
            return l;
        });
        TopicListener listener = TOPIC_LISTENERS.get(topic);
        if (null != delayHandler) {
            listener.addCustomizeHandler(msgId, delayHandler);
        }

        DelayMessage delayMessage = new DelayMessage(msgId, topic, delayTimeInMillis, triggerTime, message);


        Long cnt = delayQueueContext.connection.syncEval(PUSH_MSG_SCRIPT, ScriptOutputType.INTEGER,
            ArrayUtils.ofArray(delayQueueContext.delayQueueKeyPrefix, msgId, topic, delayQueueContext.notifierChannel),
            delayMessage.toString(),
            StringUtils.toString(Math.max(delayTimeInMillis - System.currentTimeMillis() + triggerTime - listener.getOffset(), 0)));

        if (!Objects.equals(1L, cnt)) {
            listener.removeCustomizeHandler(msgId);
            throw new SimplifiedException("Delay message delivery failed");
        }
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
            TOPIC_LISTENERS.get(handler.getTopic()).close();
        }
        TOPIC_LISTENERS.put(handler.getTopic(), topicListener);
        topicListener.startListen();
    }
}
