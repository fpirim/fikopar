package com.dizel.fikopar.service.dto;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the {@link com.dizel.fikopar.domain.Makbuz} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MakbuzDTO implements Serializable {

    private Long id;

    @NotNull
    @NotBlank
    private String makbuzno;

    @NotNull
    private EvrakTipi evraktipi;

    @NotNull
    private DepoTipi depotipi;

    @NotNull
    private LocalDate tarihi;

    @NotNull
    private LocalDate vade;

    private String aciklama;

    @NotNull
    private BigDecimal tutari;

    @NotNull
    private SicilDTO sicil;

    private CariDTO cari;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMakbuzno() {
        return makbuzno;
    }

    public void setMakbuzno(String makbuzno) {
        this.makbuzno = makbuzno;
    }

    public EvrakTipi getEvraktipi() {
        return evraktipi;
    }

    public void setEvraktipi(EvrakTipi evraktipi) {
        this.evraktipi = evraktipi;
    }

    public DepoTipi getDepotipi() {
        return depotipi;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getTarihi() {
        return tarihi;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
    }

    public LocalDate getVade() {
        return vade;
    }

    public void setVade(LocalDate vade) {
        this.vade = vade;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public BigDecimal getTutari() {
        return tutari;
    }

    public void setTutari(BigDecimal tutari) {
        this.tutari = tutari;
    }

    public SicilDTO getSicil() {
        return sicil;
    }

    public void setSicil(SicilDTO sicil) {
        this.sicil = sicil;
    }

    public CariDTO getCari() {
        return cari;
    }

    public void setCari(CariDTO cari) {
        this.cari = cari;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MakbuzDTO)) {
            return false;
        }

        MakbuzDTO makbuzDTO = (MakbuzDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, makbuzDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MakbuzDTO{" +
            "id=" + getId() +
            ", makbuzno='" + getMakbuzno() + "'" +
            ", evraktipi='" + getEvraktipi() + "'" +
            ", depotipi='" + getDepotipi() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", vade='" + getVade() + "'" +
            ", aciklama='" + getAciklama() + "'" +
            ", tutari=" + getTutari() +
            ", sicil=" + getSicil() +
            ", cari=" + getCari() +
            "}";
    }
}
