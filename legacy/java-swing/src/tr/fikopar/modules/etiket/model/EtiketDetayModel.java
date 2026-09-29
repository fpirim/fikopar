package tr.fikopar.modules.etiket.model;

import java.math.BigDecimal;

public class EtiketDetayModel {
	private BigDecimal katno = BigDecimal.ZERO;
	private String stno = "";
	private String prcno = "";
	private String oemno = "";
	private String tipi = "";
	private String cinsi = "";
	private String marka = "";
	private BigDecimal etiketAdet = new BigDecimal(2);
	private BigDecimal paketAdet = BigDecimal.ONE;
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
	public BigDecimal getEtiketAdet() {
		return etiketAdet;
	}
	public void setEtiketAdet(BigDecimal etiketAdet) {
		this.etiketAdet = etiketAdet;
	}
	public BigDecimal getPaketAdet() {
		return paketAdet;
	}
	public void setPaketAdet(BigDecimal paketAdet) {
		this.paketAdet = paketAdet;
	}
	
	
}
