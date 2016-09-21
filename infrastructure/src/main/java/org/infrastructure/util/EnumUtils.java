package org.infrastructure.util;

import org.infrastructure.annotations.UserEnum;
import org.infrastructure.sys.Validate;
import org.infrastructure.throwable.SimplifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举工具类
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class EnumUtils {
    private static Logger logger = LoggerFactory.getLogger(EnumUtils.class);
    private static final String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
    private static final String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
    private static final String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";
    private static final String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";

    /**
     * This constructor is public to permit tools that require a JavaBean
     * instance to operate.
     */
    public EnumUtils() {
    }

    /**
     * <p>Gets the {@code Map} of enums by name.</p>
     * <p>
     * <p>This method is useful when you need a map of enums by name.</p>
     *
     * @param <E>       the type of the enumeration
     * @param enumClass the class of the enum to query, not null
     * @return the modifiable map of enum names to enums, never null
     */
    public static <E extends Enum<E>> Map<String, E> getEnumMap(final Class<E> enumClass) {
        final Map<String, E> map = new LinkedHashMap<>();
        for (final E e : enumClass.getEnumConstants()) {
            map.put(e.name(), e);
        }
        return map;
    }

    /**
     * <p>Gets the {@code List} of enums.</p>
     * <p>
     * <p>This method is useful when you need a list of enums rather than an array.</p>
     *
     * @param <E>       the type of the enumeration
     * @param enumClass the class of the enum to query, not null
     * @return the modifiable list of enums, never null
     */
    public static <E extends Enum<E>> List<E> getEnumList(final Class<E> enumClass) {
        return new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()));
    }

    /**
     * <p>Checks if the specified name is a valid enum for the class.</p>
     * <p>
     * <p>This method differs from {@link Enum#valueOf} in that checks if the name is
     * a valid enum without needing to catch the exception.</p>
     *
     * @param <E>       the type of the enumeration
     * @param enumClass the class of the enum to query, not null
     * @param enumName  the enum name, null returns false
     * @return true if the enum name is valid, otherwise false
     */
    public static <E extends Enum<E>> boolean isValidEnum(final Class<E> enumClass, final String enumName) {
        if (enumName == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, enumName);
            return true;
        } catch (final IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Validate that {@code enumClass} is compatible with representation in a {@code long}.
     *
     * @param <E>       the type of the enumeration
     * @param enumClass to check
     * @return {@code enumClass}
     * @throws NullPointerException     if {@code enumClass} is {@code null}
     * @throws IllegalArgumentException if {@code enumClass} is not an enum class or has more than 64 values
     * @since 3.0.1
     */
    private static <E extends Enum<E>> Class<E> checkBitVectorable(final Class<E> enumClass) {
        final E[] constants = asEnum(enumClass).getEnumConstants();
        Validate.isTrue(constants.length <= Long.SIZE, CANNOT_STORE_S_S_VALUES_IN_S_BITS,
                constants.length, enumClass.getSimpleName(), Long.SIZE);

        return enumClass;
    }

    /**
     * Validate {@code enumClass}.
     *
     * @param <E>       the type of the enumeration
     * @param enumClass to check
     * @return {@code enumClass}
     * @throws NullPointerException     if {@code enumClass} is {@code null}
     * @throws IllegalArgumentException if {@code enumClass} is not an enum class
     * @since 3.2
     */
    private static <E extends Enum<E>> Class<E> asEnum(final Class<E> enumClass) {
        Validate.notNull(enumClass, ENUM_CLASS_MUST_BE_DEFINED);
        Validate.isTrue(enumClass.isEnum(), S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE, enumClass);
        return enumClass;
    }

    /**
     * 通过枚举文本字段，获得枚举类型的常量
     *
     * @param enumCls 枚举类型
     * @param text    枚举文本
     * @return 枚举常量
     */
    public static Enum getEnum(Class<?> enumCls, String text) {
        for (Object o : enumCls.getEnumConstants()) {
            if (o.toString().equals(text)) {
                return (Enum) o;
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
    public static Enum getEnum(Class<?> enumCls, int value, String... field) {
        String fieldName;
        if (null != field && field.length > 0 && StringUtils.isNotEmpty(field[0]))
            fieldName = field[0];
        else
            fieldName = "value";
        Field valueField;
        try {
            valueField = enumCls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new SimplifiedException("Enum:" + enumCls.getName() + " has no such field:" + fieldName, e);
        }
        ReflectionUtils.makeAccessible(valueField);
        for (Object o : enumCls.getEnumConstants()) {
            int fVal = (Integer) ReflectionUtils.getField(valueField, o);
            if (value == fVal) {
                return (Enum) o;
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
    public static Enum getByValue(Class<?> enumCls, Map<?, Object> map, String key) {
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
    public static int getEnumValue(Class<?> enumClass, Enum enumValue) {
        if (enumClass == null)
            enumClass = enumValue.getClass();

        Field vField;
        try {
            vField = enumClass.getDeclaredField("value");
            ReflectionUtils.makeAccessible(vField);

        } catch (Exception e) {
            throw new SimplifiedException("Enum" + enumClass + "未声明value字段", e);
        }

        int value;
        try {
            value = Integer.valueOf(vField.get(enumValue).toString());
        } catch (Exception e) {
            throw new SimplifiedException("Enum" + enumClass + "获取value失败", e);
        }

        return value;

    }

    /**
     * 解析所有枚举
     *
     * @param basePkg 包名
     * @return 包含所有枚举常量name-value的map
     */
    public static Map<String, List<HashMap>> parseEnums(String basePkg) throws Exception {
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
                        int value = Integer.parseInt(vField.get(o).toString());
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