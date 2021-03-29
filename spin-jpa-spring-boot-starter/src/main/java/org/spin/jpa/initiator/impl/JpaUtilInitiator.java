package org.spin.jpa.initiator.impl;

import org.spin.jpa.JpaUtil;
import org.spin.jpa.strategy.GetEntityManagerFactoryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Yang (mailto:kevin.yang@bstek.com)
 * @since 2017年7月3日
 */
@Component
public class JpaUtilInitiator implements org.spin.jpa.initiator.JpaUtilInitiator {

    @Autowired
    private GetEntityManagerFactoryStrategy getEntityManagerFactoryStrategy;

    @Override
    public void initialize(ApplicationContext applicationContext) {
        JpaUtil.setGetEntityManagerFactoryStrategy(getEntityManagerFactoryStrategy);
        JpaUtil.setApplicationContext(applicationContext);
    }

}
