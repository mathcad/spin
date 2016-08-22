package org.infrastructure.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    @Override
    public void initialize(IsMobile arg0) {

    }

    @Override
    public boolean isValid(String mobile, ConstraintValidatorContext constraintContext) {
        if (mobile == null) {
            return false;
        }

        String regex = "^13[0-9]{9}|15[012356789][0-9]{8}|18[0123456789][0-9]{8}|147[0-9]{8}$";
        return mobile.matches(regex);
    }
}
