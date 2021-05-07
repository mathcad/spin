package org.spin.data.sql.resolver;

import java.util.Map;

/**
 * 模板解析器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 */
public interface TemplateResolver {

    /**
     * 使用指定的数据解析模板，生成解析后的文本
     *
     * @param id       模板id
     * @param template 模板对象
     * @param model    数据
     * @return 解析后的文本
     */
    String resolve(String id, String template, Map<String, ?> model);
}
