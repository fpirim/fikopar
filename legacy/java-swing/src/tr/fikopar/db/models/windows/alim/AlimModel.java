package tr.fikopar.db.models.windows.alim;

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
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FilterJoinTable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import tr.fikopar.db.models.cobol.sicil.SicilModel;
import tr.fikopar.db.models.windows.excel.ExcelModel;

@Entity
@Table(name = "win_alim", catalog="windows")
public class AlimModel implements Serializable {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@Column
	private Boolean dusukseAktar;
	@Column
	private Boolean yuksekseAktar;
	@Column
	private String tarih;
	@Column
	private String note;
	@NotFound(action=NotFoundAction.IGNORE)
	@OneToOne(targetEntity = SicilModel.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "hesapno", referencedColumnName="`SC-HESAPNO`")
	private SicilModel sicilModel;
	
	@OneToOne(targetEntity = ExcelModel.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "idexcel")
	@NotFound(action=NotFoundAction.IGNORE)
	private ExcelModel excelModel;
	 
	@OneToMany(targetEntity = AlimDetayModel.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderColumn(name = "LINDEX")
	@JoinColumn(name = "idalim")
	private List<AlimDetayModel> alimDetayModels = new ArrayList<>();
	
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Boolean getDusukseAktar() {
		return dusukseAktar;
	}
	public void setDusukseAktar(Boolean dusukseAktar) {
		this.dusukseAktar = dusukseAktar;
	}
	public Boolean getYuksekseAktar() {
		return yuksekseAktar;
	}
	public void setYuksekseAktar(Boolean yuksekseAktar) {
		this.yuksekseAktar = yuksekseAktar;
	}
	public String getTarih() {
		return tarih;
	}
	public void setTarih(String tarih) {
		this.tarih = tarih;
	}
	public ExcelModel getExcelModel() {
		return excelModel;
	}
	public void setExcelModel(ExcelModel excelModel) {
		this.excelModel = excelModel;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SicilModel getSicilModel() {
		return sicilModel;
	}

	public void setSicilModel(SicilModel sicilModel) {
		this.sicilModel = sicilModel;
	}

	public List<AlimDetayModel> getAlimDetayModels() {
		return alimDetayModels;
	}

	public void setAlimDetayModels(List<AlimDetayModel> alimDetayModels) {
		this.alimDetayModels = alimDetayModels;
	}
	
	
}
