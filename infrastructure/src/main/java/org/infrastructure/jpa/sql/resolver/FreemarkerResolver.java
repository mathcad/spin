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

package org.infrastructure.jpa.sql.resolver;

import org.infrastructure.freemarker.ConcurrentStrTemplateLoader;
import org.infrastructure.freemarker.EnumValueFunc;
import org.infrastructure.freemarker.ValidValueFunc;
import org.infrastructure.throwable.SimplifiedException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * 基于freemarker的解析器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 */
public class FreemarkerResolver implements TemplateResolver {
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerResolver.class);
    private String charset = "UTF-8";
    private Configuration configuration;
    private ConcurrentStrTemplateLoader strTemplateLoader;

    public FreemarkerResolver() {
        this.configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        strTemplateLoader = new ConcurrentStrTemplateLoader();
        configuration.setEncoding(Locale.CHINESE, this.charset);
        configuration.setNumberFormat("#");
        configuration.setSharedVariable("V", new ValidValueFunc());
        configuration.setSharedVariable("E", new EnumValueFunc());
        configuration.setTemplateLoader(strTemplateLoader);
    }

    @Override
    public String resolve(String id, String templateSrc, Map<String, ?> model) {
        this.strTemplateLoader.putTemplate(id, templateSrc);
        Template template;
        try {
            template = this.configuration.getTemplate(id);
        } catch (IOException e) {
            throw new SimplifiedException("Freemarker模板处理失败", e);
        }
        StringWriter writer = new StringWriter();
        try {
            template.process(model, writer);
        } catch (TemplateException | IOException e) {
            throw new SimplifiedException("填充SQL语句模板异常", e);
        }
        String sql = writer.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("填充SQL语句模板：" + id);
            logger.trace(sql);
        }
        return sql;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
