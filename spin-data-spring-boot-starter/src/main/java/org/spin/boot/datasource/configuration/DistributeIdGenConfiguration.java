package org.spin.boot.datasource.configuration;

import org.spin.boot.datasource.property.IdGenProperties;
import org.spin.data.pk.DistributedId;
import org.spin.data.pk.generator.DistributedIdGenerator;
import org.spin.data.pk.generator.IdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
@EnableConfigurationProperties(IdGenProperties.class)
public class DistributeIdGenConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator<Long, DistributedId> idGenerator(IdGenProperties properties) {
        return new DistributedIdGenerator(properties);
    }
}
