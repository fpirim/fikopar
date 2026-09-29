package com.dizel.fikopar.service.constraints;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BorcAlacakValidator.class)
@Documented
public @interface BorcAlacak {
    String message() default "{borcAlacak.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Fields to validate against null.
     */
    String[] fields() default {};
}
