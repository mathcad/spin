package org.spin.data.query;

import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.sql.JoinType;
import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.LambdaUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.IEntity;
import org.spin.data.core.PageRequest;
import org.spin.data.util.EntityUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 离线查询条件构造器
 * <p>Created by xuweinan on 2016/12/14.</p>
 *
 * @param <T> 实体类型参数，必须具有{@link javax.persistence.Entity}注解
 * @author xuweinan
 */
public class CriteriaBuilder<T extends IEntity<?>> {

    /**
     * 包含所有字段
     */
    public static final String ALL_COLUMNS = "ALL_COLUMNS";

    private Class<T> enCls;
    private DetachedCriteria deCriteria;
    private Set<String> fields = new HashSet<>();
    private Map<String, String> aliasMap = new HashMap<>();
    private PageRequest pageRequest;

    private volatile boolean projected = false;

    private final Set<String> condJoins = new HashSet<>();

    private CriteriaBuilder() {
    }

    /**
     * 创建离线查询条件
     *
     * @param enCls 查询的实体类
     * @param <T>   实体类型
     * @param <P>   实体主键类型
     * @return {@link CriteriaBuilder}
     */
    public static <T extends IEntity<P>, P extends Serializable> CriteriaBuilder<T> forClass(Class<T> enCls) {
        CriteriaBuilder<T> instance = new CriteriaBuilder<>();
        instance.enCls = enCls;
        instance.projected = false;
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
    public CriteriaBuilder<T> page(int page, int size) {
        pageRequest = new PageRequest(page, size);
        return this;
    }

    /**
     * 增加查询字段
     *
     * @param fields 字段列表
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> addFields(String... fields) {
        Arrays.stream(fields).filter(StringUtils::isNotEmpty).forEach(this.fields::add);
        projected = false;
        return this;
    }

    /**
     * 增加查询字段
     *
     * @param fields 字段列表
     * @return {@link CriteriaBuilder}
     */
    @SuppressWarnings("unchecked")
    public CriteriaBuilder<T> addFields(Function<T, ?>... fields) {
        Arrays.stream(fields).map(CriteriaBuilder::resolveLambda).filter(StringUtils::isNotEmpty).forEach(this.fields::add);
        projected = false;
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param field 字段名
     * @param alias 别名
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> createAlias(String field, String alias) {
        aliasMap.put(field, alias);
        projected = false;
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param field 字段名
     * @param alias 别名
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> createAlias(Function<T, ?> field, String alias) {
        return createAlias(resolveLambda(field), alias);
    }

    /**
     * 设置查询字段别名
     *
     * @param params 字段与别名的映射：field1, alias1, field2, alias2...
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> createAliases(String... params) {
        if (null != params) {
            if (params.length % 2 != 0) {
                throw new IllegalArgumentException("别名映射参数长度必须为偶数");
            }
            for (int i = 0; i < params.length; i += 2) {
                this.aliasMap.put(params[i], params[i + 1]);
            }
            projected = false;
        }
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param params 字段与别名的映射：field1, alias1, field2, alias2...
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> createAliases(Map<Function<T, ?>, String> params) {
        if (null != params) {
            params.forEach((key, value) -> aliasMap.put(resolveLambda(key), value));
            projected = false;
        }
        return this;
    }

    /**
     * 追加多个查询条件
     *
     * @param criterions 条件
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> addCriterion(Criterion... criterions) {
        if (null != criterions) {
            for (Criterion ct : criterions) {
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
    public CriteriaBuilder<T> addCriterion(Iterable<Criterion> criterions) {
        if (null != criterions) {
            for (Criterion ct : criterions) {
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
    public CriteriaBuilder<T> orderBy(Order... ords) {
        if (null != ords)
            for (Order ord : ords) {
                if (null != ord)
                    deCriteria.addOrder(ord);
            }
        return this;
    }

    /**
     * 快速eq条件
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> eq(String prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.eq(prop, value));
    }

    /**
     * 快速eq条件
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> eq(Function<T, ?> prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.eq(resolveLambda(prop), value));
    }

    /**
     * 快速id equal条件
     * <p>当value为null时会被忽略</p>
     *
     * @param value id
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> idEq(Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.idEq(value));
    }

    /**
     * 快速not eq条件
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> notEq(String prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.ne(prop, value));
    }

    /**
     * 快速not eq条件
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> notEq(Function<T, ?> prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.ne(resolveLambda(prop), value));
    }

    /**
     * 快速is null条件
     *
     * @param prop 属性名
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> isNull(String prop) {
        return addCriterion(Restrictions.isNull(prop));
    }

    /**
     * 快速is null条件
     *
     * @param prop 属性名
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> isNull(Function<T, ?> prop) {
        return addCriterion(Restrictions.isNull(resolveLambda(prop)));
    }

    /**
     * 快速eq或is null条件
     * <p>当value为null时，等同于isNull条件</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> eqOrIsNull(String prop, Object value) {
        if (null == value) {
            return addCriterion(Restrictions.isNull(prop));
        }
        return addCriterion(Restrictions.eqOrIsNull(prop, value));
    }

    /**
     * 快速eq或is null条件
     * <p>当value为null时，等同于isNull条件</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> eqOrIsNull(Function<T, ?> prop, Object value) {
        if (null == value) {
            return addCriterion(Restrictions.isNull(resolveLambda(prop)));
        }
        return addCriterion(Restrictions.eqOrIsNull(resolveLambda(prop), value));
    }

    /**
     * 快速not null条件
     *
     * @param prop 属性名
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> notNull(String prop) {
        return addCriterion(Restrictions.isNotNull(prop));
    }

    /**
     * 快速not null条件
     *
     * @param prop 属性名
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> notNull(Function<T, ?> prop) {
        return addCriterion(Restrictions.isNotNull(resolveLambda(prop)));
    }

    /**
     * like '*%' 模糊
     * <p>当value为空时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> startWith(String prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return addCriterion(Restrictions.like(prop, value, MatchMode.START));
    }

    /**
     * like '*%' 模糊
     * <p>当value为空时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> startWith(Function<T, ?> prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return addCriterion(Restrictions.like(resolveLambda(prop), value, MatchMode.START));
    }

    /**
     * like '%*' 模糊
     * <p>当value为空时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> endWith(String prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return addCriterion(Restrictions.like(prop, value, MatchMode.END));
    }

    /**
     * like '%*' 模糊
     * <p>当value为空时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> endWith(Function<T, ?> prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return addCriterion(Restrictions.like(resolveLambda(prop), value, MatchMode.END));
    }

    /**
     * like '%%' 全模糊
     * <p>当value为空时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> like(String prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return addCriterion(Restrictions.like(prop, value, MatchMode.ANYWHERE));
    }

    /**
     * like '%%' 全模糊
     * <p>当value为空时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> like(Function<T, ?> prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return addCriterion(Restrictions.like(resolveLambda(prop), value, MatchMode.ANYWHERE));
    }

    /**
     * 快速gt条件 &gt;
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> gt(String prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.gt(prop, value));
    }

    /**
     * 快速gt条件 &gt;
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> gt(Function<T, ?> prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.gt(resolveLambda(prop), value));
    }

    /**
     * 快速ge条件 &gt;=
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> ge(String prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.ge(prop, value));
    }

    /**
     * 快速ge条件 &gt;=
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> ge(Function<T, ?> prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.ge(resolveLambda(prop), value));
    }

    /**
     * 快速between条件
     * <p>当low, high均为null时会被忽略，当low为空时等同于lt(high), 当high为空时等同于ge(low)</p>
     *
     * @param prop 属性名
     * @param low  下限
     * @param high 上限
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> between(String prop, Object low, Object high) {
        if (null == low && null == high) {
            return this;
        } else if (null == low) {
            return addCriterion(Restrictions.lt(prop, high));
        } else if (null == high) {
            return addCriterion(Restrictions.ge(prop, low));
        } else {
            return addCriterion(Restrictions.between(prop, low, high));
        }
    }

    /**
     * 快速between条件
     * <p>当low, high均为null时会被忽略，当low为空时等同于lt(high), 当high为空时等同于ge(low)</p>
     *
     * @param prop 属性名
     * @param low  下限
     * @param high 上限
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> between(Function<T, ?> prop, Object low, Object high) {
        if (null == low && null == high) {
            return this;
        } else if (null == low) {
            return addCriterion(Restrictions.lt(resolveLambda(prop), high));
        } else if (null == high) {
            return addCriterion(Restrictions.ge(resolveLambda(prop), low));
        } else {
            return addCriterion(Restrictions.between(resolveLambda(prop), low, high));
        }
    }

    /**
     * 快速lt条件 &lt;
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> lt(String prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.lt(prop, value));
    }

    /**
     * 快速lt条件 &lt;
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> lt(Function<T, ?> prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.lt(resolveLambda(prop), value));
    }

    /**
     * 快速le条件 &lt;=
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> le(String prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.le(prop, value));
    }

    /**
     * 快速le条件 &lt;=
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> le(Function<T, ?> prop, Object value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.le(resolveLambda(prop), value));
    }

    /**
     * 附加in查询
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> in(String prop, Collection<?> value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.in(prop, value));
    }

    /**
     * 附加in查询
     * <p>当value为null时会被忽略</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> in(Function<T, ?> prop, Collection<?> value) {
        if (null == value) {
            return this;
        }
        return addCriterion(Restrictions.in(resolveLambda(prop), value));
    }

    /**
     * 附加in条件
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> in(String prop, Object... value) {
        return addCriterion(Restrictions.in(prop, value));
    }

    /**
     * 附加in条件
     *
     * @param prop  属性名
     * @param value 属性值
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> in(Function<T, ?> prop, Object... value) {
        return addCriterion(Restrictions.in(resolveLambda(prop), value));
    }

    /**
     * 附加and条件
     *
     * @param predicates 需要and运算的条件
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> and(Criterion... predicates) {
        return addCriterion(Restrictions.and(predicates));
    }

    /**
     * 附加or条件
     *
     * @param predicates 需要or运算的条件
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> or(Criterion... predicates) {
        return addCriterion(Restrictions.or(predicates));
    }

    /**
     * 快速sql条件
     *
     * @param sql sql条件
     * @return {@link CriteriaBuilder}
     */
    public CriteriaBuilder<T> sqlRestriction(String sql) {
        return addCriterion(Restrictions.sqlRestriction(sql));
    }

    /**
     * 构造Hibernate离线查询条件
     *
     * @param useProjection 是否解析投影字段
     * @return {@link DetachedCriteria}
     */
    public DetachedCriteria buildDeCriteria(boolean useProjection) {
        if (!projected)
            processProjection(useProjection);
        return deCriteria;
    }

    public Class<T> getEnCls() {
        return enCls;
    }

    public final void setEnCls(Class<T> enCls) {
        this.enCls = enCls;
        Field criteriaField = ReflectionUtils.findField(DetachedCriteria.class, "criteria");
        ReflectionUtils.makeAccessible(criteriaField);
        try {
            Criteria criteria = (Criteria) criteriaField.get(deCriteria);
            Field classNameField = ReflectionUtils.findField(CriteriaImpl.class, "entityOrClassName");
            ReflectionUtils.makeAccessible(classNameField);
            classNameField.set(criteria, enCls.getName());
        } catch (IllegalAccessException e) {
            throw new SimplifiedException("Set query entity class error", e);
        }
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        projected = false;
        this.fields = fields;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, String> aliasMap) {
        projected = false;
        this.aliasMap = aliasMap;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    /**
     * 处理条件中用到的关联关系(尾递归优化)
     *
     * @param cts 条件列表
     */
    private void processCondAlias(Collection<Criterion> cts) {
        if (CollectionUtils.isEmpty(cts)) {
            return;
        }
        List<Criterion> ts = new LinkedList<>();
        for (Criterion ct : cts) {
            if (ct instanceof NotExpression) {
                Criterion t = BeanUtils.getFieldValue(ct, "criterion");
                ts.add(t);
            } else if (ct instanceof Junction) {
                ts.addAll(BeanUtils.getFieldValue(ct, "conditions"));
            } else if (ct instanceof LogicalExpression) {
                Criterion lhs = BeanUtils.getFieldValue(ct, "lhs");
                Criterion rhs = BeanUtils.getFieldValue(ct, "rhs");
                ts.add(lhs);
                ts.add(rhs);
            } else if (ct instanceof SubqueryExpression) {
                List<CriteriaImpl.CriterionEntry> ces = BeanUtils.getFieldValue(ct, "criteriaImpl.criterionEntries");
                for (CriteriaImpl.CriterionEntry ce : ces) {
                    ts.add(ce.getCriterion());
                }
            } else {
                String cond = BeanUtils.getFieldValue(ct, "propertyName");
                if (StringUtils.isNotEmpty(cond)) {
                    int idx = cond.lastIndexOf('.');
                    while (idx > 0) {
                        cond = cond.substring(0, idx);
                        condJoins.add(cond);
                        idx = cond.lastIndexOf('.');
                    }
                }
            }
        }
        processCondAlias(ts);
    }

    private void processCondJoin() {
        List<CriteriaImpl.CriterionEntry> ces = BeanUtils.getFieldValue(deCriteria, "impl.criterionEntries");
        List<Criterion> cts = new ArrayList<>(ces.size());
        for (CriteriaImpl.CriterionEntry ce : ces) {
            cts.add(ce.getCriterion());
        }
        processCondAlias(cts);
    }

    /**
     * 处理字段投影，设置2层甚至更多层关联的关联关系
     *
     * @param useProjection 是否使用投影
     */
    private void processProjection(boolean useProjection) {
        processCondJoin();
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

            String[] objFileds = pf.split("\\.");
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
        queryjoins.addAll(condJoins);
        queryjoins.addAll(referFields);
        queryjoins.forEach(jf -> deCriteria.createAlias(jf, jf.replaceAll("\\.", "_"), JoinType.LEFT_OUTER_JOIN));

        // 关联对象，只抓取Id值
        for (String referField : referFields) {
            Field mapField = entityJoinFields.get(referField);
            String pkf = EntityUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            projectionList.add(Property.forName(fetchF), fetchF);
        }
        if (useProjection) {
            deCriteria.setProjection(projectionList);
        }
        projected = true;
    }

    public static String resolveLambda(Function<?, ?> lambda) {
        return BeanUtils.toFieldName(LambdaUtils.resolveLambda(lambda).getImplMethodName());
    }
}
