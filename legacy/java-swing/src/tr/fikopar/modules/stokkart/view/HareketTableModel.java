package tr.fikopar.modules.stokkart.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import tr.fikopar.modules.stokkart.model.HareketDetayModel;
import tr.fikopar.swing.table.FTableModel;

public class HareketTableModel extends FTableModel {

	public static final int GIRIS = 0;
	public static final int CIKIS = 1;
	private HareketToplamTableModel hareketToplamTableModel;
	public HareketTableModel(int type) {
		this.hareketToplamTableModel = new HareketToplamTableModel(type);
	}
	
	private BigDecimal aHundered = new BigDecimal(100);
	private BigDecimal iskonto1;
	private BigDecimal iskonto2;
	private BigDecimal kdv;
	
	
	public HareketToplamTableModel getHareketToplamTableModel() {
		return hareketToplamTableModel;
	}
	@Override
	public void setList(List list) {
		super.setList(list);
		hareketToplamTableModel.setList(list);
	}
	@Override
	public void fireTableDataChanged() {
		super.fireTableDataChanged();
		hareketToplamTableModel.fireTableDataChanged();
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		HareketDetayModel hareketDetayModel = (HareketDetayModel) list.get(rowIndex);
				
		switch(columnIndex) {
		case 0:
			return hareketDetayModel.getTarih();
		case 1:
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.DEVIR) == 0)
				return "DVR.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_CIKIS) == 0)
				return "FAT.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_FARK_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_FARK_CIKIS) == 0)
				return "FRK.FAT.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_IADE_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_IADE_CIKIS) == 0)
				return "ÝAD.FAT.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_CIKIS) == 0)
				return "ÝFT.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_IADE_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_IADE_CIKIS) == 0)
				return "ÝAD.ÝFT";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_CIKIS) == 0)
				return "ÝRS.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_IADE_GIRIS) == 0 || hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_IADE_CIKIS) == 0)
				return "ÝAD.ÝRS.";
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.COKLU) == 0)
				return "(Çoklu)";
			return hareketDetayModel.getTip();
		case 2:
			return hareketDetayModel.getEvrakno();
		case 3:
			return hareketDetayModel.getDepono();
		case 4:
			return hareketDetayModel.getHesapno();
		case 5:
			return hareketDetayModel.getHesapadi();
		case 6:
			return hareketDetayModel.getAdet();
		case 7:
			return hareketDetayModel.getIskonto1();
		case 8:
			return hareketDetayModel.getIskonto2();
		case 9:
			return hareketDetayModel.getKdv();
		case 10:
			return hareketDetayModel.getFiyat();
		case 11:
			iskonto1 = hareketDetayModel.getFiyat().multiply(hareketDetayModel.getIskonto1()).divide(aHundered).setScale(2, RoundingMode.HALF_UP);
			iskonto2 = hareketDetayModel.getFiyat().subtract(iskonto1).multiply(hareketDetayModel.getIskonto2()).divide(aHundered).setScale(2, RoundingMode.HALF_UP);
			return hareketDetayModel.getFiyat().subtract(iskonto1).subtract(iskonto2);
		case 12:
			iskonto1 = hareketDetayModel.getFiyat().multiply(hareketDetayModel.getIskonto1()).divide(aHundered).setScale(2, RoundingMode.HALF_UP);
			iskonto2 = hareketDetayModel.getFiyat().subtract(iskonto1).multiply(hareketDetayModel.getIskonto2()).divide(aHundered).setScale(2, RoundingMode.HALF_UP);
			kdv = hareketDetayModel.getFiyat().subtract(iskonto1).subtract(iskonto2).multiply(hareketDetayModel.getKdv()).divide(aHundered).setScale(2, RoundingMode.HALF_UP);
			return hareketDetayModel.getFiyat().subtract(iskonto1).subtract(iskonto2).add(kdv);
		}
		
		return null;
	}

}
