package org.spin.jpa;

import org.spin.jpa.initiator.JpaUtilAble;
import org.spin.jpa.initiator.JpaUtilInitiator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 模块配置类
 */
@Configuration
@ComponentScan(basePackages = {"org.spin.jpa"})
public class LinqConfiguration implements ApplicationContextAware {

    @Autowired
    List<JpaUtilInitiator> jpaUtilInitiators;

    @Autowired(required = false)
    List<JpaUtilAble> jpaUtilAbles;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        for (JpaUtilInitiator jpaUtilInitiator : jpaUtilInitiators) {
            jpaUtilInitiator.initialize(applicationContext);
        }
        if (jpaUtilAbles != null) {
            for (JpaUtilAble jpaUtilAble : jpaUtilAbles) {
                jpaUtilAble.afterPropertiesSet(applicationContext);
            }
        }
    }

}
