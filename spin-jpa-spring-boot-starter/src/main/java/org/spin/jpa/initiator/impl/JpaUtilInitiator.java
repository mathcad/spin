package org.spin.jpa.initiator.impl;

import org.spin.jpa.R;
import org.spin.jpa.strategy.GetEntityManagerFactoryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JpaUtilInitiator implements org.spin.jpa.initiator.JpaUtilInitiator {

    @Autowired
    private GetEntityManagerFactoryStrategy getEntityManagerFactoryStrategy;

    @Override
    public void initialize(ApplicationContext applicationContext) {
        R.setGetEntityManagerFactoryStrategy(getEntityManagerFactoryStrategy);
        R.setApplicationContext(applicationContext);
    }

}
