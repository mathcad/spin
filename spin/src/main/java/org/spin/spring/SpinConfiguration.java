package org.spin.spring;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.spin.cache.RedisCache;
import org.spin.jpa.core.SQLLoader;
import org.spin.jpa.extend.DataBaseConfiguration;
import org.spin.jpa.sql.loader.ClasspathMdLoader;
import org.spin.jpa.sql.resolver.FreemarkerResolver;
import org.spin.spring.condition.ConditionalOnBean;
import org.spin.spring.condition.ConditionalOnMissingBean;
import org.spin.sys.auth.InMemorySecretDao;
import org.spin.sys.auth.SecretDao;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 框架自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
public class SpinConfiguration {

    @Bean
    @ConditionalOnMissingBean(SQLLoader.class)
    public SQLLoader sqlLoader() {
        SQLLoader loader = new ClasspathMdLoader();
        loader.setTemplateResolver(new FreemarkerResolver());
        return loader;
    }

    @Autowired
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

    @Autowired
    @Bean
    @ConditionalOnBean(DataBaseConfiguration.class)
    public DataSource getDataSource(DataBaseConfiguration configuration) {
        DruidDataSource dataSource = new DruidDataSource();
        if (StringUtils.isEmpty(configuration.getUrl())
            || StringUtils.isEmpty(configuration.getUsername())
            || StringUtils.isEmpty(configuration.getPassword())) {
            throw new BeanCreationException("数据库连接必需配置url, username, password");
        }
        dataSource.setUrl(configuration.getUrl());
        dataSource.setUsername(configuration.getUsername());
        dataSource.setPassword(configuration.getPassword());
        dataSource.setMaxActive(configuration.getMaxActive());
        dataSource.setMinIdle(configuration.getMinIdle());
        dataSource.setInitialSize(configuration.getInitialSize());
        dataSource.setMaxWait(configuration.getMaxWait());
        dataSource.setRemoveAbandoned(configuration.isRemoveAbandoned());
        dataSource.setRemoveAbandonedTimeoutMillis(configuration.getRemoveAbandonedTimeoutMillis());
        Properties proper = new Properties();
        proper.setProperty("clientEncoding", configuration.getClientEncoding());
        dataSource.setConnectProperties(proper);
        return dataSource;
    }

    @Autowired
    @Bean(name = "sessionFactory")
    @ConditionalOnBean(DataBaseConfiguration.class)
    public LocalSessionFactoryBean getSessionFactory(DataSource dataSource, DataBaseConfiguration configuration) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(configuration.getPackagesToScan());
        if (null != configuration.getNamingStrategy())
            sessionFactory.setPhysicalNamingStrategy(configuration.getNamingStrategy());
        Properties proper = new Properties();
        // proper.setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");
        proper.setProperty("hibernate.show_sql", configuration.getShowSql());
        proper.setProperty("hibernate.format_sql", configuration.getFormatSql());
        if (StringUtils.isEmpty(configuration.getDialect())) {
            throw new BeanCreationException("数据库连接必需配置Dialect");
        }
        proper.setProperty("hibernate.dialect", configuration.getDialect());
        proper.setProperty("hibernate.hbm2ddl.auto", configuration.getHbm2ddl());
        sessionFactory.setHibernateProperties(proper);
        return sessionFactory;
    }

    @Autowired
    @Bean(name = "transactionManager")
    @ConditionalOnBean(DataBaseConfiguration.class)
    public PlatformTransactionManager getTransactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

//    @Bean
//    @ConditionalOnMissingBean(SecurityManager.class)
//    public SecurityManager securityManager() {
//        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
//        ((ModularRealmAuthenticator) securityManager.getAuthenticator()).setAuthenticationStrategy(new AnyoneSuccessfulStrategy());
//        return securityManager;
//    }

    @Bean(name = "secretDao")
    @ConditionalOnMissingBean(SecretDao.class)
    public SecretDao getSecretDao() {
        return new InMemorySecretDao();
    }
}
