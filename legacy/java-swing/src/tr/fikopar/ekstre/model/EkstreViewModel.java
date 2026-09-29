package tr.fikopar.ekstre.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class EkstreViewModel implements Serializable {
	private String tarih;
	private Integer tip;
	private Integer depono;
	private String evrakno;
	private String vade;
	private String aciklama;
	private BigDecimal borc;
	private BigDecimal alacak;
	private BigDecimal bakiye = BigDecimal.ZERO;

	public String getTarih() {
		return tarih;
	}

	public Integer getDepono() {
		return depono;
	}

	public void setDepono(Integer depono) {
		this.depono = depono;
	}

	public void setTarih(String tarih) {
		this.tarih = tarih;
	}

	public Integer getTip() {
		return tip;
	}

	public void setTip(Integer tip) {
		this.tip = tip;
	}

	public String getEvrakno() {
		return evrakno;
	}

	public void setEvrakno(String evrakno) {
		this.evrakno = evrakno;
	}

	public String getVade() {
		return vade;
	}

	public void setVade(String vade) {
		this.vade = vade;
	}

	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	public BigDecimal getBorc() {
		return borc;
	}

	public void setBorc(BigDecimal borc) {
		this.borc = borc;
	}

	public BigDecimal getAlacak() {
		return alacak;
	}

	public void setAlacak(BigDecimal alacak) {
		this.alacak = alacak;
	}

	public BigDecimal getBakiye() {
		return bakiye;
	}

	public void setBakiye(BigDecimal bakiye) {
		this.bakiye = bakiye;
	}

	public String getDepoString() {
		if (depono == 1)
			return "1.Depo";
		return "2.Depo";
	}

	public String getTipString() {
		switch(tip) {
		case 0: return "Devir";
		case 1: case 4: case 7: case 10: return "Ýft.";
		case 3: case 6: case 9: case 12: case 13: case 14: return "Fat";
		case 16: case 17: return "Mak.";
		case 18: case 19: return "Çek";
		case 20: case 21: return "Senet";
		}
		return String.valueOf(tip);
	}
}
