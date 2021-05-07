package org.spin.datasource.creator;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.spin.core.util.StringUtils;
import org.spin.datasource.exception.ErrorCreateDataSourceException;
import org.spin.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.spin.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import org.spin.datasource.spring.boot.autoconfigure.druid.DruidSlf4jConfig;
import org.spin.datasource.spring.boot.autoconfigure.druid.DruidWallConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.spin.datasource.support.DdConstants.DRUID_DATASOURCE;

/**
 * Druid数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
public class DruidDataSourceCreator extends AbstractDataSourceCreator {

    private static Boolean druidExists = false;

    static {
        try {
            Class.forName(DRUID_DATASOURCE);
            druidExists = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private DruidConfig gConfig;

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    public DruidDataSourceCreator(DruidConfig gConfig) {
        this.gConfig = gConfig;
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
            dataSourceProperty.setPublicKey(publicKey);
        }
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        dataSource.setName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (StringUtils.isNotEmpty(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        DruidConfig config = dataSourceProperty.getDruid();
        Properties properties = config.toProperties(gConfig);
        String filters = properties.getProperty("druid.filters");
        List<Filter> proxyFilters = new ArrayList<>(2);
        if (StringUtils.isNotEmpty(filters) && filters.contains("stat")) {
            StatFilter statFilter = new StatFilter();
            statFilter.configFromProperties(properties);
            proxyFilters.add(statFilter);
        }
        if (StringUtils.isNotEmpty(filters) && filters.contains("wall")) {
            WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(dataSourceProperty.getDruid().getWall(), gConfig.getWall());
            WallFilter wallFilter = new WallFilter();
            wallFilter.setConfig(wallConfig);
            proxyFilters.add(wallFilter);
        }
        if (StringUtils.isNotEmpty(filters) && filters.contains("slf4j")) {
            Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
            // 由于properties上面被用了，LogFilter不能使用configFromProperties方法，这里只能一个个set了。
            DruidSlf4jConfig slf4jConfig = gConfig.getSlf4j();
            slf4jLogFilter.setStatementLogEnabled(slf4jConfig.getEnable());
            slf4jLogFilter.setStatementExecutableSqlLogEnable(slf4jConfig.getStatementExecutableSqlLogEnable());
            proxyFilters.add(slf4jLogFilter);
        }

        if (this.applicationContext != null) {
            for (String filterId : gConfig.getProxyFilters()) {
                proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
            }
        }
        dataSource.setProxyFilters(proxyFilters);
        dataSource.configFromPropety(properties);
        //连接参数单独设置
        dataSource.setConnectProperties(config.getConnectionProperties());
        //设置druid内置properties不支持的的参数
        setParam(dataSource, config);

        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new ErrorCreateDataSourceException("druid create error", e);
        }
        return dataSource;
    }

    public DruidDataSourceCreator(DruidConfig gConfig, ApplicationContext applicationContext) {
        this.gConfig = gConfig;
        this.applicationContext = applicationContext;
    }

    public DruidDataSourceCreator() {
    }

    public static Boolean getDruidExists() {
        return druidExists;
    }

    public static void setDruidExists(Boolean druidExists) {
        DruidDataSourceCreator.druidExists = druidExists;
    }

    public DruidConfig getgConfig() {
        return gConfig;
    }

    public void setgConfig(DruidConfig gConfig) {
        this.gConfig = gConfig;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DruidDataSourceCreator)) return false;
        DruidDataSourceCreator that = (DruidDataSourceCreator) o;
        return Objects.equals(gConfig, that.gConfig) && Objects.equals(applicationContext, that.applicationContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gConfig, applicationContext);
    }


    @Override
    public String toString() {
        return "DruidDataSourceCreator{" +
            "gConfig=" + gConfig +
            ", applicationContext=" + applicationContext +
            '}';
    }

    private void setParam(DruidDataSource dataSource, DruidConfig config) {
        String defaultCatalog = config.getDefaultCatalog() == null ? gConfig.getDefaultCatalog() : config.getDefaultCatalog();
        if (defaultCatalog != null) {
            dataSource.setDefaultCatalog(defaultCatalog);
        }
        Boolean defaultAutoCommit = config.getDefaultAutoCommit() == null ? gConfig.getDefaultAutoCommit() : config.getDefaultAutoCommit();
        if (defaultAutoCommit != null && !defaultAutoCommit) {
            dataSource.setDefaultAutoCommit(false);
        }
        Boolean defaultReadOnly = config.getDefaultReadOnly() == null ? gConfig.getDefaultReadOnly() : config.getDefaultReadOnly();
        if (defaultReadOnly != null) {
            dataSource.setDefaultReadOnly(defaultReadOnly);
        }
        Integer defaultTransactionIsolation = config.getDefaultTransactionIsolation() == null ? gConfig.getDefaultTransactionIsolation() : config.getDefaultTransactionIsolation();
        if (defaultTransactionIsolation != null) {
            dataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
        }

        Boolean testOnReturn = config.getTestOnReturn() == null ? gConfig.getTestOnReturn() : config.getTestOnReturn();
        if (testOnReturn != null && testOnReturn) {
            dataSource.setTestOnReturn(true);
        }
        Integer validationQueryTimeout =
            config.getValidationQueryTimeout() == null ? gConfig.getValidationQueryTimeout() : config.getValidationQueryTimeout();
        if (validationQueryTimeout != null && !validationQueryTimeout.equals(-1)) {
            dataSource.setValidationQueryTimeout(validationQueryTimeout);
        }

        Boolean sharePreparedStatements =
            config.getSharePreparedStatements() == null ? gConfig.getSharePreparedStatements() : config.getSharePreparedStatements();
        if (sharePreparedStatements != null && sharePreparedStatements) {
            dataSource.setSharePreparedStatements(true);
        }
        Integer connectionErrorRetryAttempts =
            config.getConnectionErrorRetryAttempts() == null ? gConfig.getConnectionErrorRetryAttempts()
                : config.getConnectionErrorRetryAttempts();
        if (connectionErrorRetryAttempts != null && !connectionErrorRetryAttempts.equals(1)) {
            dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
        }
        Boolean breakAfterAcquireFailure =
            config.getBreakAfterAcquireFailure() == null ? gConfig.getBreakAfterAcquireFailure() : config.getBreakAfterAcquireFailure();
        if (breakAfterAcquireFailure != null && breakAfterAcquireFailure) {
            dataSource.setBreakAfterAcquireFailure(true);
        }

        Integer timeout = config.getRemoveAbandonedTimeoutMillis() == null ? gConfig.getRemoveAbandonedTimeoutMillis()
            : config.getRemoveAbandonedTimeoutMillis();
        if (timeout != null) {
            dataSource.setRemoveAbandonedTimeoutMillis(timeout);
        }

        Boolean abandoned = config.getRemoveAbandoned() == null ? gConfig.getRemoveAbandoned() : config.getRemoveAbandoned();
        if (abandoned != null) {
            dataSource.setRemoveAbandoned(abandoned);
        }

        Boolean logAbandoned = config.getLogAbandoned() == null ? gConfig.getLogAbandoned() : config.getLogAbandoned();
        if (logAbandoned != null) {
            dataSource.setLogAbandoned(logAbandoned);
        }

        Integer queryTimeOut = config.getQueryTimeout() == null ? gConfig.getQueryTimeout() : config.getQueryTimeout();
        if (queryTimeOut != null) {
            dataSource.setQueryTimeout(queryTimeOut);
        }

        Integer transactionQueryTimeout =
            config.getTransactionQueryTimeout() == null ? gConfig.getTransactionQueryTimeout() : config.getTransactionQueryTimeout();
        if (transactionQueryTimeout != null) {
            dataSource.setTransactionQueryTimeout(transactionQueryTimeout);
        }
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null && druidExists) || (type != null && DRUID_DATASOURCE.equals(type.getName()));
    }
}