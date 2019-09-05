package org.spin.common.config;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.optionals.OptionalDecoder;
import org.spin.common.feign.*;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
@AutoConfigureBefore(FeignClientsConfiguration.class)
@EnableSpinFeignClients("org.spin.common.service.remote")
public class FeignAutoConfiguration {

    private final ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired
    public FeignAutoConfiguration(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Bean
    public Decoder feignDecoder() {
        return new OptionalDecoder(new ResponseEntityDecoder(new RestfulHandledDecoder(this.messageConverters)));
    }

    @Configuration
    @ConditionalOnClass(name = "feign.hystrix.HystrixFeign")
    protected static class HystrixFeignTargeterConfiguration {

        @Bean("spinFeignTargeter")
        @ConditionalOnMissingBean
        public Targeter feignTargeter() {
            return new HystrixTargeter();
        }

    }

    @Configuration
    @ConditionalOnMissingClass("feign.hystrix.HystrixFeign")
    protected static class DefaultFeignTargeterConfiguration {

        @Bean("spinFeignTargeter")
        @ConditionalOnMissingBean
        public Targeter feignTargeter() {
            return new DefaultTargeter();
        }

    }

    @Bean
    public RequestInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }

    @Bean
    @ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
    public Encoder feignEncoder() {
        return new AuthSpringEncoder(messageConverters);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
    public Encoder feignEncoderPageable() {
        return new PageableSpringEncoder(new AuthSpringEncoder(this.messageConverters));
    }
}
