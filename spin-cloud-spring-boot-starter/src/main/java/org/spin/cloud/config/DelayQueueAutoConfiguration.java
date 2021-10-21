package org.spin.cloud.config;

import org.spin.cloud.config.properties.DelayQueueProperties;
import org.spin.data.delayqueue.DelayMessageHandler;
import org.spin.data.delayqueue.RedisDelayQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DelayQueueProperties.class)
@ConditionalOnClass(name = "io.lettuce.core.cluster.RedisClusterClient")
@ConditionalOnProperty(name = "spin.delay-queue.enable", havingValue = "true")
public class DelayQueueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisDelayQueue redisDelayQueue(DelayQueueProperties delayQueueProperties,
                                           @Nullable List<DelayMessageHandler> messageHandlers) {
        RedisDelayQueue redisDelayQueue = new RedisDelayQueue(delayQueueProperties);
        redisDelayQueue.setMessageHandlers(messageHandlers);
        return redisDelayQueue;
    }
}
