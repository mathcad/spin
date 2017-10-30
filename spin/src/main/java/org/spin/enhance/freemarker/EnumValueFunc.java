package org.spin.enhance.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.spin.core.annotation.UserEnum;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Enum值转名称 生成sql段(CASE ... WHEN ... THEN ... END) AS ...
 *
 * @author xuweinan
 * @version V1.0
 */
public class EnumValueFunc implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 3)
            throw new IllegalArgumentException("ValidValue 参数个数不正确（需3个）");
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
        if (cls.isEnum()) {
            Field vField;
            try {
                vField = cls.getDeclaredField("value");
                ReflectionUtils.makeAccessible(vField);
            } catch (Exception e) {
                throw new IllegalArgumentException("Enum" + cls + "未声明value字段", e);
            }

            // 初始化转化
            if (cls.getAnnotation(UserEnum.class) != null) {
                for (Object o : cls.getEnumConstants()) {
                    String name = o.toString();
                    int value;
                    try {
                        value = Integer.parseInt(vField.get(o).toString());
                    } catch (IllegalAccessException e) {
                        throw new SimplifiedException(e);
                    }
                    sb.append(" WHEN ").append(field).append("=").append(value).append(" THEN '").append(name).append("'");
                }
            }
        }
        sb.append("END) AS ").append(asField);
        return sb.toString();
    }
}
