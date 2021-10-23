package org.spin.data.delayqueue;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.JsonUtils;
import org.spin.data.redis.RedisConnectionWrapper;

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
class TopicListener implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TopicListener.class);

    private final String uuid = UUID.randomUUID().toString();
    private final String topic;
    private final DelayQueueContext delayQueueContext;
    private final DelayMessageHandler delayMessageHandler;
    private RedisConnectionWrapper<String, String> connection;
    private final ConcurrentHashMap<String, Consumer<DelayMessage>> customizeHandler = new ConcurrentHashMap<>();
    private final Thread workThread;
    private volatile boolean isListen = true;
    private final OffsetWindow offsetWindow = new OffsetWindow();

    TopicListener(DelayQueueContext delayQueueContext, DelayMessageHandler delayMessageHandler) {
        this.topic = delayMessageHandler.getTopic();
        this.delayQueueContext = delayQueueContext;
        this.delayMessageHandler = delayMessageHandler;
        connection = delayQueueContext.redisClientWrapper.connect();
        workThread = new Thread(this::listen, "Thread-RedisDelayQueueListener-" + topic);
        workThread.setDaemon(true);
    }

    TopicListener(String topic, DelayQueueContext delayQueueContext) {
        this.topic = topic;
        this.delayQueueContext = delayQueueContext;
        this.delayMessageHandler = null;
        connection = delayQueueContext.redisClientWrapper.connect();
        workThread = new Thread(this::listen, "Thread-RedisDelayQueueListener-" + topic);
        workThread.setDaemon(true);
    }

    public String getTopic() {
        return topic;
    }

    void startListen() {
        workThread.start();
    }

    @Override
    public void close() {
        isListen = false;
        try {
            connection.close();
        } catch (Exception ignore) {
        }
    }

    public void addCustomizeHandler(String messageId, Consumer<DelayMessage> handler) {
        customizeHandler.put(messageId, handler);
    }

    public void removeCustomizeHandler(String messageId) {
        customizeHandler.remove(messageId);
    }

    public long getOffset() {
        return offsetWindow.getOffset();
    }

    void listen() {
        logger.info("Listening on topic [{}] is started [{}]", topic, uuid);

        while (delayQueueContext.isRunning && isListen) {
            String message;
            try {
                KeyValue<String, String> value =
                    connection.syncBlpop(300L, delayQueueContext.delayQueueTopicPrefix + topic);
                if (!value.hasValue()) {
                    continue;
                }
                message = value.getValue();
            } catch (RedisCommandTimeoutException ignore) {
                continue;
            } catch (RedisException e) {
                if (isListen && delayQueueContext.isRunning && e.getMessage().contains("close")) {
                    connection = delayQueueContext.redisClientWrapper.connect();
                }
                continue;
            }
            logger.info("RedisDelayQueue listen on Topic [{}] message arrived", topic);
            DelayMessage delayMessage = JsonUtils.fromJson(message, DelayMessage.class);
            long time = System.currentTimeMillis();
            delayMessage.setScheduleTime(time);
            time -= (delayMessage.getTriggerTime() + delayMessage.getDelayTimeInMillis());
            offsetWindow.put(time, 500L);
            if (time > 1000L) {
                logger.warn("RedisDelayQueue schedule offset on message [{}] is {}ms", delayMessage.getMessageId(), time);
            }
            try {
                if (customizeHandler.containsKey(delayMessage.getMessageId())) {
                    customizeHandler.remove(delayMessage.getMessageId()).accept(delayMessage);
                } else if (null != delayMessageHandler) {
                    delayMessageHandler.handle(delayMessage);
                } else {
                    logger.warn("RedisDelayQueue messageId [{}] has no handler, message: {}", delayMessage.getMessageId(), message);
                }
            } catch (Exception e) {
                logger.warn("RedisDelayQueue listen on Topic [{}] throws an exception {}", topic, e.getMessage());
                if (null != delayMessageHandler) {
                    delayMessageHandler.handleException(delayMessage, e);
                }
            }
        }

        logger.info("Listening on topic [{}] stopped [{}]", topic, uuid);
    }
}
