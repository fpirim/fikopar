package tr.fikopar.modules.kur.model;

import java.math.BigDecimal;

import tr.fikopar.swing.view.FViewModel;

public class KurViewModel extends FViewModel{
	private String kur;
	private BigDecimal fiyat;
	
	public String getKur() {
		return kur;
	}
	public void setKur(String kur) {
		this.kur = kur;
	}
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}
	@Override
	public String toString() {
		return kur;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KurViewModel)
			return ((KurViewModel) obj).getKur() == null ? kur == null : ((KurViewModel) obj).getKur().equals(kur); 
		return super.equals(obj);
	}
}
