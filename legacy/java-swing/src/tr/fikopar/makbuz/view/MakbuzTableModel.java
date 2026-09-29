package tr.fikopar.makbuz.view;

import java.math.BigDecimal;

import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;

import tr.fikopar.swing.table.FTableModel;

public class MakbuzTableModel extends FTableModel {
	
	private MakbuzTableToplamModel makbuzTableToplamModel;
	public MakbuzTableModel() {
		makbuzTableToplamModel = new MakbuzTableToplamModel();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		
		return ((Object[])list.get(rowIndex))[columnIndex];
	}
	@SuppressWarnings("rawtypes")
	@Override
	public void sorterChanged(RowSorterEvent e) {
		makbuzTableToplamModel.setToplam(BigDecimal.ZERO);
		RowSorter sorter = e.getSource();
		for(int i=0; i<sorter.getViewRowCount(); i++) {
			Object value = getValueAt(sorter.convertRowIndexToModel(i), 9);
			if(value != null && value instanceof BigDecimal) 
				makbuzTableToplamModel.setToplam(makbuzTableToplamModel.getToplam().add((BigDecimal)value));
		}
		makbuzTableToplamModel.fireTableDataChanged();
		
				
		
	}
	
	public MakbuzTableToplamModel getMakbuzTableToplamModel() {
		return makbuzTableToplamModel;
	}
	
	private class MakbuzTableToplamModel extends FTableModel {
		
		private BigDecimal toplam = BigDecimal.ZERO;
		
		@Override
		public int getRowCount() {
			return 1;
		}
		
		public void setToplam(BigDecimal toplam) {
			this.toplam = toplam;
		}
		public BigDecimal getToplam() {
			return toplam;
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 8)
				return "TOPLAM :";
			if(columnIndex == 9)
				return toplam;
			return null;
		}
	}

}
