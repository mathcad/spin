package org.infrastructure.jpa.query;

import com.google.gson.Gson;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.infrastructure.jpa.core.IEntity;
import org.infrastructure.throwable.SQLException;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.DateUtils;
import org.infrastructure.util.EnumUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sql解析器
 *
 * @author xuweinan
 */
@Component
public class QueryParamParser {
    private static final Logger logger = LoggerFactory.getLogger(QueryParamParser.class);
    private static final String SPLITOR = "__";
    private Gson gson = new Gson();

    /**
     * 将通用查询参数转换为DetachedCriteria
     * <pre>
     * PageRequest pr = new PageRequest(args.page, args.pageSize);
     * DetachedCriteria deCriteria = DetachedCriteria.forClass(SpTransOrder.class);
     * deCriteria.createAlias("CUSTOMER", "c");
     * deCriteria.createAlias("ROUTE", "r");
     * </pre>
     */
    public DetachedCriteriaBag parseDetachedCriteria(QueryParam p, QueryParamHandler... handlers) throws ClassNotFoundException {
        // 解析查询实体类型
        if (StringUtils.isEmpty(p.getCls()))
            throw new SimplifiedException("未指定查询实体");
        Class<?> enCls = Class.forName(p.getCls());
        if (!IEntity.class.isAssignableFrom(enCls) || null == enCls.getAnnotation(Entity.class))
            throw new SimplifiedException(p.getCls() + " is not an Entity Class");
        DetachedCriteriaBag result = new DetachedCriteriaBag();
        result.setEnCls(enCls);

        // 设置字段别名
        result.setAliasMap(p.getAliasMap());

        // 自定义字段处理handler
        Map<String, QueryParamHandler> handlersMap = new HashMap<>();
        for (QueryParamHandler qh : handlers) {
            handlersMap.put(qh.getField(), qh);
        }

        // 处理查询条件
        for (Map.Entry<String, String> entry : p.getConditions().entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            // 值为空，忽略
            if (StringUtils.isEmpty(val))
                continue;

            // 自定义属性处理
            if (handlersMap.containsKey(key)) {
                handlersMap.get(entry.getKey()).processCriteria(result.getDeCriteria(), val);
                continue;
            }

            // 如果没有自定义处理，采用默认处理
            if (key.contains(SPLITOR)) {
                if (key.contains("|")) {
                    List<Criterion> orInners = new ArrayList<>();
                    for (String singleKey : key.split("\\|")) {
                        if (logger.isTraceEnabled())
                            logger.trace(singleKey);
                        Criterion ct = parseSinglePropCritetion(result, enCls, singleKey, val);
                        if (ct != null)
                            orInners.add(ct);
                    }

                    if (!orInners.isEmpty())
                        result.getDeCriteria().add(Restrictions.or(orInners.toArray(new Criterion[]{})));
                } else {
                    Criterion ct = parseSinglePropCritetion(result, enCls, key, val);
                    if (ct != null)
                        result.getDeCriteria().add(ct);
                }
            }
        }

        // 处理排序
        this.parseOrders(p).forEach(result.getDeCriteria()::addOrder);

        // 处理分页
        if (null != p.getPageSize())
            result.setPageRequest(new PageRequest(p.getPageIdx() - 1, p.getPageSize()));
        return result;
    }

    /**
     * 处理排序字段
     * order__desc,id__desc
     */
    public List<Order> parseOrders(QueryParam p) {
        List<Order> orders = new ArrayList<>();
        if (StringUtils.isNotEmpty(p.getSort())) {
            String[] sorts = p.getSort().split(",");
            for (String sort : sorts) {
                String[] t = sort.split(SPLITOR);
                if (t.length == 1 || "asc".equalsIgnoreCase(t[1]))
                    orders.add(Order.asc(t[0]));
                else if ("desc".equalsIgnoreCase(t[1]))
                    orders.add(Order.desc(t[0]));
                else
                    throw new SQLException(SQLException.UNKNOW_MAPPER_SQL_TYPE, "查询参数存在非法的排序字段: [" + sort + "]");
            }
        }
        return orders;
    }

    private Criterion parseSinglePropCritetion(DetachedCriteriaBag result, Class enCls, String qKey, String val) {
        List<String> qPath = new ArrayList<>(Arrays.asList(qKey.split(SPLITOR)));
        String qOp = qPath.remove(qPath.size() - 1);

        //去除空格
        if (StringUtils.isNotEmpty(val)) {
            val = StringUtils.trimWhitespace(val);
        }

        Object qVal;
        if (qOp.contains("in") || qOp.contains("notIn")) {
            List<Object> objList = new ArrayList<>();
            for (String strV : val.split(",")) {
                objList.add(convertValue(enCls, qPath, strV));
            }
            qVal = objList.toArray(new Object[]{});
        } else if (qOp.contains("isNull") || qOp.contains("notNull")) {
            qVal = StringUtils.isNotEmpty(val) ? Boolean.valueOf(val) : true;
        } else
            qVal = convertValue(enCls, qPath, val);

        //追加属性查询
        return addPropQuery(qPath, qOp, qVal, result);
    }

    // 将String类型的val转换为实体属性的类型
    private Object convertValue(Class cls, final List<String> qPath, String val) {
        Field field;
        field = ReflectionUtils.findField(cls, qPath.get(0));
        if (field == null)
            throw new SimplifiedException(StringUtils.format("未找到{0}字段{1}", cls.getName(), qPath.get(0)));
        Object v;
        Class<?> fieldType = field.getType();
        try {
            if (fieldType.equals(String.class)) {
                v = val;
            } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                v = Boolean.valueOf(val);
            } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                v = Long.valueOf(val);
            } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                v = Integer.valueOf(val);
            } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                v = Float.valueOf(val);
            } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                v = Double.valueOf(val);
            } else if (fieldType.equals(BigDecimal.class)) {
                v = new BigDecimal(val);
            } else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                v = Byte.valueOf(val);
            } else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                v = val.charAt(0);
            } else if (fieldType.equals(java.sql.Date.class) || fieldType.equals(Date.class)) {
                v = DateUtils.parseDate(val);
            } else if (fieldType.equals(Timestamp.class)) {
                v = new Timestamp(DateUtils.parseDate(val).getTime());
            } else if (fieldType.isEnum()) {
                //noinspection unchecked
                v = EnumUtils.getEnum((Class<Enum>) fieldType, Integer.valueOf(val));
            } else {
                v = this.gson.fromJson(val, fieldType);
            }
        } catch (Exception e) {
            throw new SimplifiedException(StringUtils.format("不支持的类型\"{0}\", 值{1}", fieldType.getName(), val));
        }
        return v;
    }

    private Criterion addPropQuery(List<String> qPath, String op, Object val, DetachedCriteriaBag dr) {
        String propName;
        if (qPath.size() == 2) {
            String ofield = qPath.get(0);
            if (!dr.getAliasMap().containsKey(ofield)) {
                dr.getAliasMap().put(ofield, ofield);
                dr.getDeCriteria().createAlias(qPath.get(0), ofield, JoinType.LEFT_OUTER_JOIN);
            }
            propName = ofield + "." + qPath.get(1);
        } else if (qPath.size() == 1)
            propName = qPath.get(0);
        else
            throw new SimplifiedException("查询字段最多2层:" + qPath);
        return this.createCriterion(propName, op, val);
    }

    /**
     * 创建Criterion
     *
     * @param propName 条件字段
     * @param op       操作符
     * @param value    条件值
     * @return Criterion实例
     */
    private Criterion createCriterion(String propName, String op, Object value) {
        Criterion ct = null;
        switch (op) {
            case "eq":
                ct = Restrictions.eq(propName, value);
                break;
            case "notEq":
                ct = Restrictions.not(Restrictions.eq(propName, value));
                break;
            case "like":
                ct = Restrictions.like(propName, StringUtils.trimWhitespace(value.toString()), MatchMode.ANYWHERE);
                break;
            case "startwith":
                ct = Restrictions.like(propName, StringUtils.trimWhitespace(value.toString()), MatchMode.START);
                break;
            case "endwith":
                ct = Restrictions.like(propName, StringUtils.trimWhitespace(value.toString()), MatchMode.END);
                break;
            case "gt":
                ct = Restrictions.gt(propName, value);
                break;
            case "ge":
                ct = Restrictions.ge(propName, value);
                break;
            case "lt":
                ct = Restrictions.lt(propName, value);
                break;
            case "le":
                ct = Restrictions.le(propName, value);
                break;
            case "in":
                ct = Restrictions.in(propName, (Object[]) value);
                break;
            case "notIn":
                ct = Restrictions.not(Restrictions.in(propName, (Object[]) value));
                break;
            case "notNull":
                if ((boolean) value)
                    ct = Restrictions.isNotNull(propName);
                break;
            case "isNull":
                if ((boolean) value)
                    ct = Restrictions.isNull(propName);
                break;
            default:
                throw new SQLException(SQLException.UNKNOW_MAPPER_SQL_TYPE, StringUtils.format("不支持的查询条件[{0}.{1}={2}]", propName, op, value));
        }
        return ct;
    }
}