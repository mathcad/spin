package org.spin.cloud.seata;

import feign.Client;
import org.spin.cloud.config.SentinelAutoConfiguration;
import org.spin.cloud.seata.feign.SeataBeanPostProcessor;
import org.spin.cloud.seata.feign.SeataContextBeanPostProcessor;
import org.spin.cloud.seata.feign.SeataFeignObjectWrapper;
import org.spin.cloud.seata.rest.SeataRestTemplateInterceptor;
import org.spin.cloud.seata.web.SeataHandlerInterceptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Seata自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/9/16</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = {"io.seata.core.context.RootContext"})
public class SeataAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Client.class)
    @AutoConfigureBefore({FeignAutoConfiguration.class, SentinelAutoConfiguration.class})
    public static class SeataFeignClientAutoConfiguration {

        @Bean
        SeataBeanPostProcessor seataBeanPostProcessor(
            SeataFeignObjectWrapper seataFeignObjectWrapper) {
            return new SeataBeanPostProcessor(seataFeignObjectWrapper);
        }

        @Bean
        SeataContextBeanPostProcessor seataContextBeanPostProcessor(
            BeanFactory beanFactory) {
            return new SeataContextBeanPostProcessor(beanFactory);
        }

        @Bean
        SeataFeignObjectWrapper seataFeignObjectWrapper(BeanFactory beanFactory) {
            return new SeataFeignObjectWrapper(beanFactory);
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class SeataRestTemplateAutoConfiguration {

        @Bean
        public SeataRestTemplateInterceptor seataRestTemplateInterceptor() {
            return new SeataRestTemplateInterceptor();
        }

        @Autowired(required = false)
        private Collection<RestTemplate> restTemplates;

        @Autowired
        private SeataRestTemplateInterceptor seataRestTemplateInterceptor;

        @PostConstruct
        public void init() {
            if (this.restTemplates != null) {
                for (RestTemplate restTemplate : restTemplates) {
                    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(
                        restTemplate.getInterceptors());
                    interceptors.add(this.seataRestTemplateInterceptor);
                    restTemplate.setInterceptors(interceptors);
                }
            }
        }
    }

    @ConditionalOnWebApplication
    @Configuration(proxyBeanMethods = false)
    public static class SeataHandlerInterceptorConfiguration implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new SeataHandlerInterceptor()).addPathPatterns("/**");
        }
    }
}
