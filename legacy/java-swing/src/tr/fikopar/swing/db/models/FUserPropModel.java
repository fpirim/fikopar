package tr.fikopar.swing.db.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="userpropmodel")
public class FUserPropModel implements Serializable {
	@Id
	@Column(name="propKey", columnDefinition="string")
	private String propKey;
	@Column(name="propValue", columnDefinition="string")
	private String propValue;
	
	public String getPropKey() {
		return propKey;
	}
	public void setPropKey(String propKey) {
		this.propKey = propKey;
	}
	
	public String getPropValue() {
		return propValue;
	}
	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}
}
