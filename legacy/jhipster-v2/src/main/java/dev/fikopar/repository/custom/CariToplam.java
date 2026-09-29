package dev.fikopar.repository.custom;

import dev.fikopar.domain.Sicil;
import java.math.BigDecimal;

public class CariToplam {

    private final Long id;
    private final String hesapno;
    private final String hesapadi;
    private final String ilce;
    private final String sehir;
    private final BigDecimal borc;
    private final BigDecimal alacak;

    public CariToplam(Long id, String hesapno, String hesapadi, String ilce, String sehir, BigDecimal borc, BigDecimal alacak) {
        this.id = id;
        this.hesapno = hesapno;
        this.hesapadi = hesapadi;
        this.ilce = ilce;
        this.sehir = sehir;
        this.borc = borc;
        this.alacak = alacak;
    }

    public Long getId() {
        return id;
    }

    public String getHesapno() {
        return hesapno;
    }

    public String getHesapadi() {
        return hesapadi;
    }

    public String getIlce() {
        return ilce;
    }

    public String getSehir() {
        return sehir;
    }

    public BigDecimal getBorc() {
        return borc;
    }

    public BigDecimal getAlacak() {
        return alacak;
    }
}
