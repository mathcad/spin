package org.spin.data.configuration;

import org.spin.core.Assert;
import org.spin.data.pk.DistributedId;
import org.spin.data.pk.IdGeneratorConfig;
import org.spin.data.pk.generator.DistributedIdGenerator;
import org.spin.data.pk.generator.IdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
public class DistributeIdGenConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spin.data.pk")
    public IdGeneratorConfig idGeneratorConfig() {
        return new IdGeneratorConfig();
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator<Long, DistributedId> idGenerator(IdGeneratorConfig idGeneratorConfig) {
        Assert.notNull(idGeneratorConfig.getProviderType(), "主键生成必须指定机器ID提供者: spin.data.pk.providerType");
        return new DistributedIdGenerator(idGeneratorConfig);
    }
}
