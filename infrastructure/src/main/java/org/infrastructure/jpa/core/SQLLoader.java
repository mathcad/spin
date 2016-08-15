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

package org.infrastructure.jpa.core;

import org.infrastructure.jpa.sql.SQLSource;
import org.infrastructure.jpa.sql.resolver.TemplateResolver;

import java.util.Map;

/**
 * 拥有缓存机制的SQL装载器
 * Created by xuweinan on 2016/8/13.
 *
 * @author xuweinan
 * @version 1.0
 */

public interface SQLLoader {

    /**
     * 加载参数化的SQL语句
     *
     * @param id    sql的path
     * @param model 参数
     * @return 参数化的sql
     */
    SQLSource getSQL(String id, Map<String, ?> model);

    /**
     * 加载SQL模板
     */
    String getSqlTemplateSrc(String id);

    /**
     * 判断一个SQL是否修改过
     */
    boolean isModified(String id);

    /**
     * 获取SQL加载器的模板解析器
     */
    TemplateResolver getTemplateResolver();

    /**
     * 设置SQL加载器的模板解析器
     */
    void setTemplateResolver(TemplateResolver resolver);

    /**
     * SQL模板的根路径
     */
    String getRootUri();

    /**
     * 设置SQL模板的根路径
     */
    void setRootUri(String rootUri);

    /**
     * 是否每次都检测sql变化
     */
    boolean isAutoCheck();

    /**
     * 设置是否检测SQL变化，开发模式下检查，生产环境不应检查
     */
    void setAutoCheck(boolean check);

    /**
     * 模板编码字符集
     */
    String getCharset();

    /**
     * 设置模板编码字符集
     */
    void setCharset(String charset);

    /**
     * 开启缓存机制
     */
    SQLLoader enableCache();

    /**
     * 关闭缓存机制
     */
    SQLLoader disableCache();
}