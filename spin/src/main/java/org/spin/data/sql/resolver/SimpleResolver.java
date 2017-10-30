package org.spin.data.sql.resolver;

import org.spin.core.util.StringUtils;

import java.util.Map;

/**
 * 简易模板解析器，实现了最简单的变量绑定
 * <p>模板变量定义格式为${paramName}<br>所有未绑定数据的模板变量将会被移除</p>
 * Created by xuweinan on 2016/8/15.
 *
 * @author xuweinan
 * @version 1.0
 */
public class SimpleResolver implements TemplateResolver {
    @Override
    public String resolve(String id, String template, Map<String, ?> model) {
        if (StringUtils.isEmpty(template))
            return StringUtils.EMPTY;
        String result = template;
        if (null != model)
            for (Map.Entry<String, ?> param : model.entrySet()) {
                result = template.replace("${" + param.getKey() + "}", param.getValue().toString());
            }
        return result.replaceAll("\\$\\{.+}", "");
    }
}
