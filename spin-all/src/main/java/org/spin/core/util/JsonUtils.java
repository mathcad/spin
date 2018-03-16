package org.spin.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.TypeIdentifier;
import org.spin.core.throwable.SimplifiedException;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 包含操作 {@code JSON} 数据的常用方法的工具类。
 * <p>
 * 该工具类使用的 {@code JSON} 转换引擎是{@code Google Gson}。
 * </p>
 */
public abstract class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 空{@code JSON}字符串 - <code>""</code>
     */
    private static final String EMPTY = "";

    /**
     * 默认的{@code JSON}日期/时间字段的格式化模式
     */
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_LOCAL_TIME_PATTERN = "HH:mm:ss";
    private static final String DEFAULT_ERROR_MSG = "%s 无法转换为[%s]对象!";

    private static final Gson defaultGson;

    static {
        defaultGson = procGson(baseBuilder(CollectionUtils.ofArray(DEFAULT_DATE_PATTERN, DEFAULT_LOCAL_DATE_PATTERN, DEFAULT_LOCAL_TIME_PATTERN)).create());
    }

    private JsonUtils() {
    }

    /**
     * 获取一个默认的GSON实例
     *
     * @return 默认gson实例
     */
    public static Gson getDefaultGson() {
        return defaultGson;
    }

    /**
     * 将给定的目标对象根据指定的条件参数转换成 {@code JSON} 格式的字符串。<br>
     * <strong>该方法转换发生错误时，不会抛出任何异常。若发生错误时，曾通对象返回 <code>"{}"</code>； 集合或数组对象返回
     * <code>"[]"</code></strong>
     *
     * @param target                      目标对象。
     * @param targetType                  目标对象的类型。
     * @param isSerializeNulls            是否序列化 {@code null} 值字段。
     * @param version                     字段的版本号注解。
     * @param datePattern                 日期字段的格式化模式。
     * @param excludesFieldsWithoutExpose 是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Type targetType, boolean isSerializeNulls, Double version, boolean excludesFieldsWithoutExpose, String... datePattern) {
        if (target == null)
            return EMPTY;
        Class<?> clazz = target.getClass();
        if (ClassUtils.wrapperToPrimitive(clazz) != null && ClassUtils.wrapperToPrimitive(clazz).isPrimitive() || target instanceof CharSequence)
            return target.toString();
        GsonBuilder builder = baseBuilder(datePattern);
        if (isSerializeNulls)
            builder.serializeNulls();
        if (version != null)
            builder.setVersion(version);
        if (excludesFieldsWithoutExpose)
            builder.excludeFieldsWithoutExposeAnnotation();
        String result;
        Gson gson = procGson(builder.create());

        try {
            if (targetType != null) {
                result = gson.toJson(target, targetType);
            } else {
                result = gson.toJson(target);
            }
        } catch (Exception ex) {
            throw new SimplifiedException(ErrorCode.SERIALIZE_EXCEPTION, "目标对象 " + target.getClass().getName() + " 转换 JSON 字符串时，发生异常！", ex);
        }
        return result;
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target 要转换成 {@code JSON} 的目标对象。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target) {
        if (Objects.isNull(target)) {
            return EMPTY;
        }
        Class<?> clazz = target.getClass();
        if (ClassUtils.wrapperToPrimitive(clazz) != null && ClassUtils.wrapperToPrimitive(clazz).isPrimitive() || target instanceof CharSequence)
            return target.toString();
        return defaultGson.toJson(target);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * </ul>
     *
     * @param target      要转换成 {@code JSON} 的目标对象。
     * @param datePattern 日期字段的格式化模式。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, String... datePattern) {
        return toJson(target, null, false, null, true, datePattern);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target  要转换成 {@code JSON} 的目标对象。
     * @param version 字段的版本号注解({@literal @Since})。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Double version) {
        return toJson(target, null, false, version, true);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target                      要转换成 {@code JSON} 的目标对象。
     * @param excludesFieldsWithoutExpose 是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, boolean excludesFieldsWithoutExpose) {
        return toJson(target, null, false, null, excludesFieldsWithoutExpose);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target                      要转换成 {@code JSON} 的目标对象。
     * @param version                     字段的版本号注解({@literal @Since})。
     * @param excludesFieldsWithoutExpose 是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Double version, boolean excludesFieldsWithoutExpose) {
        return toJson(target, null, false, version, excludesFieldsWithoutExpose);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法通常用来转换使用泛型的对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSSS}；</li>
     * </ul>
     *
     * @param target     要转换成 {@code JSON} 的目标对象。
     * @param targetType 目标对象的类型。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Type targetType) {
        return toJson(target, targetType, false, null, true);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法通常用来转换使用泛型的对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSSS}；</li>
     * </ul>
     *
     * @param target     要转换成 {@code JSON} 的目标对象。
     * @param targetType 目标对象的类型。
     * @param version    字段的版本号注解({@literal @Since})。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Type targetType, Double version) {
        return toJson(target, targetType, false, version, true);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法通常用来转换使用泛型的对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target                      要转换成 {@code JSON} 的目标对象。
     * @param targetType                  目标对象的类型。
     * @param excludesFieldsWithoutExpose 是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Type targetType, boolean excludesFieldsWithoutExpose) {
        return toJson(target, targetType, false, null, excludesFieldsWithoutExpose);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法通常用来转换使用泛型的对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target                      要转换成 {@code JSON} 的目标对象。
     * @param targetType                  目标对象的类型。
     * @param version                     字段的版本号注解({@literal @Since})。
     * @param excludesFieldsWithoutExpose 是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     */
    public static String toJson(Object target, Type targetType, Double version, boolean excludesFieldsWithoutExpose) {
        return toJson(target, targetType, false, version, excludesFieldsWithoutExpose);
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。
     *
     * @param <T>         要转换的目标类型。
     * @param json        给定的 {@code JSON} 字符串。
     * @param token       {@code TypeIdentifier} 的类型指示类对象。
     * @param datePattern 日期格式模式。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     */
    public static <T> T fromJson(String json, TypeIdentifier<T> token, String... datePattern) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return procGson(baseBuilder(datePattern).create()).fromJson(json, token.getType());
        } catch (Exception ex) {
            throw new SimplifiedException(ErrorCode.SERIALIZE_EXCEPTION, String.format(DEFAULT_ERROR_MSG, json, token.toString()), ex);
        }
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。
     *
     * @param <T>   要转换的目标类型。
     * @param json  给定的 {@code JSON} 字符串。
     * @param token {@code TypeIdentifier} 的类型指示类对象。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     */
    public static <T> T fromJson(String json, TypeIdentifier<T> token) {
        try {
            return defaultGson.fromJson(json, token.getType());
        } catch (Exception ex) {
            throw new SimplifiedException(ErrorCode.SERIALIZE_EXCEPTION, String.format(DEFAULT_ERROR_MSG, json, token.toString()), ex);
        }
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            return defaultGson.fromJson(json, type);
        } catch (Exception ex) {
            throw new SimplifiedException(ErrorCode.SERIALIZE_EXCEPTION, String.format(DEFAULT_ERROR_MSG, json, type.getTypeName()), ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return defaultGson.fromJson(json, clazz);
        } catch (Exception ex) {
            throw new SimplifiedException(ErrorCode.SERIALIZE_EXCEPTION, String.format(DEFAULT_ERROR_MSG, json, clazz.getTypeName()), ex);
        }
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。<strong>此方法通常用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     *
     * @param <T>         要转换的目标类型。
     * @param json        给定的 {@code JSON} 字符串。
     * @param clazz       要转换的目标类。
     * @param datePattern 日期格式模式。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     */
    public static <T> T fromJson(String json, Class<T> clazz, String... datePattern) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return procGson(baseBuilder(datePattern).create()).fromJson(json, clazz);
        } catch (Exception ex) {
            throw new SimplifiedException(ErrorCode.SERIALIZE_EXCEPTION, String.format(DEFAULT_ERROR_MSG, json, clazz.getTypeName()), ex);
        }
    }

    private static Gson procGson(Gson gson) {
        try {
            Class<?> aClass = ClassUtils.getClass("com.google.gson.internal.bind.SpinReflectiveTypeAdapterFactory");
            Object[] factories = ClassUtils.getFieldValue(gson, "factories.list.elementData");
            for (int i = factories.length - 1; i != -1; --i) {
                if (null != factories[i] && factories[i] instanceof ReflectiveTypeAdapterFactory) {
                    factories[i] = ConstructorUtils.invokeConstructor(aClass, factories[i]);
                    break;
                }
            }
            return gson;
        } catch (Exception ignore) {
            return gson;
        }

    }

    private static GsonBuilder baseBuilder(String[] pattern) {
        GsonBuilder builder = new GsonBuilder();
        String[] patterns = new String[3];
        patterns[0] = null != pattern && pattern.length > 0 ? pattern[0] : DEFAULT_DATE_PATTERN;
        patterns[1] = null != pattern && pattern.length > 1 ? pattern[1] : DEFAULT_LOCAL_DATE_PATTERN;
        patterns[2] = null != pattern && pattern.length > 2 ? pattern[2] : DEFAULT_LOCAL_TIME_PATTERN;
        try {
            @SuppressWarnings("unchecked")
            Class<TypeAdapterFactory> factoryClass = (Class<TypeAdapterFactory>) ClassUtils.getClass("org.spin.enhance.gson.SpinTypeAdapterFactory");
            TypeAdapterFactory factory = ConstructorUtils.invokeConstructor(factoryClass, patterns);
            builder.registerTypeAdapterFactory(factory);
            builder.setDateFormat(patterns[0]);
        } catch (Exception ignore) {
            logger.info("Spin Enhance package not imported");
        }
        try {
            Class<?> queryParamCls = ClassUtils.getClass("org.spin.data.query.QueryParam");
            @SuppressWarnings("unchecked")
            Class<InstanceCreator> instanceCreatorCls = (Class<InstanceCreator>) ClassUtils.getClass("org.spin.enhance.gson.adapter.QueryParamInstanceCreater");
            InstanceCreator instanceCreator = instanceCreatorCls.newInstance();
            builder.registerTypeAdapter(queryParamCls, instanceCreator);
        } catch (Exception ignore) {
            logger.info("data module not imported");
        }
        return builder;
    }
}
