package com.dizel.fikopar.domain;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A HareketSicil.
 */
@Entity
@Table(name = "hareket_sicil")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HareketSicil implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "evraktipi")
    private EvrakTipi evraktipi;

    @NotNull
    @NotBlank
    @Column(name = "evrakno")
    private String evrakno;

    @Column(name = "tarihi")
    private LocalDate tarihi;

    @Column(name = "sevktarihi")
    private LocalDate sevktarihi;

    @Column(name = "saati")
    private String saati;

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

    @Column(name = "vda")
    private String vda;

    @Column(name = "tcno")
    private String tcno;

    @Column(name = "vno")
    private String vno;

    @Column(name = "telefon")
    private String telefon;

    @Column(name = "gun")
    private Integer gun;

    @Column(name = "tutari", precision = 21, scale = 2)
    private BigDecimal tutari;

    @Column(name = "notlar")
    private String notlar;

    @Enumerated(EnumType.STRING)
    @Column(name = "depotipi")
    private DepoTipi depotipi;

    @Column(name = "toplam", precision = 21, scale = 2)
    private BigDecimal toplam;

    @Column(name = "isktoplam", precision = 21, scale = 2)
    private BigDecimal isktoplam;

    @Column(name = "kdvtoplam", precision = 21, scale = 2)
    private BigDecimal kdvtoplam;

    @Column(name = "aratoplam", precision = 21, scale = 2)
    private BigDecimal aratoplam;

    @JsonIgnoreProperties(value = { "carises" }, allowSetters = true)
    @ManyToOne
    private Sicil sicil;

    @JsonIgnoreProperties(value = { "sicil" }, allowSetters = true)
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(unique = true)
    private Cari cari;

    @OneToMany(mappedBy = "hareketSicil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "hareketSicil" }, allowSetters = true)
    private List<HareketDetay> hareketDetays = new ArrayList<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public HareketSicil id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvrakTipi getEvraktipi() {
        return this.evraktipi;
    }

    public HareketSicil evraktipi(EvrakTipi evraktipi) {
        this.setEvraktipi(evraktipi);
        return this;
    }

    public void setEvraktipi(EvrakTipi evraktipi) {
        this.evraktipi = evraktipi;
    }

    public String getEvrakno() {
        return this.evrakno;
    }

    public HareketSicil evrakno(String evrakno) {
        this.setEvrakno(evrakno);
        return this;
    }

    public void setEvrakno(String evrakno) {
        this.evrakno = evrakno;
    }

    public LocalDate getTarihi() {
        return this.tarihi;
    }

    public HareketSicil tarihi(LocalDate tarihi) {
        this.setTarihi(tarihi);
        return this;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
    }

    public LocalDate getSevktarihi() {
        return this.sevktarihi;
    }

    public HareketSicil sevktarihi(LocalDate sevktarihi) {
        this.setSevktarihi(sevktarihi);
        return this;
    }

    public void setSevktarihi(LocalDate sevktarihi) {
        this.sevktarihi = sevktarihi;
    }

    public String getSaati() {
        return this.saati;
    }

    public HareketSicil saati(String saati) {
        this.setSaati(saati);
        return this;
    }

    public void setSaati(String saati) {
        this.saati = saati;
    }

    public String getHesapno() {
        return this.hesapno;
    }

    public HareketSicil hesapno(String hesapno) {
        this.setHesapno(hesapno);
        return this;
    }

    public void setHesapno(String hesapno) {
        this.hesapno = hesapno;
    }

    public String getHesapadi() {
        return this.hesapadi;
    }

    public HareketSicil hesapadi(String hesapadi) {
        this.setHesapadi(hesapadi);
        return this;
    }

    public void setHesapadi(String hesapadi) {
        this.hesapadi = hesapadi;
    }

    public String getAdresi() {
        return this.adresi;
    }

    public HareketSicil adresi(String adresi) {
        this.setAdresi(adresi);
        return this;
    }

    public void setAdresi(String adresi) {
        this.adresi = adresi;
    }

    public String getIlce() {
        return this.ilce;
    }

    public HareketSicil ilce(String ilce) {
        this.setIlce(ilce);
        return this;
    }

    public void setIlce(String ilce) {
        this.ilce = ilce;
    }

    public String getSehir() {
        return this.sehir;
    }

    public HareketSicil sehir(String sehir) {
        this.setSehir(sehir);
        return this;
    }

    public void setSehir(String sehir) {
        this.sehir = sehir;
    }

    public String getVda() {
        return this.vda;
    }

    public HareketSicil vda(String vda) {
        this.setVda(vda);
        return this;
    }

    public void setVda(String vda) {
        this.vda = vda;
    }

    public String getTcno() {
        return this.tcno;
    }

    public HareketSicil tcno(String tcno) {
        this.setTcno(tcno);
        return this;
    }

    public void setTcno(String tcno) {
        this.tcno = tcno;
    }

    public String getVno() {
        return this.vno;
    }

    public HareketSicil vno(String vno) {
        this.setVno(vno);
        return this;
    }

    public void setVno(String vno) {
        this.vno = vno;
    }

    public String getTelefon() {
        return this.telefon;
    }

    public HareketSicil telefon(String telefon) {
        this.setTelefon(telefon);
        return this;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public Integer getGun() {
        return this.gun;
    }

    public HareketSicil gun(Integer gun) {
        this.setGun(gun);
        return this;
    }

    public void setGun(Integer gun) {
        this.gun = gun;
    }

    public BigDecimal getTutari() {
        return this.tutari;
    }

    public HareketSicil tutari(BigDecimal tutari) {
        this.setTutari(tutari);
        return this;
    }

    public void setTutari(BigDecimal tutari) {
        this.tutari = tutari;
    }

    public String getNotlar() {
        return this.notlar;
    }

    public HareketSicil notlar(String notlar) {
        this.setNotlar(notlar);
        return this;
    }

    public void setNotlar(String notlar) {
        this.notlar = notlar;
    }

    public DepoTipi getDepotipi() {
        return this.depotipi;
    }

    public HareketSicil depotipi(DepoTipi depotipi) {
        this.setDepotipi(depotipi);
        return this;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public BigDecimal getToplam() {
        return this.toplam;
    }

    public HareketSicil toplam(BigDecimal toplam) {
        this.setToplam(toplam);
        return this;
    }

    public void setToplam(BigDecimal toplam) {
        this.toplam = toplam;
    }

    public BigDecimal getIsktoplam() {
        return this.isktoplam;
    }

    public HareketSicil isktoplam(BigDecimal isktoplam) {
        this.setIsktoplam(isktoplam);
        return this;
    }

    public void setIsktoplam(BigDecimal isktoplam) {
        this.isktoplam = isktoplam;
    }

    public BigDecimal getKdvtoplam() {
        return this.kdvtoplam;
    }

    public HareketSicil kdvtoplam(BigDecimal kdvtoplam) {
        this.setKdvtoplam(kdvtoplam);
        return this;
    }

    public void setKdvtoplam(BigDecimal kdvtoplam) {
        this.kdvtoplam = kdvtoplam;
    }

    public BigDecimal getAratoplam() {
        return this.aratoplam;
    }

    public HareketSicil aratoplam(BigDecimal aratoplam) {
        this.setAratoplam(aratoplam);
        return this;
    }

    public void setAratoplam(BigDecimal aratoplam) {
        this.aratoplam = aratoplam;
    }

    public Sicil getSicil() {
        return this.sicil;
    }

    public void setSicil(Sicil sicil) {
        this.sicil = sicil;
    }

    public HareketSicil sicil(Sicil sicil) {
        this.setSicil(sicil);
        return this;
    }

    public Cari getCari() {
        return this.cari;
    }

    public void setCari(Cari cari) {
        this.cari = cari;
    }

    public HareketSicil cari(Cari cari) {
        this.setCari(cari);
        return this;
    }

    public List<HareketDetay> getHareketDetays() {
        return this.hareketDetays;
    }

    public void setHareketDetays(List<HareketDetay> hareketDetays) {
        if (this.hareketDetays != null) {
            this.hareketDetays.forEach(i -> i.setHareketSicil(null));
        }
        if (hareketDetays != null) {
            hareketDetays.forEach(i -> i.setHareketSicil(this));
        }
        this.hareketDetays = hareketDetays;
    }

    public HareketSicil hareketDetays(List<HareketDetay> hareketDetays) {
        this.setHareketDetays(hareketDetays);
        return this;
    }

    public HareketSicil addHareketDetays(HareketDetay hareketDetay) {
        this.hareketDetays.add(hareketDetay);
        hareketDetay.setHareketSicil(this);
        return this;
    }

    public HareketSicil removeHareketDetays(HareketDetay hareketDetay) {
        this.hareketDetays.remove(hareketDetay);
        hareketDetay.setHareketSicil(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HareketSicil)) {
            return false;
        }
        return id != null && id.equals(((HareketSicil) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HareketSicil{" +
            "id=" + getId() +
            ", evraktipi='" + getEvraktipi() + "'" +
            ", evrakno='" + getEvrakno() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", sevktarihi='" + getSevktarihi() + "'" +
            ", saati='" + getSaati() + "'" +
            ", hesapno='" + getHesapno() + "'" +
            ", hesapadi='" + getHesapadi() + "'" +
            ", adresi='" + getAdresi() + "'" +
            ", ilce='" + getIlce() + "'" +
            ", sehir='" + getSehir() + "'" +
            ", vda='" + getVda() + "'" +
            ", tcno='" + getTcno() + "'" +
            ", vno='" + getVno() + "'" +
            ", telefon='" + getTelefon() + "'" +
            ", gun=" + getGun() +
            ", tutari=" + getTutari() +
            ", notlar='" + getNotlar() + "'" +
            ", depotipi='" + getDepotipi() + "'" +
            ", toplam=" + getToplam() +
            ", isktoplam=" + getIsktoplam() +
            ", kdvtoplam=" + getKdvtoplam() +
            ", aratoplam=" + getAratoplam() +
            "}";
    }
}
