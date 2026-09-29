package tr.fikopar.modules.alim.model;

import java.util.ArrayList;
import java.util.List;

import tr.fikopar.modules.excel.model.ExcelViewModel;
import tr.fikopar.swing.view.FViewModel;

public class AlimViewModel extends FViewModel {
	private Integer id;
	private String hesapno;
	private String hesapadi;
	private String yetkili;
	private ExcelViewModel excelViewModel;
	private Boolean dusukseAktar;
	private Boolean yuksekseAktar;
	private String tarih;
	private String note;
	private List<AlimViewDetayModel> alimViewDetayModels = new ArrayList<>();

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		pcs.firePropertyChange("note", this.note, this.note = note);
	}
	public Boolean getDusukseAktar() {
		return dusukseAktar;
	}

	public void setDusukseAktar(Boolean dusukseAktar) {
		pcs.firePropertyChange("dusukseAktar", this.dusukseAktar, this.dusukseAktar = dusukseAktar);
	}

	public Boolean getYuksekseAktar() {
		return yuksekseAktar;
	}

	public void setYuksekseAktar(Boolean yuksekseAktar) {
		pcs.firePropertyChange("yuksekseAktar", this.yuksekseAktar, this.yuksekseAktar = yuksekseAktar);
	}

	public String getTarih() {
		return tarih;
	}

	public void setTarih(String tarih) {
		pcs.firePropertyChange("tarih", this.tarih, this.tarih = tarih);
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
		pcs.firePropertyChange("yetkili",  this.yetkili, this.yetkili = yetkili);
	}
	public ExcelViewModel getExcelViewModel() {
		return excelViewModel;
	}
	public void setExcelViewModel(ExcelViewModel excelViewModel) {
		pcs.firePropertyChange("excelViewModel", this.excelViewModel, this.excelViewModel = excelViewModel);
	}

	public List<AlimViewDetayModel> getAlimViewDetayModels() {
		return alimViewDetayModels;
	}

	public void setAlimViewDetayModels(List<AlimViewDetayModel> alimViewDetayModels) {
		pcs.firePropertyChange("alimViewDetayModels", this.alimViewDetayModels,
				this.alimViewDetayModels = alimViewDetayModels);
	}

}
