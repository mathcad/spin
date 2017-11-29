package org.spin.boot;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.spin.boot.bean.DbInit;
import org.spin.boot.properties.DruidDataSourceProperties;
import org.spin.boot.properties.MultiDruidDataSourceProperties;
import org.spin.boot.properties.SpinDataProperties;
import org.spin.boot.properties.SpinWebPorperties;
import org.spin.boot.properties.WxConfigProperties;
import org.spin.core.auth.SecretManager;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MethodUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.cache.RedisCache;
import org.spin.data.core.SQLLoader;
import org.spin.data.extend.RepositoryContext;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.spin.web.converter.JsonHttpMessageConverter;
import org.spin.web.filter.TokenResolveFilter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.MultipartConfigElement;
import javax.sql.XADataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * 框架自动配置
 * Created by xuweinan on 2017/1/24.
 *
 * @author xuweinan
 */
@Configuration
@EnableConfigurationProperties({SpinDataProperties.class, SpinWebPorperties.class, WxConfigProperties.class, MultiDruidDataSourceProperties.class})
@ComponentScan("org.spin.spring")
public class SpinAutoConfiguration {

    @Autowired
    private SpinDataProperties dataProperties;

    @Autowired
    private ApplicationContext applicationContext;

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
    @ConditionalOnBean(XADataSource.class)
    public DbInit dsInit(List<XADataSource> dataSources, MultiDruidDataSourceProperties dbConfigs) {
        if (Objects.nonNull(dataSources)) {
            DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            Properties defaultProperties = readProperties(null);
            for (int i = 0; i < dataSources.size(); i++) {
                // AtomikosDataSourceBean
                XADataSource ds = dataSources.get(i);
                String name = "db" + i;
                try {
                    name = MethodUtils.invokeMethod(ds, "getName", new Object[0]).toString();
                } catch (Exception e) {
                }
                BeanDefinitionBuilder bdb = dsDefinitionBuilder(name, ds, dbConfigs.getDataSourceConfig(name));
                String beanName = name + "AtomikosDataSource";
                acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());

                // SessionFactory
                bdb = sfDefinitionBuilder(name, acf.getBean(beanName), defaultProperties);
                beanName = name + "SessionFactory";
                acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
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
    @ConditionalOnBean(DbInit.class)
    @ConditionalOnMissingBean(SQLLoader.class)
    public SQLLoader sqlLoader(DbInit dbInit) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        SQLLoader loader = (SQLLoader) Class.forName(dataProperties.getSqlLoader()).getDeclaredConstructor().newInstance();
        if (StringUtils.isEmpty(dataProperties.getSqlUri())) {
            loader.setRootUri(dataProperties.getSqlUri());
        }
        loader.setTemplateResolver(dataProperties.getResolverObj());
        return loader;
    }

    @Bean
    @ConditionalOnBean(SQLLoader.class)
    public SQLManager sqlManager(SQLLoader sqlLoader, LocalSessionFactoryBean sessionFactory) {
        return new SQLManager(sessionFactory, sqlLoader);
    }

    @Bean
    @ConditionalOnBean(SQLManager.class)
    public RepositoryContext repositoryContext() {
        RepositoryContext context = new RepositoryContext();
        context.setApplicationContext(applicationContext);
        return context;
    }

    @Bean(name = "jtaTransactionManager")
    @ConditionalOnBean({TransactionManager.class, UserTransaction.class})
    public PlatformTransactionManager jtaTransactionManager(TransactionManager tm, UserTransaction userTransaction) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(tm);
        jtaTransactionManager.setUserTransaction(userTransaction);
        return jtaTransactionManager;
    }

    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    @ConditionalOnBean(DbInit.class)
    public UserTransactionManager transactionManager(DbInit dbInit) {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(true);
        return userTransactionManager;
    }

    @Bean("atomikosUserTransaction")
    @ConditionalOnBean(DbInit.class)
    public UserTransaction userTransaction(DbInit dbInit) throws SystemException {
        UserTransactionImp userTransaction = new UserTransactionImp();
        userTransaction.setTransactionTimeout(60);
        return userTransaction;
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

    /**
     * 准备Atomikos数据源的bean
     *
     * @param name 名称
     * @param ds   XA数据源
     */
    private BeanDefinitionBuilder dsDefinitionBuilder(String name, XADataSource ds, DruidDataSourceProperties dbConfig) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(AtomikosDataSourceBean.class);
        bdb.addPropertyValue("xaDataSource", ds);
        bdb.addPropertyValue("uniqueResourceName", name);
        bdb.addPropertyValue("maxPoolSize", dbConfig.getMaxActive() * 2);
        bdb.addPropertyValue("minPoolSize", dbConfig.getMinIdle());
        bdb.setInitMethodName("doInit");
        bdb.setDestroyMethodName("doClose");
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
        bdb = BeanDefinitionBuilder.rootBeanDefinition(LocalSessionFactoryBean.class);
        bdb.addPropertyValue("dataSource", dataSource);
        bdb.addPropertyValue("packagesToScan", ("org.spin.data," + StringUtils.trimToEmpty(properties.getProperty("hibernate.packages"))).split(","));
        if (Objects.nonNull(dataProperties.getNamingStrategyObj())) {
            bdb.addPropertyValue("physicalNamingStrategy", dataProperties.getNamingStrategyObj());
        }
        bdb.addPropertyValue("hibernateProperties", properties);
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
