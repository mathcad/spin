package org.spin.cloud.validation;

import org.spin.cloud.validation.constraints.IsPhone;
import org.spin.core.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<IsPhone, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    @Override
    public void initialize(IsPhone constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.isEmpty(value) || PHONE_PATTERN.matcher(value).matches();
    }
}
