package org.spin.boot.properties;


import org.spin.core.security.AES;
import org.spin.core.util.NumericUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.Properties;

/**
 * <p>Created by xuweinan on 2017/10/8.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DruidDataSourceProperties implements DataSourceConfig {
    private String name;
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    private Boolean testWhileIdle = true;
    private Boolean testOnBorrow;
    private String validationQuery = "SELECT 1";
    private Boolean useGlobalDataSourceStat;
    private String filters;
    private Long timeBetweenLogStatsMillis;
    private Integer maxSize;
    private Boolean clearFiltersEnable;
    private Boolean resetStatEnable;
    private Integer notFullTimeoutRetryCount;
    private Integer maxWaitThreadCount;
    private Boolean failFast;
    private Boolean phyTimeoutMillis;
    private Long minEvictableIdleTimeMillis = 300000L;
    private Long maxEvictableIdleTimeMillis;
    private Integer initialSize = 5;
    private Integer minIdle = 5;
    private Integer maxActive = 20;
    private Long maxWait = 60000L;
    private Long timeBetweenEvictionRunsMillis = 60000L;
    private Boolean poolPreparedStatements = true;
    private Integer maxPoolPreparedStatementPerConnectionSize = 20;
    private Boolean removeAbandoned = true;
    private Integer removeAbandonedTimeout = 120;
    private String servletPath = "/druid/*";
    private Properties connectionProperties = new Properties() {
        private static final long serialVersionUID = -8638010368833820798L;

        {
            put("druid.stat.mergeSql", "true");
            put("druid.stat.slowSqlMillis", "5000");
        }
    };

    @Override
    public Properties toProperties() {
        Properties properties = new Properties();
        notNullAdd(properties, null, "name", name);
        notNullAdd(properties, null, "url", url);
        notNullAdd(properties, null, "driverClassName", driverClassName);
        notNullAdd(properties, null, "username", username);
        notNullAdd(properties, null, "password", password);
        notNullAdd(properties, null, "testWhileIdle", testWhileIdle);
        notNullAdd(properties, null, "testOnBorrow", testOnBorrow);
        notNullAdd(properties, null, "validationQuery", validationQuery);
        notNullAdd(properties, null, "useGlobalDataSourceStat", useGlobalDataSourceStat);
        notNullAdd(properties, null, "filters", filters);
        notNullAdd(properties, null, "timeBetweenLogStatsMillis", timeBetweenLogStatsMillis);
        notNullAdd(properties, null, "stat.sql.MaxSize", maxSize);
        notNullAdd(properties, null, "clearFiltersEnable", clearFiltersEnable);
        notNullAdd(properties, null, "resetStatEnable", resetStatEnable);
        notNullAdd(properties, null, "notFullTimeoutRetryCount", notFullTimeoutRetryCount);
        notNullAdd(properties, null, "maxWaitThreadCount", maxWaitThreadCount);
        notNullAdd(properties, null, "failFast", failFast);
        notNullAdd(properties, null, "phyTimeoutMillis", phyTimeoutMillis);
        notNullAdd(properties, null, "minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
        notNullAdd(properties, null, "maxEvictableIdleTimeMillis", maxEvictableIdleTimeMillis);
        notNullAdd(properties, null, "initialSize", initialSize);
        notNullAdd(properties, null, "minIdle", minIdle);
        notNullAdd(properties, null, "maxActive", maxActive);
        notNullAdd(properties, null, "maxWait", maxWait);
        notNullAdd(properties, null, "timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
        notNullAdd(properties, null, "poolPreparedStatements", poolPreparedStatements);
        notNullAdd(properties, null, "maxPoolPreparedStatementPerConnectionSize", maxPoolPreparedStatementPerConnectionSize);
        return properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
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

    public void setPassword(String password) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        this.password = AES.decrypt("c4b2a7d36f9a2e61", password);
    }

    @Override
    public int getMaxPoolSize() {
        return NumericUtils.nullToZero(maxSize) == 0 ? (maxActive * 2) : maxSize;
    }

    @Override
    public int getMinPoolSize() {
        return minIdle;
    }

    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public Boolean getUseGlobalDataSourceStat() {
        return useGlobalDataSourceStat;
    }

    public void setUseGlobalDataSourceStat(Boolean useGlobalDataSourceStat) {
        this.useGlobalDataSourceStat = useGlobalDataSourceStat;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public Long getTimeBetweenLogStatsMillis() {
        return timeBetweenLogStatsMillis;
    }

    public void setTimeBetweenLogStatsMillis(Long timeBetweenLogStatsMillis) {
        this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Boolean getClearFiltersEnable() {
        return clearFiltersEnable;
    }

    public void setClearFiltersEnable(Boolean clearFiltersEnable) {
        this.clearFiltersEnable = clearFiltersEnable;
    }

    public Boolean getResetStatEnable() {
        return resetStatEnable;
    }

    public void setResetStatEnable(Boolean resetStatEnable) {
        this.resetStatEnable = resetStatEnable;
    }

    public Integer getNotFullTimeoutRetryCount() {
        return notFullTimeoutRetryCount;
    }

    public void setNotFullTimeoutRetryCount(Integer notFullTimeoutRetryCount) {
        this.notFullTimeoutRetryCount = notFullTimeoutRetryCount;
    }

    public Integer getMaxWaitThreadCount() {
        return maxWaitThreadCount;
    }

    public void setMaxWaitThreadCount(Integer maxWaitThreadCount) {
        this.maxWaitThreadCount = maxWaitThreadCount;
    }

    public Boolean getFailFast() {
        return failFast;
    }

    public void setFailFast(Boolean failFast) {
        this.failFast = failFast;
    }

    public Boolean getPhyTimeoutMillis() {
        return phyTimeoutMillis;
    }

    public void setPhyTimeoutMillis(Boolean phyTimeoutMillis) {
        this.phyTimeoutMillis = phyTimeoutMillis;
    }

    public Long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Long getMaxEvictableIdleTimeMillis() {
        return maxEvictableIdleTimeMillis;
    }

    public void setMaxEvictableIdleTimeMillis(Long maxEvictableIdleTimeMillis) {
        this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Long maxWait) {
        this.maxWait = maxWait;
    }

    public Long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Boolean getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public Integer getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(Integer maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public Boolean getRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(Boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public Integer getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getXaDataSourceClassName() {
        return "com.alibaba.druid.pool.xa.DruidXADataSource";
    }

    @Override
    public String getDataSourceClassName() {
        return "com.alibaba.druid.pool.DruidDataSource";
    }
}
