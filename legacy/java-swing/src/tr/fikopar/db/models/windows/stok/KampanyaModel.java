package tr.fikopar.db.models.windows.stok;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="web_kampanya", catalog="`web_product`")
public class KampanyaModel implements Serializable{
	@Id
	private Integer id;
	@Column(name = "katno", columnDefinition = "DECIMAL", precision=4, scale=0, nullable = false)
	private BigDecimal katno;
	@Column(name = "stno", columnDefinition = "VARCHAR", length=15, nullable = false)
	private String stno;
	@Column(name="tarih", columnDefinition="VARCHAR", nullable = false, length=8)
	private String tarih;
	@Column(name="kampanya", columnDefinition="DECIMAL", precision=20, scale=4)
	private BigDecimal kampanya;
	@Column(name="bitince", nullable = false, columnDefinition="TINYINT(1)")
	private boolean bitince;
	@Column(name="curadet", columnDefinition="DECIMAL", precision=20, scale=4)
	private BigDecimal curadet;
	@Column(name="sevkadet", columnDefinition="DECIMAL", precision=20, scale=4)
	private BigDecimal sevkadet;
	@Column(name="bitti", nullable = false, columnDefinition="TINYINT(1)")
	private boolean bitti;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public BigDecimal getKatno() {
		return katno;
	}
	public void setKatno(BigDecimal katno) {
		this.katno = katno;
	}
	public String getStno() {
		return stno;
	}
	public void setStno(String stno) {
		this.stno = stno;
	}
	public String getTarih() {
		return tarih;
	}
	public void setTarih(String tarih) {
		this.tarih = tarih;
	}
	public BigDecimal getKampanya() {
		return kampanya;
	}
	public void setKampanya(BigDecimal kampanya) {
		this.kampanya = kampanya;
	}
	public boolean isBitince() {
		return bitince;
	}
	public void setBitince(boolean bitince) {
		this.bitince = bitince;
	}
	public BigDecimal getCuradet() {
		return curadet;
	}
	public void setCuradet(BigDecimal curadet) {
		this.curadet = curadet;
	}
	public BigDecimal getSevkadet() {
		return sevkadet;
	}
	public void setSevkadet(BigDecimal sevkadet) {
		this.sevkadet = sevkadet;
	}
	public boolean isBitti() {
		return bitti;
	}
	public void setBitti(boolean bitti) {
		this.bitti = bitti;
	}
	
	
	
		
}
