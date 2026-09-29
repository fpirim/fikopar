package tr.fikopar.modules.excel.model;

import java.util.ArrayList;
import java.util.List;

import tr.fikopar.swing.view.FViewModel;

public class ExcelViewModel extends FViewModel {
	private Integer id;
	private String excelFileName;
	
	private List<ExcelViewDetayModel> excelViewDetayModels = new ArrayList<>();

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

	public List<ExcelViewDetayModel> getExcelViewDetayModels() {
		return excelViewDetayModels;
	}

	public void setExcelViewDetayModels(List<ExcelViewDetayModel> excelViewDetayModels) {
		this.excelViewDetayModels = excelViewDetayModels;
	}
	
	@Override
	public String toString() {
		return id+","+excelFileName;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ExcelViewModel) {
			return ((ExcelViewModel) obj).getId() == getId();
		}
		return super.equals(obj);
	}
	
}
