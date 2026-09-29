package tr.fikopar.modules.siparis.report;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.modules.siparis.model.SiparisViewModel;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.swing.component.FDateField;

public class SiparisJRDataSource implements JRDataSource{

	private SiparisViewModel siparisViewModel;
	private int counter = 0;
	private boolean firstaccess = false;
	public SiparisJRDataSource(SiparisViewModel siparisViewModel) {
		this.siparisViewModel = siparisViewModel;
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("title".equals(field.getName())) {
			if(siparisViewModel.getSiparistipi().equals(SiparisListViewModel.Tip.ALMA))
				return "DÝZEL TARIM TRAKTÖR SÝPARÝÞ ALMA FORMU";
			else
				return "DÝZEL TARIM TRAKTÖR SÝPARÝÞ VERME FORMU";
		}
		if("id".equals(field.getName()))
			return siparisViewModel.getId();
		else if("hesapno".equals(field.getName()))
			return siparisViewModel.getHesapno();
		else if("hesapadi".equals(field.getName()))
			return siparisViewModel.getHesapadi();
		else if("yetkili".equals(field.getName()))
			return siparisViewModel.getYetkili();
		else if("adres".equals(field.getName()))
			return siparisViewModel.getAdres();
		else if("tanim".equals(field.getName()))
			return siparisViewModel.getTanim();
		else if("vda".equals(field.getName()))
			return siparisViewModel.getVda();
		else if("vno".equals(field.getName()))
			return siparisViewModel.getVno();
		else if("tel".equals(field.getName()))
			return siparisViewModel.getTel();
		else if("tarih".equals(field.getName()))
			return FDateField.checkDate(siparisViewModel.getTarih());
		else if("sevksekli".equals(field.getName()))
			return siparisViewModel.getSevksekli();
		else if("siparisialan".equals(field.getName()))
			return siparisViewModel.getSiparisialan();
		else if("siparisnot".equals(field.getName()))
			return siparisViewModel.getNote();
		else if("stokno".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size()) {
			if(siparisViewModel.getSiparistipi().equals(SiparisListViewModel.Tip.ALMA))
				return siparisViewModel.getSiparisDetayViewModels().get(counter).getStno();
			else
				return siparisViewModel.getSiparisDetayViewModels().get(counter).getOemno();
		}
		else if("parcano".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getPrcno();
		else if("tipi".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getTipi();
		else if("stokadi".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getParcaadi();
		else if("marka".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getMarka();
		else if("isk1".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getIsk1();
		else if("isk2".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getIsk2();
		else if("kdv".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getKdv();
		else if("adet".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getAdet();
		else if("fiyat".equals(field.getName()) && siparisViewModel.getSiparisDetayViewModels()!=null && counter<siparisViewModel.getSiparisDetayViewModels().size())
			return siparisViewModel.getSiparisDetayViewModels().get(counter).getFiyati();
			
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if(!firstaccess) {
			firstaccess = true;
			return true;
		}
		counter++;
		if(siparisViewModel.getSiparisDetayViewModels()==null)
			return false;
		if(siparisViewModel.getSiparisDetayViewModels().size() <= counter)
			return false;
		return true;
	}

}
