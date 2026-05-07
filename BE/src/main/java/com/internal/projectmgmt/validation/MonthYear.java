package com.internal.projectmgmt.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MonthYearValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MonthYear {

    String message() default "Tháng phải có định dạng mm/yyyy (ví dụ: 01/2026)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
