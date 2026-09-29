package tr.fikopar.modules.etiket.model;

import java.util.ArrayList;
import java.util.List;

import tr.fikopar.swing.view.FViewModel;

public class EtiketViewModel extends FViewModel{

	private List<EtiketDetayModel> etiketDetayModels = new ArrayList<>();

	public List<EtiketDetayModel> getEtiketDetayModels() {
		return etiketDetayModels;
	}
	public void setEtiketDetayModels(List<EtiketDetayModel> etiketDetayModels) {
		pcs.firePropertyChange("etiketDetayModels", this.etiketDetayModels, this.etiketDetayModels = etiketDetayModels);
	}
}
