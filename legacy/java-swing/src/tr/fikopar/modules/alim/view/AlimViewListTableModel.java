package tr.fikopar.modules.alim.view;

import tr.fikopar.modules.alim.model.AlimViewModel;
import tr.fikopar.swing.table.FTableModel;

public class AlimViewListTableModel extends FTableModel{

	@Override
	public Object getValueAt(int r, int c) {
		if(list == null || list.size() <= r) 
			return null;
		AlimViewModel alimViewModel = (AlimViewModel) list.get(r);
		switch(c) {
			case 0 : return alimViewModel.getId();
			case 1:  return alimViewModel.getTarih();
			case 2:  return alimViewModel.getDusukseAktar();
			case 3:  return alimViewModel.getYuksekseAktar();
			case 4 : return alimViewModel.getHesapno();
			case 5 : return alimViewModel.getHesapadi();
			case 6 : return alimViewModel.getYetkili();
			case 7 : return alimViewModel.getNote();
			case 8 : return alimViewModel.getExcelViewModel() == null ? null : alimViewModel.getExcelViewModel().getExcelFileName();
		}
		return null;
	}

}
