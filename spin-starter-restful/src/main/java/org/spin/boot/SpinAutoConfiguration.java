package org.spin.boot;

import org.hibernate.SessionFactory;
import org.spin.boot.properties.SpinDataProperties;
import org.spin.boot.properties.SpinWebPorperties;
import org.spin.core.auth.SecretManager;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.cache.RedisCache;
import org.spin.data.core.SQLLoader;
import org.spin.web.converter.JsonHttpMessageConverter;
import org.spin.web.filter.TokenResolveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.MultipartConfigElement;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * 框架自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
@EnableConfigurationProperties({SpinDataProperties.class, SpinWebPorperties.class})
@ComponentScan("org.spin")
public class SpinAutoConfiguration {

    @Autowired
    private SpinDataProperties dataProperties;

    @Bean
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
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(SQLLoader.class)
    public SQLLoader sqlLoader() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        SQLLoader loader = (SQLLoader) Class.forName(dataProperties.getSqlLoader()).getDeclaredConstructor().newInstance();
        if (StringUtils.isEmpty(dataProperties.getSqlUri())) {
            loader.setRootUri(dataProperties.getSqlUri());
        }
        loader.setTemplateResolver(dataProperties.getResolverObj());
        return loader;
    }

    @Bean(name = "sessionFactory")
    @ConditionalOnBean(DataSource.class)
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource, Properties hibernateConfig) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(("org.spin.data," + StringUtils.trimToEmpty(hibernateConfig.getProperty("hibernate.packages"))).split(","));
        if (null != dataProperties.getNamingStrategy()) {
            sessionFactory.setPhysicalNamingStrategy(dataProperties.getNamingStrategyObj());
        }

        sessionFactory.setHibernateProperties(hibernateConfig);
        return sessionFactory;
    }

    @Bean
    public PropertiesFactoryBean hibernateConfig() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("hibernate.properties"));
        return bean;
    }

    @Bean(name = "transactionManager")
    @ConditionalOnBean(SessionFactory.class)
    public PlatformTransactionManager getTransactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public FilterRegistrationBean encodingFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CharacterEncodingFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("encoding", "UTF-8");
        registration.setName("encodingFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean openSessionInViewFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OpenSessionInViewFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("sessionFactoryBeanName", "sessionFactory");
        registration.setName("openSessionInViewFilter");
        registration.setOrder(2);
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
    public FilterRegistrationBean apiFilter(SecretManager secretManager) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new TokenResolveFilter(secretManager));
        registration.addUrlPatterns("/*");
        registration.setName("tokenFilter");
        registration.setOrder(4);
        return registration;
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

//    @Bean
//    public InitializingBean platformInit() {
//        return () -> SpinContext.DEV_MODE = "dev".equals(env.getProperty("spring.profiles.active"));
//    }
}
