package tr.fikopar.modules.depolist.view;

import java.util.List;

import tr.fikopar.modules.depolist.model.DepoListViewDetayModel;
import tr.fikopar.swing.table.FTableModel;

public class DepoListTableModel extends FTableModel {

	public DepoListTableModel() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setList(List list) {
		super.setList(list);
	}
	

	@Override
	public int getRowCount() {
		if (list == null)
			return 0;
		return list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= list.size())
			return null;
		DepoListViewDetayModel depoListViewDetayModel = (DepoListViewDetayModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return depoListViewDetayModel.getId();
		case 1:
			return depoListViewDetayModel.getTarih();
		case 2:
			return depoListViewDetayModel.getHesapno();
		case 3:
			return depoListViewDetayModel.getHesapadi();
		case 4:
			return depoListViewDetayModel.getAdres();
		case 5:
			return depoListViewDetayModel.getDepoalan();
		case 6:
			return depoListViewDetayModel.getNote();
		}
		return null;
	}
}
