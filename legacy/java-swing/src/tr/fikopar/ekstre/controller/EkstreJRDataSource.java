package tr.fikopar.ekstre.controller;

import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.db.models.sicil.SicilModel;
import tr.fikopar.ekstre.model.EkstreViewModel;
import tr.fikopar.swing.component.FDatefield;

public class EkstreJRDataSource implements JRDataSource{

	private Iterator<Object> itr;
	private EkstreViewModel ekstreModel;
	private SicilModel sicilModel;
	private String baslama;
	private String bitis;
	private String depo;
	
	public EkstreJRDataSource(SicilModel sicilModel, String baslama, String bitis, String depo,
					List<Object> list) {
		this.sicilModel = sicilModel;
		this.baslama = baslama;
		this.bitis = bitis;
		this.depo = depo;
		itr = list.iterator();
		
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("hesapno".equals(field.getName()))
			return sicilModel.getHesapno();
		else if("hesapadi".equals(field.getName()))
			return sicilModel.getHesapadi();
		else if("yetkili".equals(field.getName()))
			return sicilModel.getYetkili();
		else if("adres".equals(field.getName()))
			return sicilModel.getMahalle()+" "+sicilModel.getIlce()+" "+sicilModel.getIl()+" "+sicilModel.getUlke();
		else if("telefon".equals(field.getName()))
			return "";
		else if("baslama".equals(field.getName()))
			return FDatefield.checkDate(baslama);
		else if("bitis".equals(field.getName()))
			return FDatefield.checkDate(bitis);
		else if("depofilter".equals(field.getName()))
			return depo;
		else if("tarih".equals(field.getName()))
			return FDatefield.checkDate(ekstreModel.getTarih());
		else if("tip".equals(field.getName()))
			return ekstreModel.getTipString();
		else if("evrakno".equals(field.getName()))
			return ekstreModel.getEvrakno();
		else if("vade".equals(field.getName()))
			return FDatefield.checkDate(ekstreModel.getVade());
		else if("depo".equals(field.getName()))
			return ekstreModel.getDepoString();
		else if("aciklama".equals(field.getName()))
			return ekstreModel.getAciklama();
		else if("borc".equals(field.getName()))
			return ekstreModel.getBorc();
		else if("alacak".equals(field.getName()))
			return ekstreModel.getAlacak();
			
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if(itr.hasNext()) {
			ekstreModel = (EkstreViewModel) itr.next();
			return true;
		}
		return false;
	}

}
