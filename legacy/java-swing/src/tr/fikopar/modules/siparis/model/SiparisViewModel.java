package tr.fikopar.modules.siparis.model;

import java.util.ArrayList;
import java.util.List;

import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.swing.view.FViewModel;

public class SiparisViewModel extends FViewModel{
	
	private SiparisListViewModel.Tip siparistipi;
	private Integer id;
	private String hesapno;
	private String hesapadi;
	private String yetkili;
	private String adres;
	private String tanim;
	private String vda;
	private String vno;
	private String tcno;
	private String tel;
	private String tarih;
	private String sevksekli;
	private String siparisialan;
	private String note;
	private String sepetid;
	private Boolean kampanya;
	private List<SiparisDetayViewModel> siparisDetayViewModels = new ArrayList<>();
	private List<String> plasiyers = new ArrayList<>();
	private List<String> sevkseklis = new ArrayList<>();
		
	public List<String> getSevkseklis() {
		return sevkseklis;
	}
	public void setSevkseklis(List<String> sevkseklis) {
		pcs.firePropertyChange("sevkseklis", this.sevkseklis, this.sevkseklis = sevkseklis);
	}
	public List<String> getPlasiyers() {
		return plasiyers;
	}
	public void setPlasiyers(List<String> plasiyers) {
		pcs.firePropertyChange("plasiyers", this.plasiyers, this.plasiyers = plasiyers);
	}
	public String getSepetid() {
		return sepetid;
	}
	public void setSepetid(String sepetid) {
		this.sepetid = sepetid;
	}
	public Boolean getKampanya() {
		return kampanya;
	}
	public void setKampanya(Boolean kampanya) {
		this.kampanya = kampanya;
	}
	public SiparisListViewModel.Tip getSiparistipi() {
		return siparistipi;
	}
	public void setSiparistipi(SiparisListViewModel.Tip siparistipi) {
		pcs.firePropertyChange("siparistipi", this.siparistipi, this.siparistipi = siparistipi);
	}
	
	public List<SiparisDetayViewModel> getSiparisDetayViewModels() {
		return siparisDetayViewModels;
	}
	public void setSiparisDetayViewModels(List<SiparisDetayViewModel> siparisDetayViewModels) {
		pcs.firePropertyChange("siparisDetayViewModels", this.siparisDetayViewModels, this.siparisDetayViewModels = siparisDetayViewModels);
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		pcs.firePropertyChange("id", this.id, this.id = id);
	}
	public String getHesapno() {
		return hesapno;
	}
	public void setHesapno(String hesapno) {
		pcs.firePropertyChange("hesapno", this.hesapno, this.hesapno = hesapno);
	}
	public String getHesapadi() {
		return hesapadi;
	}
	public void setHesapadi(String hesapadi) {
		pcs.firePropertyChange("hesapadi", this.hesapadi, this.hesapadi = hesapadi);
	}
	public String getYetkili() {
		return yetkili;
	}
	public void setYetkili(String yetkili) {
		pcs.firePropertyChange("yetkili", this.yetkili, this.yetkili = yetkili);
	}
	public String getAdres() {
		return adres;
	}
	public void setAdres(String adres) {
		pcs.firePropertyChange("adres", this.adres, this.adres = adres);
	}
	public String getTanim() {
		return tanim;
	}
	public void setTanim(String tanim) {
		pcs.firePropertyChange("tanim", this.tanim, this.tanim = tanim);
	}
	public String getVda() {
		return vda;
	}
	public void setVda(String vda) {
		pcs.firePropertyChange("vda", this.vda, this.vda = vda);
	}
	public String getVno() {
		return vno;
	}
	public void setVno(String vno) {
		pcs.firePropertyChange("vno", this.vno, this.vno = vno);
	}
	public String getTcno() {
		return tcno;
	}
	public void setTcno(String tcno) {
		pcs.firePropertyChange("tcno", this.tcno, this.tcno = tcno);
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		pcs.firePropertyChange("tel", this.tel, this.tel = tel);
	}
	public String getTarih() {
		return tarih;
	}
	public void setTarih(String tarih) {
		pcs.firePropertyChange("tarih", this.tarih, this.tarih = tarih);
	}
	public String getSevksekli() {
		return sevksekli;
	}
	public void setSevksekli(String sevksekli) {
		pcs.firePropertyChange("sevksekli", this.sevksekli, this.sevksekli = sevksekli);
	}
	public String getSiparisialan() {
		return siparisialan;
	}
	public void setSiparisialan(String siparisialan) {
		pcs.firePropertyChange("siparisialan", this.siparisialan, this.siparisialan = siparisialan);
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		pcs.firePropertyChange("note", this.note, this.note = note);
	}
	
	

}
