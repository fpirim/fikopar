package tr.fikopar.modules.depo.model;

import java.math.BigDecimal;

public class DepoDetayViewModel {
	private Integer id;
	private BigDecimal katno = BigDecimal.ZERO;
	private String stno = "";
	private String prcno = "";
	private String oemno = "";
	private String tipi = "";
	private String parcaadi = "";
	private String marka = "";
	private BigDecimal adet = BigDecimal.ZERO;
	private BigDecimal bakiye = BigDecimal.ZERO;
	
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
	public BigDecimal getAdet() {
		return adet;
	}
	public void setAdet(BigDecimal adet) {
		this.adet = adet;
	}
	public BigDecimal getBakiye() {
		return bakiye;
	}
	public void setBakiye(BigDecimal bakiye) {
		this.bakiye = bakiye;
	}
	
	@Override
    public boolean equals(Object obj) {
    	if(obj instanceof DepoDetayViewModel) {
    		DepoDetayViewModel o1 = (DepoDetayViewModel) obj;
    		return o1.getId() == null ? false : o1.getId().equals(getId());
    	}
    		
    	return super.equals(obj);
    }
	
}
