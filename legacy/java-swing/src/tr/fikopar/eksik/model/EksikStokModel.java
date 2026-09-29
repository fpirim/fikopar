package tr.fikopar.eksik.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EksikStokModel {
	private Integer katno;
	private String stno;
	private String prcno;
	private String oemno;
	private String tipi;
	private String cinsi;
	private String marka;
	private String note;
	private BigDecimal minadet;
	private BigDecimal maxadet;
	private BigDecimal fiyat;
	private Integer paket;
	private BigDecimal maliyet;
	private BigDecimal isk;
	private BigDecimal kdv;
	private String rafyeri;
	private BigDecimal bakiye = BigDecimal.ZERO;
	private BigDecimal verbakiye = BigDecimal.ZERO;
	private BigDecimal adet;
	private List<EksikSicilModel> eksikSicilModels = new ArrayList<EksikSicilModel>();
	private List<EksikKatalogModel> eksikKatalogModels = new ArrayList<EksikKatalogModel>();
	
	public List<EksikKatalogModel> getEksikKatalogModels() {
		return eksikKatalogModels;
	}
	
	public BigDecimal getVerbakiye() {
		return verbakiye;
	}

	public void setVerbakiye(BigDecimal verbakiye) {
		this.verbakiye = verbakiye;
	}

	public BigDecimal getMaliyet() {
		return maliyet;
	}
	public void setMaliyet(BigDecimal maliyet) {
		this.maliyet = maliyet;
	}
	public BigDecimal getIsk() {
		return isk;
	}
	public void setIsk(BigDecimal isk) {
		this.isk = isk;
	}
	public BigDecimal getKdv() {
		return kdv;
	}
	public void setKdv(BigDecimal kdv) {
		this.kdv = kdv;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Integer getPaket() {
		return paket;
	}
	public void setPaket(Integer paket) {
		this.paket = paket;
	}
	public BigDecimal getMinadet() {
		return minadet;
	}
	public void setMinadet(BigDecimal minadet) {
		this.minadet = minadet;
	}
	public BigDecimal getMaxadet() {
		return maxadet;
	}
	public void setMaxadet(BigDecimal maxadet) {
		this.maxadet = maxadet;
	}
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}
	public Integer getKatno() {
		return katno;
	}
	public void setKatno(Integer katno) {
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
	public String getRafyeri() {
		return rafyeri;
	}
	public void setRafyeri(String rafyeri) {
		this.rafyeri = rafyeri;
	}
	public BigDecimal getBakiye() {
		return bakiye;
	}
	public void setBakiye(BigDecimal bakiye) {
		this.bakiye = bakiye;
	}
	public BigDecimal getAdet() {
		return adet;
	}
	public void setAdet(BigDecimal adet) {
		this.adet = adet;
	}
	public List<EksikSicilModel> getEksikSicilModels() {
		return eksikSicilModels;
	}
	public void setEksikSicilModels(List<EksikSicilModel> eksikSicilModels) {
		this.eksikSicilModels = eksikSicilModels;
	}
	
}
