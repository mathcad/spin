package org.spin.boot.configuration;

import org.spin.boot.properties.SecretManagerProperties;
import org.spin.core.auth.InMemorySecretDao;
import org.spin.core.auth.SecretDao;
import org.spin.core.auth.SecretManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
@EnableConfigurationProperties(SecretManagerProperties.class)
public class InMemorySecretManagerConfiguration {

    @Bean(name = "secretDao")
    @ConditionalOnMissingBean(SecretDao.class)
    public SecretDao getSecretDao() {
        return new InMemorySecretDao();
    }

    @Bean(name = "secretManager")
    @ConditionalOnMissingBean(SecretManager.class)
    public SecretManager getSecretManager(SecretDao secretDao, SecretManagerProperties properties) {
        SecretManager manager = new SecretManager(secretDao);
        manager.setRsaPubkey(properties.getRsaPubkey());
        manager.setRsaPrikey(properties.getRsaPrikey());
        manager.setKeyExpiredIn(properties.getKeyExpireTime());
        manager.setTokenExpiredIn(properties.getTokenExpireTime());
        return manager;
    }
}
