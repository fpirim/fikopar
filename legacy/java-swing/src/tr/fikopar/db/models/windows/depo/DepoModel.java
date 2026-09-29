package tr.fikopar.db.models.windows.depo;

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
@Table(name = "win_depo", catalog="windows")
public class DepoModel implements Serializable {

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
	private String depoalan;
	@Column
	private String deponot;
	@OneToMany(targetEntity = DepoDetayModel.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderColumn(name = "LINDEX")
	@JoinColumn(name = "iddepo", referencedColumnName="id")
	private List<DepoDetayModel> depoDetayModels = new ArrayList<>();
	
	
	public List<DepoDetayModel> getDepoDetayModels() {
		return depoDetayModels;
	}

	public void setDepoDetayModels(List<DepoDetayModel> depoDetayModels) {
		this.depoDetayModels = depoDetayModels;
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

	public String getDepoalan() {
		return depoalan;
	}

	public void setDepoalan(String depoalan) {
		this.depoalan = depoalan;
	}

	public void setSicilModel(SicilModel sicilModel) {
		this.sicilModel = sicilModel;
	}

	public SicilModel getSicilModel() {
		return sicilModel;
	}

	public String getDeponot() {
		return deponot;
	}

	public void setDeponot(String deponot) {
		this.deponot = deponot;
	}

	public String getHesapadi() {
		return hesapadi;
	}

	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi;
	}

}
