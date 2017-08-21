package com.consultant;

import com.alibaba.druid.pool.DruidDataSource;
import org.spin.core.util.StringUtils;
import org.spin.data.extend.DataBaseConfiguration;
import org.spin.spring.condition.ConditionalOnBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * <描述>
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-08-20 上午10:12
 */
@Configuration
public class DBConfiguration {

    @Autowired
    @Bean
    @ConditionalOnBean(DataBaseConfiguration.class)
    public DataSource getDataSource(DataBaseConfiguration configuration) {
        DruidDataSource dataSource = new DruidDataSource();
        if (StringUtils.isEmpty(configuration.getUrl())
            || StringUtils.isEmpty(configuration.getUsername())
            || StringUtils.isEmpty(configuration.getPassword())) {
            throw new BeanCreationException("数据库连接必需配置url, username, password");
        }
        dataSource.setUrl(configuration.getUrl());
        dataSource.setUsername(configuration.getUsername());
        dataSource.setPassword(configuration.getPassword());
        dataSource.setMaxActive(configuration.getMaxActive());
        dataSource.setMinIdle(configuration.getMinIdle());
        dataSource.setInitialSize(configuration.getInitialSize());
        dataSource.setMaxWait(configuration.getMaxWait());
        dataSource.setRemoveAbandoned(configuration.isRemoveAbandoned());
        dataSource.setRemoveAbandonedTimeoutMillis(configuration.getRemoveAbandonedTimeoutMillis());
        Properties proper = new Properties();
        proper.setProperty("clientEncoding", configuration.getClientEncoding());
        dataSource.setConnectProperties(proper);
        return dataSource;
    }


}
