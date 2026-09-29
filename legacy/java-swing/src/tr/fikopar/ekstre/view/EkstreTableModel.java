package tr.fikopar.ekstre.view;

import java.math.BigDecimal;
import java.util.ArrayList;

import tr.fikopar.ekstre.model.EkstreViewModel;
import tr.fikopar.swing.table.FTableModel;

public class EkstreTableModel extends FTableModel {

	public EkstreTableModel() {
		list = new ArrayList<>();
	}

	@Override
	public int getRowCount() {
		return list.size();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		EkstreViewModel ekstreViewModel = (EkstreViewModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return ekstreViewModel.getTarih();
		case 1:
			return ekstreViewModel.getTipString();
		case 2:
			return ekstreViewModel.getEvrakno();
		case 3:
			return ekstreViewModel.getVade();
		case 4:
			return ekstreViewModel.getDepoString();
		case 5:
			return ekstreViewModel.getAciklama();
		case 6:
			return ekstreViewModel.getBorc();
		case 7:
			return ekstreViewModel.getAlacak();
		case 8:
			return ekstreViewModel.getBakiye().compareTo(BigDecimal.ZERO) > 0 ? ekstreViewModel.getBakiye() : BigDecimal.ZERO;
		case 9:
			return ekstreViewModel.getBakiye().compareTo(BigDecimal.ZERO) < 0 ? ekstreViewModel.getBakiye().multiply(new BigDecimal(-1)) : BigDecimal.ZERO;

		}
		return null;
	}

}
