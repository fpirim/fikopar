package tr.fikopar.modules.siparis.model;

import java.math.BigDecimal;

import tr.fikopar.db.models.windows.siparis.SiparisDetayModel;

public class SiparisDetayViewModel {
	private Integer id;
	private BigDecimal katno = BigDecimal.ZERO;
	private String stno = "";
	private String prcno = "";
	private String oemno = "";
	private String tipi = "";
	private String parcaadi = "";
	private String marka = "";
	private BigDecimal isk1 = BigDecimal.ZERO;
	private BigDecimal isk2 = BigDecimal.ZERO;
	private BigDecimal kdv = BigDecimal.ZERO;
	private BigDecimal adet = BigDecimal.ZERO;
	private BigDecimal fiyati = BigDecimal.ZERO;
	private BigDecimal sevkadet = BigDecimal.ZERO;
	private Integer idwb_siparis;
	
	public Integer getIdwb_siparis() {
		return idwb_siparis;
	}
	public void setIdwb_siparis(Integer idwb_siparis) {
		this.idwb_siparis = idwb_siparis;
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
	public String getOemno() {
		return oemno;
	}
	public void setOemno(String oemno) {
		this.oemno = oemno;
	}
	public String getTipi() {
		return tipi;
	}
	public void setTipi(String tipi) {
		this.tipi = tipi;
	}
	public String getParcaadi() {
		return parcaadi;
	}
	public void setParcaadi(String parcaadi) {
		this.parcaadi = parcaadi;
	}
	public String getMarka() {
		return marka;
	}
	public void setMarka(String marka) {
		this.marka = marka;
	}
	public BigDecimal getIsk1() {
		return isk1;
	}
	public void setIsk1(BigDecimal isk1) {
		this.isk1 = isk1;
	}
	public BigDecimal getIsk2() {
		return isk2;
	}
	public void setIsk2(BigDecimal isk2) {
		this.isk2 = isk2;
	}
	public BigDecimal getKdv() {
		return kdv;
	}
	public void setKdv(BigDecimal kdv) {
		this.kdv = kdv;
	}
	public BigDecimal getAdet() {
		return adet;
	}
	public void setAdet(BigDecimal adet) {
		this.adet = adet;
	}
	public BigDecimal getFiyati() {
		return fiyati;
	}
	public void setFiyati(BigDecimal fiyati) {
		this.fiyati = fiyati;
	}
	
	public BigDecimal getSevkadet() {
		return sevkadet;
	}
	public void setSevkadet(BigDecimal sevkadet) {
		this.sevkadet = sevkadet;
	}
	public boolean isFullyEqual(SiparisDetayModel o1) {
		return false;
	}
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof SiparisDetayModel) {
    		SiparisDetayModel o1 = (SiparisDetayModel) obj;
    		//return o1.getId() == null ? getId() == null : o1.getId().equals(o1.get)
    		
    		
    	} else if(obj instanceof SiparisDetayViewModel) {
    		SiparisDetayViewModel o1 = (SiparisDetayViewModel) obj;
    		return o1.getId() == null ? false : o1.getId().equals(getId());
    	}
    		
    	return super.equals(obj);
    }
	
}
