package org.spin.cloud.config;

import org.spin.cloud.config.properties.SpinRedisProperties;
import org.spin.core.concurrent.DistributedLock;
import org.spin.data.delayqueue.DelayMessageHandler;
import org.spin.data.delayqueue.RedisDelayQueue;
import org.spin.data.lock.RedisDistributedLock;
import org.spin.data.redis.RedisClientWrapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 扩展的Redis自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpinRedisProperties.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnClass(name = "io.lettuce.core.cluster.RedisClusterClient")
@ConditionalOnProperty(name = "spin.redis.enable", havingValue = "true")
public class RedisExtendsAutoConfiguration {

    @Bean
    public RedisClientWrapper redisClientWrapper(SpinRedisProperties spinRedisProperties) {
        return new RedisClientWrapper(spinRedisProperties);
    }

    @Bean
    @ConditionalOnBean(RedisClientWrapper.class)
    public DistributedLock redisDistributedLock(RedisClientWrapper redisClientWrapper) {
        return new RedisDistributedLock(redisClientWrapper);
    }

    @Bean
    @ConditionalOnBean(RedisClientWrapper.class)
    @ConditionalOnProperty(name = "spin.redis.delay-queue-name")
    public RedisDelayQueue redisDelayQueue(SpinRedisProperties spinRedisProperties,
                                           RedisClientWrapper redisClientWrapper,
                                           @Nullable List<DelayMessageHandler> messageHandlers) {
        RedisDelayQueue redisDelayQueue = new RedisDelayQueue(spinRedisProperties.getDelayQueueName(),
            redisClientWrapper);
        redisDelayQueue.setMessageHandlers(messageHandlers);
        return redisDelayQueue;
    }
}
