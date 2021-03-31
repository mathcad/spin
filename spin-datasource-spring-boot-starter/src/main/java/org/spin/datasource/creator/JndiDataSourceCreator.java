package org.spin.datasource.creator;

import org.spin.core.util.StringUtils;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

/**
 * JNDI数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/27
 */
public class JndiDataSourceCreator extends AbstractDataSourceCreator {

    private static final JndiDataSourceLookup LOOKUP = new JndiDataSourceLookup();

    /**
     * 创建JNDI数据源
     *
     * @param dataSourceProperty jndi数据源名称
     * @param publicKey          publicKey
     * @return 数据源
     */
    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        return LOOKUP.getDataSource(dataSourceProperty.getJndiName());
    }

    public DataSource createDataSource(String jndiName) {
        return LOOKUP.getDataSource(jndiName);
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        return StringUtils.isNotBlank(dataSourceProperty.getJndiName());
    }
}
