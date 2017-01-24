package org.infrastructure.spring;

import org.infrastructure.redis.RedisCacheSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * Created by Arvin on 2017/1/23.
 */
//@Component
public class BaseBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        if (null != redisConnectionFactory && !applicationContext.containsBean("redisCacheSupport")) {
//            RedisTemplate<String, Object> template = new RedisTemplate<>();
//            template.setConnectionFactory(redisConnectionFactory);
//            template.afterPropertiesSet();
//
//            DefaultListableBeanFactory acf = (DefaultListableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory();
//            BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(RedisCacheSupport.class);
//            bdb.addPropertyValue("redisTemplate", template);
//            bdb.addPropertyValue("redisSerializer", new JdkSerializationRedisSerializer());
//            AbstractBeanDefinition abd = bdb.getBeanDefinition();
//            acf.registerBeanDefinition("redisCacheSupport", abd);
//        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}