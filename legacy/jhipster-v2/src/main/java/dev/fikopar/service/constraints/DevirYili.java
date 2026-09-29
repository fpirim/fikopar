package dev.fikopar.service.constraints;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DevirYiliValidator.class)
@Documented
public @interface DevirYili {
    String message() default "{devirYili.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
