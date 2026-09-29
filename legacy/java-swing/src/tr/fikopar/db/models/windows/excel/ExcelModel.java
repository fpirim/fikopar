package tr.fikopar.db.models.windows.excel;

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
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(name="win_excel", catalog="windows")
public class ExcelModel implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;
	@Column
	private String excelFileName;
	
	@OneToMany(targetEntity = ExcelDetayModel.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderColumn(name = "LINDEX")
	@JoinColumn(name="idexcel")
	private List<ExcelDetayModel> excelDetayModels = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getExcelFileName() {
		return excelFileName;
	}

	public void setExcelFileName(String excelFileName) {
		this.excelFileName = excelFileName;
	}

	public List<ExcelDetayModel> getExcelDetayModels() {
		return excelDetayModels;
	}

	public void setExcelDetayModels(List<ExcelDetayModel> excelDetayModels) {
		this.excelDetayModels = excelDetayModels;
	}
	
	
}
