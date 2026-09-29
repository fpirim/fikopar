package tr.fikopar.db.models.windows.siparis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import tr.fikopar.db.models.cobol.sicil.SicilModel;

@Entity
@Table(name = "win_siparis", catalog="windows")
public class SiparisModel implements Serializable {

	public static final Integer ALMA = 0;
	public static final Integer VERME = 1;

	@Column
	private Integer siparistipi;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne(targetEntity = SicilModel.class, orphanRemoval = false, cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinColumn(name = "hesapno", referencedColumnName="`SC-HESAPNO`")
	private SicilModel sicilModel;
	private String hesapadi;
	@Column
	private String tarih;
	@Column
	private String sevksekli;
	@Column
	private String siparisialan;
	@Column
	private String siparisnot;
	@Column
	private String sepetid;
	@OneToMany(targetEntity = SiparisDetayModel.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderColumn(name = "LINDEX")
	@JoinColumn(name = "idsiparis", referencedColumnName="id")
	private List<SiparisDetayModel> siparisDetayModels = new ArrayList<>();
	
	
	public List<SiparisDetayModel> getSiparisDetayModels() {
		return siparisDetayModels;
	}

	public void setSiparisDetayModels(List<SiparisDetayModel> siparisDetayModels) {
		this.siparisDetayModels = siparisDetayModels;
	}
	public String getSepetid() {
		return sepetid;
	}
	public void setSepetid(String sepetid) {
		this.sepetid = sepetid;
	}
	
	public Integer getSiparistipi() {
		return siparistipi;
	}

	public void setSiparistipi(Integer siparistipi) {
		this.siparistipi = siparistipi;
	}

	public String getTarih() {
		return tarih;
	}

	public void setTarih(String tarih) {
		this.tarih = tarih;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public void setSicilModel(SicilModel sicilModel) {
		this.sicilModel = sicilModel;
	}

	public SicilModel getSicilModel() {
		return sicilModel;
	}

	public String getSiparisnot() {
		return siparisnot;
	}

	public void setSiparisnot(String siparisnot) {
		this.siparisnot = siparisnot;
	}

	public String getHesapadi() {
		return hesapadi;
	}

	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi;
	}

}
