package org.spin.cloud.config;

import com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration;
import com.alibaba.csp.sentinel.SphU;
import feign.Feign;
import org.spin.cloud.sentinel.SentinelFeign;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SphU.class, Feign.class})
@AutoConfigureBefore({SentinelFeignAutoConfiguration.class})
public class SentinelAutoConfiguration {

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "feign.sentinel.enabled")
    public Feign.Builder feignSentinelBuilder() {
        return SentinelFeign.builder();
    }

}
