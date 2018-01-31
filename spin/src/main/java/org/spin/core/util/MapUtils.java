package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Map的工具类
 *
 * @author xuweinan
 * @since 1.0
 */
public abstract class MapUtils {
    private static final Logger logger = LoggerFactory.getLogger(MapUtils.class);

    /**
     * 通过数组快速创建参数Map (HashMap)
     *
     * @param params key1,value1,key2,value2,key3,value3 ...
     * @return map
     */
    @SafeVarargs
    public static <T> Map<String, T> ofMap(T... params) {
        Map<String, T> map = new HashMap<>();
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("键值对必须为偶数个");
        }
        for (int i = 0; i < params.length; ) {
            map.put(params[i].toString(), params[i + 1]);
            i += 2;
        }
        return map;
    }

    public static Map<String, String> ofMap(Properties properties) {
        properties.propertyNames();
        return StreamUtils.enumerationAsStream(properties.propertyNames()).map(Object::toString).collect(Collectors.toMap(n -> n, properties::getProperty));
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
     * 获取String值
     */
    public static String getStringValue(Map<?, ?> rec, Object key) {
        return Objects.isNull(rec) ? null : ObjectUtils.toString(rec.get(key), null);
    }

    /**
     * 获取Long值
     */
    public static Long getLongValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof Long) {
            return (Long) val;
        } else if (val instanceof CharSequence) {
            try {
                return Long.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return null;
            }
        } else if (val instanceof Number) {
            return ((Number) val).longValue();
        } else {
            return null;
        }
    }

    /**
     * 获取Integer值
     */
    public static Integer getIntValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        } else if (val instanceof CharSequence) {
            try {
                return Integer.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return null;
            }
        } else if (val instanceof Number) {
            return ((Number) val).intValue();
        } else {
            return null;
        }
    }

    /**
     * 获取Double值
     */
    public static Double getDoubleValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof Double) {
            return (Double) val;
        } else if (val instanceof CharSequence) {
            try {
                return Double.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return null;
            }
        } else if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else {
            return null;
        }
    }

    /**
     * 获取Double值
     */
    public static Float getFloatValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof Float) {
            return (Float) val;
        } else if (val instanceof CharSequence) {
            try {
                return Float.valueOf(val.toString());
            } catch (Exception e) {
                logger.error("数值格式不正确", e);
                return null;
            }
        } else if (val instanceof Number) {
            return ((Number) val).floatValue();
        } else {
            return null;
        }
    }

    /**
     * 获取BigDecimal值
     */
    public static BigDecimal getBigDecimalValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof Number || val instanceof CharSequence) {
            return new BigDecimal(val.toString());
        } else {
            return null;
        }
    }

    /**
     * 得到时间值
     */
    public static Date getDateValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof Date) {
            return (Date) ((Date) val).clone();
        } else if (val instanceof CharSequence) {
            try {
                return DateUtils.toDate(val.toString());
            } catch (Exception e) {
                logger.error("日期格式不正确", e);
                return null;
            }
        } else if (val instanceof TemporalAccessor) {
            return DateUtils.toDate(DateUtils.formatDateForMillSec((TemporalAccessor) val), "yyyy-MM-dd HH:mm:ss SSS");
        } else {
            return null;
        }
    }

    /**
     * 得到时间值
     */
    public static LocalDateTime getLocalDateTimeValue(Map<?, ?> map, Object key) {
        Object val = Objects.isNull(map) ? null : map.get(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val instanceof LocalDateTime) {
            return (LocalDateTime) val;
        } else if (val instanceof CharSequence) {
            try {
                return DateUtils.toLocalDateTime(val.toString());
            } catch (Exception e) {
                logger.error("日期格式不正确", e);
                return null;
            }
        } else if (val instanceof Date) {
            return DateUtils.toLocalDateTime((Date) val);
        } else if (val instanceof TemporalAccessor) {
            return DateUtils.toLocalDateTime(DateUtils.formatDateForMillSec((TemporalAccessor) val), "yyyy-MM-dd HH:mm:ss SSS");
        } else {
            return null;
        }
    }

    /**
     * 比较两个Map，指定字段是否所有值都相等
     */
    public static boolean equalsWith(Map<?, ?> ht1, Map<?, ?> ht2, Object... keys) {
        final List<Boolean> eq = new ArrayList<>();
        Optional.ofNullable(keys).ifPresent(ks -> Arrays.stream(ks).forEach(k -> eq.add(ObjectUtils.equal(ht1.get(k), ht2.get(k)))));
        return !eq.contains(false);
    }

    /**
     * 获取Map列表中，某列去重后的结果
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
     * 统计某数值列的汇总
     */
    public static BigDecimal sumList(List<Map<?, ?>> list, Object key) {
        return list.stream().map(m -> getBigDecimalValue(m, key)).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 统计某数值列最大值
     */
    public static BigDecimal maxList(List<Map<?, ?>> list, Object key) {
        return list.stream().map(m -> getBigDecimalValue(m, key)).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(null);
    }

    /**
     * 统计某数值列最小值
     */
    public static BigDecimal minList(List<Map<?, ?>> list, Object key) {
        return list.stream().map(m -> getBigDecimalValue(m, key)).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null);
    }

    /**
     * 统计某数值列平均值
     */
    public static BigDecimal avgList(List<Map<?, ?>> list, Object key) {
        BigDecimal total = sumList(list, key);
        return total.divide(new BigDecimal(list.size()), BigDecimal.ROUND_HALF_UP);
    }
}
