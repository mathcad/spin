package org.spin.cloud.validation;

import org.spin.cloud.validation.constraints.IsIdCard;
import org.spin.core.util.IdCardUtils;
import org.spin.core.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IdCardValidator implements ConstraintValidator<IsIdCard, String> {

    private boolean allowOld;

    @Override
    public void initialize(IsIdCard constraintAnnotation) {
        allowOld = constraintAnnotation.allowOld();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.isEmpty(value) || IdCardUtils.isValid18(value) || (allowOld && IdCardUtils.isValid15(value));
    }
}
