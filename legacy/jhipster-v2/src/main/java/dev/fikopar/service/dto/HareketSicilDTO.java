package dev.fikopar.service.dto;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the {@link dev.fikopar.domain.HareketSicil} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HareketSicilDTO implements Serializable {

    private Long id;

    @NotNull
    private EvrakTipi evraktipi;

    private String evrakno;

    @NotNull
    private LocalDate tarihi;

    private LocalDate sevktarihi;

    private String saati;

    private String hesapno;

    private String hesapadi;

    private String adresi;

    private String ilce;

    private String sehir;

    private String vda;

    private String tcno;

    private String vno;

    private String telefon;

    private Integer gun;

    private BigDecimal tutari;

    private String notlar;

    private DepoTipi depotipi;

    private BigDecimal toplam;

    private BigDecimal isktoplam;

    private BigDecimal kdvtoplam;

    private BigDecimal aratoplam;

    private SicilDTO sicil;

    private CariDTO cari;

    private List<HareketDetayDTO> hareketDetays;

    public List<HareketDetayDTO> getHareketDetays() {
        return hareketDetays;
    }

    public void setHareketDetays(List<HareketDetayDTO> hareketDetays) {
        this.hareketDetays = hareketDetays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvrakTipi getEvraktipi() {
        return evraktipi;
    }

    public void setEvraktipi(EvrakTipi evraktipi) {
        this.evraktipi = evraktipi;
    }

    public String getEvrakno() {
        return evrakno;
    }

    public void setEvrakno(String evrakno) {
        this.evrakno = evrakno;
    }

    public LocalDate getTarihi() {
        return tarihi;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
    }

    public LocalDate getSevktarihi() {
        return sevktarihi;
    }

    public void setSevktarihi(LocalDate sevktarihi) {
        this.sevktarihi = sevktarihi;
    }

    public String getSaati() {
        return saati;
    }

    public void setSaati(String saati) {
        this.saati = saati;
    }

    public String getHesapno() {
        return hesapno;
    }

    public void setHesapno(String hesapno) {
        this.hesapno = hesapno;
    }

    public String getHesapadi() {
        return hesapadi;
    }

    public void setHesapadi(String hesapadi) {
        this.hesapadi = hesapadi;
    }

    public String getAdresi() {
        return adresi;
    }

    public void setAdresi(String adresi) {
        this.adresi = adresi;
    }

    public String getIlce() {
        return ilce;
    }

    public void setIlce(String ilce) {
        this.ilce = ilce;
    }

    public String getSehir() {
        return sehir;
    }

    public void setSehir(String sehir) {
        this.sehir = sehir;
    }

    public String getVda() {
        return vda;
    }

    public void setVda(String vda) {
        this.vda = vda;
    }

    public String getTcno() {
        return tcno;
    }

    public void setTcno(String tcno) {
        this.tcno = tcno;
    }

    public String getVno() {
        return vno;
    }

    public void setVno(String vno) {
        this.vno = vno;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public Integer getGun() {
        return gun;
    }

    public void setGun(Integer gun) {
        this.gun = gun;
    }

    public BigDecimal getTutari() {
        return tutari;
    }

    public void setTutari(BigDecimal tutari) {
        this.tutari = tutari;
    }

    public String getNotlar() {
        return notlar;
    }

    public void setNotlar(String notlar) {
        this.notlar = notlar;
    }

    public DepoTipi getDepotipi() {
        return depotipi;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public BigDecimal getToplam() {
        return toplam;
    }

    public void setToplam(BigDecimal toplam) {
        this.toplam = toplam;
    }

    public BigDecimal getIsktoplam() {
        return isktoplam;
    }

    public void setIsktoplam(BigDecimal isktoplam) {
        this.isktoplam = isktoplam;
    }

    public BigDecimal getKdvtoplam() {
        return kdvtoplam;
    }

    public void setKdvtoplam(BigDecimal kdvtoplam) {
        this.kdvtoplam = kdvtoplam;
    }

    public BigDecimal getAratoplam() {
        return aratoplam;
    }

    public void setAratoplam(BigDecimal aratoplam) {
        this.aratoplam = aratoplam;
    }

    public SicilDTO getSicil() {
        return sicil;
    }

    public void setSicil(SicilDTO sicil) {
        this.sicil = sicil;
    }

    public CariDTO getCari() {
        return cari;
    }

    public void setCari(CariDTO cari) {
        this.cari = cari;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HareketSicilDTO)) {
            return false;
        }

        HareketSicilDTO hareketSicilDTO = (HareketSicilDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, hareketSicilDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HareketSicilDTO{" +
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
            ", sicil=" + getSicil() +
            ", cari=" + getCari() +
            "}";
    }
}
