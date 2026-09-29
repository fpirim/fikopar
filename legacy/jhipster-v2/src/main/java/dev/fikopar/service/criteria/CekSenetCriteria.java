package dev.fikopar.service.criteria;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link dev.fikopar.domain.CekSenet} entity. This class is used
 * in {@link dev.fikopar.web.rest.CekSenetResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cek-senets?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CekSenetCriteria implements Serializable, Criteria {

    /**
     * Class for filtering EvrakTipi
     */
    public static class EvrakTipiFilter extends Filter<EvrakTipi> {

        public EvrakTipiFilter() {}

        public EvrakTipiFilter(EvrakTipiFilter filter) {
            super(filter);
        }

        @Override
        public EvrakTipiFilter copy() {
            return new EvrakTipiFilter(this);
        }
    }

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

    private StringFilter ceksenetno;

    private EvrakTipiFilter evraktipi;

    private DepoTipiFilter depotipi;

    private LocalDateFilter tarihi;

    private LocalDateFilter vade;

    private StringFilter aciklama;

    private BigDecimalFilter tutar;

    private LongFilter sicilId;

    private LongFilter cekSenetDetaysId;

    private Boolean distinct;

    public CekSenetCriteria() {}

    public CekSenetCriteria(CekSenetCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.ceksenetno = other.ceksenetno == null ? null : other.ceksenetno.copy();
        this.evraktipi = other.evraktipi == null ? null : other.evraktipi.copy();
        this.depotipi = other.depotipi == null ? null : other.depotipi.copy();
        this.tarihi = other.tarihi == null ? null : other.tarihi.copy();
        this.vade = other.vade == null ? null : other.vade.copy();
        this.aciklama = other.aciklama == null ? null : other.aciklama.copy();
        this.tutar = other.tutar == null ? null : other.tutar.copy();
        this.sicilId = other.sicilId == null ? null : other.sicilId.copy();
        this.cekSenetDetaysId = other.cekSenetDetaysId == null ? null : other.cekSenetDetaysId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CekSenetCriteria copy() {
        return new CekSenetCriteria(this);
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

    public StringFilter getCeksenetno() {
        return ceksenetno;
    }

    public StringFilter ceksenetno() {
        if (ceksenetno == null) {
            ceksenetno = new StringFilter();
        }
        return ceksenetno;
    }

    public void setCeksenetno(StringFilter ceksenetno) {
        this.ceksenetno = ceksenetno;
    }

    public EvrakTipiFilter getEvraktipi() {
        return evraktipi;
    }

    public EvrakTipiFilter evraktipi() {
        if (evraktipi == null) {
            evraktipi = new EvrakTipiFilter();
        }
        return evraktipi;
    }

    public void setEvraktipi(EvrakTipiFilter evraktipi) {
        this.evraktipi = evraktipi;
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

    public LocalDateFilter getVade() {
        return vade;
    }

    public LocalDateFilter vade() {
        if (vade == null) {
            vade = new LocalDateFilter();
        }
        return vade;
    }

    public void setVade(LocalDateFilter vade) {
        this.vade = vade;
    }

    public StringFilter getAciklama() {
        return aciklama;
    }

    public StringFilter aciklama() {
        if (aciklama == null) {
            aciklama = new StringFilter();
        }
        return aciklama;
    }

    public void setAciklama(StringFilter aciklama) {
        this.aciklama = aciklama;
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

    public LongFilter getCekSenetDetaysId() {
        return cekSenetDetaysId;
    }

    public LongFilter cekSenetDetaysId() {
        if (cekSenetDetaysId == null) {
            cekSenetDetaysId = new LongFilter();
        }
        return cekSenetDetaysId;
    }

    public void setCekSenetDetaysId(LongFilter cekSenetDetaysId) {
        this.cekSenetDetaysId = cekSenetDetaysId;
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
        final CekSenetCriteria that = (CekSenetCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(ceksenetno, that.ceksenetno) &&
            Objects.equals(evraktipi, that.evraktipi) &&
            Objects.equals(depotipi, that.depotipi) &&
            Objects.equals(tarihi, that.tarihi) &&
            Objects.equals(vade, that.vade) &&
            Objects.equals(aciklama, that.aciklama) &&
            Objects.equals(tutar, that.tutar) &&
            Objects.equals(sicilId, that.sicilId) &&
            Objects.equals(cekSenetDetaysId, that.cekSenetDetaysId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ceksenetno, evraktipi, depotipi, tarihi, vade, aciklama, tutar, sicilId, cekSenetDetaysId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CekSenetCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (ceksenetno != null ? "ceksenetno=" + ceksenetno + ", " : "") +
            (evraktipi != null ? "evraktipi=" + evraktipi + ", " : "") +
            (depotipi != null ? "depotipi=" + depotipi + ", " : "") +
            (tarihi != null ? "tarihi=" + tarihi + ", " : "") +
            (vade != null ? "vade=" + vade + ", " : "") +
            (aciklama != null ? "aciklama=" + aciklama + ", " : "") +
            (tutar != null ? "tutar=" + tutar + ", " : "") +
            (sicilId != null ? "sicilId=" + sicilId + ", " : "") +
            (cekSenetDetaysId != null ? "cekSenetDetaysId=" + cekSenetDetaysId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
