package com.consultant;

import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.spin.core.SpinContext;
import org.spin.core.auth.SecretManager;
import org.spin.core.security.AES;
import org.spin.core.util.JsonUtils;
import org.spin.data.core.SQLLoader;
import org.spin.data.extend.DataBaseConfiguration;
import org.spin.data.sql.loader.ArchiveMdLoader;
import org.spin.data.sql.resolver.FreemarkerResolver;
import org.spin.enhance.shiro.AnyoneSuccessfulStrategy;
import org.spin.spring.SpinConfiguration;
import org.spin.web.converter.JsonHttpMessageConverter;
import org.spin.web.filter.AccessAllowFilter;
import org.spin.web.filter.TokenResolveFilter;
import org.spin.wx.WxConfig;
import org.spin.wx.aes.AesException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.MultipartConfigElement;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 启动类
 * <p>Created by xuweinan on 2016/9/14.</p>
 *
 * @author xuweinan
 */

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class, scanBasePackages = {"org.spin", "com.consultant"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
public class ConsultantApplication {

    @Autowired
    Environment env;

    public static void main(String[] args) {
        SpringApplication.run(new Object[]{ConsultantApplication.class, SpinConfiguration.class}, args);
    }

    @Bean
    public DataBaseConfiguration dbConfiguration() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        DataBaseConfiguration dbConfiguration = new DataBaseConfiguration();
        dbConfiguration.setUrl(env.getProperty("dbConfig.url"));
        dbConfiguration.setUsername(env.getProperty("dbConfig.username"));
        dbConfiguration.setPassword(AES.decrypt("c4b2a7d36f9a2e61", env.getProperty("dbConfig.password")));
        dbConfiguration.setMaxActive(Integer.valueOf(env.getProperty("dbConfig.maxActive")));
        dbConfiguration.setMinIdle(Integer.valueOf(env.getProperty("dbConfig.minIdle")));
        dbConfiguration.setInitialSize(Integer.valueOf(env.getProperty("dbConfig.initialSize")));
        dbConfiguration.setMaxWait(Integer.valueOf(env.getProperty("dbConfig.maxWait")));
        dbConfiguration.setValidationQueryTimeout(Integer.valueOf(env.getProperty("dbConfig.validationQueryTimeout")));
        dbConfiguration.setValidationQuery(env.getProperty("dbConfig.validationQuery"));
        dbConfiguration.setRemoveAbandoned(Boolean.valueOf(env.getProperty("dbConfig.removeAbandoned")));
        dbConfiguration.setRemoveAbandonedTimeout(Integer.valueOf(env.getProperty("dbConfig.removeAbandonedTimeout")));
        dbConfiguration.setClientEncoding(env.getProperty("dbConfig.clientEncoding"));
        dbConfiguration.setPackagesToScan(env.getProperty("dbConfig.packagesToScan").split(","));
        dbConfiguration.setNamingStrategy((PhysicalNamingStrategy) Class.forName(env.getProperty("dbConfig.namingStrategy")).newInstance());
        dbConfiguration.setShowSql(env.getProperty("dbConfig.showSql"));
        dbConfiguration.setFormatSql(env.getProperty("dbConfig.formatSql"));
        dbConfiguration.setHbm2ddl(env.getProperty("dbConfig.hbm2ddl"));
        dbConfiguration.setDialect(env.getProperty("dbConfig.dialect"));
        return dbConfiguration;
    }

    @Bean
    public SQLLoader sqlLoader() {
        SQLLoader loader = new ArchiveMdLoader();
        loader.setTemplateResolver(new FreemarkerResolver());
        return loader;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(Long.parseLong(env.getProperty("web.maxUploadSize")) * 1024 * 1024);
        return factory.createMultipartConfig();
    }

    //redis配置
//    @Bean
//    public RedisConnectionFactory getJedisConnectionFactory() {
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setMaxIdle(200);
//        poolConfig.setTestOnBorrow(true);
//
//        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
//        connectionFactory.setUsePool(true);
//        connectionFactory.setHostName("127.0.0.1");
//        connectionFactory.setPort(6379);
//        connectionFactory.setPoolConfig(poolConfig);
//        return connectionFactory;
//    }

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
    public FilterRegistrationBean accessAllowFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AccessAllowFilter());
        registration.addUrlPatterns("/*");
        registration.setName("accessAllowFilter");
        registration.setOrder(3);
        return registration;
    }

    @Bean
    @Autowired
    public FilterRegistrationBean apiFilter(SecretManager secretManager) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new TokenResolveFilter(secretManager));
        registration.addUrlPatterns("/*");
        registration.setName("tokenFilter");
        registration.setOrder(4);
        return registration;
    }

    @Bean
    public org.apache.shiro.mgt.SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        ((ModularRealmAuthenticator) securityManager.getAuthenticator()).setAuthenticationStrategy(new AnyoneSuccessfulStrategy());
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(org.apache.shiro.mgt.SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/**", "anon");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
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
    public InitializingBean readConfig(SecretManager secretManager) {
        return () -> {
            SpinContext.FileUploadDir = env.getProperty("web.fileUploadDir");
            SpinContext.devMode = "dev".equals(env.getProperty("spring.profiles.active"));
            secretManager.setRsaPubkey(env.getProperty("encrypt.rsa.publicKey"));
            secretManager.setRsaPrikey(env.getProperty("encrypt.rsa.privatekey"));
            if (env.containsProperty("secretManager.tokenExpireTime")) {
                secretManager.setTokenExpiredIn(env.getProperty("secretManager.tokenExpireTime"));
            }
            if (env.containsProperty("secretManager.keyExpireTime")) {
                secretManager.setKeyExpiredIn(env.getProperty("secretManager.keyExpireTime"));
            }

            if (env.getProperty("wxConfig.encodingAesKey").length() != 43) {
                throw new AesException(AesException.IllegalAesKey);
            }
            WxConfig.ConfigInfo configInfo = new WxConfig.ConfigInfo();
            configInfo.setAppId(env.getProperty("wxConfig.appId"));
            configInfo.setAppSecret(env.getProperty("wxConfig.appSecret"));
            configInfo.setToken(env.getProperty("wxConfig.token"));
            configInfo.setEncodingAesKey(env.getProperty("wxConfig.encodingAesKey"));
            configInfo.setPayMchId(env.getProperty("wxConfig.mchId"));
            configInfo.setMchKey(env.getProperty("wxConfig.mchKey"));
            WxConfig.putConfig("default", configInfo);
        };
    }
}
