package tr.fikopar.swing.db.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="tablestyle")
public class FTableStyle implements Serializable {
	@Id
	@Column(name="classKey", columnDefinition="string")
	private String classKey;
	@OneToMany(targetEntity = FTableCellStyle.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "classKey")
	private List<FTableCellStyle> fTableCellStyles = new ArrayList<>();
	
	public FTableStyle() {
	}
	public FTableStyle(String classKey, FTableCellStyle [] fTableCellStyles) {
		this.classKey = classKey;
		for(int i = 0; i<fTableCellStyles.length; i++) {
			fTableCellStyles[i].setTableStyle(this);
			this.fTableCellStyles.add(fTableCellStyles[i]);
		}
	}
	
	public void setClassKey(String classKey) {
		this.classKey = classKey;
	}
	public String getClassKey() {
		return classKey;
	}
	public List<FTableCellStyle> getfTableCellStyles() {
		return fTableCellStyles;
	}
	public void setfTableCellStyles(List<FTableCellStyle> fTableCellStyles) {
		this.fTableCellStyles = fTableCellStyles;
	}
	
}
