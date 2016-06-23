package org.infrastructure.jpa.api;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * 通用搜索
 * 
 * @author zhou
 *
 */
@Component
public class CmdParser {
	static final Logger logger = LoggerFactory.getLogger(CmdParser.class);
	static final String SPLITOR = "__";
	
	public static class DetachedCriteriaResult{
		
		public static DetachedCriteriaResult from(DetachedCriteria dc){
			DetachedCriteriaResult dr = new DetachedCriteriaResult();
			dr.dc = dc;
			return dr;
		}
		public DetachedCriteria dc;
		public Map<String,Integer> aliasMap = new HashMap<String,Integer>();
	}
	
	/**
	 * 	PageRequest pr = new PageRequest(args.page, args.pageSize);
		DetachedCriteria dc = DetachedCriteria.forClass(SpTransOrder.class);
		dc.createAlias("CUSTOMER", "c");
		dc.createAlias("ROUTE", "r"); 
	 * 
	 * @param qValue
	 */
	public DetachedCriteriaResult getDetachedCriteria(QueryParam p,QParamHandler... handlers) throws Exception{
		DetachedCriteriaResult result = new DetachedCriteriaResult();
		try {
			Class enCls = Class.forName(p.cls);
			DetachedCriteria dc = DetachedCriteria.forClass(enCls);
			Map<String,QParamHandler> handlersMap = new HashMap<String, QParamHandler>();
			for(QParamHandler qh : handlers){
				handlersMap.put(qh.field, qh);
			}
			
			for(Object qkey : p.q.keySet()){
				String qKey = qkey.toString();
				String val =p.q.get(qKey) == null ? null :  p.q.get(qKey).toString();
				
				//值为空，忽略
				if(StringUtils.isEmpty(val))
					continue;
				
				//自定义属性处理
				if(handlersMap.containsKey(qKey)){
					handlersMap.get(qKey).appendCriteria(dc,val);
					continue;
				}
				
				//属性分隔符
				if(qKey.contains(SPLITOR)){
					//and(..or..)的结构
					if(qKey.indexOf("|") > -1){
						List<Criterion> orInners = new ArrayList<Criterion>(); 
						for(String singleKey : qKey.split("\\|")){
							logger.info(singleKey);
							Criterion ct = parseSinglePropCritetion(result, enCls, dc,singleKey, val);
							if(ct != null)
								orInners.add(ct);
						}
						
						if(orInners.size() >0)
							dc.add(Restrictions.or(orInners.toArray(new Criterion[]{})));
					}else{
						Criterion ct = parseSinglePropCritetion(result, enCls, dc,qKey, val);
						if(ct != null)
							dc.add(ct);
					}
				}
			}		
			result.dc = dc;
			return result;
		} catch (Exception e) {
			throw e;
		}		
	}

	private Criterion parseSinglePropCritetion(DetachedCriteriaResult result,
			Class enCls, DetachedCriteria dc, String qKey, String val) {
		List<String> qPath = new ArrayList<String>();
		qPath.addAll(Arrays.asList(qKey.split(SPLITOR)));
		String qOp = qPath.remove(qPath.size() -1);
		
		Object qVal = null;
		if(qOp.indexOf("in") > -1 || qOp.indexOf("notIn") > -1){
			List<Object> objList = new ArrayList<Object>();
			for(String strV : val.split(",")){
				objList.add(convertValue(enCls,qPath, strV));
			}
			qVal = objList.toArray(new Object[]{});
		} else if(qOp.indexOf("isNull") > -1 || qOp.indexOf("notNull") > -1){
			qVal = StringUtils.isNotEmpty(val) ? Boolean.valueOf(val) : false;
	    }else
			qVal = convertValue(enCls,qPath, val);
		
		//追加属性查询
		Criterion ct = addPropQuery(qPath, qOp, qVal, dc, result.aliasMap);
		return ct;
	}
	
	/**
	 * 
	 * order__desc,id__desc
	 * 
	 * @param p
	 * @return
	 */
	public Order[] getOrders(QueryParam p){
		List<Order> orders = new ArrayList<Order>();
		if(StringUtils.isNotEmpty(p.sort)){
			for(String s : p.sort.split(",")){
				String[] so = s.split(SPLITOR);
				if(so.length == 1){
					orders.add(Order.asc(s));
				}else{
					orders.add(so[1].equalsIgnoreCase("desc")? Order.desc(so[0]) : Order.asc(so[0]));
				}
			}
		}
		return orders.toArray(new Order[]{});
	}
	
	public Criterion addPropQuery(List<String> qPath,String op,Object val,DetachedCriteria dc,Map<String,Integer> aliasMap){
			
		String propName = "";
		if(qPath.size() == 2){
			String ofield = qPath.get(0);
			
			if(!aliasMap.containsKey(ofield)){
				aliasMap.put(ofield, 0);
				aliasMap.put(ofield, aliasMap.get(ofield));
				dc.createAlias(qPath.get(0), ofield,JoinType.LEFT_OUTER_JOIN);
			}
			
			propName = ofield + "." + qPath.get(1);
		}else if(qPath.size() == 1)
			propName = qPath.get(0);
		else
			throw new RuntimeException("查询字段最多2层:" + qPath);
		
		Criterion ct = this.addCt(dc,propName,op,val);
		return ct;
	}
	
	Criterion addCt(DetachedCriteria dc,String propName,String op,Object value){
		//logger.info(propName + " " + op + "  " + value);
		Criterion ct = null;
		if("eq".equals(op)){
			ct = Restrictions.eq(propName, value);
		}else if("notEq".equals(op)){
			ct = Restrictions.not(Restrictions.eq(propName, value));
		}else if("like".equals(op)){
			ct =Restrictions.like(propName, value.toString(), MatchMode.ANYWHERE);
		}else if("startwith".equalsIgnoreCase(op)){
			ct =Restrictions.like(propName, value.toString(), MatchMode.START);
		}else if("endwith".equalsIgnoreCase(op)){
			ct =Restrictions.like(propName, value.toString(), MatchMode.END);
		}else if("gt".equals(op)){
			ct =Restrictions.gt(propName, value);
		}else if("ge".equals(op)){
			ct =Restrictions.ge(propName, value);
		}else if("lt".equals(op)){
			ct =Restrictions.lt(propName, value);
		}else if("le".equals(op)){
			ct =Restrictions.le(propName, value);
		}else if("in".equals(op)){
			ct =Restrictions.in(propName, (Object[])value);
		}else if("notIn".equals(op)){
			ct =Restrictions.not(Restrictions.in(propName, (Object[])value));
		}else if("notNull".equals(op)){
			boolean need = (boolean)value;
			if(need)
				ct =Restrictions.isNotNull(propName);
		}else if("isNull".equals(op)){
			boolean need = (boolean)value;
			if(need)
				ct =Restrictions.isNull(propName);
		}else
			throw new RuntimeException(FmtUtils.format("不支持的查询条件[{0}.{1}={2}]",propName,op,value));
		
		return ct;
	}
	
	Object convertValue(Class cls,final List<String> qPath,String val){
		Class enCls = cls;
		Field field = null;
		for(String fieldName : qPath){
			field = ReflectionUtils.findField(enCls, fieldName);		
			if(field==null)
				throw new BizException(FmtUtils.format("未找到{0}字段{1}",enCls.getName(),fieldName));
			enCls = field.getType();
		}
		
		Object v = null;
		Class fieldType = field.getType();
		try{
			if(fieldType.equals(String.class)){
				v = val;
			}else if(fieldType.equals(Boolean.class)){
				v = Boolean.valueOf(val);
			}else if(fieldType.equals(boolean.class)){
				v = Boolean.valueOf(val);
			}else if(fieldType.equals(Long.class)){
				v = Long.valueOf(val);
			}else if(fieldType.equals(long.class)){
				v = Long.valueOf(val).longValue();
			}else if(fieldType.equals(Integer.class)){
				v = Integer.valueOf(val);
			}else if(fieldType.equals(int.class)){
				v = Integer.valueOf(val).intValue();
			}else if(fieldType.equals(java.sql.Date.class)){
				SimpleDateFormat sdf = FmtUtils.getDateFmt(val.length());
				if(sdf != null)
					v = sdf.parse(val);
				else
					throw new RuntimeException("不支持的日期类型值：" + val);
			}else if(fieldType.equals(Timestamp.class)){
				SimpleDateFormat sdf = FmtUtils.getDateFmt(val.length());
				if(sdf != null)
					v = new Timestamp(sdf.parse(val).getTime());
				else
					throw new RuntimeException("不支持的日期类型值：" + val);
			}else if(fieldType.equals(Date.class)){
				SimpleDateFormat sdf = FmtUtils.getDateFmt(val.length());
				if(sdf != null)
					v = sdf.parse(val);
				else
					throw new RuntimeException("不支持的日期类型值：" + val);
			}else if(fieldType.equals(java.sql.Date.class)){
				SimpleDateFormat sdf = FmtUtils.getDateFmt(val.length());
				if(sdf != null)
					v = sdf.parse(val);
				else
					throw new RuntimeException("不支持的日期类型值：" + val);
			}else if(fieldType.isEnum()){
				Double dv = Double.parseDouble(val);
				v= EnumUtils.getEnum(fieldType, dv.intValue());			
			} else if (fieldType.equals(Double.class)) {
				v = Double.valueOf(val);
			} else if (fieldType.equals(BigDecimal.class)) {
				v = new BigDecimal(val);
			}
		}catch(ParseException e){
			throw new RuntimeException(FmtUtils.format("不支持的类型{0}值{1}",fieldType.getName(),val));
		}
		return v;
	}
}
