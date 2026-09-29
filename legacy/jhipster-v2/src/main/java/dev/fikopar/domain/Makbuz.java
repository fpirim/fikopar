package dev.fikopar.domain;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Makbuz.
 */
@Entity
@Table(name = "makbuz")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Makbuz implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Column(name = "makbuzno")
    private String makbuzno;

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

    @Column(name = "tutari", precision = 21, scale = 2)
    private BigDecimal tutari;

    @JsonIgnoreProperties(value = { "sicil" }, allowSetters = true)
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(unique = true)
    private Cari cari;

    @ManyToOne
    @JsonIgnoreProperties(value = { "carises" }, allowSetters = true)
    private Sicil sicil;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Makbuz id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMakbuzno() {
        return this.makbuzno;
    }

    public Makbuz makbuzno(String makbuzno) {
        this.setMakbuzno(makbuzno);
        return this;
    }

    public void setMakbuzno(String makbuzno) {
        this.makbuzno = makbuzno;
    }

    public EvrakTipi getEvraktipi() {
        return this.evraktipi;
    }

    public Makbuz evraktipi(EvrakTipi evraktipi) {
        this.setEvraktipi(evraktipi);
        return this;
    }

    public void setEvraktipi(EvrakTipi evraktipi) {
        this.evraktipi = evraktipi;
    }

    public DepoTipi getDepotipi() {
        return this.depotipi;
    }

    public Makbuz depotipi(DepoTipi depotipi) {
        this.setDepotipi(depotipi);
        return this;
    }

    public void setDepotipi(DepoTipi depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDate getTarihi() {
        return this.tarihi;
    }

    public Makbuz tarihi(LocalDate tarihi) {
        this.setTarihi(tarihi);
        return this;
    }

    public void setTarihi(LocalDate tarihi) {
        this.tarihi = tarihi;
    }

    public LocalDate getVade() {
        return this.vade;
    }

    public Makbuz vade(LocalDate vade) {
        this.setVade(vade);
        return this;
    }

    public void setVade(LocalDate vade) {
        this.vade = vade;
    }

    public String getAciklama() {
        return this.aciklama;
    }

    public Makbuz aciklama(String aciklama) {
        this.setAciklama(aciklama);
        return this;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public BigDecimal getTutari() {
        return this.tutari;
    }

    public Makbuz tutari(BigDecimal tutari) {
        this.setTutari(tutari);
        return this;
    }

    public void setTutari(BigDecimal tutari) {
        this.tutari = tutari;
    }

    public Cari getCari() {
        return this.cari;
    }

    public void setCari(Cari cari) {
        this.cari = cari;
    }

    public Makbuz cari(Cari cari) {
        this.setCari(cari);
        return this;
    }

    public Sicil getSicil() {
        return this.sicil;
    }

    public void setSicil(Sicil sicil) {
        this.sicil = sicil;
    }

    public Makbuz sicil(Sicil sicil) {
        this.setSicil(sicil);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Makbuz)) {
            return false;
        }
        return id != null && id.equals(((Makbuz) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Makbuz{" +
            "id=" + getId() +
            ", makbuzno='" + getMakbuzno() + "'" +
            ", evraktipi='" + getEvraktipi() + "'" +
            ", depotipi='" + getDepotipi() + "'" +
            ", tarihi='" + getTarihi() + "'" +
            ", vade='" + getVade() + "'" +
            ", aciklama='" + getAciklama() + "'" +
            ", tutari=" + getTutari() +
            "}";
    }
}
