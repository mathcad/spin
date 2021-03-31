package org.spin.datasource.creator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.spin.core.util.StringUtils;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.spin.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;

import javax.sql.DataSource;
import java.util.Objects;

import static org.spin.datasource.support.DdConstants.HIKARI_DATASOURCE;

/**
 * Hikari数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
public class HikariDataSourceCreator extends AbstractDataSourceCreator {

    private static Boolean hikariExists = false;

    static {

        try {
            Class.forName(HIKARI_DATASOURCE);
            hikariExists = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private HikariCpConfig hikariCpConfig;

    public HikariDataSourceCreator() {
    }

    public HikariDataSourceCreator(HikariCpConfig hikariCpConfig) {
        this.hikariCpConfig = hikariCpConfig;
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
            dataSourceProperty.setPublicKey(publicKey);
        }
        HikariConfig config = dataSourceProperty.getHikari().toHikariConfig(hikariCpConfig);
        config.setUsername(dataSourceProperty.getUsername());
        config.setPassword(dataSourceProperty.getPassword());
        config.setJdbcUrl(dataSourceProperty.getUrl());
        config.setPoolName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (StringUtils.isNotEmpty(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }
        return new HikariDataSource(config);
    }


    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null && hikariExists) || (type != null && HIKARI_DATASOURCE.equals(type.getName()));
    }

    public static Boolean getHikariExists() {
        return hikariExists;
    }

    public static void setHikariExists(Boolean hikariExists) {
        HikariDataSourceCreator.hikariExists = hikariExists;
    }

    public HikariCpConfig getHikariCpConfig() {
        return hikariCpConfig;
    }

    public void setHikariCpConfig(HikariCpConfig hikariCpConfig) {
        this.hikariCpConfig = hikariCpConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HikariDataSourceCreator)) return false;
        HikariDataSourceCreator that = (HikariDataSourceCreator) o;
        return Objects.equals(hikariCpConfig, that.hikariCpConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hikariCpConfig);
    }

    @Override
    public String toString() {
        return "HikariDataSourceCreator{" +
            "hikariCpConfig=" + hikariCpConfig +
            '}';
    }
}
