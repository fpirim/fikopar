package tr.fikopar.modules.alim.view;

import java.math.BigDecimal;

import tr.fikopar.modules.alim.model.AlimViewDetayModel;
import tr.fikopar.modules.kur.model.KurViewModel;
import tr.fikopar.swing.table.FTableModel;

public class AlimTableModel extends FTableModel {

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0 || columnIndex == 7 || columnIndex == 8 ||columnIndex > 15)
			return false;
		return true;
	}

	@Override
	public int getRowCount() {
		if (list == null)
			return 0;
		return list.size() + 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		boolean inserted = false;
		if (rowIndex == list.size()) {
			AlimViewDetayModel o1 = new AlimViewDetayModel();
			if (rowIndex != 0) {
				AlimViewDetayModel o2 = (AlimViewDetayModel) list.get(rowIndex - 1);
				o1.setKurViewModel(o2.getKurViewModel());
				o1.setIskonto1(o2.getIskonto1());
				o1.setIskonto2(o2.getIskonto2());
				o1.setKdv(o2.getKdv());
				o1.setMarj(o2.getMarj());
			}
			list.add(o1);
			inserted = true;
		}

		AlimViewDetayModel alimViewDetayModel = (AlimViewDetayModel) list.get(rowIndex);
		switch (columnIndex) {
		case 1:
			if (aValue instanceof Integer)
				alimViewDetayModel.setKatno(new BigDecimal((Integer) aValue));
			else
				alimViewDetayModel.setKatno((BigDecimal) aValue);
			break;
		case 2:
			alimViewDetayModel.setStno((String) aValue);
			break;
		case 3:
			alimViewDetayModel.setPrcno((String) aValue);
			break;
		case 4:
			alimViewDetayModel.setTipi((String) aValue);
			break;
		case 5:
			alimViewDetayModel.setCinsi((String) aValue);
			break;
		case 6:
			alimViewDetayModel.setMarka((String) aValue);
			break;
		case 7:
			alimViewDetayModel.setNote((String) aValue);
			break;
		case 8:
			alimViewDetayModel.setFiyat((BigDecimal) aValue);
			break;
		case 9:
			alimViewDetayModel.setOemno((String) aValue);
			break;
		case 10:
			alimViewDetayModel.setIskonto1((BigDecimal) aValue);
			break;
		case 11:
			alimViewDetayModel.setIskonto2((BigDecimal) aValue);
			break;
		case 12:
			alimViewDetayModel.setKdv((BigDecimal) aValue);
			break;
		case 13:
			alimViewDetayModel.setMaliyet((BigDecimal) aValue);
			break;
		case 14:
			alimViewDetayModel.setKurViewModel((KurViewModel) aValue);
			break;
		case 15:
			alimViewDetayModel.setMarj((BigDecimal) aValue);
			break;
		}
		if (inserted)
			fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
		fireTableCellUpdated(rowIndex, columnIndex);
		fireTableCellUpdated(rowIndex, 16);
		fireTableCellUpdated(rowIndex, 17);
		fireTableCellUpdated(rowIndex, 18);
	}

	private static final BigDecimal HUNDRED = new BigDecimal(100);

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		AlimViewDetayModel o1 = (AlimViewDetayModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return o1.getId();
		case 1:
			return o1.getKatno();
		case 2:
			return o1.getStno();
		case 3:
			return o1.getPrcno();
		case 4:
			return o1.getTipi();
		case 5:
			return o1.getCinsi();
		case 6:
			return o1.getMarka();
		case 7:
			return o1.getNote();
		case 8:
			return o1.getFiyat();
		case 9:
			return o1.getOemno();
		case 10:
			return o1.getIskonto1();
		case 11:
			return o1.getIskonto2();
		case 12:
			return o1.getKdv();
		case 13:
			return o1.getMaliyet();
		case 14:
			return o1.getKurViewModel();
		case 15:
			return o1.getMarj();
		case 16:
			return o1.getNetMaliyet();
		case 17:
			return o1.getListe();
		case 18:
			return o1.getFark();
		}
		return null;
	}

}
