package tr.fikopar.modules.siparis.view;

import java.math.BigDecimal;

import tr.fikopar.modules.siparis.model.SiparisDetayViewModel;
import tr.fikopar.swing.table.FTableModel;

public class SiparisTableModel extends FTableModel {
	
	private SiparisToplamTableModel siparisToplamTableModel;
	
	public SiparisTableModel() {
		siparisToplamTableModel = new SiparisToplamTableModel();
	}
	public SiparisToplamTableModel getSiparisToplamTableModel() {
		return siparisToplamTableModel;
	}
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0 || columnIndex == 13 || columnIndex == 14)
			return false;
		return true;
	}
	
	@Override
	public int getRowCount() {
		if(list==null)
			return 0;
		return list.size() + 1;
	}
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		
		boolean inserted = false;
		if(rowIndex == list.size()) {
			list.add(new SiparisDetayViewModel());
			inserted = true;
		}
		
		SiparisDetayViewModel siparisDetayViewModel = (SiparisDetayViewModel) list.get(rowIndex); 
		switch (columnIndex) {
		case 1:
			if(aValue instanceof Integer)
				siparisDetayViewModel.setKatno(new BigDecimal((Integer) aValue));
			else 
				siparisDetayViewModel.setKatno(((BigDecimal) aValue));
			break;
		case 2:
			siparisDetayViewModel.setStno((String) aValue);
			break;
		case 3:
			siparisDetayViewModel.setPrcno((String) aValue);
			break;
		case 4:
			siparisDetayViewModel.setOemno((String) aValue);
			break;
		case 5:
			siparisDetayViewModel.setTipi((String) aValue);
			break;
		case 6:
			siparisDetayViewModel.setParcaadi((String) aValue);
			break;
		case 7:
			siparisDetayViewModel.setMarka((String) aValue);
			break;
		case 8:
			siparisDetayViewModel.setIsk1((BigDecimal) aValue);
			break;
		case 9:
			siparisDetayViewModel.setIsk2((BigDecimal) aValue);
			break;
		case 10:
			siparisDetayViewModel.setKdv((BigDecimal) aValue);
			break;
		case 11:
			siparisDetayViewModel.setAdet((BigDecimal) aValue);
			break;
		case 12:
			siparisDetayViewModel.setFiyati((BigDecimal) aValue);
			break;
		}
		if(inserted)
			fireTableRowsInserted(rowIndex+1, rowIndex+1);
		fireTableCellUpdated(rowIndex, columnIndex);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= list.size())
			return null;
		SiparisDetayViewModel siparisDetayViewModel = (SiparisDetayViewModel) list.get(rowIndex); 
		switch (columnIndex) {
		case 0: return siparisDetayViewModel.getId();
		case 1: return siparisDetayViewModel.getKatno(); 
		case 2: return siparisDetayViewModel.getStno();
		case 3: return siparisDetayViewModel.getPrcno();
		case 4: return siparisDetayViewModel.getOemno();
		case 5: return siparisDetayViewModel.getTipi();
		case 6: return siparisDetayViewModel.getParcaadi();
		case 7: return siparisDetayViewModel.getMarka();
		case 8: return siparisDetayViewModel.getIsk1();
		case 9: return siparisDetayViewModel.getIsk2();
		case 10: return siparisDetayViewModel.getKdv();
		case 11: return siparisDetayViewModel.getAdet();
		case 12: return	siparisDetayViewModel.getFiyati();
		case 13: return (siparisDetayViewModel.getFiyati() == null ? BigDecimal.ZERO : siparisDetayViewModel.getFiyati()).
				multiply(siparisDetayViewModel.getAdet() == null ? BigDecimal.ZERO : siparisDetayViewModel.getAdet());
		case 14: return (siparisDetayViewModel.getAdet() == null ? BigDecimal.ZERO : siparisDetayViewModel.getAdet())
						.subtract(siparisDetayViewModel.getSevkadet() == null ? BigDecimal.ZERO : siparisDetayViewModel.getSevkadet());
		}
		return null;
	}
	
	@Override
	public void fireTableDataChanged() {
		super.fireTableDataChanged();
		siparisToplamTableModel.calculate(list);
	}
	@Override
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		super.fireTableRowsInserted(firstRow, lastRow);
		siparisToplamTableModel.calculate(list);
	}
	@Override
	public void fireTableRowsUpdated(int firstRow, int lastRow) {
		super.fireTableRowsUpdated(firstRow, lastRow);
		siparisToplamTableModel.calculate(list);
	}
	@Override
	public void fireTableCellUpdated(int row, int column) {
		super.fireTableCellUpdated(row, column);
		siparisToplamTableModel.calculate(list);
	}
	@Override
	public void fireTableRowsDeleted(int firstRow, int lastRow) {
		super.fireTableRowsDeleted(firstRow, lastRow);
		siparisToplamTableModel.calculate(list);
	}
	@Override
	public void fireTableStructureChanged() {
		super.fireTableStructureChanged();
		siparisToplamTableModel.calculate(list);
	}

	 
}
