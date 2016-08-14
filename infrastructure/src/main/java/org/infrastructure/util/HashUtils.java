package org.infrastructure.util;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * hashMap or Hashtable 的常用类
 * 
 * @author xuweinan
 * @since 1.0
 *
 */
public abstract class HashUtils {

	/**
	 * 获取String值
	 * 
	 * @param rec
	 * @param key
	 */
	public static String getStringValue(Map<?, ?> rec, Object key) {
		return rec == null ? null : ObjectUtils.toString(rec.get(key));
	}

	/**
	 * 获取Long值
	 * 
	 * @param rec
	 * @param key
	 */
	public static Long getLongValue(Map<?, ?> rec, Object key) {
		String strVal = rec == null ? null : ObjectUtils.toString(rec.get(key));
		return StringUtils.isBlank(strVal) ? null : Long.valueOf(strVal);
	}

	/**
	 * 获取Integer值
	 * 
	 * @param rec
	 * @param key
	 */
	public static Integer getIntegerValue(Map<?, ?> rec, Object key) {
		String strVal = rec == null ? null : ObjectUtils.toString(rec.get(key));
		return StringUtils.isBlank(strVal) ? null : new BigDecimal(strVal).intValue();
	}

	/**
	 * 获取Double值
	 * 
	 * @param rec
	 * @param key
	 */
	public static Double getDoubleValue(Map<?, ?> rec, Object key) {
		String strVal = rec == null ? null : ObjectUtils.toString(rec.get(key));
		return StringUtils.isBlank(strVal) ? null : new BigDecimal(strVal).doubleValue();
	}

	/**
	 * 两个Map，在某些字段是否相等
	 * 
	 * @param ht1
	 * @param ht2
	 * @param keys
	 * @return
	 */
	public static boolean equalsWith(Map<?, Object> ht1, Map<?, Object> ht2, String... keys) {

		boolean eq = true;
		for (String key : keys) {
			Object o1 = ht1.get(key);
			Object o2 = ht2.get(key);

			if ((o1 == null && null != o2) || (!o1.equals(o2))) {
				eq = false;
				break;
			}
		}

		return eq;
	}

	/**
	 * 通过数组快速创建参数Map (有序的LinkedHashMap)
	 * 
	 * @param params
	 *            key1,value1,key2,value2,key3,value3 ...
	 * @return map
	 */
	public static Map<String, Object> getMap(Object... params) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		if (params.length % 2 != 0) {
			throw new RuntimeException("键值对必须为偶数个");
		}

		for (int i = 0; i < params.length;) {
			map.put(params[i].toString(), params[i + 1]);
			i += 2;
		}
		return map;
	}

	/**
	 * 得到时间值
	 * 
	 * @param map
	 * @param key
	 * @return
	 * @version 1.0
	 */
	public static Date getDateValue(Map<?, Object> map, String key) {
		return map.get(key) == null ? null : (Date) map.get(key);
	}

	/**
	 * 查看列表中，cell值
	 * 
	 * @param list
	 * @param key
	 * @return
	 * @version 1.0
	 */
	public static Set<Object> distinctList(List<Map<?, Object>> list, String key, Comparator<Object> objCpt) {
		Set<Object> objSet = new HashSet<>();
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
	 * 
	 * @param list
	 * @return
	 * @version 1.0
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
	 * 
	 * @param map
	 * @param amtField
	 * @return
	 * @version 1.0
	 */
	public static BigDecimal getBigDecimal(Map<?, Object> map, String amtField) {
		String bgc = HashUtils.getStringValue(map, amtField);
		if (StringUtils.isNotEmpty(bgc))
			return new BigDecimal(bgc);

		return BigDecimal.ZERO;
	}
}
