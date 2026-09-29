package tr.fikopar.eksik.controller;

import java.util.Iterator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.eksik.model.EksikSicilModel;
import tr.fikopar.eksik.model.EksikStokModel;

public class EksikSicilJRDataSource implements JRDataSource {

	
	private Iterator<EksikSicilModel> itr;
	private EksikSicilModel eksikSicilModel;
	
	public EksikSicilJRDataSource(EksikStokModel eksikStokModel) {
		itr = eksikStokModel.getEksikSicilModels().iterator();
	}
	
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("hesapno".equals(field.getName()))
			return eksikSicilModel.getHesapno();
		else if("hesapadi".equals(field.getName()))
			return eksikSicilModel.getHesapadi();
		else if("yetkili".equals(field.getName()))
			return eksikSicilModel.getYetkili();
		else if("adres".equals(field.getName()))
			return eksikSicilModel.getAdres();
		else if("bakiye".equals(field.getName()))
			return eksikSicilModel.getBakiye();
		else if("iskonto1".equals(field.getName()))
			return eksikSicilModel.getIskonto1();
		else if("iskonto2".equals(field.getName()))
			return eksikSicilModel.getIskonto2();
		else if("kdv".equals(field.getName()))
			return eksikSicilModel.getKdv();
		else if("fiyat".equals(field.getName()))
			return eksikSicilModel.getFiyat();
		else if("tutar".equals(field.getName()))
			return eksikSicilModel.getFiyat().multiply(eksikSicilModel.getBakiye());
		else if("id".equals(field.getName()))
			return eksikSicilModel.getId(); 
		else if("tarih".equals(field.getName()))
			return eksikSicilModel.getTarih(); 
		else if("siparisnot".equals(field.getName()))
			return eksikSicilModel.getSiparisnot(); 
		else if("sevksekli".equals(field.getName()))
			return eksikSicilModel.getSevsekli();
		else if("siparisialan".equals(field.getName()))
			return eksikSicilModel.getSiparisialan();
		
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if(itr.hasNext()) {
			eksikSicilModel = itr.next();
			return true;
		}
		return false;
	}

}
