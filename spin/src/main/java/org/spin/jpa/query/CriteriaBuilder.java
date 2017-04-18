package org.spin.jpa.query;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.spin.util.CollectionUtils;
import org.spin.util.EntityUtils;
import org.spin.util.StringUtils;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 离线查询条件构造器
 * <p>Created by xuweinan on 2016/12/14.</p>
 *
 * @author xuweinan
 */
public class CriteriaBuilder {

    /**
     * 包含所有字段
     */
    public static final String ALL_COLUMNS = "ALL_COLUMNS";

    private Class<?> enCls;
    private DetachedCriteria deCriteria;
    private Set<String> fields = new HashSet<>();
    private Map<String, String> aliasMap = new HashMap<>();
    private PageRequest pageRequest;

    private CriteriaBuilder() {
    }

    /**
     * 创建离线查询条件
     *
     * @param enCls 查询的实体类
     * @return {@link DetachedCriteria}
     */
    public static CriteriaBuilder forClass(Class<?> enCls) {
        CriteriaBuilder instance = new CriteriaBuilder();
        instance.enCls = enCls;
        instance.deCriteria = DetachedCriteria.forClass(enCls);
        return instance;
    }

    /**
     * 增加分页参数
     *
     * @param page 页码(从1开始)
     * @param size 页参数
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder page(int page, int size) {
        pageRequest = new PageRequest(page - 1, size);
        return this;
    }

    /**
     * 增加查询字段
     *
     * @param fields 字段列表
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder addFields(String... fields) {
        Arrays.stream(fields).filter(StringUtils::isNotEmpty).forEach(this.fields::add);
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param field 字段名
     * @param alias 别名
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder createAlias(String field, String alias) {
        aliasMap.put(field, alias);
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param params 字段与别名的映射：field1, alias1, field2, alias2...
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder createAliases(String... params) {
        if (null != params) {
            if (params.length % 2 != 0) {
                throw new IllegalArgumentException("别名映射参数长度必须为偶数");
            }
            for (int i = 0; i < params.length; ) {
                this.aliasMap.put(params[i], params[i + 1]);
                i += 2;
            }
        }
        return this;
    }

    /**
     * 追加多个查询条件
     *
     * @param criterions 条件
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder addCriterion(Criterion... criterions) {
        if (null != criterions) {
            for (Criterion ct : criterions) {
                if (null != ct)
                    deCriteria.add(ct);
            }
        }
        return this;
    }

    /**
     * 追加多个查询条件
     *
     * @param criterions 条件
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder addCriterion(Iterable<Criterion> criterions) {
        if (null != criterions) {
            for (Criterion ct : criterions) {
                if (null != ct)
                    deCriteria.add(ct);
            }
        }
        return this;
    }

    /**
     * 追加排序
     *
     * @param ords 排序
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder orderBy(Order... ords) {
        if (null != ords)
            for (Order ord : ords) {
                if (null != ord)
                    deCriteria.addOrder(ord);
            }
        return this;
    }

    /**
     * 快速eq条件
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder eq(String prop, Object value) {
        this.addCriterion(Restrictions.eq(prop, value));
        return this;
    }

    /**
     * 快速not eq条件
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder notEq(String prop, Object value) {
        this.addCriterion(Restrictions.not(Restrictions.eq(prop, value)));
        return this;
    }

    /**
     * like '*%' 模糊
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder startWith(String prop, String value) {
        this.addCriterion(Restrictions.like(prop, value, MatchMode.START));
        return this;
    }

    /**
     * like '%*' 模糊
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder endWith(String prop, String value) {
        this.addCriterion(Restrictions.like(prop, value, MatchMode.END));
        return this;
    }

    /**
     * like '%%' 全模糊
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder like(String prop, String value) {
        this.addCriterion(Restrictions.like(prop, value, MatchMode.ANYWHERE));
        return this;
    }

    /**
     * 快速gt条件 &gt;
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder gt(String prop, Object value) {
        this.addCriterion(Restrictions.gt(prop, value));
        return this;
    }

    /**
     * 快速ge条件 &gt;=
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder ge(String prop, Object value) {
        this.addCriterion(Restrictions.ge(prop, value));
        return this;
    }

    /**
     * 快速lt条件 &lt;
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder lt(String prop, Object value) {
        this.addCriterion(Restrictions.lt(prop, value));
        return this;
    }

    /**
     * 快速le条件 &lt;=
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder le(String prop, Object value) {
        this.addCriterion(Restrictions.le(prop, value));
        return this;
    }

    /**
     * 附加in查询
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder in(String prop, Collection<?> value) {
        this.addCriterion(Restrictions.in(prop, value));
        return this;
    }

    /**
     * 附加in条件
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link DetachedCriteria}
     */
    public CriteriaBuilder in(String prop, Object... value) {
        this.addCriterion(Restrictions.in(prop, value));
        return this;
    }

    /**
     * 构造Hibernate离线查询条件
     *
     * @param processProjection 是否解析投影字段
     * @return {@link DetachedCriteria}
     */
    public DetachedCriteria buildDeCriteria(boolean processProjection) {
        if (processProjection)
            this.processProjection();
        return deCriteria;
    }

    public Class<?> getEnCls() {
        return enCls;
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

            projectionList.add(Property.forName(pf), aliasMap.getOrDefault(pf, pf));

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
