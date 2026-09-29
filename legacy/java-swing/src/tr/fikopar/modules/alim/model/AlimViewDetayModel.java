package tr.fikopar.modules.alim.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import tr.fikopar.modules.kur.model.KurViewModel;

public class AlimViewDetayModel {
	private Integer id;
	private String oemno;
	private BigDecimal katno;
	private String stno;
	private String prcno;
	private String tipi;
	private String cinsi;
	private String marka;
	private String note;
	private BigDecimal fiyat = BigDecimal.ZERO;
	private BigDecimal iskonto1 = BigDecimal.ZERO;
	private BigDecimal iskonto2 = BigDecimal.ZERO;
	private BigDecimal kdv = BigDecimal.ZERO;
	private BigDecimal maliyet = BigDecimal.ZERO;
	private BigDecimal marj = BigDecimal.ZERO;
	private KurViewModel kurViewModel;
	private static final BigDecimal HUNDRED = new BigDecimal(100);
	public BigDecimal getNetMaliyet() {
		if (kurViewModel == null)
			return null;
		return kurViewModel.getFiyat().multiply(maliyet)
				.multiply(BigDecimal.ONE.subtract(iskonto1.divide(HUNDRED)))
				.multiply(BigDecimal.ONE.subtract(iskonto2.divide(HUNDRED))).setScale(2, RoundingMode.HALF_UP);
	}
	public BigDecimal getListe() {
		if (getNetMaliyet() == null)
			return null;
		return getNetMaliyet().multiply(BigDecimal.ONE.add(marj.divide(HUNDRED))).setScale(2, RoundingMode.HALF_UP);
	}
	public BigDecimal getFark() {
		if(getKatno() == null || getKatno().equals(0))
			return null;
		if(getStno() == null || getStno().equals(""))
			return null;
		if(getListe() == null)
			return null;
		return getListe().subtract(fiyat == null ? BigDecimal.ZERO : fiyat);
	}
	
	public KurViewModel getKurViewModel() {
		return kurViewModel;
	}
	public void setKurViewModel(KurViewModel kurViewModel) {
		this.kurViewModel = kurViewModel;
	}
	
	public BigDecimal getMarj() {
		return marj;
	}
	public void setMarj(BigDecimal marj) {
		this.marj = marj;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getOemno() {
		return oemno;
	}
	public void setOemno(String oemno) {
		this.oemno = oemno;
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
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
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
	public BigDecimal getMaliyet() {
		return maliyet;
	}
	public void setMaliyet(BigDecimal maliyet) {
		this.maliyet = maliyet;
	}
	
	
}
