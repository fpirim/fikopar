package dev.fikopar.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link dev.fikopar.domain.Katalog} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class KatalogDTO implements Serializable {

    private Long id;

    private String katalogadi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKatalogadi() {
        return katalogadi;
    }

    public void setKatalogadi(String katalogadi) {
        this.katalogadi = katalogadi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KatalogDTO)) {
            return false;
        }

        KatalogDTO katalogDTO = (KatalogDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, katalogDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "KatalogDTO{" +
            "id=" + getId() +
            ", katalogadi='" + getKatalogadi() + "'" +
            "}";
    }
}
