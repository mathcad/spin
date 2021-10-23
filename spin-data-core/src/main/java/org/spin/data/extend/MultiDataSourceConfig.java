package org.spin.data.extend;

import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 多数据源配置定义
 * <p>Created by xuweinan on 2017/11/28.</p>
 *
 * @author xuweinan
 */
public interface MultiDataSourceConfig<T extends DataSourceConfig> extends InitializingBean {

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

    /**
     * 是否在View层开启Session
     *
     * @return 是/否
     */
    boolean isOpenSessionInView();

    /**
     * openSessionInview的排除路径
     *
     * @return 排除路径
     */
    String getExcluePathPattern();

    @Override
    default void afterPropertiesSet() {
        if (Objects.isNull(getDataSources())) {
            setDataSources(new HashMap<>());
            return;
        }
        if (getDataSources().size() == 1) {
            setPrimaryDataSource(getDataSources().keySet().iterator().next());
        }

        getDataSources().forEach((key, value) -> {
            if (StringUtils.isEmpty(value.getUrl())
                || StringUtils.isEmpty(value.getUsername())
                || StringUtils.isEmpty(value.getPassword())) {
                throw new IllegalArgumentException(key + "数据库连接必需配置url, username, password");
            }
            value.setName(key);
        });

        if (StringUtils.isEmpty(getPrimaryDataSource())) {
            throw new IllegalArgumentException("多数据源模式下必需配置主数据源[spin.datasource.primaryDataSource]");
        }
    }
}
