package org.infrastructure.sys;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.infrastructure.throwable.BizException;
import org.infrastructure.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于js303的申明式的验证服务
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年7月6日 下午2:30:44
 * @version V1.0
 */
public class ValidationUtils {
	static Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

	/**
	 * 检查是否符合jsr303的规范
	 * 
	 * @param dto
	 *            需校验的对象
	 * @param allField
	 *            是否校验所有字段(false有异常后立即返回)
	 * @version 1.0
	 */
	public static void check(Object dto, boolean allField) {
		if (dto == null)
			return;

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(dto);
		BizException e = new BizException();
		for (ConstraintViolation<Object> violation : violations) {
			logger.info(ObjectUtils.toString(violation.getPropertyPath()), "null");
			logger.info(violation.getMessage());
			e.addValidationMsg(violation.getPropertyPath().toString(), violation.getMessage());
			if (!allField) {
				throw e;
			}
		}

		if (e.hasValidationMsg())
			throw e;
	}
}
