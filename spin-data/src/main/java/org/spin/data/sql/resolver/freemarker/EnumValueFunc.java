package org.spin.data.sql.resolver.freemarker;

import freemarker.template.TemplateMethodModelEx;
import org.spin.core.throwable.SimplifiedException;
import org.spin.data.core.UserEnumColumn;

import java.util.List;

/**
 * Enum值转名称 生成sql段(CASE ... WHEN ... THEN ... END) AS ...
 *
 * @author xuweinan
 * @version V1.0
 */
public class EnumValueFunc implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) {
        if (arguments.size() != 3)
            throw new IllegalArgumentException("E函数参数个数不正确（需3个）");
        String enumName = arguments.get(0).toString();
        String field = arguments.get(1).toString();
        String asField = arguments.get(2).toString();
        StringBuilder sb = new StringBuilder();
        sb.append("(CASE");
        Class<?> cls;
        try {
            cls = Class.forName(enumName);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("解析枚举出错" + enumName, e);
        }
        if (cls.isEnum() && UserEnumColumn.class.isAssignableFrom(cls)) {
            for (Object o : cls.getEnumConstants()) {
                String name = o.toString();
                int value = ((UserEnumColumn) o).getValue();
                sb.append(" WHEN ").append(field).append("=").append(value).append(" THEN '").append(name).append("'");
            }
        } else {
            throw new SimplifiedException(enumName + "不是有效的枚举类型(请检查是否实现了UserEnumColumn接口)");
        }
        sb.append("END) AS ").append(asField);
        return sb.toString();
    }
}
