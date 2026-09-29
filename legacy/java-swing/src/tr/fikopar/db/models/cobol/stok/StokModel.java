package tr.fikopar.db.models.cobol.stok;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import tr.fikopar.db.models.windows.alim.AlimDetayModel;
import tr.fikopar.db.models.windows.stok.KampanyaModel;

@Entity
@Table(name = "`COB_STOK`", catalog="`COBOL`")
public class StokModel implements Serializable {
	@EmbeddedId
	private StokPK stokPK;
	@Column(name="`ST-PRCNO`", columnDefinition="VARCHAR", length=30, nullable=false)
	private String prcno;
	@Column(name="`ST-OEMNO`", columnDefinition="VARCHAR", length=30, nullable=false)
	private String oemno;
	@Column(name="`ST-TIPI`", columnDefinition="VARCHAR", length=30, nullable=false)
	private String tipi;
	@Column(name="`ST-CINSI`", columnDefinition="VARCHAR", length=60, nullable=false)
	private String cinsi;
	@Column(name="`ST-MARKA`", columnDefinition="VARCHAR", length=30, nullable=false)
	private String marka;
	@Column(name = "`ST-MIN`", columnDefinition = "DECIMAL", precision = 16, scale = 4)
	private BigDecimal min_adet;
	@Column(name = "`ST-MAX`", columnDefinition = "DECIMAL", precision = 16, scale = 4)
	private BigDecimal max_adet;
	@Column(name = "`ST-FIYAT`", columnDefinition = "DECIMAL", precision = 14, scale = 4)
	private BigDecimal fiyat;
	@Column(name="`ST-NOTE`", columnDefinition="VARCHAR", length=10, nullable=false)
	private String note;
	@Column(name="`ST-DEG`", columnDefinition="VARCHAR", length=8, nullable=false)
	private String deg;
	@Column(name="`ST-PAKET`", columnDefinition="DECIMAL", precision=5, scale=0, nullable=false)
	private BigDecimal paket;
	@Column(name="`ST-SIRA`", columnDefinition="DECIMAL", precision=15, scale=0, nullable=false)
	private BigDecimal sira;

	@OneToMany(targetEntity = StkadtModel.class, fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "`STA-KATNO`", referencedColumnName="`ST-KATNO`"),
			@JoinColumn(name = "`STA-STNO`", referencedColumnName="`ST-STNO`") })
	private Set<StkadtModel> stkadtModels = new HashSet<StkadtModel>();

	/*
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({@JoinColumn(name="katno", columnDefinition="INT"), @JoinColumn(name="stno", columnDefinition="VARCHAR")})
	private BolumModel bolumModel;
	*/
	
	@OneToMany(targetEntity = KampanyaModel.class, fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "katno", referencedColumnName="`ST-KATNO`"),
			@JoinColumn(name = "stno", referencedColumnName="`ST-STNO`") })
	private Set<KampanyaModel> kampanyaModels = new HashSet<KampanyaModel>();

	@OneToMany(targetEntity = AlimDetayModel.class, fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "katno", referencedColumnName="`ST-KATNO`"),
			@JoinColumn(name = "stno", referencedColumnName="`ST-STNO`") })
	private Set<AlimDetayModel> alimDetayModels = new HashSet<AlimDetayModel>();

	
	public Set<AlimDetayModel> getAlimDetayModels() {
		return alimDetayModels;
	}
	public void setAlimDetayModels(Set<AlimDetayModel> alimDetayModels) {
		this.alimDetayModels = alimDetayModels;
	}
	/*
	public BolumModel getBolumModel() {
		return bolumModel;
	}
	public void setBolumModel(BolumModel bolumModel) {
		this.bolumModel = bolumModel;
	}
	*/
	
	public StokPK getStokPK() {
		return stokPK;
	}

	public void setStokPK(StokPK stokPK) {
		this.stokPK = stokPK;
	}

	public Set<StkadtModel> getStkadtModels() {
		return stkadtModels;
	}

	public void setStkadtModels(Set<StkadtModel> stkadtModels) {
		this.stkadtModels = stkadtModels;
	}

	public Set<KampanyaModel> getKampanyaModels() {
		return kampanyaModels;
	}

	public void setKampanyaModels(Set<KampanyaModel> kampanyaModels) {
		this.kampanyaModels = kampanyaModels;
	}

	public String getPrcno() {
		return prcno;
	}

	public void setPrcno(String prcno) {
		this.prcno = prcno;
	}

	public String getOemno() {
		return oemno;
	}

	public void setOemno(String oemno) {
		this.oemno = oemno;
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

	public BigDecimal getMin_adet() {
		return min_adet;
	}

	public void setMin_adet(BigDecimal min_adet) {
		this.min_adet = min_adet;
	}

	public BigDecimal getMax_adet() {
		return max_adet;
	}

	public void setMax_adet(BigDecimal max_adet) {
		this.max_adet = max_adet;
	}

	public BigDecimal getFiyat() {
		return fiyat;
	}

	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getDeg() {
		return deg;
	}

	public void setDeg(String deg) {
		this.deg = deg;
	}

	public BigDecimal getPaket() {
		return paket;
	}
	public void setPaket(BigDecimal paket) {
		this.paket = paket;
	}
	public BigDecimal getSira() {
		return sira;
	}
	public void setSira(BigDecimal sira) {
		this.sira = sira;
	}
}
