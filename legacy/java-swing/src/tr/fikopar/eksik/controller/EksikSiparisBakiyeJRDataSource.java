package tr.fikopar.eksik.controller;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.eksik.model.EksikStokModel;
import tr.fikopar.eksik.view.EksikListTableModel;

public class EksikSiparisBakiyeJRDataSource implements JRDataSource {

	private Iterator<EksikStokModel> itr;
	private EksikStokModel eksikStokModel;
	private EksikListTableModel eksikListTableModel;

	public EksikSiparisBakiyeJRDataSource(List<EksikStokModel> eksikStokModels,
			EksikListTableModel eksikListTableModel) {
		itr = eksikStokModels.iterator();
		this.eksikListTableModel = eksikListTableModel;
	}

	public EksikStokModel getEksikStokModel() {
		return eksikStokModel;
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if ("katno".equals(field.getName()))
			return eksikStokModel.getKatno();
		else if ("stno".equals(field.getName()))
			return eksikStokModel.getStno();
		else if ("prcno".equals(field.getName()))
			return eksikStokModel.getPrcno();
		else if ("oemno".equals(field.getName()))
			return eksikStokModel.getOemno();
		else if ("tipi".equals(field.getName()))
			return eksikStokModel.getTipi();
		else if ("cinsi".equals(field.getName()))
			return eksikStokModel.getCinsi();
		else if ("marka".equals(field.getName()))
			return eksikStokModel.getMarka();
		else if ("rafyeri".equals(field.getName()))
			return eksikStokModel.getRafyeri();
		else if ("bakiye".equals(field.getName()))
			return eksikStokModel.getBakiye();
		else if ("stok".equals(field.getName()))
			return eksikStokModel.getAdet();
		else if ("eksik".equals(field.getName()))
			return eksikStokModel.getAdet().compareTo(
					eksikStokModel.getBakiye()) > 0 ? BigDecimal.ZERO
					: eksikStokModel.getBakiye().subtract(
							eksikStokModel.getAdet());
		else if ("eksikStokModel".equals(field.getName()))
			return eksikStokModel;

		System.out.println(field.toString());
		return null;
	}

	@Override
	public boolean next() throws JRException {
		do {
			if (itr.hasNext())
				eksikStokModel = itr.next();
			else
				return false;
		} while (eksikStokModel.getEksikSicilModels() == null
				|| eksikStokModel.getEksikSicilModels().size() == 0
				|| eksikStokModel.getAdet().compareTo(
						eksikStokModel.getBakiye()) >= 0
				|| !eksikListTableModel.contains(eksikStokModel));
		return true;

	}

}
