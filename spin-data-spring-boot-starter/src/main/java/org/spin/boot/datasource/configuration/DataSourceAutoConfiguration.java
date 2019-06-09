package org.spin.boot.datasource.configuration;

import org.hibernate.SessionFactory;
import org.spin.boot.datasource.DataSourceBuilder;
import org.spin.boot.datasource.Multipal;
import org.spin.boot.datasource.Singleton;
import org.spin.boot.datasource.TransactionModel;
import org.spin.boot.datasource.bean.DbInit;
import org.spin.boot.datasource.filter.OpenSessionInViewFilter;
import org.spin.boot.datasource.property.SpinDataProperties;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.SystemUtils;
import org.spin.data.core.DataSourceContext;
import org.spin.data.extend.DataSourceConfig;
import org.spin.data.extend.MultiDataSourceConfig;
import org.spin.data.extend.RepositoryContext;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 多数据源自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@EnableConfigurationProperties({SpinDataProperties.class, AtomikosProperties.class, JtaProperties.class})
@ConditionalOnBean(DataSourceBuilder.class)
@AutoConfigureBefore(value = {DataSourcePoolMetadataProvidersConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    JdbcTemplateAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration.class
}, name = {
    "org.springframework.boot.autoconfigure.transaction.jta.AtomikosJtaConfiguration.class"
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
public class DataSourceAutoConfiguration {

    private SpinDataProperties dataProperties;
    private DataSourceConfig dsConfig;
    private MultiDataSourceConfig<? extends DataSourceConfig> dsConfigs;
    private DataSourceBuilder dataSourceBuilder;
    private ApplicationContext applicationContext;

    @Autowired
    public DataSourceAutoConfiguration(SpinDataProperties dataProperties, DataSourceConfig dsConfig, MultiDataSourceConfig<? extends DataSourceConfig> dsConfigs, DataSourceBuilder dataSourceBuilder, ApplicationContext applicationContext) {
        this.dataProperties = dataProperties;
        this.dsConfig = dsConfig;
        this.dsConfigs = dsConfigs;
        this.dataSourceBuilder = dataSourceBuilder;
        this.applicationContext = applicationContext;
    }

    @Bean
    public TransactionModel transactionModel() {
        if (StringUtils.isNotBlank(dsConfig.getUrl()) && (null == dsConfigs.getDataSources() || dsConfigs.getDataSources().isEmpty())) {
            if (StringUtils.isBlank(dsConfig.getName())) {
                dsConfig.setName("main");
            }
            return new Singleton();
        }

        if (StringUtils.isBlank(dsConfig.getUrl()) && (null != dsConfigs.getDataSources() && dsConfigs.getDataSources().size() == 1)) {
            String name = dsConfigs.getDataSources().keySet().iterator().next();
            dsConfig = dsConfigs.getDataSources().get(name);
            dsConfig.setName(name);
            return new Singleton();
        }

        if (StringUtils.isBlank(dsConfig.getUrl()) && (null != dsConfigs.getDataSources() && dsConfigs.getDataSources().size() > 1)) {
            if (StringUtils.isBlank(dsConfigs.getPrimaryDataSource())) {
                throw new SimplifiedException("多数据源配置中未指定主数据源");
            }
            if (!dsConfigs.getDataSources().containsKey(dsConfigs.getPrimaryDataSource())) {
                throw new SimplifiedException("配置的主数据源不存在: " + dsConfigs.getPrimaryDataSource());
            }
            dsConfigs.getDataSources().forEach((n, d) -> d.setName(n));
            return new Multipal();
        }

        throw new SimplifiedException("不能同时使用单数据源与多数据源配置");
    }

    @Bean
    @SuppressWarnings("unchecked")
    public DbInit dsInit(TransactionModel model) {
        DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        Properties defaultProperties = readProperties(null);

        if (model instanceof Multipal) {
            dsConfigs.getDataSources().forEach((name, config) -> {
                // DataSource
                DataSource dataSourceBean = dataSourceBuilder.buildAtomikosDataSource(acf, config);

                // SessionFactory
                sfDefinitionBuilder(acf, name, dataSourceBean, defaultProperties);

                // OpenSessionInViewFilter
                try {
                    Class.forName("org.springframework.boot.web.servlet.FilterRegistrationBean");
                    if (config.isOpenSessionInView()) {
                        osivDefinitionBuilder(acf, name);
                    }
                } catch (ClassNotFoundException ignore) {
                    // do nothing
                }
            });
        } else {
            DataSource druidDataSource = dataSourceBuilder.buildSingletonDatasource(acf, this.dsConfig);
            // SessionFactory
            sfDefinitionBuilder(acf, dsConfig.getName(), druidDataSource, defaultProperties);

            // OpenSessionInViewFilter
            try {
                Class.forName("org.springframework.boot.web.servlet.FilterRegistrationBean");
                if (dsConfig.isOpenSessionInView()) {
                    osivDefinitionBuilder(acf, dsConfig.getName());
                }
            } catch (ClassNotFoundException ignore) {
                // do nothing
            }
        }
        return new DbInit();
    }

    @Bean
    @ConditionalOnBean(DbInit.class)
    public QueryParamParser queryParamParser(DbInit dbInit) {
        return new QueryParamParser();
    }

    @Bean
    @ConditionalOnBean({DbInit.class})
    public SQLManager sqlManager(DbInit dbInit, TransactionModel model) throws ClassNotFoundException {
        if (model instanceof Multipal) {
            return new SQLManager(dsConfigs, dataProperties.getSqlLoader(), dataProperties.getSqlUri(), dataProperties.getResolverObj());
        } else {
            return new SQLManager(dsConfig, dataProperties.getSqlLoader(), dataProperties.getSqlUri(), dataProperties.getResolverObj());
        }
    }

    @Bean
    @ConditionalOnBean({SQLManager.class, QueryParamParser.class})
    public RepositoryContext repositoryContext(SQLManager sqlManager, QueryParamParser paramParser) {
        return new RepositoryContext(sqlManager, paramParser, applicationContext);
    }


    /**
     * 准备SessionFactory的bean
     *
     * @param name              名称
     * @param dataSource        数据源
     * @param defaultProperties 默认hibernate配置
     */
    private SessionFactory sfDefinitionBuilder(DefaultListableBeanFactory acf, String name, DataSource dataSource, Properties defaultProperties) {
        Properties properties = new Properties();
        if (Objects.nonNull(defaultProperties)) {
            defaultProperties.forEach((k, v) -> properties.merge(k, v, (a, b) -> b));
        }
        Properties dsProperties = readProperties(name);
        if (Objects.nonNull(dsProperties)) {
            dsProperties.forEach((k, v) -> properties.merge(k, v, (a, b) -> b));
        }
        if (properties.isEmpty()) {
            throw new BeanCreationException(name + "缺少hibernate配置");
        }
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(LocalSessionFactoryBean.class);
        bdb.addPropertyValue("dataSource", dataSource);
        bdb.addPropertyValue("packagesToScan", ("org.spin.data," + StringUtils.trimToEmpty(properties.getProperty("hibernate.packages"))).split(","));
        if (Objects.nonNull(dataProperties.getNamingStrategyObj())) {
            bdb.addPropertyValue("physicalNamingStrategy", dataProperties.getNamingStrategyObj());
        }
        bdb.addPropertyValue("hibernateProperties", properties);

        String beanName = name + "SessionFactory";
        acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
        SessionFactory sf = (SessionFactory) acf.getBean(beanName);
        DataSourceContext.registSessionFactory(name, sf);
        return sf;
    }

    @SuppressWarnings("unchecked")
    private FilterRegistrationBean<OpenSessionInViewFilter> osivDefinitionBuilder(DefaultListableBeanFactory acf, String name) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(FilterRegistrationBean.class);
        bdb.addPropertyValue("filter", new OpenSessionInViewFilter());
        List<String> urlPatterns = Collections.singletonList("/*");
        bdb.addPropertyValue("urlPatterns", urlPatterns);
        Map<String, String> initParameters = MapUtils.ofMap("sessionFactoryBeanName", name + "SessionFactory");
        bdb.addPropertyValue("initParameters", initParameters);
        bdb.addPropertyValue("name", name + "OpenSessionInViewFilter");

        String beanName = name + "OpenSessionInViewFilter";
        acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
        return (FilterRegistrationBean<OpenSessionInViewFilter>) acf.getBean(beanName);
    }

    @Bean
    @ConditionalOnBean(TransactionModel.class)
    public InitializingBean configureTransaction(final TransactionModel transactionModel, AtomikosProperties atomikosProperties, JtaProperties jtaProperties) {
        final DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        return () -> {
            if (transactionModel instanceof Singleton) {
                BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(HibernateTransactionManager.class);
                bdb.addConstructorArgReference(dsConfig.getName() + "SessionFactory");
                String beanName = dsConfig.getName() + "TransactionManager";
                acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
                applicationContext.getBean(PlatformTransactionManager.class);
            } else {
                Properties properties = new Properties();
                if (StringUtils.isNotBlank(jtaProperties.getTransactionManagerId())) {
                    properties.setProperty("com.atomikos.icatch.tm_unique_name", jtaProperties.getTransactionManagerId());
                }
                String jtaLogDir = jtaProperties.getLogDir();
                if (StringUtils.isBlank(jtaLogDir)) {
                    jtaLogDir = SystemUtils.USER_HOME + "/transaction-logs";
                }
                properties.setProperty("com.atomikos.icatch.log_base_dir", jtaLogDir);
                properties.putAll(atomikosProperties.asProperties());

                BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(com.atomikos.icatch.config.UserTransactionServiceImp.class);
                bdb.addConstructorArgValue(properties);
                bdb.setInitMethodName("init");
                bdb.setDestroyMethodName("shutdownForce");
                acf.registerBeanDefinition("userTransactionService", bdb.getBeanDefinition());

                bdb = BeanDefinitionBuilder.rootBeanDefinition(com.atomikos.icatch.jta.UserTransactionManager.class);
                bdb.addPropertyValue("startupTransactionService", false);
                bdb.addPropertyValue("forceShutdown", true);
                bdb.addPropertyValue("transactionTimeout", 60);
                acf.registerBeanDefinition("userTransactionManager", bdb.getBeanDefinition());

                bdb = BeanDefinitionBuilder.rootBeanDefinition(JtaTransactionManager.class);
                bdb.addPropertyReference("transactionManager", "userTransactionManager");
                bdb.addPropertyReference("userTransaction", "userTransactionManager");
                acf.registerBeanDefinition("jtaTransactionManager", bdb.getBeanDefinition());
            }
        };
    }

    //    @Bean(name = "userTransactionService", initMethod = "init", destroyMethod = "shutdownForce")
//    @ConditionalOnMissingBean(UserTransactionService.class)
//    @ConditionalOnBean(Multipal.class)
//    public UserTransactionServiceImp userTransactionService(AtomikosProperties atomikosProperties, JtaProperties jtaProperties) {
//        Properties properties = new Properties();
//        if (StringUtils.isNotBlank(jtaProperties.getTransactionManagerId())) {
//            properties.setProperty("com.atomikos.icatch.tm_unique_name", jtaProperties.getTransactionManagerId());
//        }
//        String jtaLogDir = jtaProperties.getLogDir();
//        if (StringUtils.isBlank(jtaLogDir)) {
//            jtaLogDir = SystemUtils.USER_HOME + "/transaction-logs";
//        }
//        properties.setProperty("com.atomikos.icatch.log_base_dir", jtaLogDir);
//        properties.putAll(atomikosProperties.asProperties());
//        return new UserTransactionServiceImp(properties);
//    }

    //    @Bean(name = "userTransactionManager", initMethod = "init", destroyMethod = "close")
//    @ConditionalOnBean({DbInit.class, UserTransactionService.class, Multipal.class})
//    public UserTransactionManager userTransactionManager(DbInit dbInit) throws SystemException {
//        UserTransactionManager manager = new UserTransactionManager();
//        manager.setStartupTransactionService(false);
//        manager.setForceShutdown(true);
//        manager.setTransactionTimeout(60);
//        return manager;
//    }

//    @Bean("atomikosUserTransaction")
//    @ConditionalOnBean({DbInit.class, UserTransactionService.class})
//    public UserTransactionImp userTransaction(DbInit dbInit) throws SystemException {
//        UserTransactionImp userTransaction = new UserTransactionImp();
//        userTransaction.setTransactionTimeout(60);
//        return userTransaction;
//    }

    //    @Bean(name = "jtaTransactionManager")
//    @ConditionalOnBean({UserTransactionManager.class, UserTransaction.class, Multipal.class})
//    public JtaTransactionManager jtaTransactionManager(UserTransactionManager tm) {
//        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
//        jtaTransactionManager.setTransactionManager(tm);
//        jtaTransactionManager.setUserTransaction(tm);
//        return jtaTransactionManager;
//    }
//
//    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
//        return new HibernateTransactionManager(sessionFactory);
//    }

    /**
     * hibernate配置文件读取
     *
     * @param dbName db名称
     */
    private Properties readProperties(String dbName) {
        String name;
        if (StringUtils.isNotEmpty(dbName)) {
            name = "hibernate-" + dbName + ".properties";
        } else {
            name = "hibernate.properties";
        }
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
}
