package tr.fikopar.modules.stokkart.view;

import java.math.BigDecimal;
import java.util.Iterator;

import tr.fikopar.modules.stokkart.model.HareketDetayModel;
import tr.fikopar.swing.table.FTableModel;

public class HareketToplamTableModel extends FTableModel {

	private int type;
	private static BigDecimal girisAdet = BigDecimal.ZERO;
	private static BigDecimal cikisAdet = BigDecimal.ZERO;

	public HareketToplamTableModel(int type) {
		this.type = type;
	}

	@Override
	public int getRowCount() {
		if (type == HareketTableModel.CIKIS)
			return 2;
		return 1;
	}

	@Override
	public void fireTableDataChanged() {
		switch (type) {
		case HareketTableModel.GIRIS:
			girisAdet = BigDecimal.ZERO;
			break;
		case HareketTableModel.CIKIS:
			cikisAdet = BigDecimal.ZERO;
			break;
		}
		if (this.list != null) {
			for (Iterator itr = this.list.iterator(); itr.hasNext();) {
				HareketDetayModel hareketDetayModel = (HareketDetayModel) itr.next();
				switch (type) {
				case HareketTableModel.GIRIS:
					girisAdet = girisAdet.add(hareketDetayModel.getAdet());
					break;
				case HareketTableModel.CIKIS:
					cikisAdet = cikisAdet.add(hareketDetayModel.getAdet());
					break;
				}
			}
		}

		super.fireTableDataChanged();
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (r == 0 && c == 6 && type == HareketTableModel.GIRIS)
			return girisAdet;
		if (r == 0 && c == 6 && type == HareketTableModel.CIKIS)
			return cikisAdet;
		if (r == 1 && c == 6)
			return girisAdet.subtract(cikisAdet);

		return null;
	}

}
