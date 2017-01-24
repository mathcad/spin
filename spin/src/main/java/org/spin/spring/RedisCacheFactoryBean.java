package org.spin.spring;

import org.spin.cache.RedisCache;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * Redis支持Bean
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Component
public class RedisCacheFactoryBean implements FactoryBean<RedisCache> {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public RedisCache<?> getObject() throws Exception {
        if (null != redisConnectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            template.afterPropertiesSet();

            RedisCache<Object> redisCache = new RedisCache<>();
            redisCache.setRedisTemplate(template);
            redisCache.setRedisSerializer(new JdkSerializationRedisSerializer());
            return redisCache;
        }
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return RedisCacheFactoryBean.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}