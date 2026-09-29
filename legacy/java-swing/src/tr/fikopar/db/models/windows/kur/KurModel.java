package tr.fikopar.db.models.windows.kur;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "win_kur", catalog="windows")
public class KurModel implements Serializable{
	@Id
	private String kur;
	@Column(name = "fiyat", columnDefinition = "DECIMAL", precision = 20, scale = 4)
	private BigDecimal fiyat;
	public String getKur() {
		return kur;
	}
	public void setKur(String kur) {
		this.kur = kur;
	}
	public BigDecimal getFiyat() {
		return fiyat;
	}
	public void setFiyat(BigDecimal fiyat) {
		this.fiyat = fiyat;
	}
	
	
}
