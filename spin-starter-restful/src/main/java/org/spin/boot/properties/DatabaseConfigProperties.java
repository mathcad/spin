package org.spin.boot.properties;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.spin.data.sql.loader.FileSystemMdLoader;
import org.spin.data.sql.resolver.FreemarkerResolver;
import org.spin.data.sql.resolver.TemplateResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * 数据库配置
 * <p>Created by xuweinan on 2017/3/6.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.data")
public class DatabaseConfigProperties {
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

    private PhysicalNamingStrategy namingStrategyObj;
    private String namingStrategy;

    private TemplateResolver resolverObj;
    private String resolver;

    private String sqlLoader;

    private String sqlUri;

    @PostConstruct
    public void init() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (null != namingStrategy) {
            namingStrategyObj = (PhysicalNamingStrategy) Class.forName(namingStrategy).newInstance();
        }

        if (Objects.nonNull(resolver)) {
            resolverObj = (TemplateResolver) Class.forName(resolver).newInstance();
        } else {
            resolverObj = new FreemarkerResolver();
            resolver = FreemarkerResolver.class.getName();
        }

        if (Objects.isNull(sqlLoader)) {
            sqlLoader = FileSystemMdLoader.class.getName();
        }
    }

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

    public PhysicalNamingStrategy getNamingStrategyObj() {
        return namingStrategyObj;
    }

    public String getNamingStrategy() {
        return namingStrategy;
    }

    public void setNamingStrategy(String namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public TemplateResolver getResolverObj() {
        return resolverObj;
    }

    public String getResolver() {
        return resolver;
    }

    public void setResolver(String resolver) {
        this.resolver = resolver;
    }

    public String getSqlLoader() {
        return sqlLoader;
    }

    public void setSqlLoader(String sqlLoader) {
        this.sqlLoader = sqlLoader;
    }

    public String getSqlUri() {
        return sqlUri;
    }

    public void setSqlUri(String sqlUri) {
        this.sqlUri = sqlUri;
    }
}
