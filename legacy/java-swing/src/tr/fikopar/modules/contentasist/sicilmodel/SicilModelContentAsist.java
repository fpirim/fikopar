package tr.fikopar.modules.contentasist.sicilmodel;

import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FTableContentAsist;

public class SicilModelContentAsist {

	private static SicilModelContentAsist sicilModelContentAsist;

	private FTableContentAsist contentAsist;
	private SicilModelContentAsistService service;
	private SicilModelContentAsistTableModel tableModel;
	public SicilModelContentAsist() {

		service = new SicilModelContentAsistService();
		tableModel = new SicilModelContentAsistTableModel();
		contentAsist = new FTableContentAsist(tableModel, service);
	}
	public FTableContentAsist getContentAsist() {
		return contentAsist;
	}
	
	public SicilModelContentAsistService getService() {
		return service;
	}
	public static void refresh() {
		getSicilModelContentAsist().getContentAsist().refresh(null);
	}
	public static void addFTextFieldContentListener(FTextField textField) {
		getSicilModelContentAsist().getContentAsist().addFTextFieldContentListener(textField);
	}
	public static void removeFTextFieldContentListener(FTextField textField) {
		getSicilModelContentAsist().getContentAsist().removeFTextFieldContentListener(textField);
	}

	public static SicilModelContentAsist getSicilModelContentAsist() {
		if (sicilModelContentAsist == null) 
			sicilModelContentAsist = new SicilModelContentAsist();
		return sicilModelContentAsist;
	}

}
