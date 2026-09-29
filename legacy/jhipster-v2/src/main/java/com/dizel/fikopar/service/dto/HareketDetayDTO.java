package com.dizel.fikopar.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.dizel.fikopar.domain.HareketDetay} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HareketDetayDTO implements Serializable {

    private Long id;

    private String stokno;

    private String parcano;

    private String tipi;

    private String cinsi;

    private String marka;

    private BigDecimal iskonto;

    private BigDecimal kdv;

    private BigDecimal adet;

    private BigDecimal fiyati;

    //private HareketSicilDTO hareketSicil;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStokno() {
        return stokno;
    }

    public void setStokno(String stokno) {
        this.stokno = stokno;
    }

    public String getParcano() {
        return parcano;
    }

    public void setParcano(String parcano) {
        this.parcano = parcano;
    }

    public String getTipi() {
        return tipi;
    }

    public void setTipi(String tipi) {
        this.tipi = tipi;
    }

    public String getCinsi() {
        return cinsi;
    }

    public void setCinsi(String cinsi) {
        this.cinsi = cinsi;
    }

    public String getMarka() {
        return marka;
    }

    public void setMarka(String marka) {
        this.marka = marka;
    }

    public BigDecimal getIskonto() {
        return iskonto;
    }

    public void setIskonto(BigDecimal iskonto) {
        this.iskonto = iskonto;
    }

    public BigDecimal getKdv() {
        return kdv;
    }

    public void setKdv(BigDecimal kdv) {
        this.kdv = kdv;
    }

    public BigDecimal getAdet() {
        return adet;
    }

    public void setAdet(BigDecimal adet) {
        this.adet = adet;
    }

    public BigDecimal getFiyati() {
        return fiyati;
    }

    public void setFiyati(BigDecimal fiyati) {
        this.fiyati = fiyati;
    }

    //    public HareketSicilDTO getHareketSicil() {
    //        return hareketSicil;
    //    }

    //    public void setHareketSicil(HareketSicilDTO hareketSicil) {
    //        this.hareketSicil = hareketSicil;
    //    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HareketDetayDTO)) {
            return false;
        }

        HareketDetayDTO hareketDetayDTO = (HareketDetayDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, hareketDetayDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HareketDetayDTO{" +
            "id=" + getId() +
            ", stokno='" + getStokno() + "'" +
            ", parcano='" + getParcano() + "'" +
            ", tipi='" + getTipi() + "'" +
            ", cinsi='" + getCinsi() + "'" +
            ", marka='" + getMarka() + "'" +
            ", iskonto=" + getIskonto() +
            ", kdv=" + getKdv() +
            ", adet=" + getAdet() +
            ", fiyati=" + getFiyati() +
            // ", hareketSicil=" + getHareketSicil() +
            "}";
    }
}
