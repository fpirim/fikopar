package tr.fikopar.modules.contentasist.stokmodel;

import java.util.ArrayList;

import tr.fikopar.swing.table.FTableModel;

public class StokModelContentAsistTableModel extends FTableModel {

	public StokModelContentAsistTableModel() {
		list = new ArrayList<>();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object [] obj = (Object[]) list.get(rowIndex);
		return obj[columnIndex];
	}

}
