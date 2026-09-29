package com.dizel.fikopar.service.constraints;

import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.service.dto.CariDevirDTO;
import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BorcAlacakValidator implements ConstraintValidator<BorcAlacak, Object> {

    private boolean validate(BigDecimal borc, BigDecimal alacak) {
        if (borc == null || alacak == null) return false;

        if (borc.equals(BigDecimal.ZERO) && alacak.equals(BigDecimal.ZERO)) return false;

        if (borc.compareTo(BigDecimal.ZERO) > 0 && alacak.compareTo(BigDecimal.ZERO) > 0) return false;

        if (borc.compareTo(BigDecimal.ZERO) < 0) return false;

        if (alacak.compareTo(BigDecimal.ZERO) < 0) return false;

        return true;
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if (obj instanceof CariDevirDTO) {
            CariDevirDTO dto = (CariDevirDTO) obj;
            return validate(dto.getBorc(), dto.getAlacak());
        }
        if (obj instanceof Cari) {
            Cari dto = (Cari) obj;
            return validate(dto.getBorc(), dto.getAlacak());
        }
        return true;
    }
}
