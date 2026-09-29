package tr.fikopar.db.models.windows.siparis;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="win_siparisdetay", catalog="windows")
public class SiparisDetayModel implements Serializable {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	@Column(name="katno", precision=4, scale=0)
	private BigDecimal katno;
	@Column
	private String stno;
	@Column
	private String prcno;
	@Column
	private String oemno;
	@Column
	private String tipi;
	@Column
	private String cinsi;
	@Column
	private String marka;
	@Column(name="adet", columnDefinition="DECIMAL", precision=20, scale=4)
	private BigDecimal adet = BigDecimal.ZERO;
	@Column(name="iskonto1", columnDefinition="DECIMAL", precision=8, scale=4)
	private BigDecimal iskonto1 = BigDecimal.ZERO;
	@Column(name="iskonto2", columnDefinition="DECIMAL", precision=8, scale=4)
	private BigDecimal iskonto2 = BigDecimal.ZERO;
	@Column(name="kdv", columnDefinition="DECIMAL", precision=8, scale=4)
	private BigDecimal kdv = BigDecimal.ZERO;
	@Column(name="fiyat", columnDefinition="DECIMAL", precision=14, scale=4)
	private BigDecimal fiyat = BigDecimal.ZERO;
	@Column(name="sevkadet", columnDefinition="DECIMAL", precision=20, scale=4, updatable=false, insertable=false)
	private BigDecimal sevkadet;
	@Column(name="idwb_siparis")
	private Integer idwb_siparis;
	
	@ManyToOne(targetEntity=SiparisModel.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "idsiparis")
	private SiparisModel siparisModel;
	public SiparisModel getSiparisModel() {
		return siparisModel;
	}
	public void setSiparisModel(SiparisModel siparisModel) {
		this.siparisModel = siparisModel;
	}
	public BigDecimal getKatno() {
		return katno;
	}
	public void setKatno(BigDecimal katno) {
		this.katno = katno;
	}
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public BigDecimal getAdet() {
		return adet;
	}

	public void setAdet(BigDecimal adet) {
		this.adet = adet;
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

	public BigDecimal getFiyat() {
		return fiyat;
	}

	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}
	
	public BigDecimal getSevkadet() {
		return sevkadet;
	}
	public void setSevkadet(BigDecimal sevkadet) {
		this.sevkadet = sevkadet;
	}
	public Integer getIdwb_siparis() {
		return idwb_siparis;
	}
	public void setIdwb_siparis(Integer idwb_siparis) {
		this.idwb_siparis = idwb_siparis;
	}
	
	
	
}
