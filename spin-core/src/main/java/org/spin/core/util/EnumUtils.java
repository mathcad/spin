package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SpinException;
import org.spin.core.trait.Evaluatable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 枚举工具类
 *
 * @author xuweinan
 * @version V1.0
 */
public final class EnumUtils extends Util {
    private static Logger logger = LoggerFactory.getLogger(EnumUtils.class);
    private static final String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
    private static final String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
    private static final String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";
    private static final String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";


    private EnumUtils() {
    }

    /**
     * 将枚举转换为Map
     *
     * @param <E>       枚举类
     * @param enumClass 待转换的枚举
     * @return 枚举Map
     */
    public static <E extends Enum<E>> Map<String, E> getEnumMap(final Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).collect(Collectors.toMap(Enum::name, e -> e));
    }

    /**
     * 获取枚举中的枚举值列表
     *
     * @param <E>       枚举类
     * @param enumClass 待转换的枚举
     * @return 枚举常量列表
     */
    public static <E extends Enum<E>> List<E> getEnumList(final Class<E> enumClass) {
        return new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()));
    }

    /**
     * 判断给定的枚举常量名称是否存在于枚举
     *
     * @param <E>       枚举类
     * @param enumClass 枚举
     * @param enumName  枚举常量名称
     * @return 枚举
     */
    public static <E extends Enum<E>> boolean isValidEnum(final Class<E> enumClass, final String enumName) {
        if (enumName == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, enumName);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * 通过枚举名称，获得枚举类型的常量
     *
     * @param enumCls  枚举类型
     * @param enumName 枚举名称
     * @param <E>      枚举类型参数
     * @return 枚举常量
     */
    public static <E extends Enum<E>> E fromName(Class<E> enumCls, String enumName) {
        try {
            return Enum.valueOf(enumCls, enumName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过序号，获取枚举类型常量
     *
     * @param enumCls 枚举类型
     * @param ordinal 序号
     * @param <E>     枚举类型参数
     * @return 枚举常量
     */
    public static <E extends Enum<E>> E fromOrdinal(Class<E> enumCls, int ordinal) {
        E[] enumConstants = enumCls.getEnumConstants();
        if (ordinal < 0 || ordinal >= enumConstants.length) {
            throw new SpinException(
                String.format(
                    "Unknown ordinal value [%s] for enum class [%s]",
                    ordinal,
                    enumCls.getName()
                )
            );
        }
        return enumConstants[ordinal];
    }

    /**
     * 通过字段值，获得枚举类型的常量。默认为value字段
     *
     * @param enumCls 枚举类型
     * @param value   字段的值
     * @param <E>     枚举类型参数
     * @param <T>     value的类型参数
     * @return 枚举常量
     */
    public static <E extends Enum<E>, T> E getEnum(Class<E> enumCls, T value) {
        if (Evaluatable.class.isAssignableFrom(enumCls)) {
            for (E enumConstant : enumCls.getEnumConstants()) {
                Object enumVal = ((Evaluatable) enumConstant).getValue();
                if (ObjectUtils.nullSafeEquals(value, enumVal)) {
                    return enumConstant;
                } else if (null != value && enumVal.equals(ObjectUtils.convert(enumVal.getClass(), value))) {
                    return enumConstant;
                }
            }
        }
        return getEnum(enumCls, value, "value");
    }


    /**
     * 通过字段值，获得枚举类型的常量。默认为value字段
     *
     * @param enumCls 枚举类型
     * @param value   字段的值
     * @param field   字段名
     * @param <E>     枚举类型参数
     * @param <T>     value的类型参数
     * @return 枚举常量
     */
    public static <E extends Enum<E>, T> E getEnum(Class<E> enumCls, T value, String field) {
        String fieldName;
        if (StringUtils.isNotEmpty(field)) {
            fieldName = field;
        } else {
            fieldName = "value";
        }
        Field valueField = ReflectionUtils.findField(enumCls, fieldName);
        Method getMethod = null;

        if (null == valueField) {
            getMethod = ReflectionUtils.findMethod(enumCls, "get" + StringUtils.capitalize(fieldName));
            if (null == getMethod) {
                return null;
            }
        } else {
            ReflectionUtils.makeAccessible(valueField);
        }

        for (E e : enumCls.getEnumConstants()) {
            Object fVal = Objects.nonNull(valueField) ? ReflectionUtils.getField(valueField, e) : ReflectionUtils.invokeMethod(getMethod, e);
            if (ObjectUtils.nullSafeEquals(value, fVal)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 从Map中获取指定key的Value再转换为实体Enum
     *
     * @param enumCls 枚举类型
     * @param map     map
     * @param key     键
     * @param <E>     枚举类型参数
     * @param <K>     map的key的类型
     * @param <V>     map的值的类型
     * @return 枚举常量
     */
    public static <E extends Enum<E>, K, V> Enum getByValue(Class<E> enumCls, Map<K, V> map, K key) {
        Object v = MapUtils.getObjectValue(map, key);
        if (null == v)
            return null;
        return getEnum(enumCls, v, key.toString());
    }

    /**
     * 解析所有枚举
     *
     * @param basePkg 包名
     * @return 包含所有枚举常量name-value的map
     */
    public static Map<String, List<Map<String, Object>>> parseEnums(String basePkg) {
        List<String> clsList = PackageUtils.getClassName(basePkg);
        HashMap<String, List<Map<String, Object>>> enumsMap = new HashMap<>();
        Method getValueMethod = ReflectionUtils.findMethod(Evaluatable.class, "getValue");
        for (String clz : clsList) {
            Class cls;
            try {
                cls = Class.forName(clz);
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (cls.isEnum() && Evaluatable.class.isAssignableFrom(cls)) {
                List<Map<String, Object>> valueList = new ArrayList<>();
                //取value值
                for (Object o : cls.getEnumConstants()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", o.toString());
                    m.put("value", ReflectionUtils.invokeMethod(getValueMethod, o));
                    valueList.add(m);
                }
                enumsMap.put(cls.getSimpleName(), valueList);
            }
        }
        return enumsMap;
    }
}
