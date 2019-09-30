package org.spin.common.config;

import org.spin.common.redis.lock.RedisDistributedLock;
import org.spin.core.concurrent.DistributedLock;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 扩展的Redis自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
@ConditionalOnClass(name = {"org.springframework.data.redis.core.StringRedisTemplate"})
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisExtendsAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public DistributedLock redisDistributedLock(StringRedisTemplate redisTemplate) {
        return new RedisDistributedLock(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public InitializingBean initRedisTemplate(RedisTemplate<?, ?> redisTemplate) {
        return () -> {
            RedisSerializer<?> redisSerializer = new StringRedisSerializer();
            redisTemplate.setKeySerializer(redisSerializer);
            redisTemplate.setHashKeySerializer(redisSerializer);
        };
    }
}
