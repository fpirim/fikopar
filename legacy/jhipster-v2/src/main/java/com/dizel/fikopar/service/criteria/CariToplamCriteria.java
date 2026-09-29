package com.dizel.fikopar.service.criteria;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.LocalDateFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.dizel.fikopar.domain.Cari} entity. This class is used
 * in {@link com.dizel.fikopar.web.rest.CariResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /caris?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CariToplamCriteria implements Serializable, Criteria {

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

    private StringFilter filter;
    private DepoTipiFilter depotipi;
    private LocalDateFilter tarih;

    public CariToplamCriteria() {}

    public CariToplamCriteria(CariToplamCriteria other) {
        this.filter = other.filter == null ? null : other.filter.copy();
        this.depotipi = other.depotipi == null ? null : other.depotipi.copy();
        this.tarih = other.tarih == null ? null : other.tarih.copy();
    }

    @Override
    public CariToplamCriteria copy() {
        return new CariToplamCriteria(this);
    }

    public StringFilter getFilter() {
        return filter;
    }

    public StringFilter filter() {
        if (filter == null) {
            filter = new StringFilter();
        }
        return filter;
    }

    public void setFilter(StringFilter filter) {
        this.filter = filter;
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

    public LocalDateFilter getTarih() {
        return tarih;
    }

    public LocalDateFilter tarih() {
        if (tarih == null) {
            tarih = new LocalDateFilter();
        }
        return tarih;
    }

    public void setTarih(LocalDateFilter tarih) {
        this.tarih = tarih;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CariToplamCriteria that = (CariToplamCriteria) o;

        return (Objects.equals(filter, that.filter) && Objects.equals(depotipi, that.depotipi) && Objects.equals(tarih, that.tarih));
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter, depotipi, tarih);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CariToplamCriteria{" +
            (filter != null ? "filter=" + filter + ", " : "") +
            (depotipi != null ? "depotipi=" + depotipi + ", " : "") +
            (tarih != null ? "tarih=" + tarih + ", " : "") +
            "}";
    }
}
