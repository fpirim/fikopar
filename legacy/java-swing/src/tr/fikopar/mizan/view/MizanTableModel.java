package tr.fikopar.mizan.view;

import java.math.BigDecimal;

import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;

import tr.fikopar.swing.table.FTableModel;

public class MizanTableModel extends FTableModel {
	
	private MizanToplamTableModel mizanToplamTableModel;
	public MizanTableModel() {
		mizanToplamTableModel = new MizanToplamTableModel();
	}
	public MizanToplamTableModel getMizanToplamTableModel() {
		return mizanToplamTableModel;
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		Object [] obj = (Object[]) list.get(rowIndex);
		BigDecimal borc = obj[9] == null ? BigDecimal.ZERO : (BigDecimal) obj[9]; 
		BigDecimal alacak = obj[10] == null ? BigDecimal.ZERO : (BigDecimal) obj[10];
		
		switch(columnIndex) {
		case 11:
			return borc.subtract(alacak).compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : borc.subtract(alacak);
		case 12:
			return alacak.subtract(borc).compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : alacak.subtract(borc);
		}
		
		
		return obj[columnIndex];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void sorterChanged(RowSorterEvent e) {
		
		mizanToplamTableModel.setBorcToplam(BigDecimal.ZERO);
		mizanToplamTableModel.setAlacakToplam(BigDecimal.ZERO);
		RowSorter sorter = e.getSource();
		for(int i=0; i<sorter.getViewRowCount(); i++) {
			for(int j=9; j<=10; j++) {
				Object value = getValueAt(sorter.convertRowIndexToModel(i), j);
				if(value != null && value instanceof BigDecimal) {
					mizanToplamTableModel.setBorcToplam(mizanToplamTableModel.getBorcToplam().add(j == 9 ? (BigDecimal)value : BigDecimal.ZERO));
					mizanToplamTableModel.setAlacakToplam(mizanToplamTableModel.getAlacakToplam().add(j == 10 ? (BigDecimal)value : BigDecimal.ZERO));
				}
			}
		}
		mizanToplamTableModel.fireTableDataChanged();
		
	}
	private class MizanToplamTableModel extends FTableModel {

		BigDecimal borcToplam = BigDecimal.ZERO;
		BigDecimal alacakToplam = BigDecimal.ZERO;
		
		@Override
		public int getRowCount() {
			return 2;
		}
		
		public void setBorcToplam(BigDecimal borcToplam) {
			this.borcToplam = borcToplam;
		}
		public void setAlacakToplam(BigDecimal alacakToplam) {
			this.alacakToplam = alacakToplam;
		}
		public BigDecimal getBorcToplam() {
			return borcToplam;
		}
		public BigDecimal getAlacakToplam() {
			return alacakToplam;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			
			if(row == 0 && col==8)
				return "TOPLAM :";
			if(row == 1 && col==8)
				return "BAKÝYE :";
			
			if(col==9 || col==10) {
				if(row==0 && col==9)
					return borcToplam;
				if(row==0 && col==10)
					return alacakToplam;
				if(row==1 && col==9)
					return borcToplam.compareTo(alacakToplam) > 0 ? borcToplam.subtract(alacakToplam) : BigDecimal.ZERO;
				if(row==1 && col==10)
					return alacakToplam.compareTo(borcToplam) > 0 ? alacakToplam.subtract(borcToplam) : BigDecimal.ZERO;
			}
				
			return null;
		}
		
	}

}
