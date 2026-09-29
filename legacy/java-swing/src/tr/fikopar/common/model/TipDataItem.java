package tr.fikopar.common.model;

public class TipDataItem {

	private Integer kod;
	private String tip;

	public TipDataItem(Integer kod, String tip) {
		this.kod = kod;
		this.tip = tip;
	}

	public Integer getKod() {
		return kod;
	}

	public String getTip() {
		return tip;
	}

	@Override
	public String toString() {
		return tip;
	}

}