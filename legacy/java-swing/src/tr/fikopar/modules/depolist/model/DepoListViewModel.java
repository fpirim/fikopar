package tr.fikopar.modules.depolist.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.view.FViewModel;

public class DepoListViewModel extends FViewModel {
	
	private List<DepoListViewDetayModel> depoListViewDetayModels = new ArrayList<DepoListViewDetayModel>();
	private Map<String, FUserTrashModel> trashMap = new HashMap<>();
	
	public static enum Durum {
		BEKLEYEN, TAMAMLANAN, HEPSI
	};

	private Durum durum = Durum.BEKLEYEN;
	
	public Durum getDurum() {
		return durum;
	}

	public void setDurum(Durum durum) {
		pcs.firePropertyChange("durum", this.durum, this.durum = durum);
	}

	public Map<String, FUserTrashModel> getTrashMap() {
		return trashMap;
	}
	public void setTrashMap(Map<String, FUserTrashModel> trashMap) {
		pcs.firePropertyChange("trashMap", this.trashMap, this.trashMap = trashMap);
	}
	
	public boolean isRead(DepoListViewDetayModel model) {
		return trashMap.containsKey("depo_"+model.getId());
	}
	public boolean isAllRead() {
		for(Iterator<DepoListViewDetayModel> itr = depoListViewDetayModels.iterator(); itr.hasNext(); ) {
			if(!isRead(itr.next()))
				return false;
		}
		return true;
			
	}

	public List<DepoListViewDetayModel> getDepoListViewDetayModels() {
		return depoListViewDetayModels;
	}

	public void setDepoListViewDetayModels(List<DepoListViewDetayModel> depoListViewDetayModels) {
		pcs.firePropertyChange("depoListViewDetayModels", this.depoListViewDetayModels,
				this.depoListViewDetayModels = depoListViewDetayModels);
	}

}
