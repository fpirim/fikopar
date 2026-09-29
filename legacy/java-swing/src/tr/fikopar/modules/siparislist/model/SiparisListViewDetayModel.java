package tr.fikopar.modules.siparislist.model;

import java.math.BigDecimal;

public class SiparisListViewDetayModel {

	private Integer id;
	private String tarih;
	private String hesapno;
	private String hesapadi;
	private String adres;
	private String sevksekli;
	private String siparisialan;
	private BigDecimal bakiye = BigDecimal.ZERO;
	private String note;
	private Integer idwb_siparis;
	
	public Integer getIdwb_siparis() {
		return idwb_siparis;
	}
	public void setIdwb_siparis(Integer idwb_siparis) {
		this.idwb_siparis = idwb_siparis;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTarih() {
		return tarih;
	}

	public void setTarih(String tarih) {
		this.tarih = tarih;
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

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getSevksekli() {
		return sevksekli;
	}

	public void setSevksekli(String sevksekli) {
		this.sevksekli = sevksekli;
	}

	public String getSiparisialan() {
		return siparisialan;
	}

	public void setSiparisialan(String siparisialan) {
		this.siparisialan = siparisialan;
	}

	public BigDecimal getBakiye() {
		return bakiye;
	}

	public void setBakiye(BigDecimal bakiye) {
		this.bakiye = bakiye;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	public boolean isFullyEqual(SiparisListViewDetayModel o1) {
		return ((o1.getId() == null ? id == null : o1.getId().equals(id))
				&& (o1.getTarih() == null ? tarih == null : o1.getTarih().equals(tarih))
				&& (o1.getHesapno() == null ? hesapno == null : o1.getHesapno().equals(hesapno))
				&& (o1.getHesapadi() == null ? hesapadi == null : o1.getHesapadi().equals(hesapadi))
				&& (o1.getAdres() == null ? adres == null : o1.getAdres().equals(adres))
				&& (o1.getSevksekli() == null ? sevksekli == null : o1.getSevksekli().equals(sevksekli))
				&& (o1.getSiparisialan() == null ? siparisialan == null : o1.getSiparisialan().equals(siparisialan))
				&& (o1.getNote() == null ? note == null : o1.getNote().equals(note))
				&& (o1.getBakiye() == null ? bakiye == null : o1.getBakiye().compareTo(bakiye)==0));
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SiparisListViewDetayModel) {
			SiparisListViewDetayModel o1 = (SiparisListViewDetayModel) obj;
			return o1.getId() == null ? id == null : o1.getId().equals(id);
		}
		return super.equals(obj);
	}

}
