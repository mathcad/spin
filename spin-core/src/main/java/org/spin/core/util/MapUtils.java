package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.collection.MultiValueMap;
import org.spin.core.throwable.SpinException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Map的工具类
 *
 * @author xuweinan
 * @since 1.0
 */
public abstract class MapUtils {
    private static final Logger logger = LoggerFactory.getLogger(MapUtils.class);

    private MapUtils() {
    }

    /**
     * 通过数组快速创建参数Map (HashMap)
     *
     * @param params key1,value1,key2,value2,key3,value3 ...
     * @param <T>    值类型
     * @return map
     */
    @SafeVarargs
    public static <T> Map<String, T> ofMap(T... params) {
        Map<String, T> map = new HashMap<>();
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("键值对必须为偶数个");
        }
        for (int i = 0; i < params.length; i += 2) {
            map.put(params[i].toString(), params[i + 1]);
        }
        return map;
    }

    public static Map<String, String> ofMap(Properties properties) {
        properties.propertyNames();
        return StreamUtils.enumerationAsStream(properties.propertyNames()).map(Object::toString).collect(Collectors.toMap(n -> n, properties::getProperty));
    }

    public static <T extends Map<K, V>, K, V> MapBuilder<T, K, V> with(Supplier<T> mapSupplier) {
        MapBuilder<T, K, V> builder = new MapBuilder<>();
        builder.mapSupplier = mapSupplier;
        return builder;
    }

    public static <K, V> Map<K, V> ofMap() {
        return new HashMap<>();
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        map.put(k9, v9);
        return map;
    }

    public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        map.put(k9, v9);
        map.put(k10, v10);
        return map;
    }

    /**
     * 从map中获取指定的字符串属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <K, V> String getStringValue(Map<K, V> map, K key) {
        return Objects.isNull(map) ? null : ObjectUtils.toString(map.get(key), null);
    }

    /**
     * 从map中获取指定的字符串属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <K, V> String getStringValue(Map<K, V> map, K key, String defaultVal) {
        return Objects.isNull(map) ? defaultVal : ObjectUtils.toString(map.get(key), defaultVal);
    }

    /**
     * 从map中获取指定的Long属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <K, V> Long getLongValue(Map<K, V> map, K key) {
        return getLongValue(map, key, null);
    }

    /**
     * 从map中获取指定的Long属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <K, V> Long getLongValue(Map<K, V> map, K key, Long defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof Long) {
            return (Long) val;
        } else if (val instanceof CharSequence) {
            try {
                return Long.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return defaultVal;
            }
        } else if (val instanceof Number) {
            return ((Number) val).longValue();
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定的Integer属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <K, V> Integer getIntValue(Map<K, V> map, K key) {
        return getIntValue(map, key, null);
    }

    /**
     * 从map中获取指定的Integer属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <K, V> Integer getIntValue(Map<K, V> map, K key, Integer defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        } else if (val instanceof CharSequence) {
            try {
                return Integer.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return defaultVal;
            }
        } else if (val instanceof Number) {
            return ((Number) val).intValue();
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定的Double属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <K, V> Double getDoubleValue(Map<K, V> map, K key) {
        return getDoubleValue(map, key, null);
    }

    /**
     * 从map中获取指定的Double属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <K, V> Double getDoubleValue(Map<K, V> map, K key, Double defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof Double) {
            return (Double) val;
        } else if (val instanceof CharSequence) {
            try {
                return Double.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return defaultVal;
            }
        } else if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定的Float属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <K, V> Float getFloatValue(Map<K, V> map, K key) {
        return getFloatValue(map, key, null);
    }

    /**
     * 从map中获取指定的Float属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <K, V> Float getFloatValue(Map<K, V> map, K key, Float defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof Float) {
            return (Float) val;
        } else if (val instanceof CharSequence) {
            try {
                return Float.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return defaultVal;
            }
        } else if (val instanceof Number) {
            return ((Number) val).floatValue();
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定的BigDecimal属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <K, V> BigDecimal getBigDecimalValue(Map<K, V> map, K key) {
        return getBigDecimalValue(map, key, null);
    }

    /**
     * 从map中获取指定的BigDecimal属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <K, V> BigDecimal getBigDecimalValue(Map<K, V> map, K key, BigDecimal defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof Number || val instanceof CharSequence) {
            return new BigDecimal(val.toString());
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定的时间值属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return Date
     */
    public static <K, V> Date getDateValue(Map<K, V> map, K key) {
        return getDateValue(map, key, null);
    }

    /**
     * 从map中获取指定的时间值属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return Date
     */
    public static <K, V> Date getDateValue(Map<K, V> map, K key, Date defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof Date) {
            return (Date) val;
        } else if (val instanceof CharSequence) {
            try {
                return DateUtils.toDate(val.toString());
            } catch (Exception e) {
                logger.error("日期格式不正确", e);
                return defaultVal;
            }
        } else if (val instanceof TemporalAccessor) {
            return DateUtils.toDate(DateUtils.formatDateForMillSec((TemporalAccessor) val), "yyyy-MM-dd HH:mm:ss SSS");
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定的时间值属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return LocalDateTime
     */
    public static <K, V> LocalDateTime getLocalDateTimeValue(Map<K, V> map, K key) {
        return getLocalDateTimeValue(map, key, null);
    }

    /**
     * 从map中获取指定的时间值属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return LocalDateTime
     */
    public static <K, V> LocalDateTime getLocalDateTimeValue(Map<K, V> map, K key, LocalDateTime defaultVal) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return defaultVal;
        }
        if (val instanceof LocalDateTime) {
            return (LocalDateTime) val;
        } else if (val instanceof CharSequence) {
            try {
                return DateUtils.toLocalDateTime(val.toString());
            } catch (Exception e) {
                logger.error("日期格式不正确", e);
                return defaultVal;
            }
        } else if (val instanceof Date) {
            return DateUtils.toLocalDateTime((Date) val);
        } else if (val instanceof TemporalAccessor) {
            return DateUtils.toLocalDateTime(DateUtils.formatDateForMillSec((TemporalAccessor) val), "yyyy-MM-dd HH:mm:ss SSS");
        } else {
            return defaultVal;
        }
    }

    /**
     * 从map中获取指定类型的属性
     *
     * @param map map对象
     * @param key 属性键
     * @param <T> 类型参数
     * @param <K> map的key类型
     * @param <V> map的值类型
     * @return 获取的结果
     */
    public static <T, K, V> T getObjectValue(Map<K, V> map, K key) {
        return getObjectValue(map, key, null);
    }

    /**
     * 从map中获取指定类型的属性
     *
     * @param map        map对象
     * @param key        属性键
     * @param defaultVal 默认值
     * @param <T>        类型参数
     * @param <K>        map的key类型
     * @param <V>        map的值类型
     * @return 获取的结果
     */
    public static <T, K, V> T getObjectValue(Map<K, V> map, K key, T defaultVal) {
        try {
            //noinspection unchecked
            return (T) (Objects.isNull(map) ? defaultVal : map.get(key));
        } catch (Exception e) {
            throw new SpinException("对象类型不匹配");
        }
    }

    /**
     * 比较两个Map，是否所有指定字段的值都相等
     * <p>如果指定的字段为空，直接返回false</p>
     *
     * @param map1 第一个map
     * @param map2 第二个map
     * @param keys 指定的字段
     * @param <K>  key类型
     * @param <V1> map1的值类型
     * @param <V2> map2的值类型
     * @return 判断结果
     */
    @SafeVarargs
    public static <K, V1, V2> boolean equalsWith(Map<K, V1> map1, Map<K, V2> map2, K... keys) {
        if (null == keys || 0 == keys.length) {
            return false;
        }
        boolean eq = true;
        for (K key : keys) {
            eq = eq && ObjectUtils.nullSafeEquals(map1.get(key), map2.get(key));
        }
        return eq;
    }

    /**
     * 获取Map列表中，某列去重后的结果
     *
     * @param list   列表
     * @param key    统计字段
     * @param objCpt 比较器
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 统计结果
     */
    public static <K, V> List<V> distinctList(List<Map<K, V>> list, K key, Comparator<V> objCpt) {
        List<V> objSet = CollectionUtils.ofArrayList();
        list.stream().map(map -> map.get(key)).filter(Objects::nonNull).forEach(o -> {
            if (objSet.stream().noneMatch(obj -> objCpt.compare(obj, o) == 0)) {
                objSet.add(o);
            }
        });

        return objSet;
    }

    /**
     * 将Map转换为MultiValueMap
     *
     * @param map 源Map
     * @param <K> 键类型参数
     * @param <V> 值类型参数
     * @return MultiValueMap
     */
    public static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, List<V>> map) {
        return new MultiValueMapAdapter<>(map);
    }

    /**
     * 返回MultiValueMap的一个只读视图
     *
     * @param map 目标Map
     * @param <K> 键类型参数
     * @param <V> 值类型参数
     * @return 只读的MultiValueMap
     */
    @SuppressWarnings("unchecked")
    public static <K, V> MultiValueMap<K, V> unmodifiableMultiValueMap(MultiValueMap<? extends K, ? extends V> map) {
        Assert.notNull(map, "'map' must not be null");
        Map<K, List<V>> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<? extends K, ? extends List<? extends V>> entry : map.entrySet()) {
            List<? extends V> values = Collections.unmodifiableList(entry.getValue());
            result.put(entry.getKey(), (List<V>) values);
        }
        Map<K, List<V>> unmodifiableMap = Collections.unmodifiableMap(result);
        return toMultiValueMap(unmodifiableMap);
    }


    /**
     * 将Properties实例中的内容合并到Map中
     *
     * @param props 待合并的Properties实例(可以为 {@code null})
     * @param map   合并的目标Map
     * @param <K>   键的类型参数
     * @param <V>   值的类型参数
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.getProperty(key);
                if (value == null) {
                    // Potentially a non-String value...
                    value = props.get(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }

    /**
     * 判断Map是否为空
     *
     * @param map 待判断的map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static class MapBuilder<T extends Map<K, V>, K, V> {
        @SuppressWarnings("unchecked")
        private Supplier<T> mapSupplier = () -> (T) new HashMap<K, V>();

        public T ofMap() {
            return mapSupplier.get();
        }

        public T ofMap(K k1, V v1) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            map.put(k8, v8);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            map.put(k8, v8);
            map.put(k9, v9);
            return map;
        }

        public T ofMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
            T map = mapSupplier.get();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            map.put(k8, v8);
            map.put(k9, v9);
            map.put(k10, v10);
            return map;
        }
    }

    /**
     * Map到MultiValueMap的适配器
     */
    private static class MultiValueMapAdapter<K, V> implements MultiValueMap<K, V>, Serializable {
        private static final long serialVersionUID = 1704461815464000179L;
        private final Map<K, List<V>> map;

        public MultiValueMapAdapter(Map<K, List<V>> map) {
            Assert.notNull(map, "'map' must not be null");
            this.map = map;
        }

        @Override
        public void add(K key, V value) {
            List<V> values = this.map.computeIfAbsent(key, k -> new LinkedList<>());
            values.add(value);
        }

        @Override
        public V getFirst(K key) {
            List<V> values = this.map.get(key);
            return (values != null ? values.get(0) : null);
        }

        @Override
        public void set(K key, V value) {
            List<V> values = new LinkedList<>();
            values.add(value);
            this.map.put(key, values);
        }

        @Override
        public void setAll(Map<K, V> values) {
            for (Entry<K, V> entry : values.entrySet()) {
                set(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Map<K, V> toSingleValueMap() {
            LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<>(this.map.size());
            for (Entry<K, List<V>> entry : map.entrySet()) {
                singleValueMap.put(entry.getKey(), entry.getValue().get(0));
            }
            return singleValueMap;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.map.containsValue(value);
        }

        @Override
        public List<V> get(Object key) {
            return this.map.get(key);
        }

        @Override
        public List<V> put(K key, List<V> value) {
            return this.map.put(key, value);
        }

        @Override
        public List<V> remove(Object key) {
            return this.map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends List<V>> map) {
            this.map.putAll(map);
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public Set<K> keySet() {
            return this.map.keySet();
        }

        @Override
        public Collection<List<V>> values() {
            return this.map.values();
        }

        @Override
        public Set<Entry<K, List<V>>> entrySet() {
            return this.map.entrySet();
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof Map && map.equals(other);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode();
        }

        @Override
        public String toString() {
            return this.map.toString();
        }
    }
}
