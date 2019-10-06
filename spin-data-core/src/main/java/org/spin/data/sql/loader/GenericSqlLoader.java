package org.spin.data.sql.loader;

import org.spin.data.core.DatabaseType;
import org.spin.data.core.PageRequest;
import org.spin.data.sql.SQLLoader;
import org.spin.data.sql.SqlSource;
import org.spin.data.sql.resolver.SimpleResolver;
import org.spin.data.sql.resolver.TemplateResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用sql装载器基类
 * <p>实现了通用的装载器方法</p>
 * <p>当装载器未指明模板解析器时，默认使用{@link SimpleResolver}</p>
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 * @see SimpleResolver
 */
public abstract class GenericSqlLoader implements SQLLoader {
    protected volatile boolean useCache = true;
    protected boolean autoCheck = true;
    protected String charset = "UTF-8";
    protected String fileDelimiter = "/";
    protected Map<String, String> sqlSourceMap = new ConcurrentHashMap<>();
    protected Map<String, Long> sqlSourceVersion = new ConcurrentHashMap<>();

    private final Object mutex = new Object();
    private TemplateResolver resolver;
    private DatabaseType dbType;

    /**
     * sql资源根路径
     */
    private String rootUri;

    public GenericSqlLoader() {
        this.rootUri = "/sqlmapper";
    }

    @Override
    public SqlSource getSQL(String id, Map<String, ?> model) {
        if (null == resolver) {
            resolver = new SimpleResolver();
        }
        // 模板解析
        String sql = resolver.resolve(id, getSqlTemplateSrc(id), model, dbType);
        return new SqlSource(id, sql);
    }

    @Override
    public SqlSource getPagedSQL(String id, Map<String, ?> model, PageRequest pageRequest) {
        return dbType.getPagedSQL(getSQL(id, model), pageRequest);
    }

    @Override
    public DatabaseType getDbType() {
        return dbType;
    }

    @Override
    public void setDbType(DatabaseType dbType) {
        this.dbType = dbType;
    }

    @Override
    public TemplateResolver getTemplateResolver() {
        return this.resolver;
    }

    @Override
    public void setTemplateResolver(TemplateResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String getRootUri() {
        return this.rootUri;
    }

    @Override
    public void setRootUri(String rootUri) {
        this.rootUri = rootUri;
    }

    @Override
    public boolean isAutoCheck() {
        return this.autoCheck;
    }

    @Override
    public void setAutoCheck(boolean check) {
        this.autoCheck = check;
    }

    @Override
    public String getCharset() {
        return this.charset;
    }

    @Override
    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public SQLLoader enableCache() {
        if (!this.useCache)
            synchronized (this.mutex) {
                this.useCache = true;
            }
        return this;
    }

    @Override
    public SQLLoader disableCache() {
        if (this.useCache)
            synchronized (this.mutex) {
                this.useCache = false;
            }
        return this;
    }
}
