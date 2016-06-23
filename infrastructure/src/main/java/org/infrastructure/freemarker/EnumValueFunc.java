package org.infrastructure.freemarker;

import java.lang.reflect.Field;
import java.util.List;

import org.infrastructure.jpa.core.annotations.UserEnum;
import org.infrastructure.throwable.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Enum值转名称 生成sql段 case when ...
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年3月30日 下午2:39:30
 * @version V1.0
 */
public class EnumValueFunc implements TemplateMethodModelEx {
	final static Logger logger = LoggerFactory.getLogger(EnumValueFunc.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arguments) throws TemplateModelException {

		if (arguments.size() != 3)
			throw new BizException("ValidValue 参数个数不正确（需1,2个）");

		String enumName = arguments.get(0).toString();
		String field = arguments.get(1).toString();
		String asField = arguments.get(2).toString();

		StringBuffer sb = new StringBuffer();
		sb.append("case");
		try {
			Class<?> cls = Class.forName(enumName);
			if (cls.isEnum()) {
				Field vField = null;
				try {
					vField = cls.getDeclaredField("value");
					ReflectionUtils.makeAccessible(vField);
				} catch (Exception e) {
					logger.error("Enum" + cls + "未申明value字段", e);
				}

				// 初始化转化
				if (cls.getAnnotation(UserEnum.class) != null && vField != null) {
					for (Object o : cls.getEnumConstants()) {
						String name = o.toString();
						int value = Integer.valueOf(vField.get(o).toString());
						sb.append(" when ").append(field).append("=").append(value).append(" then '").append(name)
								.append("'");
					}
				}
			}
		} catch (Exception e) {
			throw new BizException("解析枚举出错" + enumName);
		}

		sb.append("end as ").append(asField);

		return sb.toString();
	}

}
