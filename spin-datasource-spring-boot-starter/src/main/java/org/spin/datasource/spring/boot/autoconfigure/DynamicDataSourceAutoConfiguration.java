package org.spin.datasource.spring.boot.autoconfigure;

import org.spin.datasource.Ds;
import org.spin.datasource.DynamicRoutingDataSource;
import org.spin.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import org.spin.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import org.spin.datasource.aop.DynamicTransactionAdvisor;
import org.spin.datasource.processor.DsHeaderProcessor;
import org.spin.datasource.processor.DsProcessor;
import org.spin.datasource.processor.DsSessionProcessor;
import org.spin.datasource.processor.DsSpelExpressionProcessor;
import org.spin.datasource.provider.DynamicDataSourceProvider;
import org.spin.datasource.provider.YmlDynamicDataSourceProvider;
import org.spin.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import org.spin.datasource.strategy.DynamicDataSourceStrategy;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源核心自动配置类
 *
 * @author TaoYu Kanyuxia
 * @see DynamicDataSourceProvider
 * @see DynamicDataSourceStrategy
 * @see DynamicRoutingDataSource
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import(value = {DruidDynamicDataSourceConfiguration.class, DynamicDataSourceCreatorAutoConfiguration.class})
@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicDataSourceAutoConfiguration {

    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceAutoConfiguration(DynamicDataSourceProperties properties) {
        this.properties = properties;
        Ds.setPrimaryDataSource(properties.getPrimary());
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        Map<String, DataSourceProperty> datasourceMap = properties.getDatasource();
        return new YmlDynamicDataSourceProvider(datasourceMap);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setPrimary(properties.getPrimary());
        dataSource.setStrict(properties.getStrict());
        dataSource.setStrategy(properties.getStrategy());
        dataSource.setProvider(dynamicDataSourceProvider);
        dataSource.setP6spy(properties.getP6spy());
        dataSource.setSeata(properties.getSeata());
        return dataSource;
    }

    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor) {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(properties.isAllowedPublicOnly(), dsProcessor);
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
        advisor.setOrder(properties.getOrder());
        return advisor;
    }

    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "seata", havingValue = "false", matchIfMissing = true)
    @Bean
    public Advisor dynamicTransactionAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(org.spin.datasource.annotation.DSTransactional)");
        return new DefaultPointcutAdvisor(pointcut, new DynamicTransactionAdvisor());
    }

    @Bean
    @ConditionalOnMissingBean
    public DsProcessor dsProcessor() {
        DsHeaderProcessor headerProcessor = new DsHeaderProcessor();
        DsSessionProcessor sessionProcessor = new DsSessionProcessor();
        DsSpelExpressionProcessor spelExpressionProcessor = new DsSpelExpressionProcessor();
        headerProcessor.setNextProcessor(sessionProcessor);
        sessionProcessor.setNextProcessor(spelExpressionProcessor);
        return headerProcessor;
    }

}
