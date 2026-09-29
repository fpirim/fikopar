package tr.fikopar.modules.stokkart.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import tr.fikopar.swing.view.FViewModel;

public class StokKartViewModel extends FViewModel {
	private BigDecimal katno;
	private String stno;
	private String oemno;
	private String prcno;
	private String tipi;
	private String parcaadi;
	private String note;
	private String marka;
	private String deg;
	private BigDecimal fiyat;
	private BigDecimal minadet;
	private BigDecimal maxadet;
	private BigDecimal paket;
	
	private List<BigDecimal> yils = new ArrayList<>();
 	
	public BigDecimal getKatno() {
		return katno;
	}
	public void setKatno(BigDecimal katno) {
		pcs.firePropertyChange("katno", this.katno, this.katno = katno);
	}
	public String getStno() {
		return stno;
	}
	public void setStno(String stno) {
		pcs.firePropertyChange("stno", this.stno, this.stno = stno);
	}
	public String getOemno() {
		return oemno;
	}
	public void setOemno(String oemno) {
		pcs.firePropertyChange("oemno", this.oemno, this.oemno = oemno);
	}
	public String getPrcno() {
		return prcno;
	}
	public void setPrcno(String prcno) {
		pcs.firePropertyChange("prcno", this.prcno, this.prcno = prcno);
	}
	public String getTipi() {
		return tipi;
	}
	public void setTipi(String tipi) {
		pcs.firePropertyChange("tipi", this.tipi, this.tipi = tipi);
	}
	public String getParcaadi() {
		return parcaadi;
	}
	public void setParcaadi(String parcaadi) {
		pcs.firePropertyChange("parcaadi", this.parcaadi, this.parcaadi = parcaadi);
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		pcs.firePropertyChange("note", this.note, this.note = note);
	}
	public String getMarka() {
		return marka;
	}
	public void setMarka(String marka) {
		pcs.firePropertyChange("marka", this.marka, this.marka = marka);
	}
	public String getDeg() {
		return deg;
	}
	public void setDeg(String deg) {
		pcs.firePropertyChange("deg", this.deg, this.deg = deg);
	}
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		pcs.firePropertyChange("fiyat", this.fiyat, this.fiyat = fiyat);
	}
	public BigDecimal getMinadet() {
		return minadet;
	}
	public void setMinadet(BigDecimal minadet) {
		pcs.firePropertyChange("minadet", this.minadet, this.minadet = minadet);
	}
	public BigDecimal getMaxadet() {
		return maxadet;
	}
	public void setMaxadet(BigDecimal maxadet) {
		pcs.firePropertyChange("maxadet", this.maxadet, this.maxadet = maxadet);
	}
	public BigDecimal getPaket() {
		return paket;
	}
	public void setPaket(BigDecimal paket) {
		pcs.firePropertyChange("paket", this.paket, this.paket = paket);
	}
	
	public List<BigDecimal> getYils() {
		return yils;
	}
	public void setYils(List<BigDecimal> yils) {
		pcs.firePropertyChange("yils", this.yils, this.yils = yils);
	}
	
	
	
}
