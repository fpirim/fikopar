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
 * A CekSenet.
 */
@Entity
@Table(name = "cek_senet")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CekSenet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "ceksenetno")
    private String ceksenetno;

    @Enumerated(EnumType.STRING)
    @Column(name = "evraktipi")
    private EvrakTipi evraktipi;

    @Enumerated(EnumType.STRING)
    @Column(name = "depotipi")
    private DepoTipi depotipi;

    @Column(name = "tarihi")
    private LocalDate tarihi;

    @Column(name = "vade")
    private LocalDate vade;

    @Column(name = "aciklama")
    private String aciklama;

    @Column(name = "tutar", precision = 21, scale = 2)
    private BigDecimal tutar;

    @OneToMany(mappedBy = "cekSenet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "makbuz", "sicil", "cekSenet" }, allowSetters = true)
    private List<CekSenetDetay> cekSenetDetays = new ArrayList<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "carises" }, allowSetters = true)
    private Sicil sicil;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CekSenet id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCeksenetno() {
        return this.ceksenetno;
    }

    public CekSenet ceksenetno(String ceksenetno) {
        this.setCeksenetno(ceksenetno);
        return this;
    }

    public void setCeksenetno(String ceksenetno) {
        this.ceksenetno = ceksenetno;
    }

    public EvrakTipi getEvraktipi() {
        return this.evraktipi;
    }

    public CekSenet evraktipi(EvrakTipi evraktipi) {
        this.setEvraktipi(evraktipi);
        return this;
    }

    public void setEvraktipi(EvrakTipi evraktipi) {
        this.evraktipi = evraktipi;
    }

    public DepoTipi getDepotipi() {
        return this.depotipi;
    }

    public CekSenet depotipi(DepoTipi depotipi) {
        this.setDepotipi(depotipi);
        return this;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getTarihi() {
        return this.tarihi;
    }

    public CekSenet tarihi(LocalDate tarihi) {
        this.setTarihi(tarihi);
        return this;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
    }

    public LocalDate getVade() {
        return this.vade;
    }

    public CekSenet vade(LocalDate vade) {
        this.setVade(vade);
        return this;
    }

    public void setVade(LocalDate vade) {
        this.vade = vade;
    }

    public String getAciklama() {
        return this.aciklama;
    }

    public CekSenet aciklama(String aciklama) {
        this.setAciklama(aciklama);
        return this;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public BigDecimal getTutar() {
        return this.tutar;
    }

    public CekSenet tutar(BigDecimal tutar) {
        this.setTutar(tutar);
        return this;
    }

    public void setTutar(BigDecimal tutar) {
        this.tutar = tutar;
    }

    public List<CekSenetDetay> getCekSenetDetays() {
        return this.cekSenetDetays;
    }

    public void setCekSenetDetays(List<CekSenetDetay> cekSenetDetays) {
        if (this.cekSenetDetays != null) {
            this.cekSenetDetays.forEach(i -> i.setCekSenet(null));
        }
        if (cekSenetDetays != null) {
            cekSenetDetays.forEach(i -> i.setCekSenet(this));
        }
        this.cekSenetDetays = cekSenetDetays;
    }

    public CekSenet cekSenetDetays(List<CekSenetDetay> cekSenetDetays) {
        this.setCekSenetDetays(cekSenetDetays);
        return this;
    }

    public CekSenet addCekSenetDetays(CekSenetDetay cekSenetDetay) {
        this.cekSenetDetays.add(cekSenetDetay);
        cekSenetDetay.setCekSenet(this);
        return this;
    }

    public CekSenet removeCekSenetDetays(CekSenetDetay cekSenetDetay) {
        this.cekSenetDetays.remove(cekSenetDetay);
        cekSenetDetay.setCekSenet(null);
        return this;
    }

    public Sicil getSicil() {
        return this.sicil;
    }

    public void setSicil(Sicil sicil) {
        this.sicil = sicil;
    }

    public CekSenet sicil(Sicil sicil) {
        this.setSicil(sicil);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CekSenet)) {
            return false;
        }
        return id != null && id.equals(((CekSenet) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CekSenet{" +
            "id=" + getId() +
            ", ceksenetno='" + getCeksenetno() + "'" +
            ", evraktipi='" + getEvraktipi() + "'" +
            ", depotipi='" + getDepotipi() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", vade='" + getVade() + "'" +
            ", aciklama='" + getAciklama() + "'" +
            ", tutar=" + getTutar() +
            "}";
    }
}
