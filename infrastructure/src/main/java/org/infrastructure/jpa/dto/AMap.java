package org.infrastructure.jpa.dto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;

/**
 * 参数Map
 */
public class AMap extends HashMap<String, Object> {
	private static final long serialVersionUID = -2535315082184545038L;

	public AMap() {
		super();
	}

	/**
	 * 构造
	 * 
	 * @param key
	 * @param val
	 */
	public AMap(String key, String val) {
		super();
		this.add(key, val);
	}

	/**
	 * 获取String值
	 * 
	 * @param this
	 * @param key
	 */
	public String strValue(String key) {
		return this.get(key) == null ? null : String.valueOf(this.get(key));
	}

	/**
	 * 获取Long值
	 * 
	 * @param this
	 * @param key
	 */
	public Long longValue(String key) {
		String strVal = this.get(key) == null ? null : this.get(key).toString();
		return StringUtils.isEmpty(strVal) ? null : Long.valueOf(strVal);
	}

	/**
	 * 获取Integer值
	 * 
	 * @param this
	 * @param key
	 */
	public Integer intValue(String key) {
		String strVal = this.get(key) == null ? null : this.get(key).toString();
		return StringUtils.isEmpty(strVal) ? null : new BigDecimal(strVal).intValue();
	}

	/**
	 * 获取Double值
	 * 
	 * @param this
	 * @param key
	 */
	public Double doubleValue(String key) {
		String strVal = this.get(key) == null ? null : this.get(key).toString();
		return StringUtils.isEmpty(strVal) ? null : new BigDecimal(strVal).doubleValue();
	}

	/**
	 * 获取String值
	 * 
	 * @param this
	 * @param key
	 * @throws SQLException
	 */
	public Date dateValue(String key) throws Exception {
		if (this.get(key) == null || StringUtils.isBlank(this.get(key).toString()))
			return null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.parse(this.get(key).toString());
		} catch (Exception e) {
			throw new BizException("日期转换错误：" + this.get(key), e);
		}
	}

	/**
	 * 
	 * @param key
	 * @param scale
	 * @return
	 */
	public BigDecimal decimalValue(String key, int scale) {
		String strVal = this.get(key) == null ? null : this.get(key).toString();
		if (StringUtils.isEmpty(strVal))
			return null;
		else {
			BigDecimal b = new BigDecimal(strVal);
			b.setScale(scale, BigDecimal.ROUND_HALF_UP);
			return b;
		}
	}

	/**
	 * put (add)
	 * 
	 * @param key
	 * @return
	 */
	public AMap addMap(String key) {
		AMap m = new AMap();
		this.put(key, m);
		return m;
	}

	/**
	 * put (add)
	 * 
	 * @param key
	 * @return
	 */
	public List<AMap> addList(String key) {
		List<AMap> m = new ArrayList<AMap>();
		this.put(key, m);
		return m;
	}

	/**
	 * put (add)
	 * 
	 * @param key
	 * @return
	 */
	public List<AMap> addArray(String key) {
		List<AMap> m = new ArrayList<AMap>();
		this.put(key, m);
		return m;
	}

	/**
	 * put (add)
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public AMap add(String key, Object val) {
		this.put(key, val);
		return this;
	}

}
