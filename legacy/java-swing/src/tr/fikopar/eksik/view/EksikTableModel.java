package tr.fikopar.eksik.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import tr.fikopar.eksik.controller.EksikController;
import tr.fikopar.eksik.model.EksikKatalogModel;
import tr.fikopar.eksik.model.EksikStokModel;

public class EksikTableModel extends AbstractTableModel {
	
	private List<EksikStokModel> list = new ArrayList<EksikStokModel>();
	private EksikController eksikController;
	
	private String[] columnNames = new String[] { 
	"Kat No.", "Stok No", "Parça No", "Oem No", "Tipi","Cinsi","Marka","Not","Fiyat","Min","Max","Sipariþ","Stok Adet","Min Eksik","Max Eksik","Sipariþ Eksik", "Sip.Verilen", "Maliyeti", "Ýskonto","Kdv","Raf Yeri","Paket"};

	public EksikTableModel(EksikController eksikController) {
		this.eksikController = eksikController;
	}
	private Integer filterKatNo;
	public void refresh(Integer filterKatNo) {
		this.filterKatNo = filterKatNo;
		list.clear();
		for(Iterator<EksikStokModel> itr = eksikController.getEksikStokModels().iterator(); itr.hasNext(); ) {
			EksikStokModel eksikStokModel = itr.next();
			if(eksikStokModel.getEksikKatalogModels().size() == 0 && filterKatNo == 0)
					list.add(eksikStokModel);
			else {
				for(Iterator<EksikKatalogModel> itr2 = eksikStokModel.getEksikKatalogModels().iterator(); itr2.hasNext(); ) {
					EksikKatalogModel eksikKatalogModel = itr2.next();
					if(eksikKatalogModel.getKatno().equals(filterKatNo))
						list.add(eksikStokModel);
				}
			}
		}
		
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		if(list==null)
			return 0;
		return list.size();
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		EksikStokModel eksikStokModel = list.get(rowIndex);
		
		switch(columnIndex) {
		case 0:
			return eksikStokModel.getKatno();
		case 1:
			return eksikStokModel.getStno();
		case 2:
			return eksikStokModel.getPrcno();
		case 3:
			return eksikStokModel.getOemno();
		case 4:
			return eksikStokModel.getTipi();
		case 5:
			return eksikStokModel.getCinsi();
		case 6:
			return eksikStokModel.getMarka();
		case 7:
			return eksikStokModel.getNote();
		case 8:
			return eksikStokModel.getFiyat();
		case 9:
			return eksikStokModel.getMinadet();
		case 10:
			return eksikStokModel.getMaxadet();
		case 11:
			return eksikStokModel.getBakiye();
		case 12:
			return eksikStokModel.getAdet();
		case 13:
			return eksikStokModel.getMinadet().compareTo(BigDecimal.ZERO) > 0 && eksikStokModel.getMinadet().compareTo(eksikStokModel.getAdet()) > 0 ? eksikStokModel.getMinadet().subtract(eksikStokModel.getAdet()) : BigDecimal.ZERO; 
		case 14:
			return eksikStokModel.getMaxadet().compareTo(BigDecimal.ZERO) > 0 && eksikStokModel.getMaxadet().compareTo(eksikStokModel.getAdet()) > 0 ? eksikStokModel.getMaxadet().subtract(eksikStokModel.getAdet()) : BigDecimal.ZERO; 
		case 15:
			return eksikStokModel.getBakiye().compareTo(BigDecimal.ZERO) > 0 && eksikStokModel.getBakiye().compareTo(eksikStokModel.getAdet()) > 0 ? eksikStokModel.getBakiye().subtract(eksikStokModel.getAdet()) : BigDecimal.ZERO;
		case 16:
			return eksikStokModel.getVerbakiye();
		case 17:
			if(filterKatNo == 0)
				return BigDecimal.ZERO;
			for(EksikKatalogModel eksikKatalogModel : eksikStokModel.getEksikKatalogModels())
				if(eksikKatalogModel.getKatno().equals(filterKatNo)) return eksikKatalogModel.getFiyat();
			return BigDecimal.ZERO;
		case 18:
			if(filterKatNo == 0)
				return BigDecimal.ZERO;
			for(EksikKatalogModel eksikKatalogModel : eksikStokModel.getEksikKatalogModels())
				if(eksikKatalogModel.getKatno().equals(filterKatNo)) return eksikKatalogModel.getIsk();
			return BigDecimal.ZERO;
		case 19:
			if(filterKatNo == 0)
				return BigDecimal.ZERO;
			for(EksikKatalogModel eksikKatalogModel : eksikStokModel.getEksikKatalogModels())
				if(eksikKatalogModel.getKatno().equals(filterKatNo)) return eksikKatalogModel.getKdv();
			return BigDecimal.ZERO;
		case 20:
			return eksikStokModel.getRafyeri();
		case 21:
			return eksikStokModel.getPaket();
		}
		
		
		return null;
	}
	

}
