package org.spin.boot.datasource.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.spin.boot.datasource.DataSourceBuilder;
import org.spin.boot.datasource.property.MultiHikariDataSourceProperties;
import org.spin.boot.datasource.property.SpinHikariDataSource;
import org.spin.data.core.DataSourceContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.sql.DataSource;

/**
 * Hikari数据源构建器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({MultiHikariDataSourceProperties.class})
public class HikariDataSourceBuilder implements DataSourceBuilder<HikariDataSource, SpinHikariDataSource> {

    @Override
    public DataSource buildAtomikosDataSource(DefaultListableBeanFactory acf, SpinHikariDataSource dbConfig) {
        return buildSingletonDatasource(acf, dbConfig);
    }

    @Override
    public HikariDataSource buildSingletonDatasource(DefaultListableBeanFactory acf, SpinHikariDataSource dbConfig) {
        String beanName = dbConfig.getName() + "DataSource";
        DataSourceContext.registDataSource(dbConfig.getName(), dbConfig);
        acf.registerSingleton(beanName, dbConfig);
        return acf.getBean(HikariDataSource.class);
    }
}
