package org.spin.boot.properties;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.spin.data.sql.loader.FileSystemMdLoader;
import org.spin.data.sql.resolver.FreemarkerResolver;
import org.spin.data.sql.resolver.TemplateResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

import javax.annotation.PostConstruct;

/**
 * 数据库配置
 * <p>Created by xuweinan on 2017/3/6.</p>
 *
 * @author xuweinan
 */
@ConfigurationProperties(prefix = "spin.data")
public class SpinDataProperties {

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
