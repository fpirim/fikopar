package com.dizel.fikopar.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Katalog.
 */
@Entity
@Table(name = "katalog")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Katalog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "katalogadi")
    private String katalogadi;

    @OneToMany(mappedBy = "katalog")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "katalog" }, allowSetters = true)
    private Set<Stok> stoks = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Katalog id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKatalogadi() {
        return this.katalogadi;
    }

    public Katalog katalogadi(String katalogadi) {
        this.setKatalogadi(katalogadi);
        return this;
    }

    public void setKatalogadi(String katalogadi) {
        this.katalogadi = katalogadi;
    }

    public Set<Stok> getStoks() {
        return this.stoks;
    }

    public void setStoks(Set<Stok> stoks) {
        if (this.stoks != null) {
            this.stoks.forEach(i -> i.setKatalog(null));
        }
        if (stoks != null) {
            stoks.forEach(i -> i.setKatalog(this));
        }
        this.stoks = stoks;
    }

    public Katalog stoks(Set<Stok> stoks) {
        this.setStoks(stoks);
        return this;
    }

    public Katalog addStoks(Stok stok) {
        this.stoks.add(stok);
        stok.setKatalog(this);
        return this;
    }

    public Katalog removeStoks(Stok stok) {
        this.stoks.remove(stok);
        stok.setKatalog(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Katalog)) {
            return false;
        }
        return id != null && id.equals(((Katalog) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Katalog{" +
            "id=" + getId() +
            ", katalogadi='" + getKatalogadi() + "'" +
            "}";
    }
}
