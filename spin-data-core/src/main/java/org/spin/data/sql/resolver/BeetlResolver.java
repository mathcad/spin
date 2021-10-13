package org.spin.data.sql.resolver;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.trait.FriendlyEnum;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * 基于Beetl模板引擎的解析器
 * Created by xuweinan on 2018/3/2.
 *
 * @author xuweinan
 * @version 1.0
 */
public class BeetlResolver implements TemplateResolver {
    private static final Logger logger = LoggerFactory.getLogger(BeetlResolver.class);
    private String charset = "UTF-8";
    private final Configuration configuration;
    private final GroupTemplate groupTemplate;

    public BeetlResolver() {
        StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
        try {
            configuration = Configuration.defaultConfiguration();
        } catch (IOException e) {
            throw new SimplifiedException("Beetl模板引擎配置失败", e);
        }
        configuration.setErrorHandlerClass("org.beetl.core.ReThrowConsoleErrorHandler");
        configuration.setCharset(charset);
        configuration.setStatementStart("```js");
        configuration.setStatementEnd("```");
        configuration.setStatementStart2("@");
        groupTemplate = new GroupTemplate(resourceLoader, configuration);

        groupTemplate.registerFunction("valid", (params, context) -> {
            if (params.length != 2 && params.length != 3) {
                throw new IllegalArgumentException("valid函数参数个数不正确（需2个或3个）");
            }
            Object cond = params[0];
            String yes = params[1].toString();
            String no = params.length == 3 ? params[2].toString() : "";
            if (null == cond) {
                return no;
            }
            if (cond instanceof CharSequence) {
                return StringUtils.isEmpty(cond.toString()) ? no : yes;
            }
            if (cond instanceof Boolean) {
                return ((Boolean) cond) ? yes : no;
            }
            if (cond.getClass().isArray()) {
                return Array.getLength(cond) > 0 ? yes : no;
            }
            if (cond instanceof Iterable) {
                return ((Iterable<?>) cond).iterator().hasNext() ? yes : no;
            }

            return yes;
        });
        groupTemplate.registerFunction("enum", (params, context) -> {
            if (params.length != 3) {
                throw new IllegalArgumentException("enum函数参数个数不正确（需3个）");
            }
            String enumName = params[0].toString();
            String field = params[1].toString();
            String asField = params[2].toString();
            StringBuilder sb = new StringBuilder();
            sb.append("(CASE");
            Class<?> cls;
            try {
                cls = Class.forName(enumName);
            } catch (ClassNotFoundException e) {
                throw new SimplifiedException("解析枚举出错" + enumName, e);
            }
            if (cls.isEnum() && FriendlyEnum.class.isAssignableFrom(cls)) {
                for (Object o : cls.getEnumConstants()) {
                    String name = ((FriendlyEnum<?>) o).getDescription();
                    Object value = ((FriendlyEnum<?>) o).getValue();
                    if (ClassUtils.wrapperToPrimitive(value.getClass()) == null || value instanceof Character) {
                        value = "'" + value + "'";
                    }
                    sb.append(" WHEN ").append(field).append(" = ").append(value).append(" THEN '").append(name).append("'");
                }
            } else {
                throw new SimplifiedException(enumName + "不是有效的枚举类型(请检查是否实现了UserEnumColumn接口)");
            }
            sb.append(" END) AS ").append(asField);
            return sb.toString();
        });

        groupTemplate.registerFunction("sqlIn", (params, context) -> {
            if (params.length != 3) {
                throw new IllegalArgumentException("in函数参数个数不正确（需3个参数）");
            }
            String paramName = StringUtils.toStringEmpty(params[1]);
            if (paramName.length() == 0) {
                throw new IllegalArgumentException("in函数需要指定参数名称");
            }
            Object param = params[2];
            int size;
            if (param instanceof Collection) {
                size = ((Collection<?>) param).size();
            } else if (param.getClass().isArray()) {
                size = ((Object[]) param).length;
            } else {
                throw new IllegalArgumentException("in函数的参数必须是集合类型");
            }
            if (0 == size) {
                return "";
            }
            StringBuilder sb = new StringBuilder(StringUtils.toStringEmpty(params[0])).append(" IN (");
            for (int i = 0; i < size; ++i) {
                sb.append(":").append(paramName).append("[").append(i).append("], ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(")");
            return sb.toString();
        });
    }

    @Override
    public String resolve(String id, String templateSrc, Map<String, ?> model) {
        Template template = groupTemplate.getTemplate(templateSrc);

        for (Map.Entry<String, ?> e : model.entrySet()) {
            template.binding(e.getKey(), e.getValue());
        }

        String sql;
        try {
            sql = template.render();
        } catch (BeetlException e) {
            throw new SimplifiedException("填充SQL语句模板异常", e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("填充SQL语句模板：" + id);
            logger.trace(sql);
        }
        return sql;
    }

    public void setCharset(String charset) {
        this.charset = charset;
        configuration.setCharset(charset);
    }
}
