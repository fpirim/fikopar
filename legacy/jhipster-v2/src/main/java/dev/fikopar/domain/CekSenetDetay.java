package dev.fikopar.domain;

import dev.fikopar.domain.enumeration.IslemTipi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A CekSenetDetay.
 */
@Entity
@Table(name = "cek_senet_detay")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CekSenetDetay implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "islemtipi")
    private IslemTipi islemtipi;

    @Column(name = "tarihi")
    private LocalDate tarihi;

    @Column(name = "tutar", precision = 21, scale = 2)
    private BigDecimal tutar;

    @JsonIgnoreProperties(value = { "cari", "sicil" }, allowSetters = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(unique = true)
    private Makbuz makbuz;

    @ManyToOne
    @JsonIgnoreProperties(value = { "carises" }, allowSetters = true)
    private Sicil sicil;

    @ManyToOne
    @JsonIgnoreProperties(value = { "cekSenetDetays", "sicil" }, allowSetters = true)
    private CekSenet cekSenet;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CekSenetDetay id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IslemTipi getIslemtipi() {
        return this.islemtipi;
    }

    public CekSenetDetay islemtipi(IslemTipi islemtipi) {
        this.setIslemtipi(islemtipi);
        return this;
    }

    public void setIslemtipi(IslemTipi islemtipi) {
        this.islemtipi = islemtipi;
    }

    public LocalDate getTarihi() {
        return this.tarihi;
    }

    public CekSenetDetay tarihi(LocalDate tarihi) {
        this.setTarihi(tarihi);
        return this;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
    }

    public BigDecimal getTutar() {
        return this.tutar;
    }

    public CekSenetDetay tutar(BigDecimal tutar) {
        this.setTutar(tutar);
        return this;
    }

    public void setTutar(BigDecimal tutar) {
        this.tutar = tutar;
    }

    public Makbuz getMakbuz() {
        return this.makbuz;
    }

    public void setMakbuz(Makbuz makbuz) {
        this.makbuz = makbuz;
    }

    public CekSenetDetay makbuz(Makbuz makbuz) {
        this.setMakbuz(makbuz);
        return this;
    }

    public Sicil getSicil() {
        return this.sicil;
    }

    public void setSicil(Sicil sicil) {
        this.sicil = sicil;
    }

    public CekSenetDetay sicil(Sicil sicil) {
        this.setSicil(sicil);
        return this;
    }

    public CekSenet getCekSenet() {
        return this.cekSenet;
    }

    public void setCekSenet(CekSenet cekSenet) {
        this.cekSenet = cekSenet;
    }

    public CekSenetDetay cekSenet(CekSenet cekSenet) {
        this.setCekSenet(cekSenet);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CekSenetDetay)) {
            return false;
        }
        return id != null && id.equals(((CekSenetDetay) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CekSenetDetay{" +
            "id=" + getId() +
            ", islemtipi='" + getIslemtipi() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", tutar=" + getTutar() +
            "}";
    }
}
