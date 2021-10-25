package org.spin.data.delayqueue;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.concurrent.Async;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MapUtils;
import org.spin.data.redis.RedisConnectionWrapper;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

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
    private final DelayQueueContext delayQueueContext;
    private final String workPoolName;
    private final Map<String, DelayMessageHandler> topicHandlers = MapUtils.ofMap();
    private final GroupScheduledHandler groupScheduledHandler;
    private final String[] topics;
    private RedisConnectionWrapper<String, String> connection;
    private final Thread watchThread;
    private volatile boolean isListen = true;
    private final OffsetWindow offsetWindow = new OffsetWindow();

    TopicListener(DelayQueueContext delayQueueContext,
                  List<DelayMessageHandler> handlerList,
                  int corePoolSize,
                  int maxPoolSize,
                  long keepAliveTimeInMs,
                  int queueSize) {
        this.delayQueueContext = delayQueueContext;
        Set<String> t = new HashSet<>();
        workPoolName = "RedisDelayQueueWorkPool-" + delayQueueContext.delayQueueName;
        if (delayQueueContext.groupScheduleEnabled) {
            groupScheduledHandler = new GroupScheduledHandler(delayQueueContext.scheduleGroupId);
            t.add(delayQueueContext.delayQueueTopicPrefix + groupScheduledHandler.getTopic());
        } else {
            groupScheduledHandler = null;
        }

        if (CollectionUtils.isNotEmpty(handlerList)) {
            for (DelayMessageHandler handler : handlerList) {
                topicHandlers.put(handler.getTopic(), handler);
                t.add(delayQueueContext.delayQueueTopicPrefix + handler.getTopic());
            }
        }

        if (t.size() > 0) {
            Async.initThreadPool(workPoolName,
                corePoolSize, maxPoolSize, keepAliveTimeInMs, queueSize,
                new ThreadPoolExecutor.CallerRunsPolicy());
            topics = t.toArray(new String[0]);
            connection = delayQueueContext.redisClientWrapper.connect();
            watchThread = new Thread(this::listen, "Thread-RedisDelayQueueListener");
            watchThread.setDaemon(true);
            watchThread.start();
        } else {
            topics = null;
            watchThread = null;
            logger.info("There's no delay queue topic to watch, listener was canceled");
        }
    }

    @Override
    public void close() {
        if (watchThread != null) {
            isListen = false;
            try {
                connection.close();
            } catch (Exception ignore) {
            }
            Async.shutdown(workPoolName);
        }
    }

    public long getOffset() {
        return offsetWindow.getOffset();
    }

    void listen() {
        logger.info("Listener is started [{}]", uuid);

        while (delayQueueContext.isRunning && isListen) {
            String message;
            try {
                KeyValue<String, String> value =
                    connection.syncBlpop(delayQueueContext.redisClientWrapper.getLettuceRedisProperties().getTimeout().getSeconds(), topics);
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
            DelayMessage delayMessage = JsonUtils.fromJson(message, DelayMessage.class);
            long time = System.currentTimeMillis();
            logger.info("RedisDelayQueue listen on Topic [{}] message arrived", delayMessage.getTopic());
            delayMessage.setScheduleTime(time);
            Async.execute(workPoolName, () -> {
                long sTime = time - (delayMessage.getTriggerTime() + delayMessage.getDelayTimeInMillis());
                offsetWindow.put(sTime, 500L);
                if (sTime > 1000L) {
                    logger.warn("RedisDelayQueue schedule offset on message [{}] is {}ms", delayMessage.getMessageId(), sTime);
                }
                DelayMessageHandler handler = null;
                try {
                    if (delayQueueContext.groupScheduleEnabled
                        && delayMessage.getTopic().equals(delayQueueContext.scheduleGroupId)) {
                        handler = groupScheduledHandler;
                        groupScheduledHandler.handle(delayMessage);
                    } else {
                        handler = topicHandlers.get(delayMessage.getTopic());
                        if (null != handler) {
                            handler.handle(delayMessage.getPayload());
                        } else {
                            logger.warn("RedisDelayQueue messageId [{}] has no handler, message: {}", delayMessage.getMessageId(), message);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("RedisDelayQueue listen on Topic [{}] throws an exception {}", delayMessage.getTopic(), e.getMessage());
                    if (null != handler) {
                        handler.handleException(delayMessage.getPayload(), e);
                    }
                }
            });
        }

        logger.info("Listener stopped [{}]", uuid);
    }
}
