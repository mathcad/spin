package org.infrastructure.jpa.query;

import org.infrastructure.util.CollectionUtils;
import org.infrastructure.util.EntityUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 离线查询条件构造器
 * Created by xuweinan on 2016/12/14.
 *
 * @author xuweinan
 */
public class DetachedCriteriaBuilder {

    /**
     * 包含所有字段
     */
    public static final String ALL_COLUMNS = "ALL_COLUMNS";

    private Class<?> enCls;
    private DetachedCriteria deCriteria;
    private Set<String> fields = new HashSet<>();
    private Map<String, String> aliasMap = new HashMap<>();
    private PageRequest pageRequest;

    private DetachedCriteriaBuilder() {
    }

    public static DetachedCriteriaBuilder forClass(Class<?> enCls) {
        DetachedCriteriaBuilder instance = new DetachedCriteriaBuilder();
        instance.enCls = enCls;
        instance.deCriteria = DetachedCriteria.forClass(enCls);
        return instance;
    }

    /**
     * 增加分页参数
     *
     * @param page 页码(从1开始)
     * @param size 页参数
     */
    public void page(int page, int size) {
        pageRequest = new PageRequest(page - 1, size);
    }

    public DetachedCriteriaBuilder addField(String... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    /**
     * 增加查询字段别名
     */
    public DetachedCriteriaBuilder createAlias(String field, String alias) {
        aliasMap.put(field, alias);
        return this;
    }

    /**
     * 增加查询字段别名
     */
    public DetachedCriteriaBuilder createAliases(String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("别名映射参数长度必须为偶数");
        }
        for (int i = 0; i < params.length; ) {
            this.aliasMap.put(params[i], params[i + 1]);
            i += 2;
        }
        return this;
    }

    public DetachedCriteriaBuilder addCriterion(Criterion criterion) {
        deCriteria.add(criterion);
        return this;
    }

    public DetachedCriteriaBuilder orderBy(Order... ords) {
        if (null != ords)
            for (Order ord : ords) {
                deCriteria.addOrder(ord);
            }
        return this;
    }

    /**
     * 快速eq条件
     */
    public DetachedCriteriaBuilder eq(String prop, Object value) {
        this.addCriterion(Restrictions.eq(prop, value));
        return this;
    }

    /**
     * like '%%' 全模糊
     */
    public DetachedCriteriaBuilder like(String prop, String value) {
        this.addCriterion(Restrictions.like(prop, value, MatchMode.ANYWHERE));
        return this;
    }

    /**
     * 快速gt条件 >
     */
    public DetachedCriteriaBuilder gt(String prop, Object value) {
        this.addCriterion(Restrictions.gt(prop, value));
        return this;
    }

    /**
     * 快速ge条件 >=
     */
    public DetachedCriteriaBuilder gte(String prop, Object value) {
        this.addCriterion(Restrictions.ge(prop, value));
        return this;
    }

    /**
     * 快速lt条件 <
     */
    public DetachedCriteriaBuilder lt(String prop, Object value) {
        this.addCriterion(Restrictions.lt(prop, value));
        return this;
    }

    /**
     * 快速le条件 <=
     */
    public DetachedCriteriaBuilder le(String prop, Object value) {
        this.addCriterion(Restrictions.le(prop, value));
        return this;
    }

    /**
     * 附件in查询
     */
    public DetachedCriteriaBuilder in(String prop, Collection<?> value) {
        this.addCriterion(Restrictions.in(prop, value));
        return this;
    }

    /**
     * 附加in条件
     */
    public DetachedCriteriaBuilder in(String prop, Object... value) {
        this.addCriterion(Restrictions.in(prop, value));
        return this;
    }


    public Class<?> getEnCls() {
        return enCls;
    }

    /**
     * 获取离线查询条件
     *
     * @param processProjection 是否处理投影字段
     */
    public DetachedCriteria buildDeCriteria(boolean processProjection) {
        if (processProjection)
            this.processProjection();
        return deCriteria;
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    private void processProjection() {
        Set<String> fieldsList = fields;
        if (CollectionUtils.isEmpty(fieldsList) || fieldsList.contains(ALL_COLUMNS))
            fieldsList.addAll(EntityUtils.parseEntityColumns(enCls));
        fieldsList.remove(ALL_COLUMNS);
        Map<String, Field> entityJoinFields = EntityUtils.getJoinFields(enCls);
        Map<String, Set<String>> referFields = new HashMap<>();
        Set<String> queryjoinFields = new HashSet<>();
        ProjectionList projectionList = Projections.projectionList();

        for (String pf : fieldsList) {
            // 如果是对象投影，不在查询中体现，后期通过对象Id来初始化
            if (entityJoinFields.containsKey(pf)) {
                referFields.put(pf, new HashSet<>());
                continue;
            }

            projectionList.add(Property.forName(pf), aliasMap.containsKey(pf) ? aliasMap.get(pf) : pf);

            int pjFieldPtIdx = pf.indexOf('.');
            if (pjFieldPtIdx > -1) {
                String objField = pf.split("\\.")[0];
                queryjoinFields.add(objField);
                if (pf.lastIndexOf('.') == pjFieldPtIdx && referFields.containsKey(objField))
                    referFields.get(objField).add(pf);
            }
        }

        // 查询结果中需外连接的表
        queryjoinFields.addAll(referFields.keySet());
        queryjoinFields.stream().filter(jf -> !aliasMap.containsKey(jf)).forEach(jf -> deCriteria.createAlias(jf, jf, JoinType.LEFT_OUTER_JOIN));

        // 关联对象，只抓取Id值
        for (String referField : referFields.keySet()) {
            Field mapField = entityJoinFields.get(referField);
            String pkf = EntityUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            referFields.get(referField).add(fetchF);
            projectionList.add(Property.forName(fetchF), fetchF);
        }
        deCriteria.setProjection(projectionList);
    }
}