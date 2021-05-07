package org.spin.cloud.config;

import org.spin.cloud.loadbalancer.GrayLoadBalancerConfiguration;
import org.spin.cloud.loadbalancer.SpinRetryableFeignBlockingLoadBalancerClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientSpecification;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.Collections;

/**
 * 负载均衡自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@AutoConfigureBefore(LoadBalancerClientConfiguration.class)
public class SpinLoadBalancerConfiguration {

    @Bean
    public BeanPostProcessor loadBalancerModifier() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

                if (bean instanceof LoadBalancerClientFactory) {
                    ((LoadBalancerClientFactory) bean).setConfigurations(Collections.singletonList(new LoadBalancerClientSpecification("default." + GrayLoadBalancerConfiguration.class.getName(), new Class[]{GrayLoadBalancerConfiguration.class})));
                } else if (bean instanceof RetryableFeignBlockingLoadBalancerClient && !(bean instanceof SpinRetryableFeignBlockingLoadBalancerClient)) {
                    return new SpinRetryableFeignBlockingLoadBalancerClient((RetryableFeignBlockingLoadBalancerClient) bean);
                }
                return bean;
            }
        };
    }

}
