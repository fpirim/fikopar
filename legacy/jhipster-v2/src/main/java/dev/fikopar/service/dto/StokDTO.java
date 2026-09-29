package dev.fikopar.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link dev.fikopar.domain.Stok} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StokDTO implements Serializable {

    private Long id;

    private String stokno;

    private String parcano;

    private String tipi;

    private String cinsi;

    private String marka;

    private BigDecimal fiyati;

    private KatalogDTO katalog;

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

    public BigDecimal getFiyati() {
        return fiyati;
    }

    public void setFiyati(BigDecimal fiyati) {
        this.fiyati = fiyati;
    }

    public KatalogDTO getKatalog() {
        return katalog;
    }

    public void setKatalog(KatalogDTO katalog) {
        this.katalog = katalog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StokDTO)) {
            return false;
        }

        StokDTO stokDTO = (StokDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, stokDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "StokDTO{" +
            "id=" + getId() +
            ", stokno='" + getStokno() + "'" +
            ", parcano='" + getParcano() + "'" +
            ", tipi='" + getTipi() + "'" +
            ", cinsi='" + getCinsi() + "'" +
            ", marka='" + getMarka() + "'" +
            ", fiyati=" + getFiyati() +
            ", katalog=" + getKatalog() +
            "}";
    }
}
