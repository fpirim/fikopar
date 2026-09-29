package tr.fikopar.modules.siparislist.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tr.fikopar.modules.siparislist.view.SiparisListView;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.view.FViewModel;

public class SiparisListViewModel extends FViewModel {
	public static enum Tip {
		ALMA, VERME
	};

	public static enum Durum {
		BEKLEYEN, TAMAMLANAN, HEPSI
	};

	private Tip tip;
	private Durum durum = Durum.BEKLEYEN;
	private List<SiparisListViewDetayModel> siparisListViewDetayModels = new ArrayList<SiparisListViewDetayModel>();
	private Map<String, FUserTrashModel> trashMap = new HashMap<>();
	
	public Map<String, FUserTrashModel> getTrashMap() {
		return trashMap;
	}
	public void setTrashMap(Map<String, FUserTrashModel> trashMap) {
		pcs.firePropertyChange("trashMap", this.trashMap, this.trashMap = trashMap);
	}
	
	public boolean isRead(SiparisListViewDetayModel model) {
		return trashMap.containsKey("siparis_"+tip+"_"+model.getId());
	}
	public boolean isAllRead() {
		for(Iterator<SiparisListViewDetayModel> itr = siparisListViewDetayModels.iterator(); itr.hasNext(); ) {
			if(!isRead(itr.next()))
				return false;
		}
		return true;
			
	}
	public Tip getTip() {
		return tip;
	}

	public void setTip(Tip tip) {
		pcs.firePropertyChange("tip", this.tip, this.tip = tip);
	}

	public Durum getDurum() {
		return durum;
	}

	public void setDurum(Durum durum) {
		pcs.firePropertyChange("durum", this.durum, this.durum = durum);
	}

	public List<SiparisListViewDetayModel> getSiparisListViewDetayModels() {
		return siparisListViewDetayModels;
	}

	public void setSiparisListViewDetayModels(List<SiparisListViewDetayModel> siparisListViewDetayModels) {
		pcs.firePropertyChange("siparisListViewDetayModels", this.siparisListViewDetayModels,
				this.siparisListViewDetayModels = siparisListViewDetayModels);
	}

}
