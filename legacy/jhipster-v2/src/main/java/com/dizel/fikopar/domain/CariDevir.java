package com.dizel.fikopar.domain;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A CariDevir.
 */
@Entity
@Table(name = "cari_devir")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CariDevir implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "depotipi")
    private DepoTipi depotipi;

    @Column(name = "yil")
    private LocalDate yil;

    @Column(name = "borc", precision = 21, scale = 2)
    private BigDecimal borc;

    @Column(name = "alacak", precision = 21, scale = 2)
    private BigDecimal alacak;

    @JsonIgnoreProperties(value = { "carises" }, allowSetters = true)
    @ManyToOne
    private Sicil sicil;

    @JsonIgnoreProperties(value = { "sicil" }, allowSetters = true)
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(unique = true)
    private Cari cari;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CariDevir id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DepoTipi getDepotipi() {
        return this.depotipi;
    }

    public CariDevir depotipi(DepoTipi depotipi) {
        this.setDepotipi(depotipi);
        return this;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getYil() {
        return this.yil;
    }

    public CariDevir yil(LocalDate yil) {
        this.setYil(yil);
        return this;
    }

    public void setYil(LocalDate yil) {
        this.yil = yil;
    }

    public BigDecimal getBorc() {
        return this.borc;
    }

    public CariDevir borc(BigDecimal borc) {
        this.setBorc(borc);
        return this;
    }

    public void setBorc(BigDecimal borc) {
        this.borc = borc;
    }

    public BigDecimal getAlacak() {
        return this.alacak;
    }

    public CariDevir alacak(BigDecimal alacak) {
        this.setAlacak(alacak);
        return this;
    }

    public void setAlacak(BigDecimal alacak) {
        this.alacak = alacak;
    }

    public Sicil getSicil() {
        return this.sicil;
    }

    public void setSicil(Sicil sicil) {
        this.sicil = sicil;
    }

    public CariDevir sicil(Sicil sicil) {
        this.setSicil(sicil);
        return this;
    }

    public Cari getCari() {
        return this.cari;
    }

    public void setCari(Cari cari) {
        this.cari = cari;
    }

    public CariDevir cari(Cari cari) {
        this.setCari(cari);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CariDevir)) {
            return false;
        }
        return id != null && id.equals(((CariDevir) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CariDevir{" +
            "id=" + getId() +
            ", depotipi='" + getDepotipi() + "'" +
            ", yil=" + getYil() +
            ", borc=" + getBorc() +
            ", alacak=" + getAlacak() +
            "}";
    }
}
