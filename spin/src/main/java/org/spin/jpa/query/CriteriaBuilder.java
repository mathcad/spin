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
import org.spin.jpa.core.IEntity;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.EntityUtils;
import org.spin.core.util.StringUtils;
import org.spin.jpa.core.PageRequest;

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

    private Class<? extends IEntity> enCls;
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
     * @return {@link CriteriaBuilder}
     */
    public static CriteriaBuilder forClass(Class<? extends IEntity> enCls) {
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
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder page(int page, int size) {
        pageRequest = new PageRequest(page - 1, size);
        return this;
    }

    /**
     * 增加查询字段
     *
     * @param fields 字段列表
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder createAlias(String field, String alias) {
        aliasMap.put(field, alias);
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param params 字段与别名的映射：field1, alias1, field2, alias2...
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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
     * @return {@link CriteriaBuilder}
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

    /**
     * 处理字段投影，设置2层甚至更多层关联的关联关系
     */
    private void processProjection() {
        if (CollectionUtils.isEmpty(fields) || fields.contains(ALL_COLUMNS)) {
            fields.addAll(EntityUtils.parseEntityColumns(enCls));
        }
        fields.remove(ALL_COLUMNS);

        // 该实体的所有n->1关联属性
        Map<String, Field> entityJoinFields = EntityUtils.getJoinFields(enCls);

        // 关联属性需要引用的子属性
        Set<String> referFields = new HashSet<>();

        // 查询构造中需要用到的所有关联
        Set<String> queryjoins = new HashSet<>();

        // 投影列表
        ProjectionList projectionList = Projections.projectionList();

        for (String pf : fields) {
            // 如果是对象投影，不在查询中体现，后期通过对象Id来初始化
            if (entityJoinFields.containsKey(pf)) {
                referFields.add(pf);
                continue;
            }

            String tmp = pf;
            int ldotIdx = tmp.lastIndexOf('.'), di = tmp.indexOf('.');
            while (di != ldotIdx) {
                tmp = tmp.replaceFirst("\\.", "_");
                di = tmp.indexOf('.');
            }
            projectionList.add(Property.forName(tmp), aliasMap.getOrDefault(pf, pf));

            String objFileds[] = pf.split("\\.");
            if (objFileds.length > 1) {
                for (int idx = 0; idx != objFileds.length - 1; ++idx) {
                    StringBuilder join = new StringBuilder();
                    for (int i = 0; i <= idx; ++i) {
                        join.append(objFileds[i]).append(".");
                    }
                    queryjoins.add(join.substring(0, join.length() - 1));
                }
            }
        }

        // 查询结果中需外连接的表
        queryjoins.addAll(referFields);
        queryjoins.forEach(jf -> deCriteria.createAlias(jf, jf.replaceAll("\\.", "_"), JoinType.LEFT_OUTER_JOIN));

        // 关联对象，只抓取Id值
        for (String referField : referFields) {
            Field mapField = entityJoinFields.get(referField);
            String pkf = EntityUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            projectionList.add(Property.forName(fetchF), fetchF);
        }
        deCriteria.setProjection(projectionList);
    }
}
