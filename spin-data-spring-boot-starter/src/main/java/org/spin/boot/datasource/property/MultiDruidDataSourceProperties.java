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
@ConfigurationProperties(prefix = "spring.datasource")
public class MultiDruidDataSourceProperties implements MultiDataSourceConfig<DruidDataSourceProperties> {
    private String primaryDataSource;
    private Map<String, DruidDataSourceProperties> druids;
    private DruidDataSourceProperties druid;

    @PostConstruct
    public void init() {
        if (Objects.isNull(druids)) {
            druids = new HashMap<>();
            return;
        }
        if (druids.size() == 1) {
            primaryDataSource = druids.keySet().iterator().next();
        }

        druids.forEach((key, value) -> {
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
    public Map<String, DruidDataSourceProperties> getDataSources() {
        return druids;
    }

    @Override
    public void setDataSources(Map<String, DruidDataSourceProperties> dataSources) {
        this.druids = dataSources;
    }

    @Override
    public DruidDataSourceProperties getDataSourceConfig(String name) {
        return druids.get(name);
    }

    @Override
    public DruidDataSourceProperties getPrimaryDataSourceConfig() {
        return druids.get(primaryDataSource);
    }

    public Map<String, DruidDataSourceProperties> getDruids() {
        return druids;
    }

    public void setDruids(Map<String, DruidDataSourceProperties> druids) {
        this.druids = druids;
    }

    public DruidDataSourceProperties getDruid() {
        return druid;
    }

    public void setDruid(DruidDataSourceProperties druid) {
        this.druid = druid;
    }

    @Override
    public DruidDataSourceProperties getSingleton() {
        return druid;
    }

    @Override
    public void setSingleton(DruidDataSourceProperties singleton) {
        this.druid = singleton;
    }
}
