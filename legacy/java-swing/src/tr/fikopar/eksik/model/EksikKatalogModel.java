package tr.fikopar.eksik.model;

import java.math.BigDecimal;

public class EksikKatalogModel {
	private Integer katno;
	private String aciklama;
	private String hesapno;
	private String stno;
	private BigDecimal isk;
	private BigDecimal kdv;
	private BigDecimal fiyat;
	private EksikStokModel eksikStokModel;
	
	public void setEksikStokModel(EksikStokModel eksikStokModel) {
		this.eksikStokModel = eksikStokModel;
	}
	public EksikStokModel getEksikStokModel() {
		return eksikStokModel;
	}
	
	public String getStno() {
		return stno;
	}
	public void setStno(String stno) {
		this.stno = stno;
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
	public String getAciklama() {
		return aciklama;
	}
	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}
	public String getHesapno() {
		return hesapno;
	}
	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}
	
	
	
}
