package tr.fikopar.db.models.windows.sayim;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(name = "win_sayim", catalog="windows")
public class SayimModel implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name="yil", columnDefinition="DECIMAL", precision=4, scale = 0, nullable = false, unique = true)
	private BigDecimal yil;
	
	@OneToMany(targetEntity = SayimDetayModel.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderColumn(name = "LINDEX")
	@JoinColumn(name = "idsayim")
	private List<SayimDetayModel> sayimDetays = new ArrayList<SayimDetayModel>();
	
	public List<SayimDetayModel> getSayimDetays() {
		return sayimDetays;
	}

	public void setSayimDetays(List<SayimDetayModel> sayimDetays) {
		this.sayimDetays = sayimDetays;
	}

	public BigDecimal getYil() {
		return yil;
	}

	public void setYil(BigDecimal yil) {
		this.yil = yil;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


}
