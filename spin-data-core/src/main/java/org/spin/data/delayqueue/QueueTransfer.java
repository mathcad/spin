package org.spin.data.delayqueue;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.ArrayUtils;

import java.util.concurrent.locks.LockSupport;

class QueueTransfer {
    private static final Logger logger = LoggerFactory.getLogger(QueueTransfer.class);
    private static final String TRANSFER_SCRIPT
        = "local t = redis.call(\"TIME\")\n" +
        "local now = t[1] * 1000 + math.ceil(t[2] / 1000)\n" +
        "local elements = redis.call(\"ZRANGEBYSCORE\", KEYS[1] .. \"PriorityQueue\", \"(0\", now)\n" +
        "if table.getn(elements) > 0 then\n" +
        "    redis.call(\"ZREMRANGEBYSCORE\", KEYS[1] .. \"PriorityQueue\", \"(0\", now)\n" +
        "    for _, idAndTopic in pairs(elements) do\n" +
        "        local id = string.sub(idAndTopic, 0, 36)\n" +
        "        local topic = string.sub(idAndTopic, 37)\n" +
        "        local data = redis.call(\"HGET\", KEYS[1] .. \"Data\", id)\n" +
        "        redis.call(\"RPUSH\", KEYS[1] .. \"Topic:\" .. topic, data)\n" +
        "        redis.call(\"HDEL\", KEYS[1] .. \"Data\", id)\n" +
        "    end\n" +
        "end\n" +
        "\n" +
        "local next = redis.call(\"ZRANGEBYSCORE\", KEYS[1] .. \"PriorityQueue\", now, \"+inf\", \"withscores\", \"limit\", 0, 1)\n" +
        "if table.getn(next) > 0 then\n" +
        "    t = redis.call(\"TIME\")\n" +
        "    now = t[1] * 1000 + math.ceil(t[2] / 1000)\n" +
        "    return math.max(next[2] - now, 1)\n" +
        "end\n" +
        "\n" +
        "return 0";

    private final Thread workThread;
    private final DelayQueueContext delayQueueContext;

    QueueTransfer(DelayQueueContext delayQueueContext) {
        this.delayQueueContext = delayQueueContext;
        delayQueueContext.pubsubConnection.addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                unPark();
            }
        });
        delayQueueContext.pubsubConnection.subscribe(delayQueueContext.notifierChannel);
        workThread = new Thread(this::run, "Thread-RedisDelayQueueTransfer");
        workThread.start();
    }

    private void run() {
        logger.info("RedisDelayQueue Transfer worker is begin");
        while (delayQueueContext.isRunning) {
            try {
                Long nextTime = delayQueueContext.connection.syncEval(TRANSFER_SCRIPT, ScriptOutputType.INTEGER, ArrayUtils.ofArray(delayQueueContext.delayQueueKeyPrefix));

                // 无数据时等待1小时
                if (null == nextTime || 0L == nextTime) {
                    nextTime = 3600_000L;
                }
                LockSupport.parkNanos(nextTime * 1_000_000L);
            } catch (Exception e) {
                logger.error("RedisDelayQueue Transfer worker throws an exception", e);
                LockSupport.parkNanos(10L * 1_000_000L);
            }
        }

        logger.info("RedisDelayQueue Transfer worker was stopped");
    }

    void unPark() {
        LockSupport.unpark(workThread);
    }
}
