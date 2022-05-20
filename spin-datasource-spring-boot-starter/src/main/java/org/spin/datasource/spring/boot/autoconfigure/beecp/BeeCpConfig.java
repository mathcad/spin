package org.spin.datasource.spring.boot.autoconfigure.beecp;

import java.util.Properties;

/**
 * BeeCp参数配置
 *
 * @author TaoYu
 * @since 3.3.4
 */
public class BeeCpConfig {

    private String defaultCatalog;
    private String defaultSchema;
    private Boolean defaultReadOnly;
    private Boolean defaultAutoCommit;
    private Integer defaultTransactionIsolationCode;
    private String defaultTransactionIsolationName;

    private Boolean fairMode;
    private Integer initialSize;
    private Integer maxActive;
    private Integer borrowSemaphoreSize;
    private Long maxWait;
    private Long idleTimeout;
    private Long holdTimeout;
    private String connectionTestSql;
    private Integer connectionTestTimeout;
    private Long connectionTestIntegererval;
    private Long idleCheckTimeIntegererval;
    private Boolean forceCloseUsingOnClear;
    private Long delayTimeForNextClear;

    private String connectionFactoryClassName;
    private String xaConnectionFactoryClassName;
    private Properties connectProperties;
    private String poolImplementClassName;
    private Boolean enableJmx;

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public Boolean getDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setDefaultReadOnly(Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public Boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public Integer getDefaultTransactionIsolationCode() {
        return defaultTransactionIsolationCode;
    }

    public void setDefaultTransactionIsolationCode(Integer defaultTransactionIsolationCode) {
        this.defaultTransactionIsolationCode = defaultTransactionIsolationCode;
    }

    public String getDefaultTransactionIsolationName() {
        return defaultTransactionIsolationName;
    }

    public void setDefaultTransactionIsolationName(String defaultTransactionIsolationName) {
        this.defaultTransactionIsolationName = defaultTransactionIsolationName;
    }

    public Boolean getFairMode() {
        return fairMode;
    }

    public void setFairMode(Boolean fairMode) {
        this.fairMode = fairMode;
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

    public Integer getBorrowSemaphoreSize() {
        return borrowSemaphoreSize;
    }

    public void setBorrowSemaphoreSize(Integer borrowSemaphoreSize) {
        this.borrowSemaphoreSize = borrowSemaphoreSize;
    }

    public Long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Long maxWait) {
        this.maxWait = maxWait;
    }

    public Long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Long getHoldTimeout() {
        return holdTimeout;
    }

    public void setHoldTimeout(Long holdTimeout) {
        this.holdTimeout = holdTimeout;
    }

    public String getConnectionTestSql() {
        return connectionTestSql;
    }

    public void setConnectionTestSql(String connectionTestSql) {
        this.connectionTestSql = connectionTestSql;
    }

    public Integer getConnectionTestTimeout() {
        return connectionTestTimeout;
    }

    public void setConnectionTestTimeout(Integer connectionTestTimeout) {
        this.connectionTestTimeout = connectionTestTimeout;
    }

    public Long getConnectionTestIntegererval() {
        return connectionTestIntegererval;
    }

    public void setConnectionTestIntegererval(Long connectionTestIntegererval) {
        this.connectionTestIntegererval = connectionTestIntegererval;
    }

    public Long getIdleCheckTimeIntegererval() {
        return idleCheckTimeIntegererval;
    }

    public void setIdleCheckTimeIntegererval(Long idleCheckTimeIntegererval) {
        this.idleCheckTimeIntegererval = idleCheckTimeIntegererval;
    }

    public Boolean getForceCloseUsingOnClear() {
        return forceCloseUsingOnClear;
    }

    public void setForceCloseUsingOnClear(Boolean forceCloseUsingOnClear) {
        this.forceCloseUsingOnClear = forceCloseUsingOnClear;
    }

    public Long getDelayTimeForNextClear() {
        return delayTimeForNextClear;
    }

    public void setDelayTimeForNextClear(Long delayTimeForNextClear) {
        this.delayTimeForNextClear = delayTimeForNextClear;
    }

    public String getConnectionFactoryClassName() {
        return connectionFactoryClassName;
    }

    public void setConnectionFactoryClassName(String connectionFactoryClassName) {
        this.connectionFactoryClassName = connectionFactoryClassName;
    }

    public String getXaConnectionFactoryClassName() {
        return xaConnectionFactoryClassName;
    }

    public void setXaConnectionFactoryClassName(String xaConnectionFactoryClassName) {
        this.xaConnectionFactoryClassName = xaConnectionFactoryClassName;
    }

    public Properties getConnectProperties() {
        return connectProperties;
    }

    public void setConnectProperties(Properties connectProperties) {
        this.connectProperties = connectProperties;
    }

    public String getPoolImplementClassName() {
        return poolImplementClassName;
    }

    public void setPoolImplementClassName(String poolImplementClassName) {
        this.poolImplementClassName = poolImplementClassName;
    }

    public Boolean getEnableJmx() {
        return enableJmx;
    }

    public void setEnableJmx(Boolean enableJmx) {
        this.enableJmx = enableJmx;
    }
}
