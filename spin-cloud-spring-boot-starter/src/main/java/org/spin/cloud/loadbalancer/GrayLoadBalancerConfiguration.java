package org.spin.cloud.loadbalancer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.loadbalancer.core.RetryAwareServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/12/29</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
@ConditionalOnDiscoveryEnabled
public class GrayLoadBalancerConfiguration {

    @Bean
    public BeanPostProcessor serviceInstanceSupplierModifier() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof ServiceInstanceListSupplier && !(bean instanceof GrayServiceInstanceListSupplier) && !(bean instanceof RetryAwareServiceInstanceListSupplier)) {
                    return new GrayServiceInstanceListSupplier((ServiceInstanceListSupplier) bean);
                }
                return bean;
            }
        };
    }
}
