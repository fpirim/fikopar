package tr.fikopar.liste.controller;

import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.liste.model.ListeBolumModel;
import tr.fikopar.liste.model.ListeModel;

public class ListeJRDataSource implements JRDataSource{

	private Iterator<Object> itr;
	private Object obj;
	private boolean kampanya;
	public ListeJRDataSource(List<Object> list, boolean kampanya) {
		itr = list.iterator();
		this.kampanya = kampanya;
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("baslik".equals(field.getName()))
			return obj instanceof ListeBolumModel;
		if("baslik".equals(field.getName()) && obj == null)
			return false;
		if("stno".equals(field.getName()) && obj instanceof ListeModel)
			return ((ListeModel) obj).getStno();
		if("prcno".equals(field.getName()) && obj instanceof ListeModel)
			return ((ListeModel) obj).getPrcno();
		if("tipi".equals(field.getName()) && obj instanceof ListeModel)
			return ((ListeModel) obj).getTipi();
		if("cinsi".equals(field.getName()) && obj instanceof ListeModel)
			return ((ListeModel) obj).getCinsi();
		if("note".equals(field.getName()) && obj instanceof ListeModel && !kampanya)
			return ((ListeModel) obj).getNote();
		if("marka".equals(field.getName()) && obj instanceof ListeModel)
			return ((ListeModel) obj).getMarka();
		if("fiyat".equals(field.getName()) && obj instanceof ListeModel && !kampanya)
			return ((ListeModel) obj).getFiyat();
		if("fiyat".equals(field.getName()) && obj instanceof ListeModel && kampanya)
			return ((ListeModel) obj).getKampanya();
		if("cinsi".equals(field.getName()) && obj instanceof ListeBolumModel)
			return ((ListeBolumModel) obj).getBolum();
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if(itr.hasNext()) {
			obj = itr.next();
			return true;
		}
		return false;
	}

}
