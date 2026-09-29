package tr.fikopar.db.models.cobol.stok;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class StokPK implements Serializable{
	@Column(name = "`ST-KATNO`", columnDefinition = "DECIMAL", precision=4, scale=0, nullable = false)
	private BigDecimal katno;
	@Column(name = "`ST-STNO`", columnDefinition = "VARCHAR", length=15, nullable = false)
	private String stno;
	public StokPK() {}
	
	public StokPK(BigDecimal katno, String stno) {
		this.katno = katno;
		this.stno = stno;
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

}
