package tr.fikopar.modules.sayim.model;

import java.math.BigDecimal;

public class SayimDetayViewModel {
	private Integer id;
	private BigDecimal katno = BigDecimal.ZERO;
	private String stno = "";
	private String prcno = "";
	private String oemno = "";
	private String tipi = "";
	private String cinsi = "";
	private String marka = "";
	private BigDecimal adet = BigDecimal.ZERO;
	private BigDecimal kdv = BigDecimal.ZERO;
	private BigDecimal maliyet = BigDecimal.ZERO;
	private String rafno = "";
	private String sayimzamani = "";
	
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
