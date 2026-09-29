package tr.fikopar.db.models.windows.sayim;

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
@Table(name="win_sayimdetay", catalog="windows")
public class SayimDetayModel implements Serializable {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	@Column(name="katno", columnDefinition="DECIMAL", precision=4, scale=0)
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
	
	@Column(name="adet", columnDefinition="DECIMAL", precision=19, scale=4)
	private BigDecimal adet = BigDecimal.ZERO;
	
	@Column(name="kdv", columnDefinition="DECIMAL", precision=19, scale=4)
	private BigDecimal kdv = BigDecimal.ZERO;
	
	@Column(name="maliyet", columnDefinition="DECIMAL", precision=19, scale=4)
	private BigDecimal maliyet = BigDecimal.ZERO;
	
	@Column
	private String rafno;
	
	@Column
	private String sayimzamani;
	
	@ManyToOne(targetEntity = SayimModel.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "idsayim")
	private SayimModel sayimModel;
	
	public SayimModel getSayimModel() {
		return sayimModel;
	}
	public void setSayimModel(SayimModel sayimModel) {
		this.sayimModel = sayimModel;
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
	public BigDecimal getKdv() {
		return kdv;
	}
	public void setKdv(BigDecimal kdv) {
		this.kdv = kdv;
	}
	public BigDecimal getMaliyet() {
		return maliyet;
	}
	public void setMaliyet(BigDecimal maliyet) {
		this.maliyet = maliyet;
	}
	public String getRafno() {
		return rafno;
	}
	public void setRafno(String rafno) {
		this.rafno = rafno;
	}
	public String getSayimzamani() {
		return sayimzamani;
	}
	public void setSayimzamani(String sayimzamani) {
		this.sayimzamani = sayimzamani;
	}
	
}
