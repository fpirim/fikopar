package com.dizel.fikopar.service.dto;

import com.dizel.fikopar.domain.enumeration.IslemTipi;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.dizel.fikopar.domain.CekSenetDetay} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CekSenetDetayDTO implements Serializable {

    private Long id;

    private IslemTipi islemtipi;

    private LocalDate tarihi;

    private BigDecimal tutar;

    private SicilDTO sicil;

    private MakbuzDTO makbuz;

    //    private CekSenetDTO cekSenet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IslemTipi getIslemtipi() {
        return islemtipi;
    }

    public void setIslemtipi(IslemTipi islemtipi) {
        this.islemtipi = islemtipi;
    }

    public LocalDate getTarihi() {
        return tarihi;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
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

    public MakbuzDTO getMakbuz() {
        return makbuz;
    }

    public void setMakbuz(MakbuzDTO makbuz) {
        this.makbuz = makbuz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CekSenetDetayDTO)) {
            return false;
        }

        CekSenetDetayDTO cekSenetDetayDTO = (CekSenetDetayDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cekSenetDetayDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CekSenetDetayDTO{" +
            "id=" + getId() +
            ", islemtipi='" + getIslemtipi() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", tutar=" + getTutar() +
            ", sicil=" + getSicil() +
            ", makbuz=" + getMakbuz() +
            "}";
    }
}
