package tr.fikopar.modules.contentasist.sicilmodel;

import java.util.ArrayList;

import tr.fikopar.swing.table.FTableModel;

public class SicilModelContentAsistTableModel extends FTableModel {

	public SicilModelContentAsistTableModel() {
		list = new ArrayList<>();
	}

	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object [] obj = (Object[]) list.get(rowIndex);
		return obj[columnIndex];
	}

}
