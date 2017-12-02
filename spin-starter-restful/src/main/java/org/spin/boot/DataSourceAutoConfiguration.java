package org.spin.boot;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.hibernate.SessionFactory;
import org.spin.boot.bean.DbInit;
import org.spin.boot.properties.SpinDataProperties;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.SystemUtils;
import org.spin.data.core.ARepository;
import org.spin.data.extend.DataSourceConfig;
import org.spin.data.extend.MultiDataSourceConfig;
import org.spin.data.extend.RepositoryContext;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 框架自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
@EnableConfigurationProperties({AtomikosProperties.class, JtaProperties.class})
@ConditionalOnBean({MultiDataSourceConfig.class, SpinDataProperties.class})
@AutoConfigureAfter(MultiDataSourceConfig.class)
public class DataSourceAutoConfiguration {

    @Autowired
    private SpinDataProperties dataProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnBean(MultiDataSourceConfig.class)
    public DbInit dsInit(MultiDataSourceConfig<?> dbConfigs) {
        DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        Properties defaultProperties = readProperties(null);

        dbConfigs.getDataSources().forEach((name, config) -> {
            // AtomikosDataSourceBean
            BeanDefinitionBuilder bdb = dsDefinitionBuilder(name, config);
            String beanName = name + "AtomikosDataSource";
            acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());

            AtomikosDataSourceBean ds = (AtomikosDataSourceBean) acf.getBean(beanName);
            SQLManager.addDataSource(name, ds);
            // SessionFactory
            bdb = sfDefinitionBuilder(name, acf.getBean(beanName), defaultProperties);
            beanName = name + "SessionFactory";
            acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
            SessionFactory sf = (SessionFactory) acf.getBean(beanName);
            ARepository.addSessionFactory(name, sf);

            // OpenSessionInViewFilter
            bdb = osivDefinitionBuilder(name);
            beanName = name + "OpenSessionInViewFilter";
            acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
            acf.getBean(beanName);
        });
        return new DbInit();
    }

    @Bean
    @ConditionalOnBean(DbInit.class)
    public QueryParamParser queryParamParser(DbInit dbInit) {
        return new QueryParamParser();
    }

    @Bean
    @ConditionalOnBean({DbInit.class})
    public SQLManager sqlManager(DbInit dbInit, MultiDataSourceConfig<?> dbConfigs) throws ClassNotFoundException {
        return new SQLManager(dbConfigs, dataProperties.getSqlLoader(), dataProperties.getSqlUri(), dataProperties.getResolverObj());
    }

    @Bean
    @ConditionalOnBean(SQLManager.class)
    public RepositoryContext repositoryContext() {
        RepositoryContext context = new RepositoryContext();
        context.setApplicationContext(applicationContext);
        return context;
    }

    @Bean(name = "atomikosTransactionService", initMethod = "init", destroyMethod = "shutdownForce")
    @ConditionalOnMissingBean(UserTransactionService.class)
    public UserTransactionServiceImp userTransactionService(AtomikosProperties atomikosProperties, JtaProperties jtaProperties) {
        Properties properties = new Properties();
        if (StringUtils.isNotBlank(jtaProperties.getTransactionManagerId())) {
            properties.setProperty("com.atomikos.icatch.tm_unique_name",
                jtaProperties.getTransactionManagerId());
        }
        String jtaLogDir = jtaProperties.getLogDir();
        if (StringUtils.isBlank(jtaLogDir)) {
            jtaLogDir = SystemUtils.USER_HOME + "/transaction-logs";
        }
        properties.setProperty("com.atomikos.icatch.log_base_dir", jtaLogDir);
        properties.putAll(atomikosProperties.asProperties());
        return new UserTransactionServiceImp(properties);
    }

    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    @ConditionalOnBean({DbInit.class, UserTransactionService.class})
    public UserTransactionManager transactionManager(DbInit dbInit) {
        UserTransactionManager manager = new UserTransactionManager();
        manager.setStartupTransactionService(false);
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean("atomikosUserTransaction")
    @ConditionalOnBean({DbInit.class, UserTransactionService.class})
    public UserTransactionImp userTransaction(DbInit dbInit) throws SystemException {
        UserTransactionImp userTransaction = new UserTransactionImp();
        userTransaction.setTransactionTimeout(60);
        return userTransaction;
    }

    @Bean(name = "jtaTransactionManager")
    @ConditionalOnBean({UserTransactionManager.class, UserTransaction.class})
    public JtaTransactionManager jtaTransactionManager(UserTransactionManager tm, UserTransactionImp atomikosUserTransaction) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(tm);
        jtaTransactionManager.setUserTransaction(atomikosUserTransaction);
        return jtaTransactionManager;
    }

    /**
     * 准备Atomikos数据源的bean
     *
     * @param name 名称
     */
    private BeanDefinitionBuilder dsDefinitionBuilder(String name, DataSourceConfig dbConfig) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(AtomikosDataSourceBean.class);
        bdb.addPropertyValue("xaDataSourceClassName", dbConfig.getXaDataSourceClassName());
        bdb.addPropertyValue("uniqueResourceName", name);
        bdb.addPropertyValue("maxPoolSize", dbConfig.getMaxPoolSize());
        bdb.addPropertyValue("minPoolSize", dbConfig.getMinPoolSize());
        bdb.addPropertyValue("xaProperties", dbConfig.toProperties());
        bdb.setInitMethodName("init");
        bdb.setDestroyMethodName("close");
        return bdb;
    }

    /**
     * 准备SessionFactory的bean
     *
     * @param name              名称
     * @param dataSource        数据源
     * @param defaultProperties 默认hibernate配置
     */
    private BeanDefinitionBuilder sfDefinitionBuilder(String name, Object dataSource, Properties defaultProperties) {
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
        return bdb;
    }

    private BeanDefinitionBuilder osivDefinitionBuilder(String name) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(FilterRegistrationBean.class);
        bdb.addPropertyValue("filter", new OpenSessionInViewFilter());
        List<String> urlPatterns = Collections.singletonList("/*");
        bdb.addPropertyValue("urlPatterns", urlPatterns);
        Map<String, String> initParameters = MapUtils.ofMap("sessionFactoryBeanName", name + "SessionFactory");
        bdb.addPropertyValue("initParameters", initParameters);
        bdb.addPropertyValue("name", name + "OpenSessionInViewFilter");
        return bdb;
    }

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
        } catch (IOException e) {
        }
        return properties;
    }
}
