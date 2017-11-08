package com.shipping;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.shipping.internal.InfoCache;
import org.spin.boot.annotation.EnableIdGenerator;
import org.spin.boot.annotation.EnableSecretManager;
import org.spin.boot.properties.DruidDataSourceProperties;
import org.spin.core.security.AES;
import org.spin.core.security.RSA;
import org.spin.core.util.StringUtils;
import org.spin.wx.WxConfig;
import org.spin.wx.aes.AesException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.sql.DataSource;
import java.security.InvalidKeyException;

/**
 * 启动类
 * <p>Created by xuweinan on 2017/7/14.</p>
 *
 * @author xuweinan
 */

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class, scanBasePackages = "com.shipping")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties(DruidDataSourceProperties.class)
@EnableSecretManager
@EnableIdGenerator
public class ShippingApplication {

    @Autowired
    Environment env;

    public static void main(String[] args) {
        SpringApplication.run(new Object[]{ShippingApplication.class}, args);
    }

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties, DruidDataSourceProperties druidDataSourceProperties) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        if (StringUtils.isEmpty(dataSourceProperties.getUrl())
            || StringUtils.isEmpty(dataSourceProperties.getUsername())
            || StringUtils.isEmpty(dataSourceProperties.getPassword())) {
            throw new BeanCreationException("数据库连接必需配置url, username, password");
        }

        dataSourceProperties.setPassword(AES.decrypt("c4b2a7d36f9a2e61", dataSourceProperties.getPassword()));
        DruidDataSource dataSource = (DruidDataSource) dataSourceProperties.initializeDataSourceBuilder().type(DruidDataSource.class).build();
        dataSource.configFromPropety(druidDataSourceProperties.toProperties());
        dataSource.setMaxWait(druidDataSourceProperties.getMaxWait());
        dataSource.setConnectProperties(druidDataSourceProperties.getConnectionProperties());
        dataSource.setRemoveAbandoned(druidDataSourceProperties.getRemoveAbandoned());
        dataSource.setRemoveAbandonedTimeout(druidDataSourceProperties.getRemoveAbandonedTimeout());
        return dataSource;
    }

    @Bean
    public ServletRegistrationBean druidStatViewServlet(DruidDataSourceProperties druidDataSourceProperties) {
        return new ServletRegistrationBean(new StatViewServlet(), druidDataSourceProperties.getServletPath());
    }

    @Bean
    public FilterRegistrationBean druidWebStatFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        //添加过滤规则.
        filterRegistrationBean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息.
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
        return filterRegistrationBean;
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
    public InitializingBean readConfig() {
        return () -> {
            InfoCache.RSA_PUBKEY = RSA.getRSAPublicKey(env.getProperty("encrypt.rsa.publicKey"));
            InfoCache.RSA_PRIKEY = RSA.getRSAPrivateKey(env.getProperty("encrypt.rsa.privatekey"));

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
