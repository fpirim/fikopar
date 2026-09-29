package tr.fikopar.modules.sayim.view;

import java.math.BigDecimal;
import java.math.RoundingMode;

import tr.fikopar.modules.sayim.model.SayimDetayViewModel;
import tr.fikopar.swing.table.FTableModel;

public class SayimTableModel extends FTableModel {

	private SayimToplamTableModel sayimToplamTableModel;

	public SayimTableModel() {
		this.sayimToplamTableModel = new SayimToplamTableModel();
	}

	public SayimToplamTableModel getSayimToplamTableModel() {
		return sayimToplamTableModel;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0 || columnIndex == 13)
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
		if (rowIndex == list.size()) {
			list.add(new SayimDetayViewModel());
			inserted = true;
		}

		SayimDetayViewModel sayimDetayViewModel = (SayimDetayViewModel) list.get(rowIndex);

		toplamChanged(sayimDetayViewModel, SUBTRACT);
		switch (columnIndex) {
		case 1:
			if (aValue instanceof Integer)
				sayimDetayViewModel.setKatno(new BigDecimal((Integer) aValue));
			else
				sayimDetayViewModel.setKatno(((BigDecimal) aValue));
			break;
		case 2:
			sayimDetayViewModel.setStno((String) aValue);
			break;
		case 3:
			sayimDetayViewModel.setPrcno((String) aValue);
			break;
		case 4:
			sayimDetayViewModel.setOemno((String) aValue);
			break;
		case 5:
			sayimDetayViewModel.setTipi((String) aValue);
			break;
		case 6:
			sayimDetayViewModel.setCinsi((String) aValue);
			break;
		case 7:
			sayimDetayViewModel.setMarka((String) aValue);
			break;
		case 8:
			sayimDetayViewModel.setRafno((String) aValue);
			break;
		case 9:
			sayimDetayViewModel.setSayimzamani((String) aValue);
			break;
		case 10:
			sayimDetayViewModel.setKdv((BigDecimal) aValue);
			break;
		case 11:
			sayimDetayViewModel.setAdet((BigDecimal) aValue);
			break;
		case 12:
			sayimDetayViewModel.setMaliyet((BigDecimal) aValue);
			break;
		}
		toplamChanged(sayimDetayViewModel, ADD);
		if (inserted)
			fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		SayimDetayViewModel sayimDetayViewModel = (SayimDetayViewModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return sayimDetayViewModel.getId();
		case 1:
			return sayimDetayViewModel.getKatno();
		case 2:
			return sayimDetayViewModel.getStno();
		case 3:
			return sayimDetayViewModel.getPrcno();
		case 4:
			return sayimDetayViewModel.getOemno();
		case 5:
			return sayimDetayViewModel.getTipi();
		case 6:
			return sayimDetayViewModel.getCinsi();
		case 7:
			return sayimDetayViewModel.getMarka();
		case 8:
			return sayimDetayViewModel.getRafno();
		case 9:
			return sayimDetayViewModel.getSayimzamani();
		case 10:
			return sayimDetayViewModel.getKdv();
		case 11:
			return sayimDetayViewModel.getAdet();
		case 12:
			return sayimDetayViewModel.getMaliyet();
		case 13:
			return sayimDetayViewModel.getAdet().multiply(sayimDetayViewModel.getMaliyet());
		}
		return null;
	}

	private BigDecimal aHundred = new BigDecimal(100);
	private static final int ADD = 0;
	private static final int SUBTRACT = 1;

	private void toplamChanged(SayimDetayViewModel sayimDetayViewModel, int type) {
		BigDecimal tutar = sayimDetayViewModel.getAdet().multiply(sayimDetayViewModel.getMaliyet()).setScale(2,
				RoundingMode.HALF_UP);
		BigDecimal kdv = tutar.multiply(sayimDetayViewModel.getKdv()).divide(aHundred).setScale(2, RoundingMode.HALF_UP);
		switch (type) {
		case ADD:
			sayimToplamTableModel.setToplam(sayimToplamTableModel.getToplam().add(tutar));
			sayimToplamTableModel.setKdvToplam(sayimToplamTableModel.getKdvToplam().add(kdv));
			break;
		case SUBTRACT:
			sayimToplamTableModel.setToplam(sayimToplamTableModel.getToplam().subtract(tutar));
			sayimToplamTableModel.setKdvToplam(sayimToplamTableModel.getKdvToplam().subtract(kdv));
			break;
		}
	}

	@Override
	public void fireTableDataChanged() {
		sayimToplamTableModel.setToplam(BigDecimal.ZERO);
		sayimToplamTableModel.setKdvToplam(BigDecimal.ZERO);

		if(list!=null) {
			for (int i = 0; i < list.size(); i++)
				toplamChanged((SayimDetayViewModel) list.get(i), ADD);
		}
		super.fireTableDataChanged();
	}

}