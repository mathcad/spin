package org.spin.datasource.creator;

import org.apache.commons.dbcp2.BasicDataSource;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.spin.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.spin.datasource.spring.boot.autoconfigure.dbcp2.Dbcp2Config;
import org.spin.datasource.toolkit.ConfigMergeCreator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.spin.datasource.support.DdConstants.DBCP2_DATASOURCE;

/**
 * DBCP数据源创建器
 *
 * @author TaoYu
 * @since 2021/5/18
 */
public class Dbcp2DataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator {

    private static final ConfigMergeCreator<Dbcp2Config, BasicDataSource> MERGE_CREATOR = new ConfigMergeCreator<>("Dbcp2", Dbcp2Config.class, BasicDataSource.class);
    private static Boolean dbcp2Exists = false;

    static {
        try {
            Class.forName(DBCP2_DATASOURCE);
            dbcp2Exists = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private final Dbcp2Config gConfig;

    public Dbcp2DataSourceCreator(DynamicDataSourceProperties dynamicDataSourceProperties) {
        super(dynamicDataSourceProperties);
        this.gConfig = dynamicDataSourceProperties.getDbcp2();
    }

    @Override
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        BasicDataSource dataSource = MERGE_CREATOR.create(gConfig, dataSourceProperty.getDbcp2());
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            try {
                dataSource.start();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return dataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null && dbcp2Exists) || (type != null && DBCP2_DATASOURCE.equals(type.getName()));
    }
}
