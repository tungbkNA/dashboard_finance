package com.internal.projectmgmt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MonthYearValidator implements ConstraintValidator<MonthYear, String> {

    private static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern
            .compile("^(0[1-9]|1[0-2])/[2-9][0-9]{3}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return PATTERN.matcher(value).matches();
    }
}
