package org.spin.boot.datasource.property;


import org.spin.data.extend.DataSourceConfig;

import java.util.Properties;

/**
 * <p>Created by xuweinan on 2017/10/8.</p>
 *
 * @author xuweinan
 */
public class DruidDataSourceProperties implements DataSourceConfig {

    private String name;
    private String url;
    private String username;
    private String password;
    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private String validationQuery;
    private boolean useGlobalDataSourceStat = false;
    private boolean asyncInit = false;
    private String filters;
    private long timeBetweenLogStatsMillis;
    private int statSqlMaxSize;
    private boolean clearFiltersEnable = true;
    private boolean resetStatEnable = true;
    private int notFullTimeoutRetryCount = 0;
    private long timeBetweenEvictionRunsMillis = 60 * 1000L;
    private int maxWaitThreadCount = -1;
    private boolean failFast = false;
    private long phyTimeoutMillis = -1L;
    private long phyMaxUseCount = -1L;
    private long minEvictableIdleTimeMillis = 1000L * 60L * 30L;
    private long maxEvictableIdleTimeMillis = 1000L * 60L * 60L * 7;
    private boolean keepAlive = false;
    private boolean poolPreparedStatements = false;
    private boolean initVariants = false;
    private boolean initGlobalVariants = false;
    private boolean useUnfairLock;
    private String driverClassName;
    private int initialSize = 0;
    private int minIdle = 0;
    private int maxActive = 8;
    private boolean killWhenSocketReadTimeout = false;
    private String connectionProperties;
    private int maxPoolPreparedStatementPerConnectionSize = 10;
    private String initConnectionSqls;

    private long maxWait = -1L;
    private boolean removeAbandoned = false;
    private long removeAbandonedTimeoutMillis = 300L * 1000L;
    private String servletPath = "/druid/*";
    private boolean openSessionInView = false;
    private FilterProperties filter = new FilterProperties();
//    private Properties connectionProperties = new Properties() {
//        private static final long serialVersionUID = -8638010368833820798L;
//
//        {
//            put("druid.stat.mergeSql", "true");
//            put("druid.stat.slowSqlMillis", "3000");
//        }
//    };

    @Override
    public Properties toProperties(String prefix) {
        Properties properties = new Properties();

        notNullAdd(properties, prefix, "name", name);
        notNullAdd(properties, prefix, "url", url);
        notNullAdd(properties, prefix, "username", username);
        notNullAdd(properties, prefix, "password", password);
        notNullAdd(properties, prefix, "testWhileIdle", testWhileIdle);
        notNullAdd(properties, prefix, "testOnBorrow", testOnBorrow);
        notNullAdd(properties, prefix, "validationQuery", validationQuery);
        notNullAdd(properties, prefix, "useGlobalDataSourceStat", useGlobalDataSourceStat);
        notNullAdd(properties, prefix, "asyncInit", asyncInit);
        notNullAdd(properties, prefix, "filters", filters);
        notNullAdd(properties, prefix, "timeBetweenLogStatsMillis", timeBetweenLogStatsMillis);
        notNullAdd(properties, prefix, "stat.sql.MaxSize", statSqlMaxSize);
        notNullAdd(properties, prefix, "clearFiltersEnable", clearFiltersEnable);
        notNullAdd(properties, prefix, "resetStatEnable", resetStatEnable);
        notNullAdd(properties, prefix, "notFullTimeoutRetryCount", notFullTimeoutRetryCount);
        notNullAdd(properties, prefix, "timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
        notNullAdd(properties, prefix, "maxWaitThreadCount", maxWaitThreadCount);
        notNullAdd(properties, prefix, "failFast", failFast);
        notNullAdd(properties, prefix, "phyTimeoutMillis", phyTimeoutMillis);
        notNullAdd(properties, prefix, "phyMaxUseCount", phyMaxUseCount);
        notNullAdd(properties, prefix, "minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
        notNullAdd(properties, prefix, "maxEvictableIdleTimeMillis", maxEvictableIdleTimeMillis);
        notNullAdd(properties, prefix, "keepAlive", keepAlive);
        notNullAdd(properties, prefix, "poolPreparedStatements", poolPreparedStatements);
        notNullAdd(properties, prefix, "initVariants", initVariants);
        notNullAdd(properties, prefix, "initGlobalVariants", initGlobalVariants);
        notNullAdd(properties, prefix, "useUnfairLock", useUnfairLock);
        notNullAdd(properties, prefix, "driverClassName", driverClassName);
        notNullAdd(properties, prefix, "initialSize", initialSize);
        notNullAdd(properties, prefix, "minIdle", minIdle);
        notNullAdd(properties, prefix, "maxActive", maxActive);
        notNullAdd(properties, prefix, "killWhenSocketReadTimeout", killWhenSocketReadTimeout);
        notNullAdd(properties, prefix, "connectProperties", connectionProperties);
        notNullAdd(properties, prefix, "maxPoolPreparedStatementPerConnectionSize", maxPoolPreparedStatementPerConnectionSize);
        notNullAdd(properties, prefix, "initConnectionSqls", initConnectionSqls);

        notNullAdd(properties, prefix, "maxWait", maxWait);
        notNullAdd(properties, prefix, "removeAbandoned", removeAbandoned);
        notNullAdd(properties, prefix, "removeAbandonedTimeoutMillis", removeAbandonedTimeoutMillis);
        notNullAdd(properties, prefix, "servletPath", servletPath);
        notNullAdd(properties, prefix, "openSessionInView", openSessionInView);
        return properties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getMaxPoolSize() {
        return maxActive;
    }

    @Override
    public int getMinPoolSize() {
        return minIdle;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public boolean isUseGlobalDataSourceStat() {
        return useGlobalDataSourceStat;
    }

    public void setUseGlobalDataSourceStat(boolean useGlobalDataSourceStat) {
        this.useGlobalDataSourceStat = useGlobalDataSourceStat;
    }

    public boolean isAsyncInit() {
        return asyncInit;
    }

    public void setAsyncInit(boolean asyncInit) {
        this.asyncInit = asyncInit;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public long getTimeBetweenLogStatsMillis() {
        return timeBetweenLogStatsMillis;
    }

    public void setTimeBetweenLogStatsMillis(long timeBetweenLogStatsMillis) {
        this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
    }

    public int getStatSqlMaxSize() {
        return statSqlMaxSize;
    }

    public void setStatSqlMaxSize(int statSqlMaxSize) {
        this.statSqlMaxSize = statSqlMaxSize;
    }

    public boolean isClearFiltersEnable() {
        return clearFiltersEnable;
    }

    public void setClearFiltersEnable(boolean clearFiltersEnable) {
        this.clearFiltersEnable = clearFiltersEnable;
    }

    public boolean isResetStatEnable() {
        return resetStatEnable;
    }

    public void setResetStatEnable(boolean resetStatEnable) {
        this.resetStatEnable = resetStatEnable;
    }

    public int getNotFullTimeoutRetryCount() {
        return notFullTimeoutRetryCount;
    }

    public void setNotFullTimeoutRetryCount(int notFullTimeoutRetryCount) {
        this.notFullTimeoutRetryCount = notFullTimeoutRetryCount;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getMaxWaitThreadCount() {
        return maxWaitThreadCount;
    }

    public void setMaxWaitThreadCount(int maxWaitThreadCount) {
        this.maxWaitThreadCount = maxWaitThreadCount;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public long getPhyTimeoutMillis() {
        return phyTimeoutMillis;
    }

    public void setPhyTimeoutMillis(long phyTimeoutMillis) {
        this.phyTimeoutMillis = phyTimeoutMillis;
    }

    public long getPhyMaxUseCount() {
        return phyMaxUseCount;
    }

    public void setPhyMaxUseCount(long phyMaxUseCount) {
        this.phyMaxUseCount = phyMaxUseCount;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getMaxEvictableIdleTimeMillis() {
        return maxEvictableIdleTimeMillis;
    }

    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
        this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public boolean isInitVariants() {
        return initVariants;
    }

    public void setInitVariants(boolean initVariants) {
        this.initVariants = initVariants;
    }

    public boolean isInitGlobalVariants() {
        return initGlobalVariants;
    }

    public void setInitGlobalVariants(boolean initGlobalVariants) {
        this.initGlobalVariants = initGlobalVariants;
    }

    public boolean isUseUnfairLock() {
        return useUnfairLock;
    }

    public void setUseUnfairLock(boolean useUnfairLock) {
        this.useUnfairLock = useUnfairLock;
    }

    @Override
    public String getDriverClassName() {
        return driverClassName;
    }

    @Override
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public boolean isKillWhenSocketReadTimeout() {
        return killWhenSocketReadTimeout;
    }

    public void setKillWhenSocketReadTimeout(boolean killWhenSocketReadTimeout) {
        this.killWhenSocketReadTimeout = killWhenSocketReadTimeout;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public int getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public String getInitConnectionSqls() {
        return initConnectionSqls;
    }

    public void setInitConnectionSqls(String initConnectionSqls) {
        this.initConnectionSqls = initConnectionSqls;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public long getRemoveAbandonedTimeoutMillis() {
        return removeAbandonedTimeoutMillis;
    }

    public void setRemoveAbandonedTimeoutMillis(long removeAbandonedTimeoutMillis) {
        this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public boolean isOpenSessionInView() {
        return openSessionInView;
    }

    public void setOpenSessionInView(boolean openSessionInView) {
        this.openSessionInView = openSessionInView;
    }

    @Override
    public String getXaDataSourceClassName() {
        return "com.alibaba.druid.pool.xa.DruidXADataSource";
    }

    @Override
    public String getDataSourceClassName() {
        return "com.alibaba.druid.pool.DruidDataSource";
    }

    public FilterProperties getFilter() {
        return filter;
    }

    public void setFilter(FilterProperties filter) {
        this.filter = filter;
    }

    public static class FilterProperties {
        private boolean stat = true;
        private boolean config;
        private boolean encoding;
        private boolean slf4j;
        private boolean log4j;
        private boolean log4j2;
        private boolean commonsLog;
        private boolean wall;

        public boolean isStat() {
            return stat;
        }

        public void setStat(boolean stat) {
            this.stat = stat;
        }

        public boolean isConfig() {
            return config;
        }

        public void setConfig(boolean config) {
            this.config = config;
        }

        public boolean isEncoding() {
            return encoding;
        }

        public void setEncoding(boolean encoding) {
            this.encoding = encoding;
        }

        public boolean isSlf4j() {
            return slf4j;
        }

        public void setSlf4j(boolean slf4j) {
            this.slf4j = slf4j;
        }

        public boolean isLog4j() {
            return log4j;
        }

        public void setLog4j(boolean log4j) {
            this.log4j = log4j;
        }

        public boolean isLog4j2() {
            return log4j2;
        }

        public void setLog4j2(boolean log4j2) {
            this.log4j2 = log4j2;
        }

        public boolean isCommonsLog() {
            return commonsLog;
        }

        public void setCommonsLog(boolean commonsLog) {
            this.commonsLog = commonsLog;
        }

        public boolean isWall() {
            return wall;
        }

        public void setWall(boolean wall) {
            this.wall = wall;
        }
    }
}
