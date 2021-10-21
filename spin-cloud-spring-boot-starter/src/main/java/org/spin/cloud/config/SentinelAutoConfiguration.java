package org.spin.cloud.config;

import feign.Feign;
import org.spin.cloud.seata.feign.SeataFeignClient;
import org.spin.cloud.sentinel.SentinelFeign;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = {"com.alibaba.csp.sentinel.SphU", "feign.Feign"})
@AutoConfigureBefore(name = {"com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration"})
public class SentinelAutoConfiguration {

    @Bean
    @Scope("prototype")
    @ConditionalOnProperty(name = "feign.sentinel.enabled", havingValue = "true", matchIfMissing = true)
    public Feign.Builder feignSentinelBuilder(BeanFactory beanFactory) {
        try {
            Class.forName("io.seata.core.context.RootContext");
            return SentinelFeign.builder().client(new SeataFeignClient(beanFactory));
        } catch (ClassNotFoundException e) {
            return SentinelFeign.builder();
        }
    }

}
