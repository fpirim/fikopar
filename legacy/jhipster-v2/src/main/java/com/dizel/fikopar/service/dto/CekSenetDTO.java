package com.dizel.fikopar.service.dto;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the {@link com.dizel.fikopar.domain.CekSenet} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CekSenetDTO implements Serializable {

    private Long id;

    private String ceksenetno;

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
    private BigDecimal tutar;

    @NotNull
    private SicilDTO sicil;

    private List<CekSenetDetayDTO> cekSenetDetays;

    public List<CekSenetDetayDTO> getCekSenetDetays() {
        return cekSenetDetays;
    }

    public void setCekSenetDetays(List<CekSenetDetayDTO> cekSenetDetays) {
        this.cekSenetDetays = cekSenetDetays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCeksenetno() {
        return ceksenetno;
    }

    public void setCeksenetno(String ceksenetno) {
        this.ceksenetno = ceksenetno;
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

    public BigDecimal getTutar() {
        return tutar;
    }

    public void setTutar(BigDecimal tutar) {
        this.tutar = tutar;
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
        if (!(o instanceof CekSenetDTO)) {
            return false;
        }

        CekSenetDTO cekSenetDTO = (CekSenetDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cekSenetDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CekSenetDTO{" +
            "id=" + getId() +
            ", ceksenetno='" + getCeksenetno() + "'" +
            ", evraktipi='" + getEvraktipi() + "'" +
            ", depotipi='" + getDepotipi() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", vade='" + getVade() + "'" +
            ", aciklama='" + getAciklama() + "'" +
            ", tutar=" + getTutar() +
            ", sicil=" + getSicil() +
            "}";
    }
}
