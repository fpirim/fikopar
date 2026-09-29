package dev.fikopar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A HareketDetay.
 */
@Entity
@Table(name = "hareket_detay")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HareketDetay implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "stokno")
    private String stokno;

    @Column(name = "parcano")
    private String parcano;

    @Column(name = "tipi")
    private String tipi;

    @Column(name = "cinsi")
    private String cinsi;

    @Column(name = "marka")
    private String marka;

    @Column(name = "iskonto", precision = 21, scale = 2)
    private BigDecimal iskonto;

    @Column(name = "kdv", precision = 21, scale = 2)
    private BigDecimal kdv;

    @Column(name = "adet", precision = 21, scale = 2)
    private BigDecimal adet;

    @Column(name = "fiyati", precision = 21, scale = 2)
    private BigDecimal fiyati;

    @ManyToOne
    @JsonIgnoreProperties(value = { "sicil", "cari", "hareketDetays" }, allowSetters = true)
    private HareketSicil hareketSicil;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public HareketDetay id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStokno() {
        return this.stokno;
    }

    public HareketDetay stokno(String stokno) {
        this.setStokno(stokno);
        return this;
    }

    public void setStokno(String stokno) {
        this.stokno = stokno;
    }

    public String getParcano() {
        return this.parcano;
    }

    public HareketDetay parcano(String parcano) {
        this.setParcano(parcano);
        return this;
    }

    public void setParcano(String parcano) {
        this.parcano = parcano;
    }

    public String getTipi() {
        return this.tipi;
    }

    public HareketDetay tipi(String tipi) {
        this.setTipi(tipi);
        return this;
    }

    public void setTipi(String tipi) {
        this.tipi = tipi;
    }

    public String getCinsi() {
        return this.cinsi;
    }

    public HareketDetay cinsi(String cinsi) {
        this.setCinsi(cinsi);
        return this;
    }

    public void setCinsi(String cinsi) {
        this.cinsi = cinsi;
    }

    public String getMarka() {
        return this.marka;
    }

    public HareketDetay marka(String marka) {
        this.setMarka(marka);
        return this;
    }

    public void setMarka(String marka) {
        this.marka = marka;
    }

    public BigDecimal getIskonto() {
        return this.iskonto;
    }

    public HareketDetay iskonto(BigDecimal iskonto) {
        this.setIskonto(iskonto);
        return this;
    }

    public void setIskonto(BigDecimal iskonto) {
        this.iskonto = iskonto;
    }

    public BigDecimal getKdv() {
        return this.kdv;
    }

    public HareketDetay kdv(BigDecimal kdv) {
        this.setKdv(kdv);
        return this;
    }

    public void setKdv(BigDecimal kdv) {
        this.kdv = kdv;
    }

    public BigDecimal getAdet() {
        return this.adet;
    }

    public HareketDetay adet(BigDecimal adet) {
        this.setAdet(adet);
        return this;
    }

    public void setAdet(BigDecimal adet) {
        this.adet = adet;
    }

    public BigDecimal getFiyati() {
        return this.fiyati;
    }

    public HareketDetay fiyati(BigDecimal fiyati) {
        this.setFiyati(fiyati);
        return this;
    }

    public void setFiyati(BigDecimal fiyati) {
        this.fiyati = fiyati;
    }

    public HareketSicil getHareketSicil() {
        return this.hareketSicil;
    }

    public void setHareketSicil(HareketSicil hareketSicil) {
        this.hareketSicil = hareketSicil;
    }

    public HareketDetay hareketSicil(HareketSicil hareketSicil) {
        this.setHareketSicil(hareketSicil);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HareketDetay)) {
            return false;
        }
        return id != null && id.equals(((HareketDetay) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HareketDetay{" +
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
            "}";
    }
}
