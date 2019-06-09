package org.spin.boot.datasource.property;

import org.spin.core.util.StringUtils;
import org.spin.data.extend.MultiDataSourceConfig;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Druid的多数据源配置
 * <p>Created by xuweinan on 2017/11/28.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spring.datasources")
public class MultiDruidDataSourceProperties implements MultiDataSourceConfig<DruidDataSourceProperties> {
    private String primaryDataSource;
    private boolean enableJtaTransaction;
    private Map<String, DruidDataSourceProperties> druid;

    @PostConstruct
    public void init() {
        if (Objects.isNull(druid)) {
            druid = new HashMap<>();
            return;
        }
        if (druid.size() == 1) {
            primaryDataSource = druid.keySet().iterator().next();
        }

        druid.forEach((key, value) -> {
            if (StringUtils.isEmpty(value.getUrl())
                || StringUtils.isEmpty(value.getUsername())
                || StringUtils.isEmpty(value.getPassword())) {
                throw new BeanCreationException(key + "数据库连接必需配置url, username, password");
            }
            value.setName(key);
        });

        if (StringUtils.isEmpty(primaryDataSource)) {
            throw new BeanCreationException("多数据源模式下必需配置主数据源[spin.datasource.primaryDataSource]");
        }
    }

    @Override
    public String getPrimaryDataSource() {
        return primaryDataSource;
    }

    @Override
    public void setPrimaryDataSource(String primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }


    @Override
    public boolean isEnableJtaTransaction() {
        return enableJtaTransaction;
    }

    @Override
    public void setEnableJtaTransaction(boolean enableJtaTransaction) {
        this.enableJtaTransaction = enableJtaTransaction;
    }

    @Override
    public Map<String, DruidDataSourceProperties> getDataSources() {
        return druid;
    }

    @Override
    public void setDataSources(Map<String, DruidDataSourceProperties> dataSources) {
        this.druid = dataSources;
    }

    @Override
    public DruidDataSourceProperties getDataSourceConfig(String name) {
        return druid.get(name);
    }

    @Override
    public DruidDataSourceProperties getPrimaryDataSourceConfig() {
        return druid.get(primaryDataSource);
    }

    public Map<String, DruidDataSourceProperties> getDruid() {
        return druid;
    }

    public void setDruid(Map<String, DruidDataSourceProperties> druid) {
        this.druid = druid;
    }
}
