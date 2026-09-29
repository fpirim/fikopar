package tr.fikopar.swing.db.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "usertrash", indexes = { @Index(columnList = "value", name = "idx_value", unique = false) })
public class FUserTrashModel {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "integer")
	private Integer id;
	@Column(name = "value", columnDefinition = "string")
	private String value;
	public FUserTrashModel() {
	}
	public FUserTrashModel(String value) {
		this.value = value;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
