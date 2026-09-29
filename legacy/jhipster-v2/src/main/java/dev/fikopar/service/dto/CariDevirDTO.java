package dev.fikopar.service.dto;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.service.constraints.BorcAlacak;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the {@link dev.fikopar.domain.CariDevir} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
@BorcAlacak
public class CariDevirDTO implements Serializable {

    private Long id;

    @NotNull
    private DepoTipi depotipi;

    //@DevirYili
    @NotNull
    private LocalDate yil;

    private BigDecimal borc;

    private BigDecimal alacak;

    @NotNull
    private SicilDTO sicil;

    private CariDTO cari;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DepoTipi getDepotipi() {
        return depotipi;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getYil() {
        return yil;
    }

    public void setYil(LocalDate yil) {
        this.yil = yil;
    }

    public BigDecimal getBorc() {
        return borc;
    }

    public void setBorc(BigDecimal borc) {
        this.borc = borc;
    }

    public BigDecimal getAlacak() {
        return alacak;
    }

    public void setAlacak(BigDecimal alacak) {
        this.alacak = alacak;
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
        if (!(o instanceof CariDevirDTO)) {
            return false;
        }

        CariDevirDTO cariDevirDTO = (CariDevirDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cariDevirDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CariDevirDTO{" +
            "id=" + getId() +
            ", depotipi='" + getDepotipi() + "'" +
            ", yil=" + getYil() +
            ", borc=" + getBorc() +
            ", alacak=" + getAlacak() +
            ", sicil=" + getSicil() +
            ", cari=" + getCari() +
            "}";
    }
}
