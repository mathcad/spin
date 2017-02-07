package org.spin.spring;

import org.spin.cache.RedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

/**
 * Redis支持Bean
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
public class SpinConfiguration {

    @Bean
    public RedisCache<?> redisCache(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();

        RedisCache<Object> redisCache = new RedisCache<>();
        redisCache.setRedisTemplate(template);
        redisCache.setRedisSerializer(new JdkSerializationRedisSerializer());
        return redisCache;
    }
}