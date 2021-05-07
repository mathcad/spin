package org.spin.cloud.seata.feign;

import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;

public class SeataFeignObjectWrapper {
    private static final Logger logger = LoggerFactory.getLogger(SeataFeignObjectWrapper.class);

    private final BeanFactory beanFactory;

    private LoadBalancerClientFactory loadBalancerClientFactory;
    private LoadBalancerProperties properties;
//
//    private SpringClientFactory springClientFactory;

    private boolean continued = true;

    public SeataFeignObjectWrapper(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    Object wrap(Object bean) {
        if (continued && bean instanceof Client && !(bean instanceof SeataFeignClient)) {
            if (bean instanceof FeignBlockingLoadBalancerClient) {
                FeignBlockingLoadBalancerClient client = (FeignBlockingLoadBalancerClient) bean;
                try {
                    return new SeataFeignBlockingLoadBalancerClient(client.getDelegate(), beanFactory.getBean(BlockingLoadBalancerClient.class),
                        this, properties(), factory());
                } catch (BeansException ignore) {
                    logger.warn("Seata Feign初始化异常");
                    continued = false;
                    return bean;
                }
            }
            return new SeataFeignClient(this.beanFactory, (Client) bean);
        }
        return bean;
    }

    LoadBalancerClientFactory factory() {
        if (loadBalancerClientFactory == null) {
            try {
                loadBalancerClientFactory = beanFactory.getBean(LoadBalancerClientFactory.class);
            } catch (BeansException ignore) {
                // ignore
            }
        }
        return loadBalancerClientFactory;
    }

    LoadBalancerProperties properties() {
        if (properties == null) {
            try {
                properties = beanFactory.getBean(LoadBalancerProperties.class);
            } catch (BeansException ignore) {
                // ignore
            }
        }

        return properties;
    }

//    SpringClientFactory clientFactory() {
//        if (this.springClientFactory == null) {
//            this.springClientFactory = this.beanFactory
//                .getBean(SpringClientFactory.class);
//        }
//        return this.springClientFactory;
//    }

}
