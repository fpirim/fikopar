package tr.fikopar.modules.depo.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.fikopar.db.models.cobol.plasiyer.PlasiyerModel;
import tr.fikopar.db.models.cobol.sicil.SicilModel;
import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.models.windows.depo.DepoDetayModel;
import tr.fikopar.db.models.windows.depo.DepoModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.depo.model.DepoDetayViewModel;
import tr.fikopar.modules.depo.model.DepoViewModel;
import tr.fikopar.modules.depo.report.DepoJRDataSource;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.view.FController;
import tr.fikopar.swing.view.FPropertiesAdapter;

public class DepoViewController extends FController {

	public void createNew() {
		DepoViewModel depoViewModel = (DepoViewModel) model;

		depoViewModel.setAdres("");
		depoViewModel.setHesapadi("");
		depoViewModel.setHesapno("");
		depoViewModel.setId(null);
		depoViewModel.setNote("");
		depoViewModel.setDepoalan(null);
		depoViewModel.setTanim("");
		depoViewModel.setTarih(new SimpleDateFormat("YYYYMMdd").format(new Date()));
		depoViewModel.setTcno("");
		depoViewModel.setTel("");
		depoViewModel.setVda("");
		depoViewModel.setVno("");
		depoViewModel.setYetkili("");
		depoViewModel.getDepoDetayViewModels().clear();
		loadCombos();

	}

	public void loadCombos() {
		DepoViewModel depoViewModel = (DepoViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		List<PlasiyerModel> plasiyers = session.createQuery("from PlasiyerModel h where h.plno > 0").list();

		depoViewModel.getPlasiyers().clear();
		for (Iterator<PlasiyerModel> itr = plasiyers.iterator(); itr.hasNext();)
			depoViewModel.getPlasiyers().add(itr.next().getPladi());

		session.clear();
		session.close();
	}

	public void load() throws Exception {
		DepoViewModel depoViewModel = (DepoViewModel) model;

		if (depoViewModel.getId() == null) {
			createNew();
			return;
		} else
			loadCombos();

		Session session = HibernateUtil.getSessionFactory().openSession();

		depoModel = (DepoModel) session.get(DepoModel.class, depoViewModel.getId());

		if (depoModel == null) {
			session.clear();
			session.close();
			throw new Exception(depoViewModel.getId() + " ait depo kaydý bulunamadý!.");
		}

		depoViewModel.getDepoDetayViewModels().clear();
		for (DepoDetayModel depoDetayModel : depoModel.getDepoDetayModels()) {
			DepoDetayViewModel depoDetayViewModel = new DepoDetayViewModel();
			depoDetayViewModel.setAdet(depoDetayModel.getAdet());
			depoDetayViewModel.setId(depoDetayModel.getId());
			depoDetayViewModel.setKatno(depoDetayModel.getKatno());
			depoDetayViewModel.setMarka(depoDetayModel.getMarka());
			depoDetayViewModel.setOemno(depoDetayModel.getOemno());
			depoDetayViewModel.setParcaadi(depoDetayModel.getCinsi());
			depoDetayViewModel.setPrcno(depoDetayModel.getPrcno());
			depoDetayViewModel.setStno(depoDetayModel.getStno());
			depoViewModel.getDepoDetayViewModels().add(depoDetayViewModel);
		}
		depoViewModel.setHesapno(depoModel.getSicilModel().getHesapno());
		depoViewModel.setTarih(depoModel.getTarih());
		depoViewModel.setAdres(String.format("%s\n%s\n%s", depoModel.getSicilModel().getAdres1(),
				depoModel.getSicilModel().getAdres2(), depoModel.getSicilModel().getAdres3()));
		depoViewModel.setHesapadi(depoModel.getHesapadi());
		depoViewModel.setId(depoModel.getId());
		depoViewModel.setNote(depoModel.getDeponot());
		depoViewModel.setDepoalan(depoModel.getDepoalan());
		depoViewModel.setTanim(String.format("%s/%s/%s/%s", depoModel.getSicilModel().getMahalle(),
				depoModel.getSicilModel().getIlce(), depoModel.getSicilModel().getIl(),
				depoModel.getSicilModel().getUlke()));
		depoViewModel.setTcno(depoModel.getSicilModel().getTcno());
		depoViewModel.setTel(depoModel.getSicilModel().getDefaultTelefonString());
		depoViewModel.setVda(depoModel.getSicilModel().getVda());
		depoViewModel.setVno(depoModel.getSicilModel().getVno());
		depoViewModel.setYetkili(depoModel.getSicilModel().getYetkili());
		
		session.clear();
		session.close();
	}

	public void loadSicilModel() throws Exception {
		DepoViewModel depoViewModel = (DepoViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, depoViewModel.getHesapno());
		if (sicilModel != null) {
			Hibernate.initialize(sicilModel.getSiciltelModels());
			Hibernate.initialize(sicilModel.getSicilHarekets());
		} else {
			session.clear();
			session.close();
			throw new Exception(depoViewModel.getHesapno() + " ait sicil kartý bulunamadý!.");
		}
		depoViewModel.setAdres(
				String.format("%s\n%s\n%s", sicilModel.getAdres1(), sicilModel.getAdres2(), sicilModel.getAdres3()));
		depoViewModel.setHesapadi(sicilModel.getHesapadi());
		depoViewModel.setHesapno(sicilModel.getHesapno());
		depoViewModel.setTanim(String.format("%s/%s/%s/%s", sicilModel.getMahalle(), sicilModel.getIlce(),
				sicilModel.getIl(), sicilModel.getUlke()));
		depoViewModel.setTcno(sicilModel.getTcno());
		depoViewModel.setTel(sicilModel.getDefaultTelefonString());
		depoViewModel.setVda(sicilModel.getVda());
		depoViewModel.setVno(sicilModel.getVno());
		depoViewModel.setYetkili(sicilModel.getYetkili());

		session.clear();
		session.close();

	}

	public void delete() throws Exception {
		DepoViewModel depoViewModel = (DepoViewModel) model;

		if (depoViewModel.getId() == null)
			return;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();

		depoModel = (DepoModel) session.get(DepoModel.class, depoViewModel.getId());
		if (depoModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(depoViewModel.getId() + " ait sipariþ kaydý bulunamadý!");
		}
		session.delete(depoModel);
		tx.commit();
		session.clear();
		session.close();

	}

	private DepoModel depoModel;
	public void save() throws Exception {
		DepoViewModel depoViewModel = (DepoViewModel) model;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, depoViewModel.getHesapno());
		if (sicilModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(depoViewModel.getHesapno() + " ait sicil kartý bulunumadý!");
		}
		// Hibernate.initialize(sicilModel);
		session.evict(sicilModel);

		depoModel = null;
		if (depoViewModel.getId() != null) {
			depoModel = (DepoModel) session.get(DepoModel.class, depoViewModel.getId());
			if (depoModel == null) {
				tx.rollback();
				session.clear();
				session.close();
				throw new Exception(depoViewModel.getId() + " ait depo kaydý bulunumadý.");
			} else {
				// Hibernate.initialize(depoModel);
				Hibernate.initialize(depoModel.getDepoDetayModels());
				session.evict(depoModel);
			}
		} else 
			depoModel = new DepoModel();
					
		depoModel.setHesapadi(depoViewModel.getHesapadi());
		
		depoModel.setSicilModel(sicilModel);
		depoModel.setDepoalan(depoViewModel.getDepoalan());
		depoModel.setDeponot(depoViewModel.getNote());
		depoModel.setTarih(depoViewModel.getTarih());
		
		depoModel.getDepoDetayModels().clear();
		List<DepoDetayViewModel> depoDetayViewModels = depoViewModel.getDepoDetayViewModels();

		for (int i = 0; i < depoDetayViewModels.size(); i++) {
			DepoDetayViewModel depoDetayViewModel = depoDetayViewModels.get(i);
			DepoDetayModel depoDetayModel = new DepoDetayModel();
			depoDetayModel.setId(depoDetayViewModel.getId());
			depoDetayModel.setAdet(depoDetayViewModel.getAdet());
			depoDetayModel.setCinsi(depoDetayViewModel.getParcaadi());
			depoDetayModel.setKatno(depoDetayViewModel.getKatno());
			depoDetayModel.setMarka(depoDetayViewModel.getMarka());
			depoDetayModel.setOemno(depoDetayViewModel.getOemno());
			depoDetayModel.setPrcno(depoDetayViewModel.getPrcno());
			depoDetayModel.setStno(depoDetayViewModel.getStno());
			depoDetayModel.setTipi(depoDetayViewModel.getTipi());
			depoDetayModel.setDepoModel(depoModel);
			depoModel.getDepoDetayModels().add(depoDetayModel);
		}
		
		if (depoModel.getDepoDetayModels().size() == 0) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception("Depo hiç satýr içermiyor !!!");
		}
		
		depoModel = (DepoModel) session.merge(depoModel);
		session.flush();

		tx.commit();
		session.clear();
		session.close();
		/*
		 * Okundu yap
		 */
		String key = "depo_" + depoModel.getId();
		FUserTrashModel trashModel = FPropertiesAdapter.readTrashProperty(key);
		if (trashModel == null)
			FPropertiesAdapter.writeTrashProperty(new FUserTrashModel(key));
		depoViewModel.setId(depoModel.getId());
	}
	
	public JasperPrint print() throws Exception {
		DepoViewModel depoViewModel = (DepoViewModel) model;

		JasperReport report = null;

		report = (JasperReport) JRLoader
				.loadObject(getClass().getResource("/tr/fikopar/modules/depo/report/depoReport.jasper"));

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("logopath", UIManager.getIcon("logoIcon"));
		JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters,
				new DepoJRDataSource(depoViewModel));
		return jasperPrint;

	}

	public void loadStokModel(int index) throws Exception {
		DepoViewModel depoViewModel = (DepoViewModel) model;
		DepoDetayViewModel depoDetayViewModel = depoViewModel.getDepoDetayViewModels().get(index);

		Session session = HibernateUtil.getSessionFactory().openSession();

		
		StokModel stokModel = (StokModel) session.get(StokModel.class,
				new StokPK(depoDetayViewModel.getKatno(), depoDetayViewModel.getStno()));
		if (stokModel == null) {
			session.clear();
			session.close();
			throw new Exception(depoDetayViewModel.getKatno() + "," + depoDetayViewModel.getStno()
					+ " ait sicil kartý bulunamadý!.");
		}
		
		depoDetayViewModel.setKatno(stokModel.getStokPK().getKatno());
		depoDetayViewModel.setMarka(stokModel.getMarka());
		depoDetayViewModel.setOemno(stokModel.getOemno());
		depoDetayViewModel.setParcaadi(stokModel.getCinsi());
		depoDetayViewModel.setPrcno(stokModel.getPrcno());
		depoDetayViewModel.setStno(stokModel.getStokPK().getStno());
		depoDetayViewModel.setTipi(stokModel.getTipi());

		session.clear();
		session.close();

	}
	
}
