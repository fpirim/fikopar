package com.dizel.fikopar.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.dizel.fikopar.domain.HareketDetay} entity. This class is used
 * in {@link com.dizel.fikopar.web.rest.HareketDetayResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /hareket-detays?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HareketDetayCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter stokno;

    private StringFilter parcano;

    private StringFilter tipi;

    private StringFilter cinsi;

    private StringFilter marka;

    private BigDecimalFilter iskonto;

    private BigDecimalFilter kdv;

    private BigDecimalFilter adet;

    private BigDecimalFilter fiyati;

    private LongFilter hareketSicilId;

    private Boolean distinct;

    public HareketDetayCriteria() {}

    public HareketDetayCriteria(HareketDetayCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.stokno = other.stokno == null ? null : other.stokno.copy();
        this.parcano = other.parcano == null ? null : other.parcano.copy();
        this.tipi = other.tipi == null ? null : other.tipi.copy();
        this.cinsi = other.cinsi == null ? null : other.cinsi.copy();
        this.marka = other.marka == null ? null : other.marka.copy();
        this.iskonto = other.iskonto == null ? null : other.iskonto.copy();
        this.kdv = other.kdv == null ? null : other.kdv.copy();
        this.adet = other.adet == null ? null : other.adet.copy();
        this.fiyati = other.fiyati == null ? null : other.fiyati.copy();
        this.hareketSicilId = other.hareketSicilId == null ? null : other.hareketSicilId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public HareketDetayCriteria copy() {
        return new HareketDetayCriteria(this);
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

    public StringFilter getStokno() {
        return stokno;
    }

    public StringFilter stokno() {
        if (stokno == null) {
            stokno = new StringFilter();
        }
        return stokno;
    }

    public void setStokno(StringFilter stokno) {
        this.stokno = stokno;
    }

    public StringFilter getParcano() {
        return parcano;
    }

    public StringFilter parcano() {
        if (parcano == null) {
            parcano = new StringFilter();
        }
        return parcano;
    }

    public void setParcano(StringFilter parcano) {
        this.parcano = parcano;
    }

    public StringFilter getTipi() {
        return tipi;
    }

    public StringFilter tipi() {
        if (tipi == null) {
            tipi = new StringFilter();
        }
        return tipi;
    }

    public void setTipi(StringFilter tipi) {
        this.tipi = tipi;
    }

    public StringFilter getCinsi() {
        return cinsi;
    }

    public StringFilter cinsi() {
        if (cinsi == null) {
            cinsi = new StringFilter();
        }
        return cinsi;
    }

    public void setCinsi(StringFilter cinsi) {
        this.cinsi = cinsi;
    }

    public StringFilter getMarka() {
        return marka;
    }

    public StringFilter marka() {
        if (marka == null) {
            marka = new StringFilter();
        }
        return marka;
    }

    public void setMarka(StringFilter marka) {
        this.marka = marka;
    }

    public BigDecimalFilter getIskonto() {
        return iskonto;
    }

    public BigDecimalFilter iskonto() {
        if (iskonto == null) {
            iskonto = new BigDecimalFilter();
        }
        return iskonto;
    }

    public void setIskonto(BigDecimalFilter iskonto) {
        this.iskonto = iskonto;
    }

    public BigDecimalFilter getKdv() {
        return kdv;
    }

    public BigDecimalFilter kdv() {
        if (kdv == null) {
            kdv = new BigDecimalFilter();
        }
        return kdv;
    }

    public void setKdv(BigDecimalFilter kdv) {
        this.kdv = kdv;
    }

    public BigDecimalFilter getAdet() {
        return adet;
    }

    public BigDecimalFilter adet() {
        if (adet == null) {
            adet = new BigDecimalFilter();
        }
        return adet;
    }

    public void setAdet(BigDecimalFilter adet) {
        this.adet = adet;
    }

    public BigDecimalFilter getFiyati() {
        return fiyati;
    }

    public BigDecimalFilter fiyati() {
        if (fiyati == null) {
            fiyati = new BigDecimalFilter();
        }
        return fiyati;
    }

    public void setFiyati(BigDecimalFilter fiyati) {
        this.fiyati = fiyati;
    }

    public LongFilter getHareketSicilId() {
        return hareketSicilId;
    }

    public LongFilter hareketSicilId() {
        if (hareketSicilId == null) {
            hareketSicilId = new LongFilter();
        }
        return hareketSicilId;
    }

    public void setHareketSicilId(LongFilter hareketSicilId) {
        this.hareketSicilId = hareketSicilId;
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
        final HareketDetayCriteria that = (HareketDetayCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(stokno, that.stokno) &&
            Objects.equals(parcano, that.parcano) &&
            Objects.equals(tipi, that.tipi) &&
            Objects.equals(cinsi, that.cinsi) &&
            Objects.equals(marka, that.marka) &&
            Objects.equals(iskonto, that.iskonto) &&
            Objects.equals(kdv, that.kdv) &&
            Objects.equals(adet, that.adet) &&
            Objects.equals(fiyati, that.fiyati) &&
            Objects.equals(hareketSicilId, that.hareketSicilId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stokno, parcano, tipi, cinsi, marka, iskonto, kdv, adet, fiyati, hareketSicilId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HareketDetayCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (stokno != null ? "stokno=" + stokno + ", " : "") +
            (parcano != null ? "parcano=" + parcano + ", " : "") +
            (tipi != null ? "tipi=" + tipi + ", " : "") +
            (cinsi != null ? "cinsi=" + cinsi + ", " : "") +
            (marka != null ? "marka=" + marka + ", " : "") +
            (iskonto != null ? "iskonto=" + iskonto + ", " : "") +
            (kdv != null ? "kdv=" + kdv + ", " : "") +
            (adet != null ? "adet=" + adet + ", " : "") +
            (fiyati != null ? "fiyati=" + fiyati + ", " : "") +
            (hareketSicilId != null ? "hareketSicilId=" + hareketSicilId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
