package org.infrastructure.jpa.api;

import com.google.gson.Gson;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.infrastructure.sys.EnumUtils;
import org.infrastructure.sys.FmtUtils;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
public class CmdParser {
    private static final Logger logger = LoggerFactory.getLogger(CmdParser.class);
    private static final String SPLITOR = "__";
    private Gson gson = new Gson();

    public static class DetachedCriteriaResult {
        public static DetachedCriteriaResult from(DetachedCriteria dc) {
            DetachedCriteriaResult dr = new DetachedCriteriaResult();
            dr.dc = dc;
            return dr;
        }

        public DetachedCriteria dc;
        public Map<String, Integer> aliasMap = new HashMap<>();
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
    public DetachedCriteriaResult parseDetachedCriteria(QueryParam p, QueryParamHandler... handlers) throws Exception {
        DetachedCriteriaResult result = new DetachedCriteriaResult();
        Class enCls = Class.forName(p.cls);
        DetachedCriteria dc = DetachedCriteria.forClass(enCls);
        Map<String, QueryParamHandler> handlersMap = new HashMap<>();
        for (QueryParamHandler qh : handlers) {
            handlersMap.put(qh.field, qh);
        }

        for (String qKey : p.q.keySet()) {
            String val = p.q.get(qKey) == null ? null : p.q.get(qKey);
            // 值为空，忽略
            if (StringUtils.isEmpty(val))
                continue;

            // 自定义属性处理
            if (handlersMap.containsKey(qKey)) {
                handlersMap.get(qKey).processCriteria(dc, val);
                continue;
            }

            // 如果没有自定义处理，采用默认处理
            // 属性分隔符
            if (qKey.contains(SPLITOR)) {
                if (qKey.contains("|")) {
                    List<Criterion> orInners = new ArrayList<>();
                    for (String singleKey : qKey.split("\\|")) {
                        if (logger.isTraceEnabled())
                            logger.trace(singleKey);
                        Criterion ct = parseSinglePropCritetion(result, enCls, dc, singleKey, val);
                        if (ct != null)
                            orInners.add(ct);
                    }

                    if (orInners.size() > 0)
                        dc.add(Restrictions.or(orInners.toArray(new Criterion[]{})));
                } else {
                    Criterion ct = parseSinglePropCritetion(result, enCls, dc, qKey, val);
                    if (ct != null)
                        dc.add(ct);
                }
            }
        }
        result.dc = dc;
        return result;
    }

    /**
     * order__desc,id__desc
     */
    public Order[] parseOrders(QueryParam p) {
        List<Order> orders = new ArrayList<>();
        if (StringUtils.isNotEmpty(p.sort)) {
            for (String s : p.sort.split(",")) {
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

    private Criterion parseSinglePropCritetion(DetachedCriteriaResult result, Class enCls, DetachedCriteria dc, String qKey, String val) {
        List<String> qPath = new ArrayList<>();
        qPath.addAll(Arrays.asList(qKey.split(SPLITOR)));
        String qOp = qPath.remove(qPath.size() - 1);

        //去除空格
        if (StringUtils.isNotEmpty(val)) {
            val = StringUtils.trimWhitespace(val);
        }

        Object qVal = null;
        if (qOp.contains("in") || qOp.contains("notIn")) {
            List<Object> objList = new ArrayList<>();
            for (String strV : val.split(",")) {
                objList.add(convertValue(enCls, qPath, strV));
            }
            qVal = objList.toArray(new Object[]{});
        } else if (qOp.contains("isNull") || qOp.contains("notNull")) {
            qVal = StringUtils.isNotEmpty(val) ? Boolean.valueOf(val) : false;
        } else
            qVal = convertValue(enCls, qPath, val);

        //追加属性查询
        return addPropQuery(qPath, qOp, qVal, dc, result.aliasMap);
    }

    // 将String类型的val转换为实体属性的类型
    private Object convertValue(Class cls, final List<String> qPath, String val) {
        Class enCls = cls;
        Field field = null;
        for (String fieldName : qPath) {
            field = ReflectionUtils.findField(enCls, fieldName);
            if (field == null)
                throw new BizException(FmtUtils.format("未找到{0}字段{1}", enCls.getName(), fieldName));
            enCls = field.getType();
        }

        Object v = null;
        assert field != null;
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
                SimpleDateFormat sdf = FmtUtils.getDateFmt(val.length());
                if (sdf != null)
                    v = sdf.parse(val);
                else
                    throw new RuntimeException("不支持的日期类型值：" + val);
            } else if (fieldType.equals(Timestamp.class)) {
                SimpleDateFormat sdf = FmtUtils.getDateFmt(val.length());
                if (sdf != null)
                    v = new Timestamp(sdf.parse(val).getTime());
                else
                    throw new RuntimeException("不支持的日期类型值：" + val);
            } else if (fieldType.isEnum()) {
                Integer dv = Integer.valueOf(val);
                v = EnumUtils.getEnum(fieldType, dv);
            } else {
                v = this.gson.fromJson(val, fieldType);
            }
        } catch (Exception e) {
            throw new RuntimeException(FmtUtils.format("不支持的类型\"{0}\", 值{1}", fieldType.getName(), val));
        }
        return v;
    }

    private Criterion addPropQuery(List<String> qPath, String op, Object val, DetachedCriteria dc, Map<String, Integer> aliasMap) {
        String propName = "";
        if (qPath.size() == 2) {
            String ofield = qPath.get(0);

            if (!aliasMap.containsKey(ofield)) {
                aliasMap.put(ofield, 0);
                dc.createAlias(qPath.get(0), ofield, JoinType.LEFT_OUTER_JOIN);
            }

            propName = ofield + "." + qPath.get(1);
        } else if (qPath.size() == 1)
            propName = qPath.get(0);
        else
            throw new RuntimeException("查询字段最多2层:" + qPath);

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
        if ("eq".equals(op)) {
            ct = Restrictions.eq(propName, value);
        } else if ("notEq".equals(op)) {
            ct = Restrictions.not(Restrictions.eq(propName, value));
        } else if ("like".equals(op)) {
            ct = Restrictions.like(propName, StringUtils.trimWhitespace(value.toString()), MatchMode.ANYWHERE);
        } else if ("startwith".equalsIgnoreCase(op)) {
            ct = Restrictions.like(propName, StringUtils.trimWhitespace(value.toString()), MatchMode.START);
        } else if ("endwith".equalsIgnoreCase(op)) {
            ct = Restrictions.like(propName, StringUtils.trimWhitespace(value.toString()), MatchMode.END);
        } else if ("gt".equals(op)) {
            ct = Restrictions.gt(propName, value);
        } else if ("ge".equals(op)) {
            ct = Restrictions.ge(propName, value);
        } else if ("lt".equals(op)) {
            ct = Restrictions.lt(propName, value);
        } else if ("le".equals(op)) {
            ct = Restrictions.le(propName, value);
        } else if ("in".equals(op)) {
            ct = Restrictions.in(propName, (Object[]) value);
        } else if ("notIn".equals(op)) {
            ct = Restrictions.not(Restrictions.in(propName, (Object[]) value));
        } else if ("notNull".equals(op)) {
            boolean need = (boolean) value;
            if (need)
                ct = Restrictions.isNotNull(propName);
        } else if ("isNull".equals(op)) {
            boolean need = (boolean) value;
            if (need)
                ct = Restrictions.isNull(propName);
        } else
            throw new RuntimeException(FmtUtils.format("不支持的查询条件[{0}.{1}={2}]", propName, op, value));
        return ct;
    }
}
