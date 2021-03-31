package org.spin.datasource.spring.boot.autoconfigure.hikari;

import com.zaxxer.hikari.HikariConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * HikariCp参数配置
 *
 * @author TaoYu
 * @since 2.4.1
 */
public class HikariCpConfig {
    private static final Logger logger = LoggerFactory.getLogger(HikariCpConfig.class);
    private static final long CONNECTION_TIMEOUT = SECONDS.toMillis(30);
    private static final long VALIDATION_TIMEOUT = SECONDS.toMillis(5);
    private static final long IDLE_TIMEOUT = MINUTES.toMillis(10);
    private static final long MAX_LIFETIME = MINUTES.toMillis(30);
    private static final int DEFAULT_POOL_SIZE = 10;

    private String username;
    private String password;
    private String driverClassName;
    private String jdbcUrl;
    private String poolName;

    private String catalog;
    private Long connectionTimeout;
    private Long validationTimeout;
    private Long idleTimeout;
    private Long leakDetectionThreshold;
    private Long maxLifetime;
    private Integer maxPoolSize;
    private Integer minIdle;

    private Long initializationFailTimeout;
    private String connectionInitSql;
    private String connectionTestQuery;
    private String dataSourceClassName;
    private String dataSourceJndiName;
    private String schema;
    private String transactionIsolationName;
    private Boolean isAutoCommit;
    private Boolean isReadOnly;
    private Boolean isIsolateInternalQueries;
    private Boolean isRegisterMbeans;
    private Boolean isAllowPoolSuspension;
    private Properties dataSourceProperties;
    private Properties healthCheckProperties;

    /**
     * 转换为HikariCP配置
     *
     * @param g 全局配置
     * @return HikariCP配置
     */
    public HikariConfig toHikariConfig(HikariCpConfig g) {
        HikariConfig config = new HikariConfig();

        String tempSchema = schema == null ? g.getSchema() : schema;
        if (tempSchema != null) {
            try {
                Field schemaField = HikariConfig.class.getDeclaredField("schema");
                schemaField.setAccessible(true);
                schemaField.set(config, tempSchema);
            } catch (NoSuchFieldException e) {
                logger.warn("动态数据源-设置了Hikari的schema属性，但当前Hikari版本不支持");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        String tempCatalog = catalog == null ? g.getCatalog() : catalog;
        if (tempCatalog != null) {
            config.setCatalog(tempCatalog);
        }

        Long tempConnectionTimeout = connectionTimeout == null ? g.getConnectionTimeout() : connectionTimeout;
        if (tempConnectionTimeout != null && !tempConnectionTimeout.equals(CONNECTION_TIMEOUT)) {
            config.setConnectionTimeout(tempConnectionTimeout);
        }

        Long tempValidationTimeout = validationTimeout == null ? g.getValidationTimeout() : validationTimeout;
        if (tempValidationTimeout != null && !tempValidationTimeout.equals(VALIDATION_TIMEOUT)) {
            config.setValidationTimeout(tempValidationTimeout);
        }

        Long tempIdleTimeout = idleTimeout == null ? g.getIdleTimeout() : idleTimeout;
        if (tempIdleTimeout != null && !tempIdleTimeout.equals(IDLE_TIMEOUT)) {
            config.setIdleTimeout(tempIdleTimeout);
        }

        Long tempLeakDetectionThreshold = leakDetectionThreshold == null ? g.getLeakDetectionThreshold() : leakDetectionThreshold;
        if (tempLeakDetectionThreshold != null) {
            config.setLeakDetectionThreshold(tempLeakDetectionThreshold);
        }

        Long tempMaxLifetime = maxLifetime == null ? g.getMaxLifetime() : maxLifetime;
        if (tempMaxLifetime != null && !tempMaxLifetime.equals(MAX_LIFETIME)) {
            config.setMaxLifetime(tempMaxLifetime);
        }

        Integer tempMaxPoolSize = maxPoolSize == null ? g.getMaxPoolSize() : maxPoolSize;
        if (tempMaxPoolSize != null && !tempMaxPoolSize.equals(-1)) {
            config.setMaximumPoolSize(tempMaxPoolSize);
        }

        Integer tempMinIdle = minIdle == null ? g.getMinIdle() : getMinIdle();
        if (tempMinIdle != null && !tempMinIdle.equals(-1)) {
            config.setMinimumIdle(tempMinIdle);
        }

        Long tempInitializationFailTimeout = initializationFailTimeout == null ? g.getInitializationFailTimeout() : initializationFailTimeout;
        if (tempInitializationFailTimeout != null && !tempInitializationFailTimeout.equals(1L)) {
            config.setInitializationFailTimeout(tempInitializationFailTimeout);
        }

        String tempConnectionInitSql = connectionInitSql == null ? g.getConnectionInitSql() : connectionInitSql;
        if (tempConnectionInitSql != null) {
            config.setConnectionInitSql(tempConnectionInitSql);
        }

        String tempConnectionTestQuery = connectionTestQuery == null ? g.getConnectionTestQuery() : connectionTestQuery;
        if (tempConnectionTestQuery != null) {
            config.setConnectionTestQuery(tempConnectionTestQuery);
        }

        String tempDataSourceClassName = dataSourceClassName == null ? g.getDataSourceClassName() : dataSourceClassName;
        if (tempDataSourceClassName != null) {
            config.setDataSourceClassName(tempDataSourceClassName);
        }

        String tempDataSourceJndiName = dataSourceJndiName == null ? g.getDataSourceJndiName() : dataSourceJndiName;
        if (tempDataSourceJndiName != null) {
            config.setDataSourceJNDI(tempDataSourceJndiName);
        }

        String tempTransactionIsolationName = transactionIsolationName == null ? g.getTransactionIsolationName() : transactionIsolationName;
        if (tempTransactionIsolationName != null) {
            config.setTransactionIsolation(tempTransactionIsolationName);
        }

        Boolean tempAutoCommit = isAutoCommit == null ? g.getIsAutoCommit() : isAutoCommit;
        if (tempAutoCommit != null && tempAutoCommit.equals(Boolean.FALSE)) {
            config.setAutoCommit(false);
        }

        Boolean tempReadOnly = isReadOnly == null ? g.getIsReadOnly() : isReadOnly;
        if (tempReadOnly != null) {
            config.setReadOnly(tempReadOnly);
        }

        Boolean tempIsolateInternalQueries = isIsolateInternalQueries == null ? g.getIsIsolateInternalQueries() : isIsolateInternalQueries;
        if (tempIsolateInternalQueries != null) {
            config.setIsolateInternalQueries(tempIsolateInternalQueries);
        }

        Boolean tempRegisterMbeans = isRegisterMbeans == null ? g.getIsRegisterMbeans() : isRegisterMbeans;
        if (tempRegisterMbeans != null) {
            config.setRegisterMbeans(tempRegisterMbeans);
        }

        Boolean tempAllowPoolSuspension = isAllowPoolSuspension == null ? g.getIsAllowPoolSuspension() : isAllowPoolSuspension;
        if (tempAllowPoolSuspension != null) {
            config.setAllowPoolSuspension(tempAllowPoolSuspension);
        }

        Properties tempDataSourceProperties = dataSourceProperties == null ? g.getDataSourceProperties() : dataSourceProperties;
        if (tempDataSourceProperties != null) {
            config.setDataSourceProperties(tempDataSourceProperties);
        }

        Properties tempHealthCheckProperties = healthCheckProperties == null ? g.getHealthCheckProperties() : healthCheckProperties;
        if (tempHealthCheckProperties != null) {
            config.setHealthCheckProperties(tempHealthCheckProperties);
        }
        return config;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Long getValidationTimeout() {
        return validationTimeout;
    }

    public void setValidationTimeout(Long validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    public Long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }

    public void setLeakDetectionThreshold(Long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
    }

    public Long getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Long getInitializationFailTimeout() {
        return initializationFailTimeout;
    }

    public void setInitializationFailTimeout(Long initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }

    public String getConnectionInitSql() {
        return connectionInitSql;
    }

    public void setConnectionInitSql(String connectionInitSql) {
        this.connectionInitSql = connectionInitSql;
    }

    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
    }

    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    public void setDataSourceJndiName(String dataSourceJndiName) {
        this.dataSourceJndiName = dataSourceJndiName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTransactionIsolationName() {
        return transactionIsolationName;
    }

    public void setTransactionIsolationName(String transactionIsolationName) {
        this.transactionIsolationName = transactionIsolationName;
    }

    public Boolean getIsAutoCommit() {
        return isAutoCommit;
    }

    public void setIsAutoCommit(Boolean isAutoCommit) {
        this.isAutoCommit = isAutoCommit;
    }

    public Boolean getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public Boolean getIsIsolateInternalQueries() {
        return isIsolateInternalQueries;
    }

    public void setIsIsolateInternalQueries(Boolean isIsolateInternalQueries) {
        this.isIsolateInternalQueries = isIsolateInternalQueries;
    }

    public Boolean getIsRegisterMbeans() {
        return isRegisterMbeans;
    }

    public void setIsRegisterMbeans(Boolean isRegisterMbeans) {
        this.isRegisterMbeans = isRegisterMbeans;
    }

    public Boolean getIsAllowPoolSuspension() {
        return isAllowPoolSuspension;
    }

    public void setIsAllowPoolSuspension(Boolean isAllowPoolSuspension) {
        this.isAllowPoolSuspension = isAllowPoolSuspension;
    }

    public Properties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(Properties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public Properties getHealthCheckProperties() {
        return healthCheckProperties;
    }

    public void setHealthCheckProperties(Properties healthCheckProperties) {
        this.healthCheckProperties = healthCheckProperties;
    }

    @Override
    public String toString() {
        return "HikariCpConfig{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", driverClassName='" + driverClassName + '\'' +
            ", jdbcUrl='" + jdbcUrl + '\'' +
            ", poolName='" + poolName + '\'' +
            ", catalog='" + catalog + '\'' +
            ", connectionTimeout=" + connectionTimeout +
            ", validationTimeout=" + validationTimeout +
            ", idleTimeout=" + idleTimeout +
            ", leakDetectionThreshold=" + leakDetectionThreshold +
            ", maxLifetime=" + maxLifetime +
            ", maxPoolSize=" + maxPoolSize +
            ", minIdle=" + minIdle +
            ", initializationFailTimeout=" + initializationFailTimeout +
            ", connectionInitSql='" + connectionInitSql + '\'' +
            ", connectionTestQuery='" + connectionTestQuery + '\'' +
            ", dataSourceClassName='" + dataSourceClassName + '\'' +
            ", dataSourceJndiName='" + dataSourceJndiName + '\'' +
            ", schema='" + schema + '\'' +
            ", transactionIsolationName='" + transactionIsolationName + '\'' +
            ", isAutoCommit=" + isAutoCommit +
            ", isReadOnly=" + isReadOnly +
            ", isIsolateInternalQueries=" + isIsolateInternalQueries +
            ", isRegisterMbeans=" + isRegisterMbeans +
            ", isAllowPoolSuspension=" + isAllowPoolSuspension +
            ", dataSourceProperties=" + dataSourceProperties +
            ", healthCheckProperties=" + healthCheckProperties +
            '}';
    }
}
