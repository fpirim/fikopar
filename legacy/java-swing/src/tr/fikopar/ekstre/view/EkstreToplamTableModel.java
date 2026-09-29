package tr.fikopar.ekstre.view;

import java.math.BigDecimal;

import tr.fikopar.swing.table.FTableModel;

public class EkstreToplamTableModel extends FTableModel{

	private BigDecimal borcToplam, alacakToplam;
	
	public void setBorcToplam(BigDecimal borcToplam) {
		this.borcToplam = borcToplam;
	}
	public void setAlacakToplam(BigDecimal alacakToplam) {
		this.alacakToplam = alacakToplam;
	}
	
	public BigDecimal getBorcToplam() {
		return borcToplam;
	}
	public BigDecimal getAlacakToplam() {
		return alacakToplam;
	}
	@Override
	public int getRowCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if(r == 0 && c == 6)
			return borcToplam;
		if(r == 0 && c == 7)
			return alacakToplam;
		if(r == 1 && c == 6 && borcToplam != null && alacakToplam != null)
			return borcToplam.compareTo(alacakToplam) > 0 ? borcToplam.subtract(alacakToplam) : BigDecimal.ZERO;
		if(r == 1 && c == 7 && borcToplam != null && alacakToplam != null)
			return alacakToplam.compareTo(borcToplam) > 0 ? alacakToplam.subtract(borcToplam) : BigDecimal.ZERO;
					
		
		return null;
	}

}
