package com.shipping;

import com.alibaba.druid.pool.DruidDataSource;
import com.shipping.internal.InfoCache;
import org.spin.boot.annotation.EnableIdGenerator;
import org.spin.boot.annotation.EnableSecretManager;
import org.spin.boot.properties.DatabaseConfigProperties;
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
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.sql.DataSource;
import java.security.InvalidKeyException;
import java.util.Properties;

/**
 * 启动类
 * <p>Created by xuweinan on 2017/7/14.</p>
 *
 * @author xuweinan
 */

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class, scanBasePackages = {"org.spin", "com.shipping"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableSecretManager
@EnableIdGenerator
public class ShippingApplication {

    @Autowired
    Environment env;

    @Autowired
    private DatabaseConfigProperties dbProperties;

    public static void main(String[] args) {
        SpringApplication.run(new Object[]{ShippingApplication.class}, args);
    }

    @Bean
    public DataSource dataSource() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        DruidDataSource dataSource = new DruidDataSource();
        if (StringUtils.isEmpty(dbProperties.getUrl())
            || StringUtils.isEmpty(dbProperties.getUsername())
            || StringUtils.isEmpty(dbProperties.getPassword())) {
            throw new BeanCreationException("数据库连接必需配置url, username, password");
        }
        dataSource.setUrl(dbProperties.getUrl());
        dataSource.setUsername(dbProperties.getUsername());
        dataSource.setPassword(AES.decrypt("c4b2a7d36f9a2e61", dbProperties.getPassword()));
        dataSource.setMaxActive(dbProperties.getMaxActive());
        dataSource.setMinIdle(dbProperties.getMinIdle());
        dataSource.setInitialSize(dbProperties.getInitialSize());
        dataSource.setMaxWait(dbProperties.getMaxWait());
        dataSource.setRemoveAbandoned(dbProperties.isRemoveAbandoned());
        dataSource.setRemoveAbandonedTimeoutMillis(dbProperties.getRemoveAbandonedTimeoutMillis());
        Properties proper = new Properties();
        proper.setProperty("clientEncoding", dbProperties.getClientEncoding());
        dataSource.setConnectProperties(proper);
        return dataSource;
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

//    @Bean
//    public org.apache.shiro.mgt.SecurityManager securityManager() {
//        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
//        ((ModularRealmAuthenticator) securityManager.getAuthenticator()).setAuthenticationStrategy(new AnyoneSuccessfulStrategy());
//        return securityManager;
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
