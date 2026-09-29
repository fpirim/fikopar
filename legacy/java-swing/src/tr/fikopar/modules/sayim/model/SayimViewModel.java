package tr.fikopar.modules.sayim.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import tr.fikopar.swing.view.FViewModel;

public class SayimViewModel extends FViewModel {

	private BigDecimal yil;
	private List<SayimDetayViewModel> sayimDetayViewModels = new ArrayList<>();
	
	public BigDecimal getYil() {
		return yil;
	}

	public void setYil(BigDecimal yil) {
		pcs.firePropertyChange("yil", this.yil, this.yil = yil);
	}

	public List<SayimDetayViewModel> getSayimDetayViewModels() {
		return this.sayimDetayViewModels;
	}

	public void setSayimDetayViewModels(List<SayimDetayViewModel> sayimDetayViewModels) {
		pcs.firePropertyChange("sayimDetayViewModels", this.sayimDetayViewModels,
				this.sayimDetayViewModels = sayimDetayViewModels);
	}

}
