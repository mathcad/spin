package org.spin.boot.properties;

import org.spin.core.util.StringUtils;
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
@ConfigurationProperties(prefix = "spin.druid")
public class MultiDruidDataSourceProperties implements MultiDataSourceConfig<DruidDataSourceProperties> {
    private String primaryDataSource;
    private Map<String, DruidDataSourceProperties> dataSources;

    @PostConstruct
    public void init() {
        if (Objects.isNull(dataSources)) {
            dataSources = new HashMap<>();
        }
        if (dataSources.size() == 1) {
            primaryDataSource = dataSources.keySet().iterator().next();
        }

        dataSources.forEach((key, value) -> {
            if (StringUtils.isEmpty(value.getUrl())
                || StringUtils.isEmpty(value.getUsername())
                || StringUtils.isEmpty(value.getPassword())) {
                throw new BeanCreationException(key + "数据库连接必需配置url, username, password");
            }
            value.setName(key);
        });
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
    public Map<String, DruidDataSourceProperties> getDataSources() {
        return dataSources;
    }

    @Override
    public void setDataSources(Map<String, DruidDataSourceProperties> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public DruidDataSourceProperties getDataSourceConfig(String name) {
        return dataSources.get(name);
    }

    @Override
    public DruidDataSourceProperties getPrimaryDataSourceConfig() {
        return dataSources.get(primaryDataSource);
    }
}
