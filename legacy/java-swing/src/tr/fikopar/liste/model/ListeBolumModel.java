package tr.fikopar.liste.model;

import java.util.TreeMap;



public class ListeBolumModel extends DirtyModel {
	private String bolum;
	private TreeMap<Integer, ListeModel> map = new TreeMap<Integer, ListeModel>();
	private int max_index = 0;
	
	public ListeBolumModel() {
		super();
	}
	
	public String getBolum() {
		return bolum;
	}
	public void setBolum(String bolum) {
		this.bolum = bolum;
	}
	
	public ListeModel getListeModel(int index) {
		if(!map.containsKey(index))
			return null;
		return map.get(index);
	}
	public void addListeModel(int index, ListeModel model) {
		if (index > max_index)
			max_index = index;
		map.put(index, model);
		
	}
	public int getMax_index() {
		return max_index;
	}
	public TreeMap<Integer, ListeModel> getMap() {
		return map;
	}
	@Override
	protected void finalize() throws Throwable {
		map.clear();
		super.finalize();
	}
	
}
