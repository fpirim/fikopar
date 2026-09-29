package tr.fikopar.db.models.cobol.hareket;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class HarsicModelPK implements Serializable{
	@Column(name="`HS-TIP`", columnDefinition="DECIMAL", precision=2, scale=0, nullable=false)
	private BigDecimal tip;
	@Column(name="`HS-EVRAKNO`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String evrakno;
	
	public HarsicModelPK() {}

	public HarsicModelPK(BigDecimal tip, String evrakno) {
		this.tip = tip;
		this.evrakno = evrakno;
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

}
