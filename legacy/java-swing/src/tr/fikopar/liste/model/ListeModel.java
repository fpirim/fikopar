package tr.fikopar.liste.model;

import java.math.BigDecimal;

public class ListeModel extends DirtyModel {
	private BigDecimal katno;
	private String stno;
	private String prcno;
	private String oemno;
	private String tipi;
	private String cinsi;
	private String note;
	private String marka;
	private BigDecimal adet;
	private BigDecimal fiyat;
	private String deg;
	private BigDecimal paket;
	private BigDecimal min_adet;
	private BigDecimal max_adet;
	private BigDecimal alinan_adet;
	private BigDecimal verilen_adet;
	private BigDecimal kampanya;
	private boolean bitince;
	 
	public ListeModel() {
		super();
	}
	
	public BigDecimal getKampanya() {
		return kampanya;
	}
	public void setKampanya(BigDecimal kampanya) {
		this.kampanya = kampanya;
	}
	public boolean isBitince() {
		return bitince;
	}
	public void setBitince(boolean bitince) {
		this.bitince = bitince;
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
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
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
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
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
	public BigDecimal getAlinan_adet() {
		return alinan_adet;
	}
	public void setAlinan_adet(BigDecimal alinan_adet) {
		this.alinan_adet = alinan_adet;
	}
	public BigDecimal getVerilen_adet() {
		return verilen_adet;
	}
	public void setVerilen_adet(BigDecimal verilen_adet) {
		this.verilen_adet = verilen_adet;
	}
	
	
	
}
