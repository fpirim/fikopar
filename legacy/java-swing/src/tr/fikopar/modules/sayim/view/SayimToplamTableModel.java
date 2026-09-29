package tr.fikopar.modules.sayim.view;

import java.math.BigDecimal;

import tr.fikopar.swing.table.FTableModel;

public class SayimToplamTableModel extends FTableModel{

	private BigDecimal toplam, kdvToplam;
	
	public void setToplam(BigDecimal toplam) {
		this.toplam = toplam;
	}
	public BigDecimal getToplam() {
		return toplam;
	}
	public void setKdvToplam(BigDecimal kdvToplam) {
		this.kdvToplam = kdvToplam;
	}
	public BigDecimal getKdvToplam() {
		return kdvToplam;
	}
	@Override
	public int getRowCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if(r == 0 && c == 12)
			return "TOPLAM :";
		if(r == 1 && c == 12)
			return "KDV :";
		if(r == 2 && c == 12)
			return "GENEL TOPLAM :";
		if(r == 0 && c == 13 && toplam != null)
			return toplam;
		if(r == 1 && c == 13 && kdvToplam != null)
			return kdvToplam;
		if(r == 2 && c == 13 && toplam != null && kdvToplam != null)
			return toplam.add(kdvToplam);
		
		return null;
	}

}
