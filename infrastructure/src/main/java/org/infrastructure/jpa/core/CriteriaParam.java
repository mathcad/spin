package org.infrastructure.jpa.core;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 使用Hibernate条件组合查询
 * <p>
 * Criterition 条件 Order 排序
 *
 * @author xuweinan
 * @version V1.0
 */
public class CriteriaParam implements Serializable {
    private static final long serialVersionUID = -944451601973493971L;
    public Set<String> fields = new HashSet<>();
    public List<Criterion> criterions = new ArrayList<>();
    public List<Order> orders = new ArrayList<>();
    public PageRequest pageRequest = null;

    /**
     * 增加分页参数
     *
     * @param page 页码
     * @param size 页参数
     */
    public void addPage(int page, int size) {
        pageRequest = new PageRequest(page, size);
    }

    public CriteriaParam addField(String... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    public CriteriaParam addCriterion(Criterion... cts) {
        Collections.addAll(this.criterions, cts);
        return this;
    }

    public CriteriaParam addOrder(Order... ords) {
        Collections.addAll(this.orders, ords);
        return this;
    }

    /**
     * 快速eq条件
     */
    public CriteriaParam eq(String prop, Object value) {
        this.addCriterion(Restrictions.eq(prop, value));
        return this;
    }

    /**
     * like '%%' 全模糊
     */
    public CriteriaParam like(String prop, String value) {
        this.addCriterion(Restrictions.like(prop, value, MatchMode.ANYWHERE));
        return this;
    }

    /**
     * 快速gt条件 >
     */
    public CriteriaParam gt(String prop, Object value) {
        this.addCriterion(Restrictions.gt(prop, value));
        return this;
    }

    /**
     * 快速ge条件 >=
     */
    public CriteriaParam gte(String prop, Object value) {
        this.addCriterion(Restrictions.ge(prop, value));
        return this;
    }

    /**
     * 快速lt条件 <
     */
    public CriteriaParam lt(String prop, Object value) {
        this.addCriterion(Restrictions.lt(prop, value));
        return this;
    }

    /**
     * 快速le条件 <=
     */
    public CriteriaParam le(String prop, Object value) {
        this.addCriterion(Restrictions.le(prop, value));
        return this;
    }

    /**
     * 附件in查询
     */
    public CriteriaParam in(String prop, Collection<?> value) {
        this.addCriterion(Restrictions.in(prop, value));
        return this;
    }

    /**
     * 附加in条件
     */
    public CriteriaParam in(String prop, Object... value) {
        this.addCriterion(Restrictions.in(prop, value));
        return this;
    }
}
