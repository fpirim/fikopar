package tr.fikopar.db.models.cobol.sicil;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "`COB_CARHAR`", catalog="`COBOL`")
public class SicilHareket implements Serializable {
	@Id
	@Column(name="`CH-HESAPNO`",columnDefinition="VARCHAR", length=15, nullable=false)
	private String hesapno;
	@Id
	@Column(name="`CH-DEPONO`", columnDefinition="DECIMAL", precision=4, scale=0, nullable=false)
	private BigDecimal depono;
	@Id
	@Column(name="`CH-TARIH`",columnDefinition="VARCHAR", length=8, nullable=false)
	private String tarih;
	@Id
	@Column(name="`CH-SIRA`", columnDefinition="DECIMAL", precision=10, scale=0, nullable=false)
	private BigDecimal sira;
	@Column(name="`CH-TIP`", columnDefinition="DECIMAL", precision=2, scale=0, nullable=false)
	private BigDecimal tip;
	@Column(name="`CH-EVRAKNO`",columnDefinition="VARCHAR", length=15, nullable=false)
	private String evrakno;
	@Column(name="`CH-ACIKLAMA`",columnDefinition="VARCHAR", length=60, nullable=false)
	private String aciklama;
	@Column(name="`CH-VADE`",columnDefinition="VARCHAR", length=15, nullable=false)
	private String vade;
	@Column(name="`CH-BORC`", columnDefinition="DECIMAL", precision=15, scale=2, nullable=false)
	private BigDecimal borc;
	@Column(name="`CH-ALACAK`", columnDefinition="DECIMAL", precision=15, scale=2, nullable=false)
	private BigDecimal alacak;
	public String getHesapno() {
		return hesapno;
	}
	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}
	public BigDecimal getDepono() {
		return depono;
	}
	public void setDepono(BigDecimal depono) {
		this.depono = depono;
	}
	public String getTarih() {
		return tarih;
	}
	public void setTarih(String tarih) {
		this.tarih = tarih;
	}
	public BigDecimal getSira() {
		return sira;
	}
	public void setSira(BigDecimal sira) {
		this.sira = sira;
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
		this.evrakno = evrakno;
	}
	public String getAciklama() {
		return aciklama;
	}
	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}
	public String getVade() {
		return vade;
	}
	public void setVade(String vade) {
		this.vade = vade;
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
	
	
	

}
