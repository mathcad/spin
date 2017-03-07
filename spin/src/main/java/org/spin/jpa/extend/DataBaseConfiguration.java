package org.spin.jpa.extend;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

/**
 * 数据库配置
 * Created by xuweinan on 2017/3/6.
 *
 * @author xuweinan
 */
public class DataBaseConfiguration {
    private String url;
    private String username;
    private String password;

    private int maxActive = 20;
    private int minIdle = 1;
    private int initialSize = 1;
    private int maxWait = 60000;
    private boolean removeAbandoned = true;
    private long removeAbandonedTimeoutMillis = 60000;
    private int validationQueryTimeout = -1;
    private String validationQuery = null;
    private String clientEncoding = "UTF-8";

    private PhysicalNamingStrategy namingStrategy;
    private String[] packagesToScan;
    private String showSql = "false";
    private String dialect;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
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

    public long getRemoveAbandonedTimeout() {
        return removeAbandonedTimeoutMillis / 1000;
    }

    public void setRemoveAbandonedTimeout(long removeAbandonedTimeout) {
        this.removeAbandonedTimeoutMillis = removeAbandonedTimeout * 1000;
    }

    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public void setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getClientEncoding() {
        return clientEncoding;
    }

    public void setClientEncoding(String clientEncoding) {
        this.clientEncoding = clientEncoding;
    }

    public PhysicalNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    public void setNamingStrategy(PhysicalNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public String[] getPackagesToScan() {
        return packagesToScan;
    }

    public void setPackagesToScan(String... packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public String getShowSql() {
        return showSql;
    }

    public void setShowSql(String showSql) {
        this.showSql = showSql;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}