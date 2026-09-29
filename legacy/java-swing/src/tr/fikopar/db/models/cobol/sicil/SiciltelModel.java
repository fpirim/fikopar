package tr.fikopar.db.models.cobol.sicil;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "`COB_SICILTEL`", catalog="`COBOL`")
public class SiciltelModel implements Serializable {
	@Id
	@Column(name="`SCT-HESAPNO`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String hesapno;
	@Id
	@Column(name="`SCT-SIRA`", columnDefinition="DECIMAL", precision=10, scale=0, nullable=false)
	private BigDecimal sira;
	@Column(name="`SCT-TEL`", columnDefinition="DECIMAL", precision=12, scale=0, nullable=false)
	private BigDecimal tel;
	@Column(name="`SCT-DAHILI`", columnDefinition="VARCHAR", length=5, nullable=false)
	private String dahili;
	@Column(name="`SCT-TIP`", columnDefinition="DECIMAL", precision=1, scale=0, nullable=false)
	private BigDecimal tip;
	
	public String getHesapno() {
		return hesapno;
	}

	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}

	public BigDecimal getSira() {
		return sira;
	}
	public void setSira(BigDecimal sira) {
		this.sira = sira;
	}
	
	public BigDecimal getTel() {
		return tel;
	}
	public void setTel(BigDecimal tel) {
		this.tel = tel;
	}
	
	public String getDahili() {
		return dahili;
	}

	public void setDahili(String dahili) {
		this.dahili = dahili;
	}

	public BigDecimal getTip() {
		return tip;
	}
	public void setTip(BigDecimal tip) {
		this.tip = tip;
	}
}
