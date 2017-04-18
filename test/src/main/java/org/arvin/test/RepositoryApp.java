package org.arvin.test;

import org.spin.gson.JsonHttpMessageConverter;
import org.spin.jpa.extend.DataBaseConfiguration;
import org.spin.jpa.extend.ImprovedNamingStrategy;
import org.spin.spring.SpinConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CharacterEncodingFilter;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Arvin on 2016/9/14.
 */

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class, scanBasePackages = {"org.spin", "org.arvin"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
public class RepositoryApp {
    public static void main(String[] args) {
        SpringApplication.run(new Object[]{RepositoryApp.class, SpinConfiguration.class}, args);
    }

    @Bean
    public DataBaseConfiguration dbConfiguration() {
        DataBaseConfiguration dbConfiguration = new DataBaseConfiguration();
        dbConfiguration.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&autoReconnect=true&failOverReadOnly=false&characterEncoding=utf-8&&serverTimezone=UTC");
        dbConfiguration.setUsername("root");
        dbConfiguration.setPassword("admin");
        dbConfiguration.setMaxActive(5);
        dbConfiguration.setMinIdle(1);
        dbConfiguration.setInitialSize(1);
        dbConfiguration.setMaxWait(60000);
        dbConfiguration.setRemoveAbandoned(true);
        dbConfiguration.setRemoveAbandonedTimeout(120);
        dbConfiguration.setClientEncoding("UTF-8");
        dbConfiguration.setPackagesToScan("org.arvin.test", "org.spin");
        dbConfiguration.setNamingStrategy(new ImprovedNamingStrategy());
//        proper.setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");
        dbConfiguration.setShowSql("true");
        dbConfiguration.setHbm2ddl("none");
        dbConfiguration.setDialect("org.hibernate.dialect.MySQLDialect");
        return dbConfiguration;
    }

    //redis配置
    @Bean
    public RedisConnectionFactory getJedisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(200);
        poolConfig.setTestOnBorrow(true);

        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setUsePool(true);
        connectionFactory.setHostName("127.0.0.1");
        connectionFactory.setPort(6379);
        connectionFactory.setPoolConfig(poolConfig);
        return connectionFactory;
    }

    @Bean
    public FilterRegistrationBean openSessionInViewFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OpenSessionInViewFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("sessionFactoryBeanName", "sessionFactory");
        registration.setName("openSessionInViewFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean encodingFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CharacterEncodingFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("encoding", "UTF-8");
        registration.setName("encodingFilter");
        registration.setOrder(2);
        return registration;
    }

//    @Bean
//    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
//        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
//        shiroFilterFactoryBean.setSecurityManager(securityManager);
//        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
//        filterChainDefinitionMap.put("/**", "anon");
//        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
//        return shiroFilterFactoryBean;
//    }

    @Bean
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        JsonHttpMessageConverter jsonHttpMessageConverter = new JsonHttpMessageConverter();
//        jsonHttpMessageConverter.setGson(JSONUtils.getDefaultGson());
        messageConverters.add(jsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }
}
