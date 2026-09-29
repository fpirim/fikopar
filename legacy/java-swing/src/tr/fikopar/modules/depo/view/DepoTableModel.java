package tr.fikopar.modules.depo.view;

import java.math.BigDecimal;

import tr.fikopar.modules.depo.model.DepoDetayViewModel;
import tr.fikopar.swing.table.FTableModel;

public class DepoTableModel extends FTableModel {
	public DepoTableModel() {
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
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
			list.add(new DepoDetayViewModel());
			inserted = true;
		}
		
		DepoDetayViewModel depoDetayViewModel = (DepoDetayViewModel) list.get(rowIndex); 
		switch (columnIndex) {
		case 1:
			if(aValue instanceof Integer)
				depoDetayViewModel.setKatno(new BigDecimal((Integer) aValue));
			else 
				depoDetayViewModel.setKatno(((BigDecimal) aValue));
			break;
		case 2:
			depoDetayViewModel.setStno((String) aValue);
			break;
		case 3:
			depoDetayViewModel.setPrcno((String) aValue);
			break;
		case 4:
			depoDetayViewModel.setOemno((String) aValue);
			break;
		case 5:
			depoDetayViewModel.setTipi((String) aValue);
			break;
		case 6:
			depoDetayViewModel.setParcaadi((String) aValue);
			break;
		case 7:
			depoDetayViewModel.setMarka((String) aValue);
			break;
		case 8:
			depoDetayViewModel.setAdet((BigDecimal) aValue);
			break;
		case 9:
			depoDetayViewModel.setBakiye(BigDecimal.ZERO);
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
		DepoDetayViewModel depoDetayViewModel = (DepoDetayViewModel) list.get(rowIndex); 
		switch (columnIndex) {
		case 0: return depoDetayViewModel.getId();
		case 1: return depoDetayViewModel.getKatno(); 
		case 2: return depoDetayViewModel.getStno();
		case 3: return depoDetayViewModel.getPrcno();
		case 4: return depoDetayViewModel.getOemno();
		case 5: return depoDetayViewModel.getTipi();
		case 6: return depoDetayViewModel.getParcaadi();
		case 7: return depoDetayViewModel.getMarka();
		case 8: return depoDetayViewModel.getAdet();
		case 9: return depoDetayViewModel.getBakiye();
		}
		return null;
	}
	 
}
