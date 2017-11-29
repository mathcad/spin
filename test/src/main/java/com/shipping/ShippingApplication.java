package com.shipping;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.shipping.internal.InfoCache;
import org.spin.boot.annotation.EnableIdGenerator;
import org.spin.boot.annotation.EnableSecretManager;
import org.spin.boot.properties.DruidDataSourceProperties;
import org.spin.boot.properties.MultiDruidDataSourceProperties;
import org.spin.core.security.RSA;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.sql.XADataSource;
import java.security.InvalidKeyException;

/**
 * 启动类
 * <p>Created by xuweinan on 2017/7/14.</p>
 *
 * @author xuweinan
 */

@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class}, scanBasePackages = "com.shipping")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableSecretManager
@EnableIdGenerator
public class ShippingApplication {

    @Autowired
    Environment env;

    public static void main(String[] args) {
        SpringApplication.run(new Object[]{ShippingApplication.class}, args);
    }

    @Bean
    public XADataSource xaDataSource(MultiDruidDataSourceProperties dbConfig) {
        DruidDataSourceProperties druidDataSourceProperties = dbConfig.getPrimaryDataSourceConfig();
        DruidXADataSource dataSource = new DruidXADataSource();
        dataSource.configFromPropety(druidDataSourceProperties.toProperties());
        dataSource.setMaxWait(druidDataSourceProperties.getMaxWait());
        dataSource.setConnectProperties(druidDataSourceProperties.getConnectionProperties());
        dataSource.setRemoveAbandoned(druidDataSourceProperties.getRemoveAbandoned());
        dataSource.setRemoveAbandonedTimeout(druidDataSourceProperties.getRemoveAbandonedTimeout());
        return dataSource;
    }

//    @Bean
//    public ServletRegistrationBean druidStatViewServlet(DruidDataSourceProperties druidDataSourceProperties) {
//        return new ServletRegistrationBean(new StatViewServlet(), druidDataSourceProperties.getServletPath());
//    }
//
//    @Bean
//    public FilterRegistrationBean druidWebStatFilter() {
//        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
//        //添加过滤规则.
//        filterRegistrationBean.addUrlPatterns("/*");
//        //添加不需要忽略的格式信息.
//        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
//        return filterRegistrationBean;
//    }

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
        };
    }
}
