package tr.fikopar.db.models.cobol.stok;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="`COB_STOKADET`", catalog="`COBOL`")
public class StkadtModel implements Serializable{
	@Id
	@Column(name = "`STA-KATNO`", columnDefinition = "DECIMAL", precision=4, scale=0, nullable = false)
	private BigDecimal katno;
	@Id
	@Column(name = "`STA-STNO`", columnDefinition = "VARCHAR", nullable = false)
	private String stno;
	@Id
	@Column(name = "`STA-YIL`", columnDefinition = "DECIMAL", precision=4, scale=0, nullable = false)
	private BigDecimal yil;
	@Id
	@Column(name = "`STA-DEPONO`", columnDefinition = "DECIMAL", precision=4, scale=0, nullable = false)
	private Integer depono;
	@Column(name="`STA-GIR`", columnDefinition="DECIMAL", precision=16, scale=4, nullable=false)
	private BigDecimal gir;
	@Column(name="`STA-CIK`", columnDefinition="DECIMAL", precision=16, scale=4, nullable=false)
	private BigDecimal cik;
	
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
	public BigDecimal getYil() {
		return yil;
	}
	public void setYil(BigDecimal yil) {
		this.yil = yil;
	}
	public Integer getDepono() {
		return depono;
	}
	public void setDepono(Integer depono) {
		this.depono = depono;
	}
	public BigDecimal getGir() {
		return gir;
	}
	public void setGir(BigDecimal gir) {
		this.gir = gir;
	}
	public BigDecimal getCik() {
		return cik;
	}
	public void setCik(BigDecimal cik) {
		this.cik = cik;
	}
	
	
	
	
}
