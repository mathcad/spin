package org.spin.data.extend;

import java.util.Map;

/**
 * 多数据源配置定义
 * <p>Created by xuweinan on 2017/11/28.</p>
 *
 * @author xuweinan
 */
public interface MultiDataSourceConfig<T extends DataSourceConfig> {

    /**
     * 获取主数据源配置名称
     *
     * @return 主数据源配置名称
     */
    String getPrimaryDataSource();

    /**
     * 设置主数据源配置名称
     *
     * @param primaryDataSource 主数据源配置名称
     */
    void setPrimaryDataSource(String primaryDataSource);

    /**
     * 获取所有数据源配置
     *
     * @return 数据源配置Map
     */
    Map<String, T> getDataSources();

    /**
     * 设置所有数据源配置
     *
     * @param dataSources 数据源配置Map
     */
    void setDataSources(Map<String, T> dataSources);

    /**
     * 获取指定名称的数据源配置
     *
     * @param name 数据源名称
     * @return 数据源配置
     */
    T getDataSourceConfig(String name);

    /**
     * 获取主数据源配置
     *
     * @return 主数据源配置
     */
    T getPrimaryDataSourceConfig();
}
