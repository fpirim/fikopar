package com.dizel.fikopar.service.criteria;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.dizel.fikopar.domain.CariDevir} entity. This class is used
 * in {@link com.dizel.fikopar.web.rest.CariDevirResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cari-devirs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CariDevirCriteria implements Serializable, Criteria {

    /**
     * Class for filtering DepoTipi
     */
    public static class DepoTipiFilter extends Filter<DepoTipi> {

        public DepoTipiFilter() {}

        public DepoTipiFilter(DepoTipiFilter filter) {
            super(filter);
        }

        @Override
        public DepoTipiFilter copy() {
            return new DepoTipiFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private DepoTipiFilter depotipi;

    private LocalDateFilter yil;

    private BigDecimalFilter borc;

    private BigDecimalFilter alacak;

    private LongFilter sicilId;

    private LongFilter cariId;

    private Boolean distinct;

    public CariDevirCriteria() {}

    public CariDevirCriteria(CariDevirCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.depotipi = other.depotipi == null ? null : other.depotipi.copy();
        this.yil = other.yil == null ? null : other.yil.copy();
        this.borc = other.borc == null ? null : other.borc.copy();
        this.alacak = other.alacak == null ? null : other.alacak.copy();
        this.sicilId = other.sicilId == null ? null : other.sicilId.copy();
        this.cariId = other.cariId == null ? null : other.cariId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CariDevirCriteria copy() {
        return new CariDevirCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public DepoTipiFilter getDepotipi() {
        return depotipi;
    }

    public DepoTipiFilter depotipi() {
        if (depotipi == null) {
            depotipi = new DepoTipiFilter();
        }
        return depotipi;
    }

    public void setDepotipi(DepoTipiFilter depotipi) {
        this.depotipi = depotipi;
    }

    public LocalDateFilter getYil() {
        return yil;
    }

    public LocalDateFilter yil() {
        if (yil == null) {
            yil = new LocalDateFilter();
        }
        return yil;
    }

    public void setYil(LocalDateFilter yil) {
        this.yil = yil;
    }

    public BigDecimalFilter getBorc() {
        return borc;
    }

    public BigDecimalFilter borc() {
        if (borc == null) {
            borc = new BigDecimalFilter();
        }
        return borc;
    }

    public void setBorc(BigDecimalFilter borc) {
        this.borc = borc;
    }

    public BigDecimalFilter getAlacak() {
        return alacak;
    }

    public BigDecimalFilter alacak() {
        if (alacak == null) {
            alacak = new BigDecimalFilter();
        }
        return alacak;
    }

    public void setAlacak(BigDecimalFilter alacak) {
        this.alacak = alacak;
    }

    public LongFilter getSicilId() {
        return sicilId;
    }

    public LongFilter sicilId() {
        if (sicilId == null) {
            sicilId = new LongFilter();
        }
        return sicilId;
    }

    public void setSicilId(LongFilter sicilId) {
        this.sicilId = sicilId;
    }

    public LongFilter getCariId() {
        return cariId;
    }

    public LongFilter cariId() {
        if (cariId == null) {
            cariId = new LongFilter();
        }
        return cariId;
    }

    public void setCariId(LongFilter cariId) {
        this.cariId = cariId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CariDevirCriteria that = (CariDevirCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(depotipi, that.depotipi) &&
            Objects.equals(yil, that.yil) &&
            Objects.equals(borc, that.borc) &&
            Objects.equals(alacak, that.alacak) &&
            Objects.equals(sicilId, that.sicilId) &&
            Objects.equals(cariId, that.cariId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, depotipi, yil, borc, alacak, sicilId, cariId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CariDevirCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (depotipi != null ? "depotipi=" + depotipi + ", " : "") +
            (yil != null ? "yil=" + yil + ", " : "") +
            (borc != null ? "borc=" + borc + ", " : "") +
            (alacak != null ? "alacak=" + alacak + ", " : "") +
            (sicilId != null ? "sicilId=" + sicilId + ", " : "") +
            (cariId != null ? "cariId=" + cariId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
