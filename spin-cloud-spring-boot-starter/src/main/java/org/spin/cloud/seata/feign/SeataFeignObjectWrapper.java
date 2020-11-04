package org.spin.cloud.seata.feign;

import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

public class SeataFeignObjectWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(SeataFeignObjectWrapper.class);

    private final BeanFactory beanFactory;

    private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;

    private SpringClientFactory springClientFactory;

    public SeataFeignObjectWrapper(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    Object wrap(Object bean) {
        if (bean instanceof Client && !(bean instanceof SeataFeignClient)) {
            if (bean instanceof LoadBalancerFeignClient) {
                LoadBalancerFeignClient client = ((LoadBalancerFeignClient) bean);
                return new SeataLoadBalancerFeignClient(client.getDelegate(), factory(),
                    clientFactory(), this);
            }
            if (bean instanceof FeignBlockingLoadBalancerClient) {
                FeignBlockingLoadBalancerClient client = (FeignBlockingLoadBalancerClient) bean;
                return new SeataFeignBlockingLoadBalancerClient(client.getDelegate(),
                    beanFactory.getBean(BlockingLoadBalancerClient.class), this);
            }
            return new SeataFeignClient(this.beanFactory, (Client) bean);
        }
        return bean;
    }

    CachingSpringLoadBalancerFactory factory() {
        if (this.cachingSpringLoadBalancerFactory == null) {
            this.cachingSpringLoadBalancerFactory = this.beanFactory
                .getBean(CachingSpringLoadBalancerFactory.class);
        }
        return this.cachingSpringLoadBalancerFactory;
    }

    SpringClientFactory clientFactory() {
        if (this.springClientFactory == null) {
            this.springClientFactory = this.beanFactory
                .getBean(SpringClientFactory.class);
        }
        return this.springClientFactory;
    }

}
