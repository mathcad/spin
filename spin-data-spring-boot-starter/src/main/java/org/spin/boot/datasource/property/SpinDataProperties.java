package org.spin.boot.datasource.property;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.spin.data.sql.SQLLoader;
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
@ConfigurationProperties(prefix = "spin.data.sql")
public class SpinDataProperties {

    private PhysicalNamingStrategy namingStrategyObj;
    private Class<? extends PhysicalNamingStrategy> namingStrategy;
    private boolean enableJtaTransaction = false;

    private TemplateResolver resolverObj;
    private Class<? extends TemplateResolver> resolver;

    private Class<? extends SQLLoader> sqlLoader;

    private String sqlUri;

    @PostConstruct
    public void init() throws IllegalAccessException, InstantiationException {
        if (null != namingStrategy) {
            namingStrategyObj = namingStrategy.newInstance();
        }

        if (Objects.nonNull(resolver)) {
            resolverObj = resolver.newInstance();
        } else {
            resolverObj = new FreemarkerResolver();
            resolver = FreemarkerResolver.class;
        }

        if (Objects.isNull(sqlLoader)) {
            sqlLoader = FileSystemMdLoader.class;
        }
    }

    public PhysicalNamingStrategy getNamingStrategyObj() {
        return namingStrategyObj;
    }

    public Class<? extends PhysicalNamingStrategy> getNamingStrategy() {
        return namingStrategy;
    }

    public void setNamingStrategy(Class<? extends PhysicalNamingStrategy> namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public boolean isEnableJtaTransaction() {
        return enableJtaTransaction;
    }

    public void setEnableJtaTransaction(boolean enableJtaTransaction) {
        this.enableJtaTransaction = enableJtaTransaction;
    }

    public TemplateResolver getResolverObj() {
        return resolverObj;
    }

    public Class<? extends TemplateResolver> getResolver() {
        return resolver;
    }

    public void setResolver(Class<? extends TemplateResolver> resolver) {
        this.resolver = resolver;
    }

    public Class<? extends SQLLoader> getSqlLoader() {
        return sqlLoader;
    }

    public void setSqlLoader(Class<? extends SQLLoader> sqlLoader) {
        this.sqlLoader = sqlLoader;
    }

    public String getSqlUri() {
        return sqlUri;
    }

    public void setSqlUri(String sqlUri) {
        this.sqlUri = sqlUri;
    }
}
