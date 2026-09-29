package tr.fikopar.modules.siparislist.view;

import java.math.BigDecimal;
import java.util.List;

import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import tr.fikopar.modules.siparislist.model.SiparisListViewDetayModel;
import tr.fikopar.swing.table.FTableModel;

public class SiparisListTableModel extends FTableModel {

	private SiparisListToplamTableModel siparisListToplamTableModel;

	public SiparisListTableModel() {
		siparisListToplamTableModel = new SiparisListToplamTableModel();
	}

	public SiparisListToplamTableModel getSiparisListToplamTableModel() {
		return siparisListToplamTableModel;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setList(List list) {
		super.setList(list);
		siparisListToplamTableModel.fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		if (list == null)
			return 0;
		return list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= list.size())
			return null;
		SiparisListViewDetayModel siparisListViewDetayModel = (SiparisListViewDetayModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return siparisListViewDetayModel.getId();
		case 1:
			return siparisListViewDetayModel.getTarih();
		case 2:
			return siparisListViewDetayModel.getHesapno();
		case 3:
			return siparisListViewDetayModel.getHesapadi();
		case 4:
			return siparisListViewDetayModel.getAdres();
		case 5:
			return siparisListViewDetayModel.getSevksekli();
		case 6:
			return siparisListViewDetayModel.getSiparisialan();
		case 7:
			return siparisListViewDetayModel.getBakiye();
		case 8:
			return siparisListViewDetayModel.getNote();
		}
		return null;
	}

	private class SiparisListToplamTableModel extends FTableModel implements RowSorterListener {

		private BigDecimal toplam = BigDecimal.ZERO;

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (SiparisListTableModel.this.list == null || c != 7)
				return null;

			return toplam;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void sorterChanged(RowSorterEvent e) {
			RowSorter sorter = e.getSource();
			toplam = BigDecimal.ZERO;
			for (int i = 0; i < sorter.getViewRowCount(); i++) {
				int index = sorter.convertRowIndexToModel(i);
				SiparisListViewDetayModel siparisListViewDetayModel = (SiparisListViewDetayModel) SiparisListTableModel.this.list
						.get(index);
				toplam = toplam.add(siparisListViewDetayModel.getBakiye() == null ? BigDecimal.ZERO : siparisListViewDetayModel.getBakiye());
			}
			fireTableCellUpdated(0, 7);
		}

	}

}
