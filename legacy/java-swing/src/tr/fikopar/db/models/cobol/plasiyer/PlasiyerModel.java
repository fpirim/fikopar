package tr.fikopar.db.models.cobol.plasiyer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "`COB_PLASIYER`", catalog="`COBOL`")
public class PlasiyerModel {
	@Id
	@Column(name="`PL-NO`", columnDefinition="DECIMAL", precision=4, scale = 0, nullable = false)
	private Integer plno;
	@Column(name="`PL-ADI`", columnDefinition="VARCHAR", length = 60, nullable = false)
	private String pladi;
	public Integer getPlno() {
		return plno;
	}
	public void setPlno(Integer plno) {
		this.plno = plno;
	}
	public String getPladi() {
		return pladi;
	}
	public void setPladi(String pladi) {
		this.pladi = pladi;
	}
	
}
