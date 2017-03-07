package org.spin.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by xuweinan on 2017/1/23.
 */
//@Component
public class SpinBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        if (null != redisConnectionFactory && !applicationContext.containsBean("redisCacheSupport")) {
//            RedisTemplate<String, Object> template = new RedisTemplate<>();
//            template.setConnectionFactory(redisConnectionFactory);
//            template.afterPropertiesSet();
//
//            DefaultListableBeanFactory acf = (DefaultListableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory();
//            BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(RedisCache.class);
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