package org.infrastructure.freemarker;

import java.util.List;

import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * 用来判断标签参数值，是否有效
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年3月30日 下午2:39:30
 * @version V1.0
 */
public class ValidValueFunc implements TemplateMethodModelEx {
	final static Logger logger = LoggerFactory.getLogger(ValidValueFunc.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean valid = false;
		Object o = null;
		if (arguments.get(0) == null) {
			o = null;
		} else if (arguments.get(0) instanceof TemplateBooleanModel) {
			o = ((TemplateBooleanModel) arguments.get(0)).getAsBoolean();
		} else if (arguments.get(0) instanceof SimpleScalar) {
			o = ((SimpleScalar) arguments.get(0)).getAsString();
		} else
			o = arguments.get(0).toString();

		if (arguments.size() == 1) {
			if (o == null)
				valid = false;
			else if (o instanceof Boolean) {
				valid = (Boolean) o;
			} else if (o instanceof String) {
				valid = StringUtils.isNotEmpty((String) o);
			} else
				valid = true;
		} else if (arguments.size() == 2) {
			String o1 = arguments.get(1) == null ? null : arguments.get(1).toString();
			if (o == null)
				valid = o1 == null;
			else {
				valid = o1.equals(o);
			}
		} else
			throw new BizException("ValidValue 参数个数不正确（需1,2个）");

		return valid;
	}

}
