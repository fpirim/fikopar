package tr.fikopar.liste.view;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TreeMap;

import tr.fikopar.liste.model.ListeBolumModel;
import tr.fikopar.liste.model.ListeModel;
import tr.fikopar.swing.table.FTableModel;

public class ListeTableModel extends FTableModel {
	private TreeMap<Integer, ListeBolumModel> map;


	public void setMap(TreeMap<Integer, ListeBolumModel> map) {
		this.map = map;
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 15 || columnIndex == 16)
			if (getObjectAt(rowIndex) != null && getObjectAt(rowIndex) instanceof ListeModel)
				return true;

		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		ListeModel listeModel = (ListeModel) getObjectAt(rowIndex);

		listeModel.setDirty(true);
		switch (columnIndex) {
		case 15:
			listeModel.setKampanya((BigDecimal) aValue);
			break;
		case 16:
			listeModel.setBitince((Boolean) aValue);
			break;
		}

	}

	@Override
	public int getRowCount() {
		if (map == null)
			return 0;
		int size = 0;
		for (Iterator<ListeBolumModel> itr = map.values().iterator(); itr.hasNext();) {
			size += itr.next().getMax_index();
		}
		return size + map.keySet().size();
	}

	public Object getObjectAt(int rowIndex) {
		int start = 0;
		for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext();) {
			Integer key = itr.next();
			ListeBolumModel bolumModel = map.get(key);
			if (rowIndex >= start && rowIndex <= bolumModel.getMax_index() + start) {
				if (rowIndex == start)
					return bolumModel;
				return bolumModel.getListeModel(rowIndex - start);
			}
			start = bolumModel.getMax_index() + start + 1;
		}
		return null;

	}

	private SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDD");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MMYY");

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		Object object = getObjectAt(rowIndex);

		if (object == null)
			return null;

		if (object instanceof ListeBolumModel) {
			if (columnIndex == 4)
				return ((ListeBolumModel) object).getBolum();
			return null;
		}

		if (object instanceof ListeModel) {
			ListeModel listeModel = (ListeModel) object;
			switch (columnIndex) {
			case 0:
				return listeModel.getStno();
			case 1:
				return listeModel.getPrcno();
			case 2:
				return listeModel.getOemno();
			case 3:
				return listeModel.getTipi();
			case 4:
				return listeModel.getCinsi();
			case 5:
				return listeModel.getNote();
			case 6:
				return listeModel.getMarka();
			case 7:
				return listeModel.getAdet();
			case 8:
				return listeModel.getFiyat();
			case 9:
				String deg = listeModel.getDeg();
				try {
					deg = sdf2.format(sdf.parseObject(listeModel.getDeg()));
				} catch (ParseException e) {
				}
				return deg;
			case 10:
				return listeModel.getPaket();
			case 11:
				return listeModel.getMin_adet();
			case 12:
				return listeModel.getMax_adet();
			case 13:
				return listeModel.getAlinan_adet();
			case 14:
				return listeModel.getVerilen_adet();
			case 15:
				return listeModel.getKampanya();
			case 16:
				return listeModel.isBitince();

			}

		}

		return null;
	}

}
