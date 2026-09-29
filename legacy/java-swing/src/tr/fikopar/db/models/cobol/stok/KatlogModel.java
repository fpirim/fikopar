package tr.fikopar.db.models.cobol.stok;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="`COB_KATLOG`", catalog="`COBOL`")
public class KatlogModel implements Serializable{
	@Id
	@Column(name="`KT-KATNO`", columnDefinition="DECIMAL", precision=4, scale=0, nullable=false)
	private BigDecimal katno;
	@Column(name="`KT-ACIKLAMA`", columnDefinition="VARCHAR", length=60, nullable=false)
	private String aciklama;
	@Column(name="`KT-HESAPNO`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String hesapno;
	
	public BigDecimal getKatno() {
		return katno;
	}
	public void setKatno(BigDecimal katno) {
		this.katno = katno;
	}
	
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	public String getHesapno() {
		return hesapno;
	}

	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}
	
	
}
