package org.spin.boot.properties;

import java.util.Map;

/**
 * <p>Created by xuweinan on 2017/11/28.</p>
 *
 * @author xuweinan
 */
public interface MultiDataSourceConfig<T extends DataSourceConfig> {
    String getPrimaryDataSource();

    void setPrimaryDataSource(String primaryDataSource);

    Map<String, T> getDataSources();

    void setDataSources(Map<String, T> dataSources);

    T getDataSourceConfig(String name);

    T getPrimaryDataSourceConfig();
}
