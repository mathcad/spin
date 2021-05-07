package org.spin.datasource.spring.boot.autoconfigure.druid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.alibaba.druid.pool.DruidAbstractDataSource.*;
import static org.spin.datasource.spring.boot.autoconfigure.druid.DruidConsts.*;

/**
 * Druid参数配置
 *
 * @author TaoYu
 * @since 1.2.0
 */
public class DruidConfig {
    private static final Logger logger = LoggerFactory.getLogger(DruidConfig.class);

    private Integer initialSize;
    private Integer maxActive;
    private Integer minIdle;
    private Integer maxWait;
    private Long timeBetweenEvictionRunsMillis;
    private Long timeBetweenLogStatsMillis;
    private Integer statSqlMaxSize;
    private Long minEvictableIdleTimeMillis;
    private Long maxEvictableIdleTimeMillis;
    private String defaultCatalog;
    private Boolean defaultAutoCommit;
    private Boolean defaultReadOnly;
    private Integer defaultTransactionIsolation;
    private Boolean testWhileIdle;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private String validationQuery;
    private Integer validationQueryTimeout;
    private Boolean useGlobalDataSourceStat;
    private Boolean asyncInit;
    private String filters;
    private Boolean clearFiltersEnable;
    private Boolean resetStatEnable;
    private Integer notFullTimeoutRetryCount;
    private Integer maxWaitThreadCount;
    private Boolean failFast;
    private Long phyTimeoutMillis;
    private Boolean keepAlive;
    private Boolean poolPreparedStatements;
    private Boolean initVariants;
    private Boolean initGlobalVariants;
    private Boolean useUnfairLock;
    private Boolean killWhenSocketReadTimeout;
    private Properties connectionProperties;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private String initConnectionSqls;
    private Boolean sharePreparedStatements;
    private Integer connectionErrorRetryAttempts;
    private Boolean breakAfterAcquireFailure;
    private Boolean removeAbandoned;
    private Integer removeAbandonedTimeoutMillis;
    private Boolean logAbandoned;
    private Integer queryTimeout;
    private Integer transactionQueryTimeout;
    private String publicKey;

    @NestedConfigurationProperty
    private DruidWallConfig wall = new DruidWallConfig();

    @NestedConfigurationProperty
    private DruidStatConfig stat = new DruidStatConfig();

    @NestedConfigurationProperty
    private DruidSlf4jConfig slf4j = new DruidSlf4jConfig();

    private List<String> proxyFilters = new ArrayList<>();

    /**
     * 根据全局配置和本地配置结合转换为Properties
     *
     * @param g 全局配置
     * @return Druid配置
     */
    public Properties toProperties(DruidConfig g) {
        Properties properties = new Properties();
        Integer initialSize = this.initialSize == null ? g.getInitialSize() : this.initialSize;
        if (initialSize != null && !initialSize.equals(DEFAULT_INITIAL_SIZE)) {
            properties.setProperty(INITIAL_SIZE, String.valueOf(initialSize));
        }

        Integer maxActive = this.maxActive == null ? g.getMaxActive() : this.maxActive;
        if (maxActive != null && !maxActive.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty(MAX_ACTIVE, String.valueOf(maxActive));
        }

        Integer minIdle = this.minIdle == null ? g.getMinIdle() : this.minIdle;
        if (minIdle != null && !minIdle.equals(DEFAULT_MIN_IDLE)) {
            properties.setProperty(MIN_IDLE, String.valueOf(minIdle));
        }

        Integer maxWait = this.maxWait == null ? g.getMaxWait() : this.maxWait;
        if (maxWait != null && !maxWait.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty(MAX_WAIT, String.valueOf(maxWait));
        }

        Long timeBetweenEvictionRunsMillis =
            this.timeBetweenEvictionRunsMillis == null ? g.getTimeBetweenEvictionRunsMillis() : this.timeBetweenEvictionRunsMillis;
        if (timeBetweenEvictionRunsMillis != null && !timeBetweenEvictionRunsMillis.equals(DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS)) {
            properties.setProperty(TIME_BETWEEN_EVICTION_RUNS_MILLIS, String.valueOf(timeBetweenEvictionRunsMillis));
        }

        Long timeBetweenLogStatsMillis =
            this.timeBetweenLogStatsMillis == null ? g.getTimeBetweenLogStatsMillis() : this.timeBetweenLogStatsMillis;
        if (timeBetweenLogStatsMillis != null && timeBetweenLogStatsMillis > 0) {
            properties.setProperty(TIME_BETWEEN_LOG_STATS_MILLIS, String.valueOf(timeBetweenLogStatsMillis));
        }

        Long minEvictableIdleTimeMillis =
            this.minEvictableIdleTimeMillis == null ? g.getMinEvictableIdleTimeMillis() : this.minEvictableIdleTimeMillis;
        if (minEvictableIdleTimeMillis != null && !minEvictableIdleTimeMillis.equals(DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS)) {
            properties.setProperty(MIN_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(minEvictableIdleTimeMillis));
        }

        Long maxEvictableIdleTimeMillis =
            this.maxEvictableIdleTimeMillis == null ? g.getMaxEvictableIdleTimeMillis() : this.maxEvictableIdleTimeMillis;
        if (maxEvictableIdleTimeMillis != null && !maxEvictableIdleTimeMillis.equals(DEFAULT_MAX_EVICTABLE_IDLE_TIME_MILLIS)) {
            properties.setProperty(MAX_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(maxEvictableIdleTimeMillis));
        }

        Boolean testWhileIdle = this.testWhileIdle == null ? g.getTestWhileIdle() : this.testWhileIdle;
        if (testWhileIdle != null && !testWhileIdle.equals(DEFAULT_WHILE_IDLE)) {
            properties.setProperty(TEST_WHILE_IDLE, "false");
        }

        Boolean testOnBorrow = this.testOnBorrow == null ? g.getTestOnBorrow() : this.testOnBorrow;
        if (testOnBorrow != null && !testOnBorrow.equals(DEFAULT_TEST_ON_BORROW)) {
            properties.setProperty(TEST_ON_BORROW, "true");
        }

        String validationQuery = this.validationQuery == null ? g.getValidationQuery() : this.validationQuery;
        if (validationQuery != null && validationQuery.length() > 0) {
            properties.setProperty(VALIDATION_QUERY, validationQuery);
        }

        Boolean useGlobalDataSourceStat = this.useGlobalDataSourceStat == null ? g.getUseGlobalDataSourceStat() : this.useGlobalDataSourceStat;
        if (useGlobalDataSourceStat != null && useGlobalDataSourceStat.equals(Boolean.TRUE)) {
            properties.setProperty(USE_GLOBAL_DATA_SOURCE_STAT, "true");
        }

        Boolean asyncInit = this.asyncInit == null ? g.getAsyncInit() : this.asyncInit;
        if (asyncInit != null && asyncInit.equals(Boolean.TRUE)) {
            properties.setProperty(ASYNC_INIT, "true");
        }

        //filters单独处理，默认了stat,wall
        String filters = this.filters == null ? g.getFilters() : this.filters;
        if (filters == null) {
            filters = "stat,wall";
        }
        if (publicKey != null && publicKey.length() > 0 && !filters.contains("config")) {
            filters += ",config";
        }
        properties.setProperty(FILTERS, filters);

        Boolean clearFiltersEnable = this.clearFiltersEnable == null ? g.getClearFiltersEnable() : this.clearFiltersEnable;
        if (clearFiltersEnable != null && clearFiltersEnable.equals(Boolean.FALSE)) {
            properties.setProperty(CLEAR_FILTERS_ENABLE, "false");
        }

        Boolean resetStatEnable = this.resetStatEnable == null ? g.getResetStatEnable() : this.resetStatEnable;
        if (resetStatEnable != null && resetStatEnable.equals(Boolean.FALSE)) {
            properties.setProperty(RESET_STAT_ENABLE, "false");
        }

        Integer notFullTimeoutRetryCount =
            this.notFullTimeoutRetryCount == null ? g.getNotFullTimeoutRetryCount() : this.notFullTimeoutRetryCount;
        if (notFullTimeoutRetryCount != null && !notFullTimeoutRetryCount.equals(0)) {
            properties.setProperty(NOT_FULL_TIMEOUT_RETRY_COUNT, String.valueOf(notFullTimeoutRetryCount));
        }

        Integer maxWaitThreadCount = this.maxWaitThreadCount == null ? g.getMaxWaitThreadCount() : this.maxWaitThreadCount;
        if (maxWaitThreadCount != null && !maxWaitThreadCount.equals(-1)) {
            properties.setProperty(MAX_WAIT_THREAD_COUNT, String.valueOf(maxWaitThreadCount));
        }

        Boolean failFast = this.failFast == null ? g.getFailFast() : this.failFast;
        if (failFast != null && failFast.equals(Boolean.TRUE)) {
            properties.setProperty(FAIL_FAST, "true");
        }

        Long phyTimeoutMillis = this.phyTimeoutMillis == null ? g.getPhyTimeoutMillis() : this.phyTimeoutMillis;
        if (phyTimeoutMillis != null && !phyTimeoutMillis.equals(DEFAULT_PHY_TIMEOUT_MILLIS)) {
            properties.setProperty(PHY_TIMEOUT_MILLIS, String.valueOf(phyTimeoutMillis));
        }

        Boolean keepAlive = this.keepAlive == null ? g.getKeepAlive() : this.keepAlive;
        if (keepAlive != null && keepAlive.equals(Boolean.TRUE)) {
            properties.setProperty(KEEP_ALIVE, "true");
        }

        Boolean poolPreparedStatements = this.poolPreparedStatements == null ? g.getPoolPreparedStatements() : this.poolPreparedStatements;
        if (poolPreparedStatements != null && poolPreparedStatements.equals(Boolean.TRUE)) {
            properties.setProperty(POOL_PREPARED_STATEMENTS, "true");
        }

        Boolean initVariants = this.initVariants == null ? g.getInitVariants() : this.initVariants;
        if (initVariants != null && initVariants.equals(Boolean.TRUE)) {
            properties.setProperty(INIT_VARIANTS, "true");
        }

        Boolean initGlobalVariants = this.initGlobalVariants == null ? g.getInitGlobalVariants() : this.initGlobalVariants;
        if (initGlobalVariants != null && initGlobalVariants.equals(Boolean.TRUE)) {
            properties.setProperty(INIT_GLOBAL_VARIANTS, "true");
        }

        Boolean useUnfairLock = this.useUnfairLock == null ? g.getUseUnfairLock() : this.useUnfairLock;
        if (useUnfairLock != null) {
            properties.setProperty(USE_UNFAIR_LOCK, String.valueOf(useUnfairLock));
        }

        Boolean killWhenSocketReadTimeout =
            this.killWhenSocketReadTimeout == null ? g.getKillWhenSocketReadTimeout() : this.killWhenSocketReadTimeout;
        if (killWhenSocketReadTimeout != null && killWhenSocketReadTimeout.equals(Boolean.TRUE)) {
            properties.setProperty(KILL_WHEN_SOCKET_READ_TIMEOUT, "true");
        }

        Properties connectProperties = connectionProperties == null ? g.getConnectionProperties() : connectionProperties;

        if (publicKey != null && publicKey.length() > 0) {
            if (connectProperties == null) {
                connectProperties = new Properties();
            }
            logger.debug("dynamic-datasource detect druid publicKey,It is highly recommended that you use the built-in encryption method \n " +
                "https://github.com/baomidou/dynamic-datasource-spring-boot-starter/wiki/ENCODE");
            connectProperties.setProperty("config.decrypt", "true");
            connectProperties.setProperty("config.decrypt.key", publicKey);
        }
        this.connectionProperties = connectProperties;

        Integer maxPoolPreparedStatementPerConnectionSize =
            this.maxPoolPreparedStatementPerConnectionSize == null ? g.getMaxPoolPreparedStatementPerConnectionSize()
                : this.maxPoolPreparedStatementPerConnectionSize;
        if (maxPoolPreparedStatementPerConnectionSize != null && !maxPoolPreparedStatementPerConnectionSize.equals(10)) {
            properties.setProperty(MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE, String.valueOf(maxPoolPreparedStatementPerConnectionSize));
        }

        String initConnectionSqls = this.initConnectionSqls == null ? g.getInitConnectionSqls() : this.initConnectionSqls;
        if (initConnectionSqls != null && initConnectionSqls.length() > 0) {
            properties.setProperty(INIT_CONNECTION_SQLS, initConnectionSqls);
        }

        //stat配置参数
        Integer statSqlMaxSize = this.statSqlMaxSize == null ? g.getStatSqlMaxSize() : this.statSqlMaxSize;
        if (statSqlMaxSize != null) {
            properties.setProperty(STAT_SQL_MAX_SIZE, String.valueOf(statSqlMaxSize));
        }

        Boolean logSlowSql = stat.getLogSlowSql() == null ? g.stat.getLogSlowSql() : stat.getLogSlowSql();
        if (logSlowSql != null && logSlowSql) {
            properties.setProperty(STAT_LOG_SLOW_SQL, "true");
        }
        Long slowSqlMillis = stat.getSlowSqlMillis() == null ? g.stat.getSlowSqlMillis() : stat.getSlowSqlMillis();
        if (slowSqlMillis != null) {
            properties.setProperty(STAT_SLOW_SQL_MILLIS, slowSqlMillis.toString());
        }

        Boolean mergeSql = stat.getMergeSql() == null ? g.stat.getMergeSql() : stat.getMergeSql();
        if (mergeSql != null && mergeSql) {
            properties.setProperty(STAT_MERGE_SQL, "true");
        }
        return properties;
    }

    public List<String> getProxyFilters() {
        return proxyFilters;
    }

    public void setProxyFilters(List<String> proxyFilters) {
        this.proxyFilters = proxyFilters;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Long getTimeBetweenLogStatsMillis() {
        return timeBetweenLogStatsMillis;
    }

    public void setTimeBetweenLogStatsMillis(Long timeBetweenLogStatsMillis) {
        this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
    }

    public Integer getStatSqlMaxSize() {
        return statSqlMaxSize;
    }

    public void setStatSqlMaxSize(Integer statSqlMaxSize) {
        this.statSqlMaxSize = statSqlMaxSize;
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

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public Boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public Boolean getDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setDefaultReadOnly(Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public Integer getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(Integer defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
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

    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public Integer getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public void setValidationQueryTimeout(Integer validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    public Boolean getUseGlobalDataSourceStat() {
        return useGlobalDataSourceStat;
    }

    public void setUseGlobalDataSourceStat(Boolean useGlobalDataSourceStat) {
        this.useGlobalDataSourceStat = useGlobalDataSourceStat;
    }

    public Boolean getAsyncInit() {
        return asyncInit;
    }

    public void setAsyncInit(Boolean asyncInit) {
        this.asyncInit = asyncInit;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
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

    public Long getPhyTimeoutMillis() {
        return phyTimeoutMillis;
    }

    public void setPhyTimeoutMillis(Long phyTimeoutMillis) {
        this.phyTimeoutMillis = phyTimeoutMillis;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public Boolean getInitVariants() {
        return initVariants;
    }

    public void setInitVariants(Boolean initVariants) {
        this.initVariants = initVariants;
    }

    public Boolean getInitGlobalVariants() {
        return initGlobalVariants;
    }

    public void setInitGlobalVariants(Boolean initGlobalVariants) {
        this.initGlobalVariants = initGlobalVariants;
    }

    public Boolean getUseUnfairLock() {
        return useUnfairLock;
    }

    public void setUseUnfairLock(Boolean useUnfairLock) {
        this.useUnfairLock = useUnfairLock;
    }

    public Boolean getKillWhenSocketReadTimeout() {
        return killWhenSocketReadTimeout;
    }

    public void setKillWhenSocketReadTimeout(Boolean killWhenSocketReadTimeout) {
        this.killWhenSocketReadTimeout = killWhenSocketReadTimeout;
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public Integer getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(Integer maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public String getInitConnectionSqls() {
        return initConnectionSqls;
    }

    public void setInitConnectionSqls(String initConnectionSqls) {
        this.initConnectionSqls = initConnectionSqls;
    }

    public Boolean getSharePreparedStatements() {
        return sharePreparedStatements;
    }

    public void setSharePreparedStatements(Boolean sharePreparedStatements) {
        this.sharePreparedStatements = sharePreparedStatements;
    }

    public Integer getConnectionErrorRetryAttempts() {
        return connectionErrorRetryAttempts;
    }

    public void setConnectionErrorRetryAttempts(Integer connectionErrorRetryAttempts) {
        this.connectionErrorRetryAttempts = connectionErrorRetryAttempts;
    }

    public Boolean getBreakAfterAcquireFailure() {
        return breakAfterAcquireFailure;
    }

    public void setBreakAfterAcquireFailure(Boolean breakAfterAcquireFailure) {
        this.breakAfterAcquireFailure = breakAfterAcquireFailure;
    }

    public Boolean getRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(Boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public Integer getRemoveAbandonedTimeoutMillis() {
        return removeAbandonedTimeoutMillis;
    }

    public void setRemoveAbandonedTimeoutMillis(Integer removeAbandonedTimeoutMillis) {
        this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
    }

    public Boolean getLogAbandoned() {
        return logAbandoned;
    }

    public void setLogAbandoned(Boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public Integer getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(Integer queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public Integer getTransactionQueryTimeout() {
        return transactionQueryTimeout;
    }

    public void setTransactionQueryTimeout(Integer transactionQueryTimeout) {
        this.transactionQueryTimeout = transactionQueryTimeout;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public DruidWallConfig getWall() {
        return wall;
    }

    public void setWall(DruidWallConfig wall) {
        this.wall = wall;
    }

    public DruidStatConfig getStat() {
        return stat;
    }

    public void setStat(DruidStatConfig stat) {
        this.stat = stat;
    }

    public DruidSlf4jConfig getSlf4j() {
        return slf4j;
    }

    public void setSlf4j(DruidSlf4jConfig slf4j) {
        this.slf4j = slf4j;
    }

    @Override
    public String toString() {
        return "DruidConfig{" +
            "initialSize=" + initialSize +
            ", maxActive=" + maxActive +
            ", minIdle=" + minIdle +
            ", maxWait=" + maxWait +
            ", timeBetweenEvictionRunsMillis=" + timeBetweenEvictionRunsMillis +
            ", timeBetweenLogStatsMillis=" + timeBetweenLogStatsMillis +
            ", statSqlMaxSize=" + statSqlMaxSize +
            ", minEvictableIdleTimeMillis=" + minEvictableIdleTimeMillis +
            ", maxEvictableIdleTimeMillis=" + maxEvictableIdleTimeMillis +
            ", defaultCatalog='" + defaultCatalog + '\'' +
            ", defaultAutoCommit=" + defaultAutoCommit +
            ", defaultReadOnly=" + defaultReadOnly +
            ", defaultTransactionIsolation=" + defaultTransactionIsolation +
            ", testWhileIdle=" + testWhileIdle +
            ", testOnBorrow=" + testOnBorrow +
            ", testOnReturn=" + testOnReturn +
            ", validationQuery='" + validationQuery + '\'' +
            ", validationQueryTimeout=" + validationQueryTimeout +
            ", useGlobalDataSourceStat=" + useGlobalDataSourceStat +
            ", asyncInit=" + asyncInit +
            ", filters='" + filters + '\'' +
            ", clearFiltersEnable=" + clearFiltersEnable +
            ", resetStatEnable=" + resetStatEnable +
            ", notFullTimeoutRetryCount=" + notFullTimeoutRetryCount +
            ", maxWaitThreadCount=" + maxWaitThreadCount +
            ", failFast=" + failFast +
            ", phyTimeoutMillis=" + phyTimeoutMillis +
            ", keepAlive=" + keepAlive +
            ", poolPreparedStatements=" + poolPreparedStatements +
            ", initVariants=" + initVariants +
            ", initGlobalVariants=" + initGlobalVariants +
            ", useUnfairLock=" + useUnfairLock +
            ", killWhenSocketReadTimeout=" + killWhenSocketReadTimeout +
            ", connectionProperties=" + connectionProperties +
            ", maxPoolPreparedStatementPerConnectionSize=" + maxPoolPreparedStatementPerConnectionSize +
            ", initConnectionSqls='" + initConnectionSqls + '\'' +
            ", sharePreparedStatements=" + sharePreparedStatements +
            ", connectionErrorRetryAttempts=" + connectionErrorRetryAttempts +
            ", breakAfterAcquireFailure=" + breakAfterAcquireFailure +
            ", removeAbandoned=" + removeAbandoned +
            ", removeAbandonedTimeoutMillis=" + removeAbandonedTimeoutMillis +
            ", logAbandoned=" + logAbandoned +
            ", queryTimeout=" + queryTimeout +
            ", transactionQueryTimeout=" + transactionQueryTimeout +
            ", publicKey='" + publicKey + '\'' +
            ", wall=" + wall +
            ", stat=" + stat +
            ", slf4j=" + slf4j +
            ", proxyFilters=" + proxyFilters +
            '}';
    }
}
