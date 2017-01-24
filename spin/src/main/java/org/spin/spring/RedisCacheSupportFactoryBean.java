package org.spin.spring;

import org.spin.redis.RedisCacheSupport;
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
public class RedisCacheSupportFactoryBean implements FactoryBean<RedisCacheSupport> {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public RedisCacheSupport<?> getObject() throws Exception {
        if (null != redisConnectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            template.afterPropertiesSet();

            RedisCacheSupport<Object> redisCacheSupport = new RedisCacheSupport<>();
            redisCacheSupport.setRedisTemplate(template);
            redisCacheSupport.setRedisSerializer(new JdkSerializationRedisSerializer());
            return redisCacheSupport;
        }
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return RedisCacheSupport.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}