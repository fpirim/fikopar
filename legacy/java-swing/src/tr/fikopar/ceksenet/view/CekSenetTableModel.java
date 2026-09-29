package tr.fikopar.ceksenet.view;

import java.math.BigDecimal;

import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;

import tr.fikopar.swing.table.FTableModel;

public class CekSenetTableModel extends FTableModel {
	
	private CekSenetTableToplamModel cekSenetTableToplamModel;
	public CekSenetTableModel() {
		cekSenetTableToplamModel = new CekSenetTableToplamModel();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		
		return ((Object[])list.get(rowIndex))[columnIndex];
	}
	public CekSenetTableToplamModel getCekSenetTableToplamModel() {
		return cekSenetTableToplamModel;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public void sorterChanged(RowSorterEvent e) {
		RowSorter sorter = e.getSource();
		cekSenetTableToplamModel.setToplam(BigDecimal.ZERO);
		for(int i=0; i<sorter.getViewRowCount(); i++) {
			Object value = ((Object []) list.get(sorter.convertRowIndexToModel(i)))[8];
			if(value != null && value instanceof BigDecimal)
				cekSenetTableToplamModel.setToplam(cekSenetTableToplamModel.getToplam().add((BigDecimal) value));
		}
		cekSenetTableToplamModel.fireTableDataChanged();
	}
	
	
	private class CekSenetTableToplamModel extends FTableModel {
		BigDecimal toplam = BigDecimal.ZERO;

		@Override
		public int getRowCount() {
			return 1;
		}
		public BigDecimal getToplam() {
			return toplam;
		}
		public void setToplam(BigDecimal toplam) {
			this.toplam = toplam;
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 7)
				return "TOPLAM :";
			if(columnIndex == 8)
				return toplam;
			return null;
		}
	}
}
