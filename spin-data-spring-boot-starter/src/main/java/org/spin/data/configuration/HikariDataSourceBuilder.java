package org.spin.data.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.spin.data.property.MultiHikariDataSourceProperties;
import org.spin.data.property.SpinHikariDataSource;
import org.spin.data.DataSourceBuilder;
import org.spin.data.core.DataSourceContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
