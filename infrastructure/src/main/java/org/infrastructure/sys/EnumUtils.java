package org.infrastructure.sys;

import org.infrastructure.jpa.core.annotations.UserEnum;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.HashUtils;
import org.infrastructure.util.ObjectUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用的枚举类型转换
 *
 * @author xuweinan
 * @version V1.0
 */
public class EnumUtils {
    static Logger logger = LoggerFactory.getLogger(EnumUtils.class);

    /**
     * 通过枚举文本字段，获得枚举类型的常量
     *
     * @param enumCls 枚举类型
     * @param text    枚举文本
     * @return 枚举常量
     */
    public static <T extends Enum<?>> T getEnum(Class<T> enumCls, String text) {
        for (T o : enumCls.getEnumConstants()) {
            if (o.name().equals(text)) {
                return o;
            }
        }
        return null;
    }

    /**
     * 通过字段值，获得枚举类型的常量。默认为value字段
     *
     * @param enumCls 枚举类型
     * @param value   字段的值
     * @param field   字段名
     * @return 枚举常量
     */
    public static <T extends Enum<?>> T getEnum(Class<T> enumCls, int value, String... field) {
        String fieldName;
        if (null != field && field.length > 0 && StringUtils.isNotEmpty(field[0]))
            fieldName = field[0];
        else
            fieldName = "value";
        Field valueField = null;
        try {
            valueField = enumCls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new BizException("Enum:" + enumCls.getName() + " has no such field:" + fieldName, e);
        }
        ReflectionUtils.makeAccessible(valueField);
        for (T o : enumCls.getEnumConstants()) {
            int fVal = (Integer) ReflectionUtils.getField(valueField, o);
            if (value == fVal) {
                return o;
            }
        }
        return null;
    }

    /**
     * 从Map中获取int类型的Value再转换为实体Enum
     *
     * @param enumCls 枚举类型
     * @param map     包含int值的map
     * @param key     键
     * @return 枚举常量
     */
    public static <T extends Enum<?>> T getByValue(Class<T> enumCls, Map<?, Object> map, String key) {
        Integer v = HashUtils.getIntegerValue(map, key);
        if (null == v)
            return null;
        return getEnum(enumCls, v);
    }

    /**
     * 获得Enum的Value值
     *
     * @param enumClass enum类型
     * @param enumValue 枚举常量
     * @return 枚举常量的value字段值
     */
    public static int getEnumValue(Class enumClass, Enum enumValue) {
        if (enumClass == null)
            enumClass = enumValue.getClass();

        Field vField = null;
        try {
            vField = enumClass.getDeclaredField("value");
            ReflectionUtils.makeAccessible(vField);

        } catch (Exception e) {
            throw new RuntimeException("Enum" + enumClass + "未声明value字段", e);
        }

        int value;
        try {
            value = Integer.valueOf(vField.get(enumValue).toString());
        } catch (Exception e) {
            throw new RuntimeException("Enum" + enumClass + "获取value失败", e);
        }

        return value;

    }

    /**
     * 解析所有枚举
     *
     * @param basePkg 包名
     * @return 包含所有枚举常量name-value的map
     * @throws Exception
     */
    public static HashMap<String, List<HashMap>> parseEnums(String basePkg) throws Exception {
        List<String> clsList = PackageUtils.getClassName(basePkg);
        HashMap<String, List<HashMap>> enumsMap = new HashMap<>();
        for (String clz : clsList) {
            Class cls = Class.forName(clz);
            if (cls.isEnum()) {
                List<HashMap> valueList = new ArrayList<>();
                //value字段
                Field vField = null;
                try {
                    vField = cls.getDeclaredField("value");
                    ReflectionUtils.makeAccessible(vField);
                } catch (Exception e) {
                    logger.error("Enum" + cls + "未声明value字段", e);
                }

                //取value值
                if (cls.getAnnotation(UserEnum.class) != null && vField != null) {
                    for (Object o : cls.getEnumConstants()) {
                        String name = o.toString();
                        int value = Integer.valueOf(vField.get(o).toString());
                        HashMap<String, String> m = new HashMap<>();
                        m.put("name", name);
                        m.put("value", ObjectUtils.toString(value));
                        valueList.add(m);
                    }
                    enumsMap.put(cls.getSimpleName(), valueList);
                }
            }
        }

        return enumsMap;
    }

}
