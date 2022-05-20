package org.spin.datasource.spring.boot.autoconfigure;

import org.spin.datasource.support.DbHealthIndicator;
import org.spin.datasource.support.HealthCheckAdapter;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author liushang@zsyjr.com
 */

@Configuration
public class DynamicDataSourceHealthCheckConfiguration {

    private static final String DYNAMIC_HEALTH_CHECK = DynamicDataSourceProperties.PREFIX + ".health";

    @Bean
    public HealthCheckAdapter healthCheckAdapter() {
        return new HealthCheckAdapter();
    }

    @ConditionalOnClass(AbstractHealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("dynamicDS")
    public static class HealthIndicatorConfiguration {

        @Bean("dynamicDataSourceHealthCheck")
        @ConditionalOnProperty(DYNAMIC_HEALTH_CHECK)
        public DbHealthIndicator healthIndicator(DataSource dataSource,
                                                 DynamicDataSourceProperties dynamicDataSourceProperties,
                                                 HealthCheckAdapter healthCheckAdapter) {
            return new DbHealthIndicator(dataSource, dynamicDataSourceProperties.getHealthValidQuery(), healthCheckAdapter);
        }

    }

}
