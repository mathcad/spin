package org.spin.cloud.validation.constraints;

import org.spin.cloud.validation.PhoneValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 手机号码校验
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */

@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsPhone {

    String message() default "手机号格式不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
