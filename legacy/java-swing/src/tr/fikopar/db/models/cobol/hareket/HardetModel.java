package tr.fikopar.db.models.cobol.hareket;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "`COB-HAREKET-DETAY`", schema="`COBOL`")
public class HardetModel implements Serializable {

	@Id
	@Column(name = "`HD-TIP`", columnDefinition = "DECIMAL", precision = 4, scale = 0, nullable = false)
	private BigDecimal tip;
	@Id
	@Column(name = "`HD-EVRAKNO`", columnDefinition = "VARCHAR", length=15, nullable = false)
	private String evrakno;
	@Id
	@Column(name = "`HD-SIRA`", columnDefinition = "DECIMAL", precision = 10, scale = 0, nullable = false)
	private Integer sira;
	@Column(name = "`HD-KATNO`", columnDefinition = "DECIMAL", precision = 4, scale = 0, nullable = false)
	private BigDecimal katno;
	@Column(name = "`HD-STNO`", columnDefinition = "VARCHAR", length = 15, nullable = false)
	private String stno;
	@Column(name = "`HD-PRCNO`", columnDefinition = "VARCHAR", length = 30, nullable = false)
	private String prcno;
	@Column(name = "`HD-TIPI`", columnDefinition = "VARCHAR", length = 30, nullable = false)
	private String tipi;
	@Column(name = "`HD-CINSI`", columnDefinition = "VARCHAR", length = 60, nullable = false)
	private String cinsi;
	@Column(name = "`HD-MARKA`", columnDefinition = "VARCHAR", length = 30, nullable = false)
	private String marka;
	@Column(name = "`HD-ISKONTO1`", columnDefinition = "DECIMAL", precision = 8, scale = 4, nullable = false)
	private BigDecimal iskonto1;
	@Column(name = "`HD-ISKONTO2`", columnDefinition = "DECIMAL", precision = 8, scale = 4, nullable = false)
	private BigDecimal iskonto2;
	@Column(name = "`HD-KDV`", columnDefinition = "DECIMAL", precision = 14, scale = 4, nullable = false)
	private BigDecimal kdv;
	@Column(name = "`HD-ADET`", columnDefinition = "DECIMAL", precision = 16, scale = 4, nullable = false)
	private BigDecimal adet;
	@Column(name = "`HD-FIYAT`", columnDefinition = "DECIMAL", precision = 8, scale = 4, nullable = false)
	private BigDecimal fiyat;
	
	@ManyToOne(targetEntity = HarsicModel.class, fetch = FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = "`HD-EVRAKNO`", referencedColumnName="`HS-EVRAKNO`"),	
		@JoinColumn(name = "`HD-TIP`", referencedColumnName="`HS-TIP`")
	})
	private HarsicModel harsicModel;
	public HarsicModel getHarsicModel() {
		return harsicModel;
	}

	public void setHarsicModel(HarsicModel harsicModel) {
		this.harsicModel = harsicModel;
	}

	
	public String getEvrakno() {
		return evrakno;
	}

	public void setEvrakno(String evrakno) {
		this.evrakno = evrakno;
	}

	public BigDecimal getTip() {
		return tip;
	}
	public void setTip(BigDecimal tip) {
		this.tip = tip;
	}
		
	public Integer getSira() {
		return sira;
	}

	public void setSira(Integer sira) {
		this.sira = sira;
	}

	public BigDecimal getKatno() {
		return katno;
	}
	
	public void setKatno(BigDecimal katno) {
		this.katno = katno;
	}
	
	public String getStno() {
		return stno;
	}

	public void setStno(String stno) {
		this.stno = stno;
	}

	public String getPrcno() {
		return prcno;
	}

	public void setPrcno(String prcno) {
		this.prcno = prcno;
	}

	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	public String getCinsi() {
		return cinsi;
	}

	public void setCinsi(String cinsi) {
		this.cinsi = cinsi;
	}

	public String getMarka() {
		return marka;
	}

	public void setMarka(String marka) {
		this.marka = marka;
	}

	public BigDecimal getIskonto1() {
		return iskonto1;
	}

	public void setIskonto1(BigDecimal iskonto1) {
		this.iskonto1 = iskonto1;
	}

	public BigDecimal getIskonto2() {
		return iskonto2;
	}

	public void setIskonto2(BigDecimal iskonto2) {
		this.iskonto2 = iskonto2;
	}

	public BigDecimal getKdv() {
		return kdv;
	}

	public void setKdv(BigDecimal kdv) {
		this.kdv = kdv;
	}

	public BigDecimal getAdet() {
		return adet;
	}

	public void setAdet(BigDecimal adet) {
		this.adet = adet;
	}

	public BigDecimal getFiyat() {
		return fiyat;
	}

	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}
		
}
