package dev.fikopar.service.criteria;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link dev.fikopar.domain.HareketSicil} entity. This class is used
 * in {@link dev.fikopar.web.rest.HareketSicilResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /hareket-sicils?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HareketSicilCriteria implements Serializable, Criteria {

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

    private EvrakTipiFilter evraktipi;

    private StringFilter evrakno;

    private LocalDateFilter tarihi;

    private LocalDateFilter sevktarihi;

    private StringFilter saati;

    private StringFilter hesapno;

    private StringFilter hesapadi;

    private StringFilter adresi;

    private StringFilter ilce;

    private StringFilter sehir;

    private StringFilter vda;

    private StringFilter tcno;

    private StringFilter vno;

    private StringFilter telefon;

    private IntegerFilter gun;

    private BigDecimalFilter tutari;

    private StringFilter notlar;

    private DepoTipiFilter depotipi;

    private BigDecimalFilter toplam;

    private BigDecimalFilter isktoplam;

    private BigDecimalFilter kdvtoplam;

    private BigDecimalFilter aratoplam;

    private LongFilter sicilId;

    private LongFilter cariId;

    private LongFilter hareketDetaysId;

    private Boolean distinct;

    public HareketSicilCriteria() {}

    public HareketSicilCriteria(HareketSicilCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.evraktipi = other.evraktipi == null ? null : other.evraktipi.copy();
        this.evrakno = other.evrakno == null ? null : other.evrakno.copy();
        this.tarihi = other.tarihi == null ? null : other.tarihi.copy();
        this.sevktarihi = other.sevktarihi == null ? null : other.sevktarihi.copy();
        this.saati = other.saati == null ? null : other.saati.copy();
        this.hesapno = other.hesapno == null ? null : other.hesapno.copy();
        this.hesapadi = other.hesapadi == null ? null : other.hesapadi.copy();
        this.adresi = other.adresi == null ? null : other.adresi.copy();
        this.ilce = other.ilce == null ? null : other.ilce.copy();
        this.sehir = other.sehir == null ? null : other.sehir.copy();
        this.vda = other.vda == null ? null : other.vda.copy();
        this.tcno = other.tcno == null ? null : other.tcno.copy();
        this.vno = other.vno == null ? null : other.vno.copy();
        this.telefon = other.telefon == null ? null : other.telefon.copy();
        this.gun = other.gun == null ? null : other.gun.copy();
        this.tutari = other.tutari == null ? null : other.tutari.copy();
        this.notlar = other.notlar == null ? null : other.notlar.copy();
        this.depotipi = other.depotipi == null ? null : other.depotipi.copy();
        this.toplam = other.toplam == null ? null : other.toplam.copy();
        this.isktoplam = other.isktoplam == null ? null : other.isktoplam.copy();
        this.kdvtoplam = other.kdvtoplam == null ? null : other.kdvtoplam.copy();
        this.aratoplam = other.aratoplam == null ? null : other.aratoplam.copy();
        this.sicilId = other.sicilId == null ? null : other.sicilId.copy();
        this.cariId = other.cariId == null ? null : other.cariId.copy();
        this.hareketDetaysId = other.hareketDetaysId == null ? null : other.hareketDetaysId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public HareketSicilCriteria copy() {
        return new HareketSicilCriteria(this);
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

    public LocalDateFilter getSevktarihi() {
        return sevktarihi;
    }

    public LocalDateFilter sevktarihi() {
        if (sevktarihi == null) {
            sevktarihi = new LocalDateFilter();
        }
        return sevktarihi;
    }

    public void setSevktarihi(LocalDateFilter sevktarihi) {
        this.sevktarihi = sevktarihi;
    }

    public StringFilter getSaati() {
        return saati;
    }

    public StringFilter saati() {
        if (saati == null) {
            saati = new StringFilter();
        }
        return saati;
    }

    public void setSaati(StringFilter saati) {
        this.saati = saati;
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

    public BigDecimalFilter getTutari() {
        return tutari;
    }

    public BigDecimalFilter tutari() {
        if (tutari == null) {
            tutari = new BigDecimalFilter();
        }
        return tutari;
    }

    public void setTutari(BigDecimalFilter tutari) {
        this.tutari = tutari;
    }

    public StringFilter getNotlar() {
        return notlar;
    }

    public StringFilter notlar() {
        if (notlar == null) {
            notlar = new StringFilter();
        }
        return notlar;
    }

    public void setNotlar(StringFilter notlar) {
        this.notlar = notlar;
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

    public BigDecimalFilter getToplam() {
        return toplam;
    }

    public BigDecimalFilter toplam() {
        if (toplam == null) {
            toplam = new BigDecimalFilter();
        }
        return toplam;
    }

    public void setToplam(BigDecimalFilter toplam) {
        this.toplam = toplam;
    }

    public BigDecimalFilter getIsktoplam() {
        return isktoplam;
    }

    public BigDecimalFilter isktoplam() {
        if (isktoplam == null) {
            isktoplam = new BigDecimalFilter();
        }
        return isktoplam;
    }

    public void setIsktoplam(BigDecimalFilter isktoplam) {
        this.isktoplam = isktoplam;
    }

    public BigDecimalFilter getKdvtoplam() {
        return kdvtoplam;
    }

    public BigDecimalFilter kdvtoplam() {
        if (kdvtoplam == null) {
            kdvtoplam = new BigDecimalFilter();
        }
        return kdvtoplam;
    }

    public void setKdvtoplam(BigDecimalFilter kdvtoplam) {
        this.kdvtoplam = kdvtoplam;
    }

    public BigDecimalFilter getAratoplam() {
        return aratoplam;
    }

    public BigDecimalFilter aratoplam() {
        if (aratoplam == null) {
            aratoplam = new BigDecimalFilter();
        }
        return aratoplam;
    }

    public void setAratoplam(BigDecimalFilter aratoplam) {
        this.aratoplam = aratoplam;
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

    public LongFilter getHareketDetaysId() {
        return hareketDetaysId;
    }

    public LongFilter hareketDetaysId() {
        if (hareketDetaysId == null) {
            hareketDetaysId = new LongFilter();
        }
        return hareketDetaysId;
    }

    public void setHareketDetaysId(LongFilter hareketDetaysId) {
        this.hareketDetaysId = hareketDetaysId;
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
        final HareketSicilCriteria that = (HareketSicilCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(evraktipi, that.evraktipi) &&
            Objects.equals(evrakno, that.evrakno) &&
            Objects.equals(tarihi, that.tarihi) &&
            Objects.equals(sevktarihi, that.sevktarihi) &&
            Objects.equals(saati, that.saati) &&
            Objects.equals(hesapno, that.hesapno) &&
            Objects.equals(hesapadi, that.hesapadi) &&
            Objects.equals(adresi, that.adresi) &&
            Objects.equals(ilce, that.ilce) &&
            Objects.equals(sehir, that.sehir) &&
            Objects.equals(vda, that.vda) &&
            Objects.equals(tcno, that.tcno) &&
            Objects.equals(vno, that.vno) &&
            Objects.equals(telefon, that.telefon) &&
            Objects.equals(gun, that.gun) &&
            Objects.equals(tutari, that.tutari) &&
            Objects.equals(notlar, that.notlar) &&
            Objects.equals(depotipi, that.depotipi) &&
            Objects.equals(toplam, that.toplam) &&
            Objects.equals(isktoplam, that.isktoplam) &&
            Objects.equals(kdvtoplam, that.kdvtoplam) &&
            Objects.equals(aratoplam, that.aratoplam) &&
            Objects.equals(sicilId, that.sicilId) &&
            Objects.equals(cariId, that.cariId) &&
            Objects.equals(hareketDetaysId, that.hareketDetaysId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            evraktipi,
            evrakno,
            tarihi,
            sevktarihi,
            saati,
            hesapno,
            hesapadi,
            adresi,
            ilce,
            sehir,
            vda,
            tcno,
            vno,
            telefon,
            gun,
            tutari,
            notlar,
            depotipi,
            toplam,
            isktoplam,
            kdvtoplam,
            aratoplam,
            sicilId,
            cariId,
            hareketDetaysId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HareketSicilCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (evraktipi != null ? "evraktipi=" + evraktipi + ", " : "") +
            (evrakno != null ? "evrakno=" + evrakno + ", " : "") +
            (tarihi != null ? "tarihi=" + tarihi + ", " : "") +
            (sevktarihi != null ? "sevktarihi=" + sevktarihi + ", " : "") +
            (saati != null ? "saati=" + saati + ", " : "") +
            (hesapno != null ? "hesapno=" + hesapno + ", " : "") +
            (hesapadi != null ? "hesapadi=" + hesapadi + ", " : "") +
            (adresi != null ? "adresi=" + adresi + ", " : "") +
            (ilce != null ? "ilce=" + ilce + ", " : "") +
            (sehir != null ? "sehir=" + sehir + ", " : "") +
            (vda != null ? "vda=" + vda + ", " : "") +
            (tcno != null ? "tcno=" + tcno + ", " : "") +
            (vno != null ? "vno=" + vno + ", " : "") +
            (telefon != null ? "telefon=" + telefon + ", " : "") +
            (gun != null ? "gun=" + gun + ", " : "") +
            (tutari != null ? "tutari=" + tutari + ", " : "") +
            (notlar != null ? "notlar=" + notlar + ", " : "") +
            (depotipi != null ? "depotipi=" + depotipi + ", " : "") +
            (toplam != null ? "toplam=" + toplam + ", " : "") +
            (isktoplam != null ? "isktoplam=" + isktoplam + ", " : "") +
            (kdvtoplam != null ? "kdvtoplam=" + kdvtoplam + ", " : "") +
            (aratoplam != null ? "aratoplam=" + aratoplam + ", " : "") +
            (sicilId != null ? "sicilId=" + sicilId + ", " : "") +
            (cariId != null ? "cariId=" + cariId + ", " : "") +
            (hareketDetaysId != null ? "hareketDetaysId=" + hareketDetaysId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
