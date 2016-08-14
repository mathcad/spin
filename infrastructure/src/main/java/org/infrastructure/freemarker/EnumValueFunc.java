package org.infrastructure.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.infrastructure.jpa.core.annotations.UserEnum;
import org.infrastructure.throwable.BizException;
import org.springframework.util.ReflectionUtils;

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
        try {
            Class<?> cls = Class.forName(enumName);
            if (cls.isEnum()) {
                Field vField = null;
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
                        int value = Integer.valueOf(vField.get(o).toString());
                        sb.append(" WHEN ").append(field).append("=").append(value).append(" THEN '").append(name)
                                .append("'");
                    }
                }
            }
        } catch (Exception e) {
            throw new BizException("解析枚举出错" + enumName);
        }
        sb.append("END) AS ").append(asField);
        return sb.toString();
    }
}
