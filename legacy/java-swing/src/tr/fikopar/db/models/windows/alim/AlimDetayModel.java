package tr.fikopar.db.models.windows.alim;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.windows.kur.KurModel;

@Entity
@Table(name = "win_alimdetay", catalog="windows")
public class AlimDetayModel implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	@Column
	private String oemno;
	@Column(name="katno", precision=4, scale=0)
	private BigDecimal katno;
	@Column
	private String stno;
	@Column
	private String prcno;
	@Column
	private String tipi;
	@Column
	private String cinsi;
	@Column
	private String marka;
	@Column(name = "iskonto1", columnDefinition = "DECIMAL", precision = 8, scale = 4)
	private BigDecimal iskonto1;
	@Column(name = "iskonto2", columnDefinition = "DECIMAL", precision = 8, scale = 4)
	private BigDecimal iskonto2;
	@Column(name = "kdv", columnDefinition = "DECIMAL", precision = 8, scale = 4)
	private BigDecimal kdv;
	@Column(name = "maliyet", columnDefinition = "DECIMAL", precision = 20, scale = 4)
	private BigDecimal maliyet;
	@Column(name = "marj", columnDefinition = "DECIMAL", precision = 20, scale = 4)
	private BigDecimal marj;
	
	@OneToOne(targetEntity=StokModel.class, fetch = FetchType.EAGER)
	@JoinColumns({@JoinColumn(name="katno", referencedColumnName="`ST-KATNO`", insertable = false, updatable = false), 
		@JoinColumn(name="stno", referencedColumnName="`ST-STNO`", insertable = false, updatable = false)})
	@NotFound(action=NotFoundAction.IGNORE)
	private StokModel stokModel;

	@OneToOne(targetEntity=KurModel.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "kur")
	private KurModel kurModel;
	
	@ManyToOne(targetEntity=AlimModel.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "idalim")
	private AlimModel alimModel;

	
	public StokModel getStokModel() {
		return stokModel;
	}
	public void setStokModel(StokModel stokModel) {
		this.stokModel = stokModel;
	}
	
	public BigDecimal getMarj() {
		return marj;
	}
	public void setMarj(BigDecimal marj) {
		this.marj = marj;
	}
	
	public String getOemno() {
		return oemno;
	}
	public void setOemno(String oemno) {
		this.oemno = oemno;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BigDecimal getKatno() {
		return katno;
	}
	public void setKatno(BigDecimal katno) {
		this.katno = katno;
	}
	public String getStno() {
		return stno;
	}

	public void setStno(String stno) {
		this.stno = stno;
	}

	public String getPrcno() {
		return prcno;
	}

	public void setPrcno(String prcno) {
		this.prcno = prcno;
	}

	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	public String getCinsi() {
		return cinsi;
	}

	public void setCinsi(String cinsi) {
		this.cinsi = cinsi;
	}

	public String getMarka() {
		return marka;
	}

	public void setMarka(String marka) {
		this.marka = marka;
	}

	public BigDecimal getIskonto1() {
		return iskonto1;
	}

	public void setIskonto1(BigDecimal iskonto1) {
		this.iskonto1 = iskonto1;
	}

	public BigDecimal getIskonto2() {
		return iskonto2;
	}

	public void setIskonto2(BigDecimal iskonto2) {
		this.iskonto2 = iskonto2;
	}

	public BigDecimal getKdv() {
		return kdv;
	}

	public void setKdv(BigDecimal kdv) {
		this.kdv = kdv;
	}

	public BigDecimal getMaliyet() {
		return maliyet;
	}

	public void setMaliyet(BigDecimal maliyet) {
		this.maliyet = maliyet;
	}

	public KurModel getKurModel() {
		return kurModel;
	}

	public void setKurModel(KurModel kurModel) {
		this.kurModel = kurModel;
	}

	public AlimModel getAlimModel() {
		return alimModel;
	}

	public void setAlimModel(AlimModel alimModel) {
		this.alimModel = alimModel;
	}
	
	
			
}
