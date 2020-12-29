package org.spin.cloud.seata.feign;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.lang.NonNull;

/**
 * @author xiaojing
 */
public class SeataContextBeanPostProcessor implements BeanPostProcessor {

    private final BeanFactory beanFactory;

    private SeataFeignObjectWrapper seataFeignObjectWrapper;

    public SeataContextBeanPostProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
        throws BeansException {
        if (bean instanceof FeignContext && !(bean instanceof SeataFeignContext)) {
            return new SeataFeignContext(getSeataFeignObjectWrapper(),
                (FeignContext) bean);
        }
        return bean;
    }

    private SeataFeignObjectWrapper getSeataFeignObjectWrapper() {
        if (this.seataFeignObjectWrapper == null) {
            this.seataFeignObjectWrapper = this.beanFactory.getBean(SeataFeignObjectWrapper.class);
        }
        return this.seataFeignObjectWrapper;
    }

}
