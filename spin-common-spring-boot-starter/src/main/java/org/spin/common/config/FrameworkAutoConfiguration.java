package org.spin.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 框架基础自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
public class FrameworkAutoConfiguration {

    @Bean
    public UtilsClassInitApplicationRunner utilsClassInitApplicationRunner() {
        return new UtilsClassInitApplicationRunner();
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return (registry) -> registry.config().commonTags("application", applicationName);
    }
}
