package org.spin.datasource.spring.boot.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.datasource.creator.BasicDataSourceCreator;
import org.spin.datasource.creator.DataSourceCreator;
import org.spin.datasource.creator.DefaultDataSourceCreator;
import org.spin.datasource.creator.DruidDataSourceCreator;
import org.spin.datasource.creator.HikariDataSourceCreator;
import org.spin.datasource.creator.JndiDataSourceCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author TaoYu
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceCreatorAutoConfiguration {

    private static final int JNDI_ORDER = 1000;
    private static final int DRUID_ORDER = 2000;
    private static final int HIKARI_ORDER = 3000;
    private static final int DEFAULT_ORDER = 5000;
    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceCreatorAutoConfiguration(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public DefaultDataSourceCreator dataSourceCreator(List<DataSourceCreator> dataSourceCreators) {
        DefaultDataSourceCreator defaultDataSourceCreator = new DefaultDataSourceCreator();
        defaultDataSourceCreator.setProperties(properties);
        defaultDataSourceCreator.setDataSourceCreators(dataSourceCreators);
        return defaultDataSourceCreator;
    }

    @Bean
    @Order(DEFAULT_ORDER)
    @ConditionalOnMissingBean
    public BasicDataSourceCreator basicDataSourceCreator() {
        return new BasicDataSourceCreator();
    }

    @Bean
    @Order(JNDI_ORDER)
    @ConditionalOnMissingBean
    public JndiDataSourceCreator jndiDataSourceCreator() {
        return new JndiDataSourceCreator();
    }

    /**
     * 存在Druid数据源时, 加入创建器
     */
    @ConditionalOnClass(DruidDataSource.class)
    @Configuration
    public class DruidDataSourceCreatorConfiguration {
        @Bean
        @Order(DRUID_ORDER)
        @ConditionalOnMissingBean
        public DruidDataSourceCreator druidDataSourceCreator() {
            return new DruidDataSourceCreator(properties.getDruid());
        }

    }

    /**
     * 存在Hikari数据源时, 加入创建器
     */
    @ConditionalOnClass(HikariDataSource.class)
    @Configuration
    public class HikariDataSourceCreatorConfiguration {
        @Bean
        @Order(HIKARI_ORDER)
        @ConditionalOnMissingBean
        public HikariDataSourceCreator hikariDataSourceCreator() {
            return new HikariDataSourceCreator(properties.getHikari());
        }
    }

}
