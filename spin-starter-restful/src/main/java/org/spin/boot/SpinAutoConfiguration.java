package org.spin.boot;

import org.spin.boot.properties.SpinWebPorperties;
import org.spin.boot.properties.WxConfigProperties;
import org.spin.core.util.JsonUtils;
import org.spin.data.cache.RedisCache;
import org.spin.web.converter.JsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.MultipartConfigElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 框架自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
@EnableConfigurationProperties({SpinWebPorperties.class, WxConfigProperties.class})
@ComponentScan("org.spin")
public class SpinAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisCache<?> redisCache(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();

        RedisCache<Object> redisCache = new RedisCache<>();
        redisCache.setRedisTemplate(template);
        redisCache.setRedisSerializer(new JdkSerializationRedisSerializer());
        return redisCache;
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
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Bean
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        JsonHttpMessageConverter jsonHttpMessageConverter = new JsonHttpMessageConverter();
        jsonHttpMessageConverter.setGson(JsonUtils.getDefaultGson());
        messageConverters.add(jsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement(SpinWebPorperties webPorperties) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(webPorperties.getMaxUploadSize() * 1024 * 1024);
        return factory.createMultipartConfig();
    }
}
