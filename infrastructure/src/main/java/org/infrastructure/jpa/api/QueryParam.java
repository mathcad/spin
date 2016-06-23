package org.infrastructure.jpa.api;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 通用查询参数
 */
public class QueryParam<T> implements Serializable {
	private static final long serialVersionUID = 4033669191222887528L;

	/**
	 * com.rb.vol.sys.User
	 */
	public String cls;

	/**
	 * 查询列表
	 */
	public List<String> fields;

	/**
	 * {name__like:"高某",organ__name__like:"总部"}
	 * 
	 * in 条件请使用,分割的id组合
	 */
	public LinkedHashMap<String, T> q = new LinkedHashMap<String, T>();

	/**
	 * name__desc,id__desc
	 */
	public String sort;

	public int start = 0;

	public int limit = 50;
}
