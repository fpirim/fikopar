package tr.fikopar.modules.stokkart.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import tr.fikopar.swing.view.FViewModel;

public class HareketViewModel extends FViewModel{

	private BigDecimal yil;
	private BigDecimal katno;
	private String stno;
	private List<HareketDetayModel> girishareketDetayModels = new ArrayList<>();
	private List<HareketDetayModel> cikishareketDetayModels = new ArrayList<>();
	public BigDecimal getYil() {
		return yil;
	}
	public BigDecimal getKatno() {
		return katno;
	}
	public void setKatno(BigDecimal katno) {
		pcs.firePropertyChange("katno", this.katno, this.katno = katno);
	}
	public String getStno() {
		return stno;
	}
	public void setStno(String stno) {
		pcs.firePropertyChange("stno", this.stno, this.stno = stno);
	}
	public void setYil(BigDecimal yil) {
		pcs.firePropertyChange("yil", this.yil, this.yil = yil);
	}
	public List<HareketDetayModel> getGirishareketDetayModels() {
		return girishareketDetayModels;
	}
	public void setGirishareketDetayModels(List<HareketDetayModel> girishareketDetayModels) {
		pcs.firePropertyChange("girishareketDetayModels", this.girishareketDetayModels, this.girishareketDetayModels = girishareketDetayModels);
	}
	public List<HareketDetayModel> getCikishareketDetayModels() {
		return cikishareketDetayModels;
	}
	public void setCikishareketDetayModels(List<HareketDetayModel> cikishareketDetayModels) {
		pcs.firePropertyChange("cikishareketDetayModels",  this.cikishareketDetayModels, this.cikishareketDetayModels = cikishareketDetayModels);
	}
	
	
	
}
