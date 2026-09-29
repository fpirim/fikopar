package com.dizel.fikopar.service.criteria;

import com.dizel.fikopar.domain.enumeration.SicilTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.dizel.fikopar.domain.Sicil} entity. This class is used
 * in {@link com.dizel.fikopar.web.rest.SicilResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /sicils?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SicilCriteria implements Serializable, Criteria {

    /**
     * Class for filtering SicilTipi
     */
    public static class SicilTipiFilter extends Filter<SicilTipi> {

        public SicilTipiFilter() {}

        public SicilTipiFilter(SicilTipiFilter filter) {
            super(filter);
        }

        @Override
        public SicilTipiFilter copy() {
            return new SicilTipiFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter hesapno;

    private StringFilter hesapadi;

    private StringFilter adresi;

    private StringFilter ilce;

    private StringFilter sehir;

    private StringFilter telefon;

    private StringFilter vda;

    private StringFilter vno;

    private StringFilter tcno;

    private IntegerFilter gun;

    private BigDecimalFilter iskonto;

    private SicilTipiFilter siciltipi;

    private LongFilter carisId;

    private Boolean distinct;

    public SicilCriteria() {}

    public SicilCriteria(SicilCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.hesapno = other.hesapno == null ? null : other.hesapno.copy();
        this.hesapadi = other.hesapadi == null ? null : other.hesapadi.copy();
        this.adresi = other.adresi == null ? null : other.adresi.copy();
        this.ilce = other.ilce == null ? null : other.ilce.copy();
        this.sehir = other.sehir == null ? null : other.sehir.copy();
        this.telefon = other.telefon == null ? null : other.telefon.copy();
        this.vda = other.vda == null ? null : other.vda.copy();
        this.vno = other.vno == null ? null : other.vno.copy();
        this.tcno = other.tcno == null ? null : other.tcno.copy();
        this.gun = other.gun == null ? null : other.gun.copy();
        this.iskonto = other.iskonto == null ? null : other.iskonto.copy();
        this.siciltipi = other.siciltipi == null ? null : other.siciltipi.copy();
        this.carisId = other.carisId == null ? null : other.carisId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public SicilCriteria copy() {
        return new SicilCriteria(this);
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

    public StringFilter getHesapno() {
        return hesapno;
    }

    public StringFilter hesapno() {
        if (hesapno == null) {
            hesapno = new StringFilter();
        }
        return hesapno;
    }

    public void setHesapno(StringFilter hesapno) {
        this.hesapno = hesapno;
    }

    public StringFilter getHesapadi() {
        return hesapadi;
    }

    public StringFilter hesapadi() {
        if (hesapadi == null) {
            hesapadi = new StringFilter();
        }
        return hesapadi;
    }

    public void setHesapadi(StringFilter hesapadi) {
        this.hesapadi = hesapadi;
    }

    public StringFilter getAdresi() {
        return adresi;
    }

    public StringFilter adresi() {
        if (adresi == null) {
            adresi = new StringFilter();
        }
        return adresi;
    }

    public void setAdresi(StringFilter adresi) {
        this.adresi = adresi;
    }

    public StringFilter getIlce() {
        return ilce;
    }

    public StringFilter ilce() {
        if (ilce == null) {
            ilce = new StringFilter();
        }
        return ilce;
    }

    public void setIlce(StringFilter ilce) {
        this.ilce = ilce;
    }

    public StringFilter getSehir() {
        return sehir;
    }

    public StringFilter sehir() {
        if (sehir == null) {
            sehir = new StringFilter();
        }
        return sehir;
    }

    public void setSehir(StringFilter sehir) {
        this.sehir = sehir;
    }

    public StringFilter getTelefon() {
        return telefon;
    }

    public StringFilter telefon() {
        if (telefon == null) {
            telefon = new StringFilter();
        }
        return telefon;
    }

    public void setTelefon(StringFilter telefon) {
        this.telefon = telefon;
    }

    public StringFilter getVda() {
        return vda;
    }

    public StringFilter vda() {
        if (vda == null) {
            vda = new StringFilter();
        }
        return vda;
    }

    public void setVda(StringFilter vda) {
        this.vda = vda;
    }

    public StringFilter getVno() {
        return vno;
    }

    public StringFilter vno() {
        if (vno == null) {
            vno = new StringFilter();
        }
        return vno;
    }

    public void setVno(StringFilter vno) {
        this.vno = vno;
    }

    public StringFilter getTcno() {
        return tcno;
    }

    public StringFilter tcno() {
        if (tcno == null) {
            tcno = new StringFilter();
        }
        return tcno;
    }

    public void setTcno(StringFilter tcno) {
        this.tcno = tcno;
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

    public SicilTipiFilter getSiciltipi() {
        return siciltipi;
    }

    public SicilTipiFilter siciltipi() {
        if (siciltipi == null) {
            siciltipi = new SicilTipiFilter();
        }
        return siciltipi;
    }

    public void setSiciltipi(SicilTipiFilter siciltipi) {
        this.siciltipi = siciltipi;
    }

    public LongFilter getCarisId() {
        return carisId;
    }

    public LongFilter carisId() {
        if (carisId == null) {
            carisId = new LongFilter();
        }
        return carisId;
    }

    public void setCarisId(LongFilter carisId) {
        this.carisId = carisId;
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
        final SicilCriteria that = (SicilCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(hesapno, that.hesapno) &&
            Objects.equals(hesapadi, that.hesapadi) &&
            Objects.equals(adresi, that.adresi) &&
            Objects.equals(ilce, that.ilce) &&
            Objects.equals(sehir, that.sehir) &&
            Objects.equals(telefon, that.telefon) &&
            Objects.equals(vda, that.vda) &&
            Objects.equals(vno, that.vno) &&
            Objects.equals(tcno, that.tcno) &&
            Objects.equals(gun, that.gun) &&
            Objects.equals(iskonto, that.iskonto) &&
            Objects.equals(siciltipi, that.siciltipi) &&
            Objects.equals(carisId, that.carisId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            hesapno,
            hesapadi,
            adresi,
            ilce,
            sehir,
            telefon,
            vda,
            vno,
            tcno,
            gun,
            iskonto,
            siciltipi,
            carisId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SicilCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (hesapno != null ? "hesapno=" + hesapno + ", " : "") +
            (hesapadi != null ? "hesapadi=" + hesapadi + ", " : "") +
            (adresi != null ? "adresi=" + adresi + ", " : "") +
            (ilce != null ? "ilce=" + ilce + ", " : "") +
            (sehir != null ? "sehir=" + sehir + ", " : "") +
            (telefon != null ? "telefon=" + telefon + ", " : "") +
            (vda != null ? "vda=" + vda + ", " : "") +
            (vno != null ? "vno=" + vno + ", " : "") +
            (tcno != null ? "tcno=" + tcno + ", " : "") +
            (gun != null ? "gun=" + gun + ", " : "") +
            (iskonto != null ? "iskonto=" + iskonto + ", " : "") +
            (siciltipi != null ? "siciltipi=" + siciltipi + ", " : "") +
            (carisId != null ? "carisId=" + carisId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
