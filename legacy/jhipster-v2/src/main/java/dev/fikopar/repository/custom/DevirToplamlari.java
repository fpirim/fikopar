package dev.fikopar.repository.custom;

import dev.fikopar.domain.enumeration.DepoTipi;
import java.math.BigDecimal;

public class DevirToplamlari {

    private final DepoTipi depoTipi;
    private final BigDecimal borc;
    private final BigDecimal alacak;

    public DevirToplamlari(DepoTipi depoTipi, BigDecimal borc, BigDecimal alacak) {
        this.depoTipi = depoTipi;
        this.borc = borc;
        this.alacak = alacak;
    }

    public DepoTipi getDepoTipi() {
        return depoTipi;
    }

    public BigDecimal getBorc() {
        return borc;
    }

    public BigDecimal getAlacak() {
        return alacak;
    }
}
