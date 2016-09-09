/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.core.SQLLoader;
import org.infrastructure.jpa.sql.resolver.SimpleResolver;
import org.infrastructure.jpa.sql.resolver.TemplateResolver;

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
    protected Map<String, String> sqlSourceMap = new ConcurrentHashMap<>();
    protected Map<String, Long> sqlSourceVersion = new ConcurrentHashMap<>();

    private final Object mutex = new Object();
    private TemplateResolver resolver;

    /**
     * sql资源根路径
     */
    private String rootUri;

    public GenericSqlLoader() {
        this.rootUri = "/sqlmap";
    }

    @Override
    public SQLSource getSQL(String id, Map<String, ?> model) {
        if (null == this.resolver)
            this.resolver = new SimpleResolver();
        String sql = this.resolver.resolve(id, this.getSqlTemplateSrc(id), model);
        return new SQLSource(id, sql);
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