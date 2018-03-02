package org.spin.data.sql.resolver;

import org.spin.enhance.freemarker.ConcurrentStrTemplateLoader;
import org.spin.enhance.freemarker.EnumValueFunc;
import org.spin.enhance.freemarker.ValidValueFunc;
import org.spin.core.throwable.SimplifiedException;
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
        configuration.setEncoding(Locale.CHINESE, charset);
    }
}
