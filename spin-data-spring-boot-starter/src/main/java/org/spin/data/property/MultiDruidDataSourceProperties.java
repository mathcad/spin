package org.spin.data.property;

import org.spin.data.extend.MultiDataSourceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Druid的多数据源配置
 * <p>Created by xuweinan on 2017/11/28.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.datasource.druid")
public class MultiDruidDataSourceProperties implements MultiDataSourceConfig<DruidDataSourceProperties> {
    private String primaryDataSource;
    private boolean openSessionInView;
    private String excluePathPattern;
    private Map<String, DruidDataSourceProperties> ds;

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
        return ds;
    }

    @Override
    public void setDataSources(Map<String, DruidDataSourceProperties> dataSources) {
        this.ds = dataSources;
    }

    @Override
    public DruidDataSourceProperties getDataSourceConfig(String name) {
        return ds.get(name);
    }

    @Override
    public DruidDataSourceProperties getPrimaryDataSourceConfig() {
        return ds.get(primaryDataSource);
    }

    @Override
    public boolean isOpenSessionInView() {
        return openSessionInView;
    }

    public void setOpenSessionInView(boolean openSessionInView) {
        this.openSessionInView = openSessionInView;
    }

    @Override
    public String getExcluePathPattern() {
        return excluePathPattern;
    }

    public void setExcluePathPattern(String excluePathPattern) {
        this.excluePathPattern = excluePathPattern;
    }

    public Map<String, DruidDataSourceProperties> getDs() {
        return ds;
    }

    public void setDs(Map<String, DruidDataSourceProperties> druids) {
        this.ds = druids;
    }
}
