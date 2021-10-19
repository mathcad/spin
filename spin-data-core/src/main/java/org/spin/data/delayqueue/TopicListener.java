package org.spin.data.delayqueue;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class TopicListener {
    private static final Logger logger = LoggerFactory.getLogger(TopicListener.class);

    private final String uuid = UUID.randomUUID().toString();
    private final String topic;
    private final DelayQueueContext delayQueueContext;
    private StatefulConnection<String, String> connection;
    private final DelayMessageHandler delayMessageHandler;
    private final Thread workThread;
    private volatile boolean isListen = true;

    TopicListener(DelayQueueContext delayQueueContext, DelayMessageHandler delayMessageHandler) {
        this.delayQueueContext = delayQueueContext;
        this.delayMessageHandler = delayMessageHandler;
        topic = delayMessageHandler.getTopic();
        connection = delayQueueContext.redisClientWrapper.newConnection();
        workThread = new Thread(this::listen, "Thread-RedisDelayQueueListener-" + topic);
        workThread.setDaemon(true);
    }

    public String getTopic() {
        return topic;
    }

    void startListen() {
        workThread.start();
    }

    void stopListen() {
        isListen = false;
        try {
            connection.close();
        } catch (Exception ignore) {
        }
    }

    void listen() {
        logger.info("Listening on topic [{}] is started [{}]", topic, uuid);

        while (delayQueueContext.isRunning && isListen) {
            String message;
            try {
                KeyValue<String, String> value =
                    connection instanceof StatefulRedisClusterConnection ?
                        ((StatefulRedisClusterConnection<String, String>) connection).sync().blpop(300L, delayQueueContext.delayQueueTopicPrefix + topic)
                        : ((StatefulRedisConnection<String, String>) connection).sync().blpop(300L, delayQueueContext.delayQueueTopicPrefix + topic);
                if (!value.hasValue()) {
                    continue;
                }
                message = value.getValue();
            } catch (RedisCommandTimeoutException ignore) {
                continue;
            } catch (RedisException e) {
                if (isListen && delayQueueContext.isRunning && e.getMessage().contains("close")) {
                    connection = delayQueueContext.redisClientWrapper.newConnection();
                }
                continue;
            }
            try {

                logger.info("RedisDelayQueue listen on Topic [{}] message arrived", topic);
                delayMessageHandler.handle(message);
            } catch (Exception e) {
                logger.warn("RedisDelayQueue listen on Topic [{}] throws an exception {}", topic, e.getMessage());
                delayMessageHandler.handleException(message, e);
            }
        }

        logger.info("Listening on topic [{}] stopped [{}]", topic, uuid);
    }
}
