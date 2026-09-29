package tr.fikopar.modules.siparis.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import tr.fikopar.modules.siparis.model.SiparisDetayViewModel;
import tr.fikopar.swing.table.FTableModel;

public class SiparisToplamTableModel extends FTableModel {

	private BigDecimal toplam, isktoplam, aratoplam, kdvtoplam, geneltoplam;
	@Override
	public int getRowCount() {
		return 5;
	}
	public void calculate(List<?> list) {
		toplam = BigDecimal.ZERO;
		isktoplam = BigDecimal.ZERO;
		kdvtoplam = BigDecimal.ZERO;
		BigDecimal adet, fiyati, isk1, isk2, kdv, tutar, isk1tutar, isk2tutar, kdvtutar;
		BigDecimal aHundered = new BigDecimal(100);
		for(Object obj : list) {
			SiparisDetayViewModel siparisDetayViewModel = (SiparisDetayViewModel) obj;
			adet = siparisDetayViewModel.getAdet() == null ? BigDecimal.ZERO : siparisDetayViewModel.getAdet();
			fiyati = siparisDetayViewModel.getFiyati() == null ? BigDecimal.ZERO : siparisDetayViewModel.getFiyati();
			isk1 = siparisDetayViewModel.getIsk1() == null ? BigDecimal.ZERO : siparisDetayViewModel.getIsk1(); 
			isk2 = siparisDetayViewModel.getIsk2() == null ? BigDecimal.ZERO : siparisDetayViewModel.getIsk2();
			kdv = siparisDetayViewModel.getKdv() == null ? BigDecimal.ZERO : siparisDetayViewModel.getKdv();
			
			tutar = adet.multiply(fiyati).setScale(2,RoundingMode.HALF_UP);
			isk1tutar = tutar.multiply(isk1).divide(aHundered);
			isk2tutar = (tutar.subtract(isk1tutar)).multiply(isk2).divide(aHundered);
			kdvtutar = ((tutar.subtract(isk1tutar)).subtract(isk2tutar)).multiply(kdv).divide(aHundered);
			
			toplam = toplam.add(tutar);
			isktoplam = isktoplam.add(isk1tutar).add(isk2tutar);
			kdvtoplam = kdvtoplam.add(kdvtutar);
			aratoplam = toplam.subtract(isktoplam);
			geneltoplam = aratoplam.add(kdvtoplam);
		}
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex < 12)
			return null;
		switch(columnIndex) {
				case 12:
				 switch(rowIndex) {
				 	case 0: return "Toplam :";
				 	case 1: return "Ýskonto Toplamý :";
				 	case 2: return "Ara Toplam :";
				 	case 3: return "Kdv Toplam :";
				 	case 4: return "Genel Toplam :";
				 }
				case 13: {
					switch(rowIndex) {
					case 0: return toplam;
					case 1: return isktoplam;
					case 2: return aratoplam;
					case 3: return kdvtoplam;
					case 4: return geneltoplam;
					}
				}
					
		}
		
		return null;
	}
	
}