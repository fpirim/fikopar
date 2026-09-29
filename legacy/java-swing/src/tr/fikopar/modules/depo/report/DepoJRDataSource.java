package tr.fikopar.modules.depo.report;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.modules.depo.model.DepoViewModel;
import tr.fikopar.swing.component.FDateField;

public class DepoJRDataSource implements JRDataSource{

	private DepoViewModel depoViewModel;
	private int counter = 0;
	private boolean firstaccess = false;
	public DepoJRDataSource(DepoViewModel depoViewModel) {
		this.depoViewModel = depoViewModel;
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("title".equals(field.getName()))
			return "DÝZEL TARIM TRAKTÖR DEPO FORMU";
		if("id".equals(field.getName()))
			return depoViewModel.getId();
		else if("hesapno".equals(field.getName()))
			return depoViewModel.getHesapno();
		else if("hesapadi".equals(field.getName()))
			return depoViewModel.getHesapadi();
		else if("yetkili".equals(field.getName()))
			return depoViewModel.getYetkili();
		else if("adres".equals(field.getName()))
			return depoViewModel.getAdres();
		else if("tanim".equals(field.getName()))
			return depoViewModel.getTanim();
		else if("vda".equals(field.getName()))
			return depoViewModel.getVda();
		else if("vno".equals(field.getName()))
			return depoViewModel.getVno();
		else if("tel".equals(field.getName()))
			return depoViewModel.getTel();
		else if("tarih".equals(field.getName()))
			return FDateField.checkDate(depoViewModel.getTarih());
		else if("depoalan".equals(field.getName()))
			return depoViewModel.getDepoalan();
		else if("deponot".equals(field.getName()))
			return depoViewModel.getNote();
		else if("stokno".equals(field.getName()) && depoViewModel.getDepoDetayViewModels()!=null && counter<depoViewModel.getDepoDetayViewModels().size()) 
			return depoViewModel.getDepoDetayViewModels().get(counter).getStno();
		else if("parcano".equals(field.getName()) && depoViewModel.getDepoDetayViewModels()!=null && counter<depoViewModel.getDepoDetayViewModels().size())
			return depoViewModel.getDepoDetayViewModels().get(counter).getPrcno();
		else if("tipi".equals(field.getName()) && depoViewModel.getDepoDetayViewModels()!=null && counter<depoViewModel.getDepoDetayViewModels().size())
			return depoViewModel.getDepoDetayViewModels().get(counter).getTipi();
		else if("stokadi".equals(field.getName()) && depoViewModel.getDepoDetayViewModels()!=null && counter<depoViewModel.getDepoDetayViewModels().size())
			return depoViewModel.getDepoDetayViewModels().get(counter).getParcaadi();
		else if("marka".equals(field.getName()) && depoViewModel.getDepoDetayViewModels()!=null && counter<depoViewModel.getDepoDetayViewModels().size())
			return depoViewModel.getDepoDetayViewModels().get(counter).getMarka();
		else if("adet".equals(field.getName()) && depoViewModel.getDepoDetayViewModels()!=null && counter<depoViewModel.getDepoDetayViewModels().size())
			return depoViewModel.getDepoDetayViewModels().get(counter).getAdet();
			
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if(!firstaccess) {
			firstaccess = true;
			return true;
		}
		counter++;
		if(depoViewModel.getDepoDetayViewModels()==null)
			return false;
		if(depoViewModel.getDepoDetayViewModels().size() <= counter)
			return false;
		return true;
	}

}
