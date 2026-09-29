package dev.fikopar.service.criteria;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link dev.fikopar.domain.Cari} entity. This class is used
 * in {@link dev.fikopar.web.rest.CariResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /caris?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CariCriteria implements Serializable, Criteria {

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

    private EvrakTipiFilter tipi;

    private StringFilter evrakno;

    private DepoTipiFilter depotipi;

    private LocalDateFilter tarih;

    private LocalDateFilter vade;

    private IntegerFilter gun;

    private StringFilter aciklama;

    private BigDecimalFilter borc;

    private BigDecimalFilter alacak;

    private LongFilter sicilId;

    private Boolean distinct;

    public CariCriteria() {}

    public CariCriteria(CariCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.tipi = other.tipi == null ? null : other.tipi.copy();
        this.evrakno = other.evrakno == null ? null : other.evrakno.copy();
        this.depotipi = other.depotipi == null ? null : other.depotipi.copy();
        this.tarih = other.tarih == null ? null : other.tarih.copy();
        this.vade = other.vade == null ? null : other.vade.copy();
        this.gun = other.gun == null ? null : other.gun.copy();
        this.aciklama = other.aciklama == null ? null : other.aciklama.copy();
        this.borc = other.borc == null ? null : other.borc.copy();
        this.alacak = other.alacak == null ? null : other.alacak.copy();
        this.sicilId = other.sicilId == null ? null : other.sicilId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CariCriteria copy() {
        return new CariCriteria(this);
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

    public EvrakTipiFilter getTipi() {
        return tipi;
    }

    public EvrakTipiFilter tipi() {
        if (tipi == null) {
            tipi = new EvrakTipiFilter();
        }
        return tipi;
    }

    public void setTipi(EvrakTipiFilter tipi) {
        this.tipi = tipi;
    }

    public StringFilter getEvrakno() {
        return evrakno;
    }

    public StringFilter evrakno() {
        if (evrakno == null) {
            evrakno = new StringFilter();
        }
        return evrakno;
    }

    public void setEvrakno(StringFilter evrakno) {
        this.evrakno = evrakno;
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

    public IntegerFilter getGun() {
        return gun;
    }

    public IntegerFilter gun() {
        if (gun == null) {
            gun = new IntegerFilter();
        }
        return gun;
    }

    public void setGun(IntegerFilter gun) {
        this.gun = gun;
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
        final CariCriteria that = (CariCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(tipi, that.tipi) &&
            Objects.equals(evrakno, that.evrakno) &&
            Objects.equals(depotipi, that.depotipi) &&
            Objects.equals(tarih, that.tarih) &&
            Objects.equals(vade, that.vade) &&
            Objects.equals(gun, that.gun) &&
            Objects.equals(aciklama, that.aciklama) &&
            Objects.equals(borc, that.borc) &&
            Objects.equals(alacak, that.alacak) &&
            Objects.equals(sicilId, that.sicilId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tipi, evrakno, depotipi, tarih, vade, gun, aciklama, borc, alacak, sicilId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CariCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (tipi != null ? "tipi=" + tipi + ", " : "") +
            (evrakno != null ? "evrakno=" + evrakno + ", " : "") +
            (depotipi != null ? "depotipi=" + depotipi + ", " : "") +
            (tarih != null ? "tarih=" + tarih + ", " : "") +
            (vade != null ? "vade=" + vade + ", " : "") +
            (gun != null ? "gun=" + gun + ", " : "") +
            (aciklama != null ? "aciklama=" + aciklama + ", " : "") +
            (borc != null ? "borc=" + borc + ", " : "") +
            (alacak != null ? "alacak=" + alacak + ", " : "") +
            (sicilId != null ? "sicilId=" + sicilId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
