package org.spin.boot;

import org.hibernate.SessionFactory;
import org.spin.boot.properties.SpinDataProperties;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.DataSourceContext;
import org.spin.data.extend.DataSourceConfig;
import org.spin.data.extend.RepositoryContext;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.spin.web.filter.OpenSessionInViewFilter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * 单数据源自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
@EnableConfigurationProperties(SpinDataProperties.class)
@ConditionalOnBean({DataSourceConfig.class})
public class DataSourceAutoConfiguration {

    private SpinDataProperties dataProperties;
    private DataSourceConfig dsConfig;
    private ApplicationContext applicationContext;

    @Autowired
    public DataSourceAutoConfiguration(SpinDataProperties dataProperties, DataSourceConfig dsConfig, ApplicationContext applicationContext) {
        this.dataProperties = dataProperties;
        this.dsConfig = dsConfig;
        this.applicationContext = applicationContext;
    }

    @Bean
    public DataSource dataSource() {
        DataSource ds = BeanUtils.instantiateClass(dsConfig.getDataSourceClassName());
        BeanUtils.applyProperties(ds, dsConfig.toProperties());
        DataSourceContext.registDataSource(getDsName(), ds);
        return ds;
    }

    @Bean(name = "sessionFactory")
    @ConditionalOnBean(DataSource.class)
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        Properties properties = readProperties();
        if (null == properties || properties.isEmpty()) {
            throw new BeanCreationException("缺少hibernate配置");
        }

        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(("org.spin.data," + StringUtils.trimToEmpty(properties.getProperty("hibernate.packages"))).split(","));
        if (null != dataProperties.getNamingStrategy()) {
            sessionFactory.setPhysicalNamingStrategy(dataProperties.getNamingStrategyObj());
        }

        sessionFactory.setHibernateProperties(properties);
        return sessionFactory;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public QueryParamParser queryParamParser(DataSource dataSource) {
        return new QueryParamParser();
    }

    @Bean
    @ConditionalOnBean({DataSource.class})
    public SQLManager sqlManager(DataSource dataSource) throws ClassNotFoundException {
        return new SQLManager(dsConfig, dataProperties.getSqlLoader(), dataProperties.getSqlUri(), dataProperties.getResolverObj());
    }

    @Bean
    @ConditionalOnBean({SQLManager.class, QueryParamParser.class})
    public RepositoryContext repositoryContext(SQLManager sqlManager, QueryParamParser paramParser) {
        return new RepositoryContext(sqlManager, paramParser, applicationContext);
    }

    @Bean(name = "transactionManager")
    @ConditionalOnBean(SessionFactory.class)
    public PlatformTransactionManager getTransactionManager(SessionFactory sessionFactory) {
        DataSourceContext.registSessionFactory(getDsName(), sessionFactory);
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public FilterRegistrationBean openSessionInViewFilterRegistration() {
        FilterRegistrationBean<OpenSessionInViewFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OpenSessionInViewFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("sessionFactoryBeanName", "sessionFactory");
        registration.setName("openSessionInViewFilter");
        registration.setOrder(2);
        return registration;
    }

    /**
     * hibernate配置文件读取
     */
    private Properties readProperties() {
        String name = "hibernate.properties";
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(name));
        Properties properties = null;
        try {
            propertiesFactoryBean.afterPropertiesSet();
            properties = propertiesFactoryBean.getObject();
        } catch (IOException ignore) {
            // ignore
        }
        return properties;
    }

    private String getDsName() {
        String name = dsConfig.getName();
        if (StringUtils.isEmpty(name)) {
            name = "main";
            dsConfig.setName(name);
        }
        return name;
    }
}
