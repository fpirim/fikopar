package dev.fikopar.service.constraints;

import java.util.Calendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DevirYiliValidator implements ConstraintValidator<DevirYili, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && value <= Calendar.getInstance().get(Calendar.YEAR) && value >= 2000;
    }
}
