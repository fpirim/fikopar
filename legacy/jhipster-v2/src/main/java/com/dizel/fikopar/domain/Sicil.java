package com.dizel.fikopar.domain;

import com.dizel.fikopar.domain.enumeration.SicilTipi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Sicil.
 */
@Entity
@Table(name = "sicil")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Sicil implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "hesapno")
    private String hesapno;

    @Column(name = "hesapadi")
    private String hesapadi;

    @Column(name = "adresi")
    private String adresi;

    @Column(name = "ilce")
    private String ilce;

    @Column(name = "sehir")
    private String sehir;

    @Column(name = "telefon")
    private String telefon;

    @Column(name = "vda")
    private String vda;

    @Column(name = "vno")
    private String vno;

    @Column(name = "tcno")
    private String tcno;

    @Column(name = "gun")
    private Integer gun;

    @Column(name = "iskonto", precision = 21, scale = 2)
    private BigDecimal iskonto;

    @Enumerated(EnumType.STRING)
    @Column(name = "siciltipi")
    private SicilTipi siciltipi;

    @OneToMany(mappedBy = "sicil")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "sicil" }, allowSetters = true)
    private Set<Cari> carises = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Sicil id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHesapno() {
        return this.hesapno;
    }

    public Sicil hesapno(String hesapno) {
        this.setHesapno(hesapno);
        return this;
    }

    public void setHesapno(String hesapno) {
        this.hesapno = hesapno;
    }

    public String getHesapadi() {
        return this.hesapadi;
    }

    public Sicil hesapadi(String hesapadi) {
        this.setHesapadi(hesapadi);
        return this;
    }

    public void setHesapadi(String hesapadi) {
        this.hesapadi = hesapadi;
    }

    public String getAdresi() {
        return this.adresi;
    }

    public Sicil adresi(String adresi) {
        this.setAdresi(adresi);
        return this;
    }

    public void setAdresi(String adresi) {
        this.adresi = adresi;
    }

    public String getIlce() {
        return this.ilce;
    }

    public Sicil ilce(String ilce) {
        this.setIlce(ilce);
        return this;
    }

    public void setIlce(String ilce) {
        this.ilce = ilce;
    }

    public String getSehir() {
        return this.sehir;
    }

    public Sicil sehir(String sehir) {
        this.setSehir(sehir);
        return this;
    }

    public void setSehir(String sehir) {
        this.sehir = sehir;
    }

    public String getTelefon() {
        return this.telefon;
    }

    public Sicil telefon(String telefon) {
        this.setTelefon(telefon);
        return this;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getVda() {
        return this.vda;
    }

    public Sicil vda(String vda) {
        this.setVda(vda);
        return this;
    }

    public void setVda(String vda) {
        this.vda = vda;
    }

    public String getVno() {
        return this.vno;
    }

    public Sicil vno(String vno) {
        this.setVno(vno);
        return this;
    }

    public void setVno(String vno) {
        this.vno = vno;
    }

    public String getTcno() {
        return this.tcno;
    }

    public Sicil tcno(String tcno) {
        this.setTcno(tcno);
        return this;
    }

    public void setTcno(String tcno) {
        this.tcno = tcno;
    }

    public Integer getGun() {
        return this.gun;
    }

    public Sicil gun(Integer gun) {
        this.setGun(gun);
        return this;
    }

    public void setGun(Integer gun) {
        this.gun = gun;
    }

    public BigDecimal getIskonto() {
        return this.iskonto;
    }

    public Sicil iskonto(BigDecimal iskonto) {
        this.setIskonto(iskonto);
        return this;
    }

    public void setIskonto(BigDecimal iskonto) {
        this.iskonto = iskonto;
    }

    public SicilTipi getSiciltipi() {
        return this.siciltipi;
    }

    public Sicil siciltipi(SicilTipi siciltipi) {
        this.setSiciltipi(siciltipi);
        return this;
    }

    public void setSiciltipi(SicilTipi siciltipi) {
        this.siciltipi = siciltipi;
    }

    public Set<Cari> getCarises() {
        return this.carises;
    }

    public void setCarises(Set<Cari> caris) {
        if (this.carises != null) {
            this.carises.forEach(i -> i.setSicil(null));
        }
        if (caris != null) {
            caris.forEach(i -> i.setSicil(this));
        }
        this.carises = caris;
    }

    public Sicil carises(Set<Cari> caris) {
        this.setCarises(caris);
        return this;
    }

    public Sicil addCaris(Cari cari) {
        this.carises.add(cari);
        cari.setSicil(this);
        return this;
    }

    public Sicil removeCaris(Cari cari) {
        this.carises.remove(cari);
        cari.setSicil(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sicil)) {
            return false;
        }
        return id != null && id.equals(((Sicil) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Sicil{" +
            "id=" + getId() +
            ", hesapno='" + getHesapno() + "'" +
            ", hesapadi='" + getHesapadi() + "'" +
            ", adresi='" + getAdresi() + "'" +
            ", ilce='" + getIlce() + "'" +
            ", sehir='" + getSehir() + "'" +
            ", telefon='" + getTelefon() + "'" +
            ", vda='" + getVda() + "'" +
            ", vno='" + getVno() + "'" +
            ", tcno='" + getTcno() + "'" +
            ", gun=" + getGun() +
            ", iskonto=" + getIskonto() +
            ", siciltipi='" + getSiciltipi() + "'" +
            "}";
    }
}
