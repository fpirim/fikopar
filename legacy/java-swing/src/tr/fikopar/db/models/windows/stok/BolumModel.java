package tr.fikopar.db.models.windows.stok;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;

@Entity
@Table(name="bolum", catalog="windows")
public class BolumModel implements Serializable {
	private StokPK stokPK;
	private String bolum;
	private Integer sira;
	private Integer bolumsira;
	private StokModel stokModel;

	@OneToOne(fetch = FetchType.LAZY, optional=false)
	@JoinColumns({@JoinColumn(name="katno", columnDefinition="INT"), @JoinColumn(name="stno", columnDefinition="VARCHAR")})
	public StokModel getStokModel() {
		return stokModel;
	}
	
	public void setStokModel(StokModel stokModel) {
		this.stokModel = stokModel;
	}
	
	@EmbeddedId
	@GenericGenerator(name="gen", strategy = "foreign", parameters = @Parameter(name="property", value="stokModel"))
	@GeneratedValue(generator = "gen")
	public StokPK getStokPK() {
		return stokPK;
	}
	
	public void setStokPK(StokPK stokPK) {
		this.stokPK = stokPK;
	}
	
	
	@Column
	public String getBolum() {
		return bolum;
	}
	public void setBolum(String bolum) {
		this.bolum = bolum;
	}
	@Column(name = "sira", columnDefinition = "INT", nullable = false)
	public Integer getSira() {
		return sira;
	}
	public void setSira(Integer sira) {
		this.sira = sira;
	}
	@Column(name = "bolumsira", columnDefinition = "INT", nullable = false)
	public Integer getBolumsira() {
		return bolumsira;
	}
	public void setBolumsira(Integer bolumsira) {
		this.bolumsira = bolumsira;
	}
}
