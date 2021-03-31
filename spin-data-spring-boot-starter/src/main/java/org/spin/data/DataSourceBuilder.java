package org.spin.data;

import org.spin.data.extend.DataSourceConfig;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import javax.sql.DataSource;

/**
 * 数据源构建器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface DataSourceBuilder<T extends DataSource, C extends DataSourceConfig> {

    /**
     * 准备Atomikos数据源的bean，并注册到Spring容器中
     *
     * @param acf      beanFactory
     * @param dbConfig 数据源配置
     * @return 注册后的数据源
     */
    DataSource buildAtomikosDataSource(DefaultListableBeanFactory acf, C dbConfig);

    /**
     * 准备普通数据源，并注册到Spring容器中
     *
     * @param acf      beanFactory
     * @param dbConfig 数据源配置
     * @return 注册后的数据源
     */
    T buildSingletonDatasource(DefaultListableBeanFactory acf, C dbConfig);
}
