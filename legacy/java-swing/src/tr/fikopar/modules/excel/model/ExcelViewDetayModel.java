package tr.fikopar.modules.excel.model;

public class ExcelViewDetayModel {
	private Integer id;
	private Integer katno;
	private String sheetName;
	private Integer keyColumn;
	private Integer fiyatColumn;
	
	public Integer getFiyatColumn() {
		return fiyatColumn;
	}
	public void setFiyatColumn(Integer fiyatColumn) {
		this.fiyatColumn = fiyatColumn;
	}
	public Integer getKeyColumn() {
		return keyColumn;
	}
	public void setKeyColumn(Integer keyColumn) {
		this.keyColumn = keyColumn;
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
	
	
}
