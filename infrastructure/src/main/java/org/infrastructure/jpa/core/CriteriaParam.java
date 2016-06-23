package org.infrastructure.jpa.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.PageRequest;

/**
 * 使用Hibernate条件组合查询
 * 
 * Criterition 条件 Order 排序
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年3月15日 下午2:51:14
 * @version V1.0
 */
public class CriteriaParam implements java.io.Serializable {
	private static final long serialVersionUID = -944451601973493971L;
	Set<String> fields = new HashSet<String>();
	List<Criterion> criterions = new ArrayList<Criterion>();
	List<Order> orders = new ArrayList<Order>();
	PageRequest pageRequest = null;

	/**
	 * 增加分页参数
	 * 
	 * @param page
	 *            页码
	 * @param size
	 *            页参数
	 * @version 1.0
	 */
	public void addPage(int page, int size) {
		pageRequest = new PageRequest(page, size);
	}

	public CriteriaParam addField(String... fields) {
		for (String f : fields) {
			this.fields.add(f);
		}
		return this;
	}

	public CriteriaParam addCriterion(Criterion... cts) {
		for (Criterion ct : cts) {
			this.criterions.add(ct);
		}
		return this;
	}

	public CriteriaParam addOrder(Order... ords) {
		for (Order ct : ords) {
			this.orders.add(ct);
		}
		return this;
	}

	/**
	 * 快速eq条件
	 * 
	 * @param prop
	 * @param value
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam eq(String prop, Object value) {
		this.addCriterion(Restrictions.eq(prop, value));
		return this;
	}

	/**
	 * like '%%' 全模糊
	 * 
	 * @param prop
	 * @param value
	 * @version 1.0
	 */
	public CriteriaParam like(String prop, String value) {
		this.addCriterion(Restrictions.like(prop, value, MatchMode.ANYWHERE));
		return this;
	}

	/**
	 * 快速gt条件 >
	 * 
	 * @param prop
	 * @param value
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam gt(String prop, Object value) {
		this.addCriterion(Restrictions.gt(prop, value));
		return this;
	}

	/**
	 * 快速ge条件 >=
	 * 
	 * @param prop
	 * @param value
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam gte(String prop, Object value) {
		this.addCriterion(Restrictions.ge(prop, value));
		return this;
	}

	/**
	 * 快速lt条件 <
	 * 
	 * @param prop
	 * @param value
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam lt(String prop, Object value) {
		this.addCriterion(Restrictions.lt(prop, value));
		return this;
	}

	/**
	 * 快速le条件 <=
	 * 
	 * @param prop
	 * @param value
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam le(String prop, Object value) {
		this.addCriterion(Restrictions.le(prop, value));
		return this;
	}

	/**
	 * 附件in查询
	 * 
	 * @param prop
	 * @param value
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam in(String prop, Collection<?> value) {
		this.addCriterion(Restrictions.in(prop, value));
		return this;
	}

	/**
	 * 附加in条件
	 * 
	 * @param prop
	 * @param value
	 *            ...变长参数
	 * @return
	 * @version 1.0
	 */
	public CriteriaParam in(String prop, Object... value) {
		this.addCriterion(Restrictions.in(prop, value));
		return this;
	}
}
