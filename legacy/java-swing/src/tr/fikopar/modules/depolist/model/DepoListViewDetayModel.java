package tr.fikopar.modules.depolist.model;

public class DepoListViewDetayModel {

	private Integer id;
	private String tarih;
	private String hesapno;
	private String hesapadi;
	private String adres;
	private String depoalan;
	private String note;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTarih() {
		return tarih;
	}

	public void setTarih(String tarih) {
		this.tarih = tarih;
	}

	public String getHesapno() {
		return hesapno;
	}

	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}

	public String getHesapadi() {
		return hesapadi;
	}

	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi;
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getDepoalan() {
		return depoalan;
	}

	public void setDepoalan(String depoalan) {
		this.depoalan = depoalan;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	public boolean isFullyEqual(DepoListViewDetayModel o1) {
		return ((o1.getId() == null ? id == null : o1.getId().equals(id))
				&& (o1.getTarih() == null ? tarih == null : o1.getTarih().equals(tarih))
				&& (o1.getHesapno() == null ? hesapno == null : o1.getHesapno().equals(hesapno))
				&& (o1.getHesapadi() == null ? hesapadi == null : o1.getHesapadi().equals(hesapadi))
				&& (o1.getAdres() == null ? adres == null : o1.getAdres().equals(adres))
				&& (o1.getDepoalan() == null ? depoalan == null : o1.getDepoalan().equals(depoalan))
				&& (o1.getNote() == null ? note == null : o1.getNote().equals(note)));
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DepoListViewDetayModel) {
			DepoListViewDetayModel o1 = (DepoListViewDetayModel) obj;
			return o1.getId() == null ? id == null : o1.getId().equals(id);
		}
		return super.equals(obj);
	}

}
