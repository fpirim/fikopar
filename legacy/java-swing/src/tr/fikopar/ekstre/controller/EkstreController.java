package tr.fikopar.ekstre.controller;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.db.models.sicil.SicilHareket;
import tr.fikopar.db.models.sicil.SicilModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.ekstre.model.EkstreViewModel;

public class EkstreController extends AbstractController{

	
	public SicilModel loadSicilModel(String hesapno) throws Exception {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, hesapno.trim());
		if(sicilModel!=null) {
			Hibernate.initialize(sicilModel.getSiciltelModels());
			Hibernate.initialize(sicilModel.getSicilHarekets());
		} else {
			session.clear();
			session.close();
			throw new Exception(hesapno+" ait sicil kartý bulunamadý!.");	
		}
		
		session.clear();
		session.close();
		return sicilModel;
	}
	
	
	public void refreshEkstreViewModel(List<Object> list, SicilModel sicilModel, String baslama, String bitis, String depo) {
		
		BigDecimal [] devirBorc = new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO };
		BigDecimal [] devirAlacak = new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO};
		
		
		for(SicilHareket sicilHareket : sicilModel.getSicilHarekets()) {
			if(sicilHareket.getSira() == 0)
				continue;
			if(depo.equals("1.Depo") && sicilHareket.getDepono() != 1)
				continue;
			if(depo.equals("2.Depo") && sicilHareket.getDepono() != 2)
				continue;
			
			if(sicilHareket.getTarih().compareTo(baslama) >= 0 && sicilHareket.getTarih().compareTo(bitis) <= 0) {
				EkstreViewModel ekstreViewModel = new EkstreViewModel();
				ekstreViewModel.setAciklama(sicilHareket.getAciklama());
				ekstreViewModel.setAlacak(sicilHareket.getAlacak());
				ekstreViewModel.setBorc(sicilHareket.getBorc());
				ekstreViewModel.setDepono(sicilHareket.getDepono());
				ekstreViewModel.setEvrakno(sicilHareket.getEvrakno());
				ekstreViewModel.setTarih(sicilHareket.getTarih());
				ekstreViewModel.setTip(sicilHareket.getTip());
				ekstreViewModel.setVade(sicilHareket.getVade());
				list.add(ekstreViewModel);
			}
			if(sicilHareket.getTarih().compareTo(baslama) < 0) {
				
				devirBorc[sicilHareket.getDepono()-1] = devirBorc[sicilHareket.getDepono()-1].add(sicilHareket.getBorc());
				devirAlacak[sicilHareket.getDepono()-1] = devirAlacak[sicilHareket.getDepono()-1].add(sicilHareket.getAlacak());
			}
			
		}
		Collections.sort(list, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				EkstreViewModel e1 = (EkstreViewModel) o1;
				EkstreViewModel e2 = (EkstreViewModel) o2;
				return e1.getTarih().compareTo(e2.getTarih());
			}
		});
		for(int i=devirBorc.length-1; i>=0; i--) {
			if(devirBorc[i].compareTo(BigDecimal.ZERO) == 0 && devirAlacak[i].compareTo(BigDecimal.ZERO) == 0)
				continue;
			EkstreViewModel ekstreViewModel = new EkstreViewModel();
			ekstreViewModel.setAciklama("Önceki dönemden devir");
			ekstreViewModel.setAlacak(devirAlacak[i]);
			ekstreViewModel.setBorc(devirBorc[i]);
			ekstreViewModel.setDepono(i+1);
			ekstreViewModel.setEvrakno("");
			ekstreViewModel.setTarih(baslama);
			ekstreViewModel.setTip(0);
			ekstreViewModel.setVade("DEVÝR");
			list.add(0, ekstreViewModel);
		}
		
		BigDecimal bakiye = BigDecimal.ZERO;
		for(int i=0; i<list.size(); i++) {
			EkstreViewModel ekstreViewModel = (EkstreViewModel) list.get(i);
			bakiye = bakiye.add(ekstreViewModel.getBorc()).subtract(ekstreViewModel.getAlacak());
			ekstreViewModel.setBakiye(bakiye);
		}
		
		
	}
	
	
	public JasperPrint print(SicilModel sicilModel, String baslama, String bitis, String depo, List<Object> list) {
		try {
			
	 		JasperReport report = null;
			
			report = (JasperReport) JRLoader.loadObject(
					getClass().getResource("/tr/fikopar/ekstre/report/ekstrereport.jasper"));
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("logopath", UIManager.getIcon("logoIcon"));
			parameters.put("totalRecordsOnReport", list.size());
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new EkstreJRDataSource(sicilModel,baslama,bitis,depo,list));
			//JasperReport report = JasperCompileManager.compileReport("report/StokDetayReport.jrxml");
			return jasperPrint;
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		return null;
		

	}
	
}
