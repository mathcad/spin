package org.arvin.test;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.hibernate.SessionFactory;
import org.spin.gson.JsonHttpMessageConverter;
import org.spin.jpa.core.SQLLoader;
import org.spin.jpa.sql.loader.ClasspathMdLoader;
import org.spin.jpa.sql.resolver.FreemarkerResolver;
import org.spin.shiro.AnyoneSuccessfulStrategy;
import org.spin.util.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CharacterEncodingFilter;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * Created by Arvin on 2016/9/14.
 */

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class, scanBasePackages = {"org.spin", "org.arvin"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
public class RepositoryApp {
    public static void main(String[] args) {
        SpringApplication.run(RepositoryApp.class, args);
    }

    @Bean
    public SQLLoader sqlLoader() {
        SQLLoader loader = new ClasspathMdLoader();
        loader.setTemplateResolver(new FreemarkerResolver());
        return loader;
    }

    @Bean
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://192.168.20.235:3306/gsh56tms?useUnicode=true&autoReconnect=true&failOverReadOnly=false");
        dataSource.setUsername("tms");
        dataSource.setPassword("tms56");
        dataSource.setMaxActive(5);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(1);
        dataSource.setMaxWait(60000);
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(120);
        Properties proper = new Properties();
        proper.setProperty("clientEncoding", "UTF-8");
        dataSource.setConnectProperties(proper);
        return dataSource;
    }

    @Autowired
    @Bean(name = "sessionFactory")
    public LocalSessionFactoryBean getSessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPhysicalNamingStrategy(new SpringPhysicalNamingStrategy());
        sessionFactory.setPackagesToScan("org.arvin.test", "org.spin");
        Properties proper = new Properties();
//        proper.setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");
        proper.setProperty("hibernate.show_sql", "true");
        proper.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        sessionFactory.setHibernateProperties(proper);
        return sessionFactory;
    }

    @Autowired
    @Bean(name = "transactionManager")
    public PlatformTransactionManager getTransactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
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
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        ((ModularRealmAuthenticator) securityManager.getAuthenticator()).setAuthenticationStrategy(new AnyoneSuccessfulStrategy());
        return securityManager;
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
        jsonHttpMessageConverter.setGson(JSONUtils.getDefaultGson());
        messageConverters.add(jsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }
}