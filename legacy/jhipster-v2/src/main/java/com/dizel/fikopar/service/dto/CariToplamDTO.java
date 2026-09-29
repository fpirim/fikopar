package com.dizel.fikopar.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class CariToplamDTO implements Serializable {

    private Long id;
    private String hesapno;
    private String hesapadi;
    private String ilce;
    private String sehir;
    private BigDecimal borc;
    private BigDecimal alacak;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHesapno() {
        return hesapno;
    }

    public void setHesapno(String hesapno) {
        this.hesapno = hesapno;
    }

    public String getHesapadi() {
        return hesapadi;
    }

    public void setHesapadi(String hesapadi) {
        this.hesapadi = hesapadi;
    }

    public String getIlce() {
        return ilce;
    }

    public void setIlce(String ilce) {
        this.ilce = ilce;
    }

    public String getSehir() {
        return sehir;
    }

    public void setSehir(String sehir) {
        this.sehir = sehir;
    }

    public BigDecimal getBorc() {
        return borc;
    }

    public void setBorc(BigDecimal borc) {
        this.borc = borc;
    }

    public BigDecimal getAlacak() {
        return alacak;
    }

    public void setAlacak(BigDecimal alacak) {
        this.alacak = alacak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CariToplamDTO)) {
            return false;
        }

        CariToplamDTO cariToplamDTO = (CariToplamDTO) o;
        if (cariToplamDTO.getId() == null) {
            return false;
        }
        return Objects.equals(this.id, cariToplamDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return (
            "CariToplamDTO{" +
            "id=" +
            getId() +
            ", hesapno='" +
            getHesapno() +
            "'" +
            ", hesapadi='" +
            getHesapadi() +
            "'" +
            ", ilce='" +
            getIlce() +
            "'" +
            ", sehir='" +
            getSehir() +
            "'" +
            ", borc=" +
            getBorc() +
            ", alacak=" +
            getAlacak() +
            "}"
        );
    }
}
