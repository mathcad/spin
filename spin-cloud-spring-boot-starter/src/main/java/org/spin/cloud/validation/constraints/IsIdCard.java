package org.spin.cloud.validation.constraints;

import org.spin.cloud.validation.IdCardValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 身份证号码校验
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */

@Constraint(validatedBy = IdCardValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsIdCard {

    /**
     * 是否允许旧的15位身份证号
     *
     * @return true or false
     */
    boolean allowOld() default false;

    String message() default "身份证号码不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
