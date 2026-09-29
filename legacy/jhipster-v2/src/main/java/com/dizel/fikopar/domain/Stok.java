package com.dizel.fikopar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Stok.
 */
@Entity
@Table(name = "stok")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Stok implements Serializable {

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

    @Column(name = "fiyati", precision = 21, scale = 2)
    private BigDecimal fiyati;

    @ManyToOne
    @JsonIgnoreProperties(value = { "stoks" }, allowSetters = true)
    private Katalog katalog;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Stok id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStokno() {
        return this.stokno;
    }

    public Stok stokno(String stokno) {
        this.setStokno(stokno);
        return this;
    }

    public void setStokno(String stokno) {
        this.stokno = stokno;
    }

    public String getParcano() {
        return this.parcano;
    }

    public Stok parcano(String parcano) {
        this.setParcano(parcano);
        return this;
    }

    public void setParcano(String parcano) {
        this.parcano = parcano;
    }

    public String getTipi() {
        return this.tipi;
    }

    public Stok tipi(String tipi) {
        this.setTipi(tipi);
        return this;
    }

    public void setTipi(String tipi) {
        this.tipi = tipi;
    }

    public String getCinsi() {
        return this.cinsi;
    }

    public Stok cinsi(String cinsi) {
        this.setCinsi(cinsi);
        return this;
    }

    public void setCinsi(String cinsi) {
        this.cinsi = cinsi;
    }

    public String getMarka() {
        return this.marka;
    }

    public Stok marka(String marka) {
        this.setMarka(marka);
        return this;
    }

    public void setMarka(String marka) {
        this.marka = marka;
    }

    public BigDecimal getFiyati() {
        return this.fiyati;
    }

    public Stok fiyati(BigDecimal fiyati) {
        this.setFiyati(fiyati);
        return this;
    }

    public void setFiyati(BigDecimal fiyati) {
        this.fiyati = fiyati;
    }

    public Katalog getKatalog() {
        return this.katalog;
    }

    public void setKatalog(Katalog katalog) {
        this.katalog = katalog;
    }

    public Stok katalog(Katalog katalog) {
        this.setKatalog(katalog);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stok)) {
            return false;
        }
        return id != null && id.equals(((Stok) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Stok{" +
            "id=" + getId() +
            ", stokno='" + getStokno() + "'" +
            ", parcano='" + getParcano() + "'" +
            ", tipi='" + getTipi() + "'" +
            ", cinsi='" + getCinsi() + "'" +
            ", marka='" + getMarka() + "'" +
            ", fiyati=" + getFiyati() +
            "}";
    }
}
