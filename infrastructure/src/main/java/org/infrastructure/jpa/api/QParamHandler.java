package org.infrastructure.jpa.api;

import org.hibernate.criterion.DetachedCriteria;

/**
 * 自定义查询条件处理
 * 
 * @author zhou
 *
 */
public abstract class QParamHandler {

	public QParamHandler(String field) {
		this.field = field;
	}

	public String field;

	/**
	 * 自定义查询条件
	 * 
	 * @param dc
	 *            离线查询条件
	 * @param val
	 *            参数值
	 */
	public abstract void appendCriteria(DetachedCriteria dc, String val);

}
