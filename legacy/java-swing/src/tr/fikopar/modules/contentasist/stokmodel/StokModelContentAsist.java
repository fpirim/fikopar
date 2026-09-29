package tr.fikopar.modules.contentasist.stokmodel;

import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FTableContentAsist;

public class StokModelContentAsist {

	private static StokModelContentAsist stokModelContentAsist;

	private FTableContentAsist contentAsist;
	private StokModelContentAsistService service;
	private StokModelContentAsistTableModel tableModel;
	
	public StokModelContentAsist() {

		service = new StokModelContentAsistService();
		tableModel = new StokModelContentAsistTableModel();
		contentAsist = new FTableContentAsist(tableModel, service);
	}
	public FTableContentAsist getContentAsist() {
		return contentAsist;
	}
	public StokModelContentAsistTableModel getTableModel() {
		return tableModel;
	}
	
	public StokModelContentAsistService getService() {
		return service;
	}
	
	public static void addFTextFieldContentListener(FTextField textField) {
		getStokModelContentAsist().getContentAsist().addFTextFieldContentListener(textField);
	}
	public static void removeFTextFieldContentListener(FTextField textField) {
		getStokModelContentAsist().getContentAsist().removeFTextFieldContentListener(textField);
	}

	public static StokModelContentAsist getStokModelContentAsist() {
		if (stokModelContentAsist == null) 
			stokModelContentAsist = new StokModelContentAsist();
		return stokModelContentAsist;
	}

}
