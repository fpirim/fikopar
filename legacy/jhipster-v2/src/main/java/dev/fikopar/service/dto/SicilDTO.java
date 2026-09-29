package dev.fikopar.service.dto;

import dev.fikopar.domain.enumeration.SicilTipi;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.NotBlank;

/**
 * A DTO for the {@link dev.fikopar.domain.Sicil} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SicilDTO implements Serializable {

    private Long id;

    @NotBlank
    private String hesapno;

    private String hesapadi;

    private String adresi;

    private String ilce;

    private String sehir;

    private String telefon;

    private String vda;

    private String vno;

    private String tcno;

    private Integer gun;

    private BigDecimal iskonto;

    private SicilTipi siciltipi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getVda() {
        return vda;
    }

    public void setVda(String vda) {
        this.vda = vda;
    }

    public String getVno() {
        return vno;
    }

    public void setVno(String vno) {
        this.vno = vno;
    }

    public String getTcno() {
        return tcno;
    }

    public void setTcno(String tcno) {
        this.tcno = tcno;
    }

    public Integer getGun() {
        return gun;
    }

    public void setGun(Integer gun) {
        this.gun = gun;
    }

    public BigDecimal getIskonto() {
        return iskonto;
    }

    public void setIskonto(BigDecimal iskonto) {
        this.iskonto = iskonto;
    }

    public SicilTipi getSiciltipi() {
        return siciltipi;
    }

    public void setSiciltipi(SicilTipi siciltipi) {
        this.siciltipi = siciltipi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SicilDTO)) {
            return false;
        }

        SicilDTO sicilDTO = (SicilDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, sicilDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SicilDTO{" +
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
