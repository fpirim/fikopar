package com.dizel.fikopar.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.dizel.fikopar.domain.Katalog} entity. This class is used
 * in {@link com.dizel.fikopar.web.rest.KatalogResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /katalogs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class KatalogCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter katalogadi;

    private LongFilter stoksId;

    private Boolean distinct;

    public KatalogCriteria() {}

    public KatalogCriteria(KatalogCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.katalogadi = other.katalogadi == null ? null : other.katalogadi.copy();
        this.stoksId = other.stoksId == null ? null : other.stoksId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public KatalogCriteria copy() {
        return new KatalogCriteria(this);
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

    public StringFilter getKatalogadi() {
        return katalogadi;
    }

    public StringFilter katalogadi() {
        if (katalogadi == null) {
            katalogadi = new StringFilter();
        }
        return katalogadi;
    }

    public void setKatalogadi(StringFilter katalogadi) {
        this.katalogadi = katalogadi;
    }

    public LongFilter getStoksId() {
        return stoksId;
    }

    public LongFilter stoksId() {
        if (stoksId == null) {
            stoksId = new LongFilter();
        }
        return stoksId;
    }

    public void setStoksId(LongFilter stoksId) {
        this.stoksId = stoksId;
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
        final KatalogCriteria that = (KatalogCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(katalogadi, that.katalogadi) &&
            Objects.equals(stoksId, that.stoksId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, katalogadi, stoksId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "KatalogCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (katalogadi != null ? "katalogadi=" + katalogadi + ", " : "") +
            (stoksId != null ? "stoksId=" + stoksId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
