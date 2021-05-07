package org.spin.jpa.configuration;

import org.hibernate.engine.spi.SessionImplementor;
import org.spin.datasource.schema.Schema;
import org.spin.jpa.initiator.JpaUtilAble;
import org.spin.jpa.initiator.JpaUtilInitiator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
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

    @Bean
    @ConditionalOnClass(name = "org.spin.datasource.schema.Schema")
    @ConditionalOnBean(EntityManager.class)
    public InitializingBean initJpaConnectionProvider(EntityManager entityManager) {
        return () -> {
            try {
                Class.forName("org.spin.datasource.schema.Schema");
                Schema.setTransactionSyncConnectionProvider(() -> entityManager.unwrap(SessionImplementor.class).connection());
            } catch (ClassNotFoundException e) {
                // do nothing
            }
        };
    }

}
