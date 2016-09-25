package org.infrastructure.jpa.api;

import com.google.gson.Gson;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.infrastructure.jpa.core.IEntity;
import org.infrastructure.util.EnumUtils;
import org.infrastructure.util.DateUtils;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

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

    public static class DetachedCriteriaResult {
        public DetachedCriteria dc;
        public Map<String, Integer> aliasMap = new HashMap<>();

        public static DetachedCriteriaResult from(DetachedCriteria dc) {
            DetachedCriteriaResult dr = new DetachedCriteriaResult();
            dr.dc = dc;
            return dr;
        }
    }

    /**
     * 将通用查询参数转换为DetachedCriteria
     * <pre>
     * PageRequest pr = new PageRequest(args.page, args.pageSize);
     * DetachedCriteria dc = DetachedCriteria.forClass(SpTransOrder.class);
     * dc.createAlias("CUSTOMER", "c");
     * dc.createAlias("ROUTE", "r");
     * </pre>
     */
    public DetachedCriteriaResult parseDetachedCriteria(QueryParam p, QueryParamHandler... handlers) throws ClassNotFoundException {
        Class enCls = Class.forName(p.getCls());
        if (!IEntity.class.isAssignableFrom(enCls))
            throw new ClassNotFoundException(p.getCls() + " is not an Entity Class");
        DetachedCriteriaResult result = new DetachedCriteriaResult();
        result.dc = DetachedCriteria.forClass(enCls);
        Map<String, QueryParamHandler> handlersMap = new HashMap<>();
        if (handlers != null)
            for (QueryParamHandler qh : handlers) {
                handlersMap.put(qh.getField(), qh);
            }

        for (Map.Entry<String, String> entry : p.getConditions().entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            // 值为空，忽略
            if (StringUtils.isEmpty(val))
                continue;

            // 自定义属性处理
            if (handlersMap.containsKey(key)) {
                handlersMap.get(entry.getKey()).processCriteria(result.dc, val);
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

                    if (orInners.size() > 0)
                        result.dc.add(Restrictions.or(orInners.toArray(new Criterion[]{})));
                } else {
                    Criterion ct = parseSinglePropCritetion(result, enCls, key, val);
                    if (ct != null)
                        result.dc.add(ct);
                }
            }
        }
        return result;
    }

    /**
     * order__desc,id__desc
     */
    public Order[] parseOrders(QueryParam p) {
        List<Order> orders = new ArrayList<>();
        if (StringUtils.isNotEmpty(p.getSort())) {
            for (String s : p.getSort().split(",")) {
                String[] so = s.split(SPLITOR);
                if (so.length == 1) {
                    orders.add(Order.asc(s));
                } else {
                    orders.add(so[1].equalsIgnoreCase("desc") ? Order.desc(so[0]) : Order.asc(so[0]));
                }
            }
        }
        return orders.toArray(new Order[]{});
    }

    private Criterion parseSinglePropCritetion(DetachedCriteriaResult result, Class enCls, String qKey, String val) {
        List<String> qPath = Arrays.asList(qKey.split(SPLITOR));
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
        Class fieldType = field.getType();
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
                v = EnumUtils.getEnum(fieldType, Integer.valueOf(val));
            } else {
                v = this.gson.fromJson(val, fieldType);
            }
        } catch (Exception e) {
            throw new SimplifiedException(StringUtils.format("不支持的类型\"{0}\", 值{1}", fieldType.getName(), val));
        }
        return v;
    }

    private Criterion addPropQuery(List<String> qPath, String op, Object val, DetachedCriteriaResult dr) {
        String propName;
        if (qPath.size() == 2) {
            String ofield = qPath.get(0);
            if (!dr.aliasMap.containsKey(ofield)) {
                dr.aliasMap.put(ofield, 0);
                dr.dc.createAlias(qPath.get(0), ofield, JoinType.LEFT_OUTER_JOIN);
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
                throw new SimplifiedException(StringUtils.format("不支持的查询条件[{0}.{1}={2}]", propName, op, value));
        }
        return ct;
    }
}