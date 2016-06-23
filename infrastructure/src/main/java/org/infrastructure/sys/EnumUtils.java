package org.infrastructure.sys;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infrastructure.jpa.core.annotations.UserEnum;
import org.infrastructure.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * 通用的枚举类型转换
* @author zhou
* @contact 电话: 18963752887, QQ: 251915460
* @create 2015年3月17日 下午3:45:36 
* @version V1.0
 */
public class EnumUtils {
	static Logger logger = LoggerFactory.getLogger(EnumUtils.class);
	
	/**
	 * 通过枚举文本字段，获得枚举类型的常量
	 * 
	 * @param enumCls 枚举类型
	 * @param text 枚举文本
	 * @return
	 * @throws Exception 转换错误
	 */
	public static Enum getEnum(Class enumCls, String text)  {
		try {
			for(Object o : enumCls.getEnumConstants()){
				if(o.toString().equals(text)){
					return (Enum)o;
				}
			}
		} catch (Exception e) {
			logger.error("枚举转换失败",e);
		}
		
		return null;
	}
	
	/**
	 * 通过value字段值，获得枚举类型的常量
	 * 
	 * @param enumCls
	 * @param value
	 * @return
	 * @throws Exception 转换错误
	 */
	public static Enum<?> getEnum(Class<?> enumCls, int value, String... field) {
		try {
			Field valueField = enumCls.getDeclaredField("value");
			ReflectionUtils.makeAccessible(valueField);
			for (Object o : enumCls.getEnumConstants()) {
				int fVal = (Integer) ReflectionUtils.getField(valueField, o);
				if (value == fVal) {
					return (Enum<?>) o;
				}
			}

		} catch (Exception e) {
			logger.error("枚举转换失败", e);
		}

		return null;
	}
	
	/**
	 * 从Map中获取int类型的Value再转换为实体Enum
	 * 
	 * @param enumCls
	 * @param map
	 * @param key
	 * @return
	* @version 1.0
	 */
	public static Enum<?> getByValue(Class<Enum<?>> enumCls,Map map,String key){
		int v = HashUtils.getIntegerValue(map, key);
		return getEnum(enumCls,v);
	}
	
	/**
	 * 获得Enum的Value值
	 * 
	 * @param enumClass enum类型
	 * @param enumValue
	 * @return
	 */
	public static int getEnumValue(Class enumClass,Enum enumValue){
		if(enumClass == null)
			enumClass = enumValue.getClass();
		
		Field vField = null;
		try {
			vField = enumClass.getDeclaredField("value");
			ReflectionUtils.makeAccessible(vField);
			
		}catch(Exception e){
			throw new RuntimeException("Enum" + enumClass +"未申明value字段",e);
		}
		
		int value;
		try {
			value = Integer.valueOf(vField.get(enumValue).toString());
		} catch (Exception e) {
			throw new RuntimeException("Enum" + enumClass +"获取value失败",e);
		}
		
		return value;
		
	}
	
	/**
	 * 解析所有枚举
	 * 
	 * @param basePkg
	 * @return
	 * @throws Exception
	* @version 1.0
	 */
	public static HashMap<String, List<HashMap>> parseEnums(String basePkg) throws Exception {
		List<String> clsList = PackageUtils.getClassName(basePkg);
		HashMap<String,List<HashMap>> enumsMap = new HashMap<String,List<HashMap>>();
		for(String clz : clsList){
			Class cls = Class.forName(clz);
			if(cls.isEnum()){
				List<HashMap> valueList = new ArrayList<HashMap>();
				//value字段
				Field vField = null;
				try{
					vField = cls.getDeclaredField("value");
					ReflectionUtils.makeAccessible(vField);
				}catch(Exception e){
					logger.error("Enum" + cls +"未申明value字段",e);
				}
				
				//取value值
				if(cls.getAnnotation(UserEnum.class) != null && vField != null){
					for(Object o : cls.getEnumConstants()){
						String name = o.toString();
						int value = Integer.valueOf(vField.get(o).toString());
						HashMap m = new HashMap();
						m.put("name", name);
						m.put("value", value);
						valueList.add(m);
					}
					enumsMap.put(cls.getSimpleName(),valueList);
				}
			}
		}
		
		return enumsMap;
	}
	
}
