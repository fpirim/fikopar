package tr.fikopar.db.models.cobol.hareket;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "`COB_HAREKET-SICIL`", schema="`COBOL`")
public class HarsicModel implements Serializable {
	@EmbeddedId
	private HarsicModelPK harsicModelPK;
	@Column(name="`HS-TARIH`", columnDefinition="VARCHAR", length=8, nullable=false)
	private String tarih;
	@Column(name="`HS-SAAT`", columnDefinition="VARCHAR", length=6, nullable=false)
	private String saat;
	@Column(name="`HS-FTARIH`", columnDefinition="VARCHAR", length=8, nullable=false)
	private String ftarih;
	@Column(name="`HS-HESAPNO`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String hesapno;
	@Column(name="`HS-HESAPADI`", columnDefinition="VARCHAR", length=50, nullable=false)
	private String hesapadi;
	@Column(name="`HS-YETKILI`", columnDefinition="VARCHAR", length=40, nullable=false)
	private String yetkili;
	@Column(name="`HS-ADRES1`", columnDefinition="VARCHAR", length=40, nullable=false)
	private String adres1;
	@Column(name="`HS-ADRES2`", columnDefinition="VARCHAR", length=40, nullable=false)
	private String adres2;
	@Column(name="`HS-ADRES3`", columnDefinition="VARCHAR", length=40, nullable=false)	
	private String adres3;
	@Column(name="`HS-MAHALLE`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String mahalle;
	@Column(name="`HS-ILCE`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String ilce;
	@Column(name="`HS-IL`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String il;
	@Column(name="`HS-ULKE`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String ulke;
	@Column(name="`HS-VDA`", columnDefinition="VARCHAR", length=30, nullable=false)
	private String vda;
	@Column(name="`HS-VNO`", columnDefinition="VARCHAR", length=11, nullable=false)
	private String vno;
	@Column(name="`HS-TCNO`", columnDefinition="VARCHAR", length=11, nullable=false)
	private String tcno;
	@Column(name="`HS-TSNO`", columnDefinition="VARCHAR", length=11, nullable=false)
	private String tsno;
	@Column(name="`HS-MERSIS`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String mersis;
	@Column(name="`HS-GUN`", columnDefinition="DECIMAL", precision=5, scale=0, nullable=false)
	private BigDecimal gun;
	@Column(name="`HS-PLKOD`", columnDefinition="DECIMAL", precision=4, scale=0, nullable=false)
	private BigDecimal plkod;
	@Column(name="`HS-DEPONO`", columnDefinition="DECIMAL", precision=4, scale=0, nullable=false)
	private BigDecimal depono;
	@Column(name="`HS-DUZEN`", columnDefinition="DECIMAL", precision=4, scale=0, nullable=false)
	private BigDecimal duzen;
	@Column(name="`HS-BAGTIPI1`", columnDefinition="DECIMAL", precision=2, scale=0, nullable=false)
	private BigDecimal bag1;
	@Column(name="`HS-BEVRAKNO1`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String bevrakno1;
	@Column(name="`HS-BAGTIPI2`", columnDefinition="DECIMAL", precision=2, scale=0, nullable=false)
	private Integer bag2;
	@Column(name="`HS-BEVRAKNO2`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String bevrakno2;
	@Column(name="`HS-SONUC`", columnDefinition="DECIMAL", precision=2, scale=0, nullable=false)
	private BigDecimal sonuc;
    
	
	@OneToMany(targetEntity=HardetModel.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumns({
		@JoinColumn(name = "`HD-EVRAKNO`", referencedColumnName="`HS-EVRAKNO`"),
		@JoinColumn(name = "`HD-TIP`", referencedColumnName="`HS-TIP`")
	})
	private Set<HardetModel> hardetModels = new HashSet<HardetModel>();
	public Set<HardetModel> getHardetModels() {
		return hardetModels;
	}
	public void setHardetModels(Set<HardetModel> hardetModels) {
		this.hardetModels = hardetModels;
	}
	
	public HarsicModelPK getHarsicModelPK() {
		return harsicModelPK;
	}
	public void setHarsicModelPK(HarsicModelPK harsicModelPK) {
		this.harsicModelPK = harsicModelPK;
	}
	
	public String getTarih() {
		return tarih;
	}
	public void setTarih(String tarih) {
		this.tarih = tarih;
	}
	public String getSaat() {
		return saat;
	}
	public void setSaat(String saat) {
		this.saat = saat;
	}
	public String getFtarih() {
		return ftarih;
	}
	public void setFtarih(String ftarih) {
		this.ftarih = ftarih;
	}
	public String getHesapno() {
		return hesapno;
	}
	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}
	public String getHesapadi() {
		return hesapadi;
	}
	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi;
	}
	public String getYetkili() {
		return yetkili;
	}
	public void setYetkili(String yetkili) {
		this.yetkili = yetkili;
	}
	public String getAdres1() {
		return adres1;
	}
	public void setAdres1(String adres1) {
		this.adres1 = adres1;
	}
	public String getAdres2() {
		return adres2;
	}
	public void setAdres2(String adres2) {
		this.adres2 = adres2;
	}
	public String getAdres3() {
		return adres3;
	}
	public void setAdres3(String adres3) {
		this.adres3 = adres3;
	}
	public String getMahalle() {
		return mahalle;
	}
	public void setMahalle(String mahalle) {
		this.mahalle = mahalle;
	}
	public String getIlce() {
		return ilce;
	}
	public void setIlce(String ilce) {
		this.ilce = ilce;
	}
	public String getIl() {
		return il;
	}
	public void setIl(String il) {
		this.il = il;
	}
	public String getUlke() {
		return ulke;
	}
	public void setUlke(String ulke) {
		this.ulke = ulke;
	}
	public String getVda() {
		return vda;
	}
	public void setVda(String vda) {
		this.vda = vda;
	}
	public String getVno() {
		return vno;
	}
	public void setVno(String vno) {
		this.vno = vno;
	}
	public String getTcno() {
		return tcno;
	}
	public void setTcno(String tcno) {
		this.tcno = tcno;
	}
	public String getTsno() {
		return tsno;
	}
	public void setTsno(String tsno) {
		this.tsno = tsno;
	}
	public String getMersis() {
		return mersis;
	}
	public void setMersis(String mersis) {
		this.mersis = mersis;
	}
	public BigDecimal getGun() {
		return gun;
	}
	public void setGun(BigDecimal gun) {
		this.gun = gun;
	}
	public BigDecimal getPlkod() {
		return plkod;
	}
	public void setPlkod(BigDecimal plkod) {
		this.plkod = plkod;
	}
	public BigDecimal getDepono() {
		return depono;
	}
	public void setDepono(BigDecimal depono) {
		this.depono = depono;
	}
	public BigDecimal getDuzen() {
		return duzen;
	}
	public void setDuzen(BigDecimal duzen) {
		this.duzen = duzen;
	}
	public BigDecimal getBag1() {
		return bag1;
	}
	public void setBag1(BigDecimal bag1) {
		this.bag1 = bag1;
	}
	public String getBevrakno1() {
		return bevrakno1;
	}
	public void setBevrakno1(String bevrakno1) {
		this.bevrakno1 = bevrakno1;
	}
	public Integer getBag2() {
		return bag2;
	}
	public void setBag2(Integer bag2) {
		this.bag2 = bag2;
	}
	public String getBevrakno2() {
		return bevrakno2;
	}
	public void setBevrakno2(String bevrakno2) {
		this.bevrakno2 = bevrakno2;
	}
	public BigDecimal getSonuc() {
		return sonuc;
	}
	public void setSonuc(BigDecimal sonuc) {
		this.sonuc = sonuc;
	}

	public static final int IFT_CIKIS = 1;
	public static final int IRS_CIKIS = 2;
	public static final int FAT_CIKIS = 3;
	public static final int IFT_GIRIS = 4;
	public static final int IRS_GIRIS = 5;
	public static final int FAT_GIRIS = 6;
	public static final int IFT_IADE_CIKIS = 7;
	public static final int IRS_IADE_CIKIS = 8;
	public static final int FAT_IADE_CIKIS = 9;
	public static final int IFT_IADE_GIRIS = 10;
	public static final int IRS_IADE_GIRIS = 11;
	public static final int FAT_IADE_GIRIS = 12;
	public static final int FAT_FARK_CIKIS = 13;
	public static final int FAT_FARK_GIRIS = 14;
}
