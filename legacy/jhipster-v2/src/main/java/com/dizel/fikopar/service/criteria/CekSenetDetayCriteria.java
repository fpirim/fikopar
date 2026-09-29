package com.dizel.fikopar.service.criteria;

import com.dizel.fikopar.domain.enumeration.IslemTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.dizel.fikopar.domain.CekSenetDetay} entity. This class is used
 * in {@link com.dizel.fikopar.web.rest.CekSenetDetayResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cek-senet-detays?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CekSenetDetayCriteria implements Serializable, Criteria {

    /**
     * Class for filtering IslemTipi
     */
    public static class IslemTipiFilter extends Filter<IslemTipi> {

        public IslemTipiFilter() {}

        public IslemTipiFilter(IslemTipiFilter filter) {
            super(filter);
        }

        @Override
        public IslemTipiFilter copy() {
            return new IslemTipiFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private IslemTipiFilter islemtipi;

    private LocalDateFilter tarihi;

    private BigDecimalFilter tutar;

    private LongFilter sicilId;

    private LongFilter makbuzId;

    private LongFilter cekSenetId;

    private Boolean distinct;

    public CekSenetDetayCriteria() {}

    public CekSenetDetayCriteria(CekSenetDetayCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.islemtipi = other.islemtipi == null ? null : other.islemtipi.copy();
        this.tarihi = other.tarihi == null ? null : other.tarihi.copy();
        this.tutar = other.tutar == null ? null : other.tutar.copy();
        this.sicilId = other.sicilId == null ? null : other.sicilId.copy();
        this.makbuzId = other.makbuzId == null ? null : other.makbuzId.copy();
        this.cekSenetId = other.cekSenetId == null ? null : other.cekSenetId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CekSenetDetayCriteria copy() {
        return new CekSenetDetayCriteria(this);
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

    public IslemTipiFilter getIslemtipi() {
        return islemtipi;
    }

    public IslemTipiFilter islemtipi() {
        if (islemtipi == null) {
            islemtipi = new IslemTipiFilter();
        }
        return islemtipi;
    }

    public void setIslemtipi(IslemTipiFilter islemtipi) {
        this.islemtipi = islemtipi;
    }

    public LocalDateFilter getTarihi() {
        return tarihi;
    }

    public LocalDateFilter tarihi() {
        if (tarihi == null) {
            tarihi = new LocalDateFilter();
        }
        return tarihi;
    }

    public void setTarihi(LocalDateFilter tarihi) {
        this.tarihi = tarihi;
    }

    public BigDecimalFilter getTutar() {
        return tutar;
    }

    public BigDecimalFilter tutar() {
        if (tutar == null) {
            tutar = new BigDecimalFilter();
        }
        return tutar;
    }

    public void setTutar(BigDecimalFilter tutar) {
        this.tutar = tutar;
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

    public LongFilter getMakbuzId() {
        return makbuzId;
    }

    public LongFilter makbuzId() {
        if (makbuzId == null) {
            makbuzId = new LongFilter();
        }
        return makbuzId;
    }

    public void setMakbuzId(LongFilter makbuzId) {
        this.makbuzId = makbuzId;
    }

    public LongFilter getCekSenetId() {
        return cekSenetId;
    }

    public LongFilter cekSenetId() {
        if (cekSenetId == null) {
            cekSenetId = new LongFilter();
        }
        return cekSenetId;
    }

    public void setCekSenetId(LongFilter cekSenetId) {
        this.cekSenetId = cekSenetId;
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
        final CekSenetDetayCriteria that = (CekSenetDetayCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(islemtipi, that.islemtipi) &&
            Objects.equals(tarihi, that.tarihi) &&
            Objects.equals(tutar, that.tutar) &&
            Objects.equals(sicilId, that.sicilId) &&
            Objects.equals(makbuzId, that.makbuzId) &&
            Objects.equals(cekSenetId, that.cekSenetId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, islemtipi, tarihi, tutar, sicilId, makbuzId, cekSenetId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CekSenetDetayCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (islemtipi != null ? "islemtipi=" + islemtipi + ", " : "") +
            (tarihi != null ? "tarihi=" + tarihi + ", " : "") +
            (tutar != null ? "tutar=" + tutar + ", " : "") +
            (sicilId != null ? "sicilId=" + sicilId + ", " : "") +
            (makbuzId != null ? "makbuzId=" + makbuzId + ", " : "") +
            (cekSenetId != null ? "cekSenetId=" + cekSenetId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
