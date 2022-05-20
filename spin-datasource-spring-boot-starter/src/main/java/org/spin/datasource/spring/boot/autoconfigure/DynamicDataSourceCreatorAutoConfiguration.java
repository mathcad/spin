package org.spin.datasource.spring.boot.autoconfigure;

import cn.beecp.BeeDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.spin.datasource.creator.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
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

    public static final int JNDI_ORDER = 1000;
    public static final int DRUID_ORDER = 2000;
    public static final int HIKARI_ORDER = 3000;
    public static final int BEECP_ORDER = 4000;
    public static final int DBCP2_ORDER = 5000;
    public static final int DEFAULT_ORDER = 6000;

    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceCreatorAutoConfiguration(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public DefaultDataSourceCreator dataSourceCreator(List<DataSourceCreator> dataSourceCreators) {
        DefaultDataSourceCreator defaultDataSourceCreator = new DefaultDataSourceCreator();
        defaultDataSourceCreator.setCreators(dataSourceCreators);
        return defaultDataSourceCreator;
    }

    @Bean
    @Order(DEFAULT_ORDER)
    @ConditionalOnMissingBean
    public BasicDataSourceCreator basicDataSourceCreator() {
        return new BasicDataSourceCreator(properties);
    }

    @Bean
    @Order(JNDI_ORDER)
    @ConditionalOnMissingBean
    public JndiDataSourceCreator jndiDataSourceCreator() {
        return new JndiDataSourceCreator(properties);
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
        public DruidDataSourceCreator druidDataSourceCreator(ApplicationContext applicationContext) {
            return new DruidDataSourceCreator(properties, applicationContext);
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
            return new HikariDataSourceCreator(properties);
        }
    }

    /**
     * 存在BeeCp数据源时, 加入创建器
     */
    @ConditionalOnClass(BeeDataSource.class)
    @Configuration
    public class BeeCpDataSourceCreatorConfiguration {

        @Bean
        @Order(BEECP_ORDER)
        @ConditionalOnMissingBean
        public BeeCpDataSourceCreator beeCpDataSourceCreator() {
            return new BeeCpDataSourceCreator(properties);
        }
    }

    /**
     * 存在Dbcp2数据源时, 加入创建器
     */
    @ConditionalOnClass(BasicDataSource.class)
    @Configuration
    public class DBCP2DataSourceCreatorConfiguration {

        @Bean
        @Order(DBCP2_ORDER)
        @ConditionalOnMissingBean
        public Dbcp2DataSourceCreator dbcp2DataSourceCreator() {
            return new Dbcp2DataSourceCreator(properties);
        }
    }

}
