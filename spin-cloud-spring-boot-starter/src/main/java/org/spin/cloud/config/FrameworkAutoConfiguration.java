package org.spin.cloud.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.vo.CurrentUser;
import org.spin.core.session.SessionUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Configuration(proxyBeanMethods = false)
public class FrameworkAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(FrameworkAutoConfiguration.class);

    public FrameworkAutoConfiguration() {
        try {
            Class.forName("org.springframework.data.redis.core.script.RedisScript");
            SessionUser.registerSupplier(CurrentUser::getCurrent);
        } catch (Exception ignore) {
            logger.warn("CurrentUser init failed: Redis not enabled");
        }
    }

    @Bean
    @ConditionalOnProperty(name = "spin.cloud.utilClass.autoInit", havingValue = "true", matchIfMissing = true)
    public UtilsClassInitApplicationRunner utilsClassInitApplicationRunner() {
        return new UtilsClassInitApplicationRunner();
    }

    @Configuration
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    static class PrometheusConfiguration {
        @Bean
        MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
            return (registry) -> registry.config().commonTags("application", applicationName);
        }
    }
}
