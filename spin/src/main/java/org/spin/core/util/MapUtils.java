package org.spin.core.util;

import java.math.BigDecimal;
import java.util.*;


/**
 * Map的工具类
 *
 * @author xuweinan
 * @since 1.0
 */
public abstract class MapUtils {

    /**
     * 获取String值
     */
    public static String getStringValue(Map<?, ?> rec, Object key) {
        return rec == null ? null : ObjectUtils.toString(rec.get(key));
    }

    /**
     * 获取Long值
     */
    public static Long getLongValue(Map<?, ?> rec, Object key) {
        String strVal = rec == null ? null : ObjectUtils.toString(rec.get(key));
        return StringUtils.isBlank(strVal) ? null : Long.valueOf(strVal);
    }

    /**
     * 获取Integer值
     */
    public static Integer getIntegerValue(Map<?, ?> rec, Object key) {
        String strVal = rec == null ? null : ObjectUtils.toString(rec.get(key));
        return StringUtils.isBlank(strVal) ? null : new BigDecimal(strVal).intValue();
    }

    /**
     * 获取Double值
     */
    public static Double getDoubleValue(Map<?, ?> rec, Object key) {
        String strVal = rec == null ? null : ObjectUtils.toString(rec.get(key));
        return StringUtils.isBlank(strVal) ? null : new BigDecimal(strVal).doubleValue();
    }

    /**
     * 两个Map，在某些字段是否相等
     */
    public static boolean equalsWith(Map<?, Object> ht1, Map<?, Object> ht2, String... keys) {
        final List<Boolean> eq = new ArrayList<>();
        Optional.ofNullable(keys).ifPresent(ks -> Arrays.stream(ks).forEach(k -> eq.add(ObjectUtils.equal(ht1.get(k), ht2.get(k)))));
        return !eq.contains(false);
    }

    /**
     * 通过数组快速创建参数Map (HashMap)
     *
     * @param params key1,value1,key2,value2,key3,value3 ...
     * @return map
     */
    @SafeVarargs
    public static <T> Map<String, T> getMap(T... params) {
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

    /**
     * 得到时间值
     */
    public static Date getDateValue(Map<?, Object> map, String key) {
        return map.get(key) == null ? null : (Date) map.get(key);
    }

    /**
     * 查看列表中，cell值
     */
    public static Set<Object> distinctList(List<Map<?, Object>> list, String key, Comparator<Object> objCpt) {
        Set<Object> objSet = new HashSet<>();
        list.stream().map(map -> map.get(key)).filter(Objects::nonNull).forEach(o -> {
//            objSet.stream().filter(obj -> objCpt.compare(obj, o)!=0).forEach();
        });
        for (Map<?, Object> map : list) {
            if (map.get(key) != null) {
                Object o = map.get(key);
                for (Object obj : objSet) {
                    if (objCpt.compare(obj, o) != 0) {
                        objSet.add(o);
                    }
                }
            }
        }

        return objSet;
    }

    /**
     * 统计列
     */
    public static BigDecimal sumList(List<Map<?, Object>> list, String key) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<?, Object> map : list) {
            if (map.get(key) != null) {
                BigDecimal dct = getBigDecimal(map, key);
                total = total.add(dct);
            }
        }
        return total;
    }

    /**
     * 获得最高项目
     */
    public static BigDecimal getBigDecimal(Map<?, Object> map, String amtField) {
        String bgc = MapUtils.getStringValue(map, amtField);
        if (StringUtils.isNotEmpty(bgc))
            return new BigDecimal(bgc);
        return BigDecimal.ZERO;
    }
}
