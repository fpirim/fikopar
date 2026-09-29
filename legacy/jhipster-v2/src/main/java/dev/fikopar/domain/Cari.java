package dev.fikopar.domain;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Cari.
 */
@Entity
@Table(name = "cari")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Cari implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipi")
    private EvrakTipi tipi;

    @Column(name = "evrakno")
    private String evrakno;

    @Enumerated(EnumType.STRING)
    @Column(name = "depotipi")
    private DepoTipi depotipi;

    @Column(name = "tarih")
    private LocalDate tarih;

    @Column(name = "vade")
    private LocalDate vade;

    @Column(name = "gun")
    private Integer gun;

    @Column(name = "aciklama")
    private String aciklama;

    @Column(name = "borc", precision = 21, scale = 2)
    private BigDecimal borc;

    @Column(name = "alacak", precision = 21, scale = 2)
    private BigDecimal alacak;

    @ManyToOne
    @JsonIgnoreProperties(value = { "carises" }, allowSetters = true)
    private Sicil sicil;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Cari id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvrakTipi getTipi() {
        return this.tipi;
    }

    public Cari tipi(EvrakTipi tipi) {
        this.setTipi(tipi);
        return this;
    }

    public void setTipi(EvrakTipi tipi) {
        this.tipi = tipi;
    }

    public String getEvrakno() {
        return this.evrakno;
    }

    public Cari evrakno(String evrakno) {
        this.setEvrakno(evrakno);
        return this;
    }

    public void setEvrakno(String evrakno) {
        this.evrakno = evrakno;
    }

    public DepoTipi getDepotipi() {
        return this.depotipi;
    }

    public Cari depotipi(DepoTipi depotipi) {
        this.setDepotipi(depotipi);
        return this;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getTarih() {
        return this.tarih;
    }

    public Cari tarih(LocalDate tarih) {
        this.setTarih(tarih);
        return this;
    }

    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }

    public LocalDate getVade() {
        return this.vade;
    }

    public Cari vade(LocalDate vade) {
        this.setVade(vade);
        return this;
    }

    public void setVade(LocalDate vade) {
        this.vade = vade;
    }

    public Integer getGun() {
        return this.gun;
    }

    public Cari gun(Integer gun) {
        this.setGun(gun);
        return this;
    }

    public void setGun(Integer gun) {
        this.gun = gun;
    }

    public String getAciklama() {
        return this.aciklama;
    }

    public Cari aciklama(String aciklama) {
        this.setAciklama(aciklama);
        return this;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public BigDecimal getBorc() {
        return this.borc;
    }

    public Cari borc(BigDecimal borc) {
        this.setBorc(borc);
        return this;
    }

    public void setBorc(BigDecimal borc) {
        this.borc = borc;
    }

    public BigDecimal getAlacak() {
        return this.alacak;
    }

    public Cari alacak(BigDecimal alacak) {
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

    public Cari sicil(Sicil sicil) {
        this.setSicil(sicil);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cari)) {
            return false;
        }
        return id != null && id.equals(((Cari) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Cari{" +
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
            "}";
    }
}
