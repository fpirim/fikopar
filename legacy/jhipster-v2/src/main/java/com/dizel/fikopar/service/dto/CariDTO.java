package com.dizel.fikopar.service.dto;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.service.constraints.BorcAlacak;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.dizel.fikopar.domain.Cari} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
@BorcAlacak
public class CariDTO implements Serializable {

    private Long id;

    private EvrakTipi tipi;

    private String evrakno;

    private DepoTipi depotipi;

    private LocalDate tarih;

    private LocalDate vade;

    private Integer gun;

    private String aciklama;

    private BigDecimal borc;

    private BigDecimal alacak;

    private SicilDTO sicil;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvrakTipi getTipi() {
        return tipi;
    }

    public void setTipi(EvrakTipi tipi) {
        this.tipi = tipi;
    }

    public String getEvrakno() {
        return evrakno;
    }

    public void setEvrakno(String evrakno) {
        this.evrakno = evrakno;
    }

    public DepoTipi getDepotipi() {
        return depotipi;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }

    public LocalDate getVade() {
        return vade;
    }

    public void setVade(LocalDate vade) {
        this.vade = vade;
    }

    public Integer getGun() {
        return gun;
    }

    public void setGun(Integer gun) {
        this.gun = gun;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
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

    public SicilDTO getSicil() {
        return sicil;
    }

    public void setSicil(SicilDTO sicil) {
        this.sicil = sicil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CariDTO)) {
            return false;
        }

        CariDTO cariDTO = (CariDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cariDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CariDTO{" +
            "id=" + getId() +
            ", tipi='" + getTipi() + "'" +
            ", evrakno='" + getEvrakno() + "'" +
            ", depotipi='" + getDepotipi() + "'" +
            ", tarih='" + getTarih() + "'" +
            ", vade='" + getVade() + "'" +
            ", gun=" + getGun() +
            ", aciklama='" + getAciklama() + "'" +
            ", borc=" + getBorc() +
            ", alacak=" + getAlacak() +
            ", sicil=" + getSicil() +
            "}";
    }
}
