package tr.fikopar.modules.stokkart.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class HareketDetayModel {
	private String tarih;
	private BigDecimal tip;
	private String evrakno;
	private BigDecimal depono;
	private String hesapno;
	private String hesapadi;
	private BigDecimal adet = BigDecimal.ZERO;
	private BigDecimal iskonto1 = BigDecimal.ZERO;
	private BigDecimal iskonto2 = BigDecimal.ZERO;
	private BigDecimal kdv = BigDecimal.ZERO;
	private BigDecimal fiyat = BigDecimal.ZERO;
	private BigDecimal bag1 = BigDecimal.ZERO;
	private String bevrakno1 = "";
	private BigDecimal bag2 = BigDecimal.ZERO;
	private String bevrakno2 = "";
	
	private List<HareketDetayModel> cokluList = new ArrayList<>();

	private static BigDecimal aHundred = new BigDecimal(100);
	
	public boolean bagEkle(HareketDetayModel b1) {
		if(!this.getHesapno().equals(b1.getHesapno()))
			return false;
		// Irsaliyeden Fatura Birlestir
		if(b1.getTip().compareTo(HareketDetayModel.IRS_CIKIS) == 0 ||
				b1.getTip().compareTo(HareketDetayModel.IRS_GIRIS) == 0 ||
				b1.getTip().compareTo(HareketDetayModel.IRS_IADE_CIKIS) == 0 ||
				b1.getTip().compareTo(HareketDetayModel.IRS_IADE_GIRIS) == 0) {
//				this.setBag2(b1.getBag2());
//				this.setBevrakno2(b1.getBevrakno2());
//				this.setIskonto1(b1.getIskonto1());
//				this.setIskonto2(b1.getIskonto2());
//				this.setKdv(b1.getKdv());
//				this.setFiyat(b1.getFiyat());
				cokluList.add(b1);
				return true;
		}
			
		// Fiyatlari birlestir
		if(cokluList.isEmpty()) {
			HareketDetayModel itSelf = new HareketDetayModel();
			itSelf.setTarih(this.getTarih());
			itSelf.setTip(this.getTip());
			itSelf.setEvrakno(this.getEvrakno());
			itSelf.setDepono(this.getDepono());
			itSelf.setHesapno(this.getHesapno());
			itSelf.setHesapadi(this.getHesapadi());
			itSelf.setAdet(this.getAdet());
			itSelf.setIskonto1(this.getIskonto1());
			itSelf.setIskonto2(this.getIskonto2());
			itSelf.setKdv(this.getKdv());
			itSelf.setFiyat(this.getFiyat());
			itSelf.setBag1(this.getBag1());
			itSelf.setBevrakno1(this.getBevrakno1());
			itSelf.setBag2(this.getBag2());
			itSelf.setBevrakno2(this.getBevrakno2());
			cokluList.add(itSelf);
			
		}
		
		this.setEvrakno("(Çoklu)");
		this.setTip(COKLU);
		
		BigDecimal iskonto1 = this.getFiyat().multiply(this.getIskonto1()).divide(aHundred).setScale(4, RoundingMode.HALF_UP);
		BigDecimal iskonto2 = ((this.getFiyat().subtract(iskonto1)).multiply(this.getIskonto2())).divide(aHundred).setScale(4, RoundingMode.HALF_UP);
		BigDecimal kdv = ((this.getFiyat().subtract(iskonto1).subtract(iskonto2)).multiply(this.getKdv())).divide(aHundred).setScale(4, RoundingMode.HALF_UP);
		
		BigDecimal b1_iskonto1 = b1.getFiyat().multiply(b1.getIskonto1()).divide(aHundred).setScale(4, RoundingMode.HALF_UP);
		BigDecimal b1_iskonto2 = ((b1.getFiyat().subtract(b1_iskonto1)).multiply(b1.getIskonto2())).divide(aHundred).setScale(4, RoundingMode.HALF_UP);
		BigDecimal b1_kdv = ((b1.getFiyat().subtract(b1_iskonto1).subtract(b1_iskonto2)).multiply(b1.getKdv())).divide(aHundred).setScale(4, RoundingMode.HALF_UP);
		
		iskonto1 = iskonto1.add(iskonto2).add(b1_iskonto1).add(b1_iskonto2);
		kdv = kdv.add(b1_kdv);
		
		this.setFiyat( this.getFiyat().add(b1.getFiyat()) );
		
		this.setIskonto1( ( iskonto1.multiply(aHundred) ).divide(this.getFiyat(), 4, RoundingMode.HALF_UP) );
		this.setIskonto2( BigDecimal.ZERO );
		this.setKdv( ( kdv.multiply(aHundred) ).divide(this.getFiyat().subtract(iskonto1), 4, RoundingMode.HALF_UP) );
		
		cokluList.add(b1);
		return true;
	}
	public List<HareketDetayModel> getCokluList() {
		return cokluList;
	}
			
	public String getTarih() {
		return tarih;
	}
	public BigDecimal getBag1() {
		return bag1;
	}
	public void setBag1(BigDecimal bag1) {
		this.bag1 = bag1;
	}
	public String getBevrakno1() {
		return bevrakno1;
	}
	public void setBevrakno1(String bevrakno1) {
		this.bevrakno1 = bevrakno1.trim();
	}
	public BigDecimal getBag2() {
		return bag2;
	}
	public void setBag2(BigDecimal bag2) {
		this.bag2 = bag2;
	}
	public String getBevrakno2() {
		return bevrakno2;
	}
	public void setBevrakno2(String bevrakno2) {
		this.bevrakno2 = bevrakno2.trim();
	}
	public void setTarih(String tarih) {
		this.tarih = tarih.trim();
	}
	public BigDecimal getTip() {
		return tip;
	}
	public void setTip(BigDecimal tip) {
		this.tip = tip;
	}
	public String getEvrakno() {
		return evrakno;
	}
	public void setEvrakno(String evrakno) {
		this.evrakno = evrakno.trim();
	}
	public BigDecimal getDepono() {
		return depono;
	}
	public void setDepono(BigDecimal depono) {
		this.depono = depono;
	}
	public String getHesapno() {
		return hesapno;
	}
	public void setHesapno(String hesapno) {
		this.hesapno = hesapno.trim();
	}
	public String getHesapadi() {
		return hesapadi;
	}
	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi.trim();
	}
	public BigDecimal getAdet() {
		return adet;
	}
	public void setAdet(BigDecimal adet) {
		this.adet = adet;
	}
	public BigDecimal getIskonto1() {
		return iskonto1;
	}
	public void setIskonto1(BigDecimal iskonto1) {
		this.iskonto1 = iskonto1;
	}
	public BigDecimal getIskonto2() {
		return iskonto2;
	}
	public void setIskonto2(BigDecimal iskonto2) {
		this.iskonto2 = iskonto2;
	}
	public BigDecimal getKdv() {
		return kdv;
	}
	public void setKdv(BigDecimal kdv) {
		this.kdv = kdv;
	}
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}
	
	public static final BigDecimal DEVIR = new BigDecimal(0);
	public static final BigDecimal IFT_CIKIS = new BigDecimal(1);
	public static final BigDecimal IRS_CIKIS = new BigDecimal(2);
	public static final BigDecimal FAT_CIKIS = new BigDecimal(3);
	public static final BigDecimal IFT_GIRIS = new BigDecimal(4);
	public static final BigDecimal IRS_GIRIS = new BigDecimal(5);
	public static final BigDecimal FAT_GIRIS = new BigDecimal(6);
	public static final BigDecimal IFT_IADE_CIKIS = new BigDecimal(7);
	public static final BigDecimal IRS_IADE_CIKIS = new BigDecimal(8);
	public static final BigDecimal FAT_IADE_CIKIS = new BigDecimal(9);
	public static final BigDecimal IFT_IADE_GIRIS = new BigDecimal(10);
	public static final BigDecimal IRS_IADE_GIRIS = new BigDecimal(11);
	public static final BigDecimal FAT_IADE_GIRIS = new BigDecimal(12);
	public static final BigDecimal FAT_FARK_CIKIS = new BigDecimal(13);
	public static final BigDecimal FAT_FARK_GIRIS = new BigDecimal(14);
	
	public static final BigDecimal COKLU = new BigDecimal(99);
	
}
