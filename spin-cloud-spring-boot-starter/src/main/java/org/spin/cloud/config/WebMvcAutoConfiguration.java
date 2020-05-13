package org.spin.cloud.config;

import org.spin.cloud.web.config.RequestMappingBeanValidator;
import org.spin.cloud.web.handler.FieldPermissionReturnValueModifier;
import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.cloud.web.interceptor.UserAuthInterceptor;
import org.spin.core.util.CollectionUtils;
import org.spin.web.InternalWhiteList;
import org.spin.web.converter.JsonHttpMessageConverter;
import org.spin.web.handler.ReplacementReturnValueHandler;
import org.spin.web.handler.RequestResponseBodyModifier;
import org.spin.web.handler.WrappedRequestResponseBodyProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description web 层控制
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/13.</p>
 */
@Configuration
@ComponentScan(basePackages = {"org.spin.cloud.web.handler", "org.spin.web.handler"})
public class WebMvcAutoConfiguration implements WebMvcConfigurer {

    private static final JsonHttpMessageConverter JSON_HTTP_MESSAGE_CONVERTER = new JsonHttpMessageConverter();

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, JSON_HTTP_MESSAGE_CONVERTER);
        return restTemplate;
    }

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> encodingFilterRegistration() {
        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CharacterEncodingFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("encoding", "UTF-8");
        registration.setName("encodingFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(JSON_HTTP_MESSAGE_CONVERTER);
        return new HttpMessageConverters(true, messageConverters);
    }

    @Bean
    public RequestMappingBeanValidator requestMappingBeanValidator() {
        return new RequestMappingBeanValidator();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(new GrayInterceptor()).addPathPatterns("/**").order(Ordered.HIGHEST_PRECEDENCE);

        registry.addInterceptor(new UserAuthInterceptor()).addPathPatterns("/**")
            .excludePathPatterns("/swagger-ui.html/**", "/webjars/**", "/swagger-resources/**", "/error", "/job/executor/**");
    }

    @Bean
    public FieldPermissionReturnValueModifier fieldPermissionReturnValueModifier() {
        return new FieldPermissionReturnValueModifier();
    }

    @Bean
    @ConditionalOnBean(RequestMappingHandlerAdapter.class)
    public InitializingBean procReturnValueHandlerBean(RequestMappingHandlerAdapter handlerAdapter, List<ReplacementReturnValueHandler> customerHandlers, List<RequestResponseBodyModifier> modifiers) {
        return () -> {
            handlerAdapter.afterPropertiesSet();
            List<HandlerMethodReturnValueHandler> originHandlers = handlerAdapter.getReturnValueHandlers();
            if (CollectionUtils.isEmpty(originHandlers)) {
                return;
            }

            RequestResponseBodyMethodProcessor handler = (RequestResponseBodyMethodProcessor) originHandlers.stream().filter(it -> it instanceof RequestResponseBodyMethodProcessor).findFirst().orElse(null);
            if (null == handler) {
                return;
            }

            List<ReplacementReturnValueHandler> collect = null == customerHandlers ? Collections.emptyList() : customerHandlers.stream().sorted(Comparator.comparingInt(Ordered::getOrder)).collect(Collectors.toList());
            List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(originHandlers.size() + 1);
            handlers.add(new WrappedRequestResponseBodyProcessor(handler, modifiers));
            for (HandlerMethodReturnValueHandler originHandler : originHandlers) {
                ReplacementReturnValueHandler matched = getMatched(originHandler, collect);
                handlers.add(null != matched ? matched : originHandler);
            }
            handlerAdapter.setReturnValueHandlers(Collections.unmodifiableList(handlers));
        };
    }

    private ReplacementReturnValueHandler getMatched(HandlerMethodReturnValueHandler handler, List<ReplacementReturnValueHandler> replacement) {
        return replacement.stream().filter(h -> h.replace().equals(handler.getClass())).findFirst().orElse(null);
    }

    @Value("${internal.whiteList:}")
    public void refreshWhiteList(String hosts) {
        InternalWhiteList.refreshList(hosts);
    }
}
