package tr.fikopar.eksik.model;

import java.math.BigDecimal;

public class EksikSicilModel {
	private Integer id;
	private String tarih;
	private String hesapno;
	private String hesapadi;
	private String yetkili;
	private String adres;
	private String siparisnot;
	private String sevsekli;
	private String siparisialan;
	private BigDecimal iskonto1;
	private BigDecimal iskonto2;
	private BigDecimal kdv;
	private BigDecimal fiyat;
	private BigDecimal bakiye;
	private Integer tipi;
	public Integer getTipi() {
		return tipi;
	}
	public void setTipi(Integer tipi) {
		this.tipi = tipi;
	}
	private EksikStokModel eksikStokModel;
	public void setEksikStokModel(EksikStokModel eksikStokModel) {
		this.eksikStokModel = eksikStokModel;
	}
	public EksikStokModel getEksikStokModel() {
		return eksikStokModel;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public BigDecimal getBakiye() {
		return bakiye;
	}
	public void setBakiye(BigDecimal bakiye) {
		this.bakiye = bakiye;
	}
	public Integer getId() {
		return id;
	}
	public String getTarih() {
		return tarih;
	}
	public void setTarih(String tarih) {
		this.tarih = tarih;
	}
	public void setSiparisnot(String siparisnot) {
		this.siparisnot = siparisnot;
	}
	public String getSiparisnot() {
		return siparisnot;
	}
	public String getHesapno() {
		return hesapno;
	}
	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}
	public String getHesapadi() {
		return hesapadi;
	}
	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi;
	}
	public String getYetkili() {
		return yetkili;
	}
	public void setYetkili(String yetkili) {
		this.yetkili = yetkili;
	}
	
	public String getAdres() {
		return adres;
	}
	public void setAdres(String adres) {
		this.adres = adres;
	}
	public String getSevsekli() {
		return sevsekli;
	}
	public void setSevsekli(String sevsekli) {
		this.sevsekli = sevsekli;
	}
	public String getSiparisialan() {
		return siparisialan;
	}
	public void setSiparisialan(String siparisialan) {
		this.siparisialan = siparisialan;
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
		
}
