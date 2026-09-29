package tr.fikopar.db.models.windows.excel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="win_exceldetay", catalog="windows")
public class ExcelDetayModel implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;
	@Column
	private Integer katno;
	@Column
	private String sheetName;
	@Column
	private Integer keyColumn;
	@Column
	private Integer fiyatColumn;
	@ManyToOne(targetEntity=ExcelModel.class, fetch = FetchType.LAZY)
	@JoinColumn(name="idexcel")
	private ExcelModel excelModel;
	
	public void setKeyColumn(Integer keyColumn) {
		this.keyColumn = keyColumn;
	}
	public Integer getKeyColumn() {
		return keyColumn;
	}
	public void setFiyatColumn(Integer fiyatColumn) {
		this.fiyatColumn = fiyatColumn;
	}
	public Integer getFiyatColumn() {
		return fiyatColumn;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getKatno() {
		return katno;
	}
	public void setKatno(Integer katno) {
		this.katno = katno;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public ExcelModel getExcelModel() {
		return excelModel;
	}
	public void setExcelModel(ExcelModel excelModel) {
		this.excelModel = excelModel;
	}
	
	
	
}
