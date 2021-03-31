package org.spin.datasource.creator;

import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;

import javax.sql.DataSource;

/**
 * 默认按照以下顺序创建数据源:
 * <pre>
 * 	JNDI(1000) &gt; DRUID(2000) &gt; HIKARI(3000) &gt; BASIC(5000)
 * </pre>
 *
 * @author ls9527
 */
public interface DataSourceCreator {

    /**
     * 通过属性创建数据源
     *
     * @param dataSourceProperty 数据源属性
     * @return 被创建的数据源
     */
    DataSource createDataSource(DataSourceProperty dataSourceProperty);

    /**
     * 通过属性创建数据源
     *
     * @param dataSourceProperty 数据源属性
     * @param publicKey          解密公钥
     * @return 被创建的数据源
     */
    DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey);

    /**
     * 当前创建器是否支持根据此属性创建
     *
     * @param dataSourceProperty 数据源属性
     * @return 是否支持
     */
    boolean support(DataSourceProperty dataSourceProperty);
}
