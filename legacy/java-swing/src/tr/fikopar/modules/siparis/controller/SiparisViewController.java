package tr.fikopar.modules.siparis.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.hibernate.jdbc.Work;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.com.fikopar.script.siparis.SevkAdetScript;
import tr.fikopar.db.models.cobol.plasiyer.PlasiyerModel;
import tr.fikopar.db.models.cobol.sicil.SicilModel;
import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.models.windows.alim.AlimDetayModel;
import tr.fikopar.db.models.windows.sevksekli.SevksekliModel;
import tr.fikopar.db.models.windows.siparis.SiparisDetayModel;
import tr.fikopar.db.models.windows.siparis.SiparisModel;
import tr.fikopar.db.models.windows.stok.KampanyaModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.siparis.model.SiparisDetayViewModel;
import tr.fikopar.modules.siparis.model.SiparisViewModel;
import tr.fikopar.modules.siparis.report.SiparisJRDataSource;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.view.FController;
import tr.fikopar.swing.view.FPropertiesAdapter;

public class SiparisViewController extends FController {

	public void createNew() {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;

		siparisViewModel.setAdres("");
		siparisViewModel.setHesapadi("");
		siparisViewModel.setHesapno("");
		siparisViewModel.setId(null);
		siparisViewModel.setNote("");
		siparisViewModel.setSevksekli(null);
		siparisViewModel.setSiparisialan(null);
		siparisViewModel.setTanim("");
		siparisViewModel.setTarih(new SimpleDateFormat("YYYYMMdd").format(new Date()));
		siparisViewModel.setTcno("");
		siparisViewModel.setTel("");
		siparisViewModel.setVda("");
		siparisViewModel.setVno("");
		siparisViewModel.setYetkili("");
		siparisViewModel.setKampanya(false);
		siparisViewModel.setSepetid(null);
		siparisViewModel.getSiparisDetayViewModels().clear();
		loadCombos();

	}

	public void loadCombos() {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		List<PlasiyerModel> plasiyers = session.createQuery("from PlasiyerModel h where h.plno > 0").list();

		siparisViewModel.getPlasiyers().clear();
		for (Iterator<PlasiyerModel> itr = plasiyers.iterator(); itr.hasNext();)
			siparisViewModel.getPlasiyers().add(itr.next().getPladi());

		session.clear();
		List<SevksekliModel> sevkseklis = session.createQuery("from SevksekliModel h").list();

		siparisViewModel.getSevkseklis().clear();
		for (Iterator<SevksekliModel> itr = sevkseklis.iterator(); itr.hasNext();)
			siparisViewModel.getSevkseklis().add(itr.next().getSevksekli());

		session.clear();
		session.close();
	}

	public void load() throws Exception {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;

		if (siparisViewModel.getId() == null) {
			createNew();
			return;
		} else
			loadCombos();

		Session session = HibernateUtil.getSessionFactory().openSession();

		siparisModel = (SiparisModel) session.get(SiparisModel.class, siparisViewModel.getId());

		if (siparisModel == null) {
			session.clear();
			session.close();
			throw new Exception(siparisViewModel.getId() + " ait sipariþ kaydý bulunamadý!.");
		}

		siparisViewModel.getSiparisDetayViewModels().clear();
		for (SiparisDetayModel siparisDetayModel : siparisModel.getSiparisDetayModels()) {
			SiparisDetayViewModel siparisDetayViewModel = new SiparisDetayViewModel();
			siparisDetayViewModel.setAdet(siparisDetayModel.getAdet());
			siparisDetayViewModel.setFiyati(siparisDetayModel.getFiyat());
			siparisDetayViewModel.setId(siparisDetayModel.getId());
			siparisDetayViewModel.setIsk1(siparisDetayModel.getIskonto1());
			siparisDetayViewModel.setIsk2(siparisDetayModel.getIskonto2());
			siparisDetayViewModel.setKatno(siparisDetayModel.getKatno());
			siparisDetayViewModel.setKdv(siparisDetayModel.getKdv());
			siparisDetayViewModel.setMarka(siparisDetayModel.getMarka());
			siparisDetayViewModel.setOemno(siparisDetayModel.getOemno());
			siparisDetayViewModel.setParcaadi(siparisDetayModel.getCinsi());
			siparisDetayViewModel.setPrcno(siparisDetayModel.getPrcno());
			siparisDetayViewModel.setStno(siparisDetayModel.getStno());
			siparisDetayViewModel.setTipi(siparisDetayModel.getTipi());
			siparisDetayViewModel.setSevkadet(siparisDetayModel.getSevkadet());
			siparisDetayViewModel.setIdwb_siparis(siparisDetayModel.getIdwb_siparis());
			siparisViewModel.getSiparisDetayViewModels().add(siparisDetayViewModel);
		}
		siparisViewModel.setSiparistipi(SiparisListViewModel.Tip.values()[siparisModel.getSiparistipi()]);
		siparisViewModel.setHesapno(siparisModel.getSicilModel().getHesapno());
		siparisViewModel.setTarih(siparisModel.getTarih());
		siparisViewModel.setAdres(String.format("%s\n%s\n%s", siparisModel.getSicilModel().getAdres1(),
				siparisModel.getSicilModel().getAdres2(), siparisModel.getSicilModel().getAdres3()));
		siparisViewModel.setHesapadi(siparisModel.getHesapadi());
		siparisViewModel.setId(siparisModel.getId());
		siparisViewModel.setNote(siparisModel.getSiparisnot());
		siparisViewModel.setSevksekli(siparisModel.getSevksekli());
		siparisViewModel.setSiparisialan(siparisModel.getSiparisialan());
		siparisViewModel.setTanim(String.format("%s/%s/%s/%s", siparisModel.getSicilModel().getMahalle(),
				siparisModel.getSicilModel().getIlce(), siparisModel.getSicilModel().getIl(),
				siparisModel.getSicilModel().getUlke()));
		siparisViewModel.setTcno(siparisModel.getSicilModel().getTcno());
		siparisViewModel.setTel(siparisModel.getSicilModel().getDefaultTelefonString());
		siparisViewModel.setVda(siparisModel.getSicilModel().getVda());
		siparisViewModel.setVno(siparisModel.getSicilModel().getVno());
		siparisViewModel.setYetkili(siparisModel.getSicilModel().getYetkili());
		siparisViewModel.setSepetid(siparisModel.getSepetid());
		
		session.clear();
		session.close();
	}

	public void loadSicilModel() throws Exception {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, siparisViewModel.getHesapno());
		if (sicilModel != null) {
			Hibernate.initialize(sicilModel.getSiciltelModels());
			Hibernate.initialize(sicilModel.getSicilHarekets());
		} else {
			session.clear();
			session.close();
			throw new Exception(siparisViewModel.getHesapno() + " ait sicil kartý bulunamadý!.");
		}
		siparisViewModel.setAdres(
				String.format("%s\n%s\n%s", sicilModel.getAdres1(), sicilModel.getAdres2(), sicilModel.getAdres3()));
		siparisViewModel.setHesapadi(sicilModel.getHesapadi());
		siparisViewModel.setHesapno(sicilModel.getHesapno());
		siparisViewModel.setTanim(String.format("%s/%s/%s/%s", sicilModel.getMahalle(), sicilModel.getIlce(),
				sicilModel.getIl(), sicilModel.getUlke()));
		siparisViewModel.setTcno(sicilModel.getTcno());
		siparisViewModel.setTel(sicilModel.getDefaultTelefonString());
		siparisViewModel.setVda(sicilModel.getVda());
		siparisViewModel.setVno(sicilModel.getVno());
		siparisViewModel.setYetkili(sicilModel.getYetkili());

		session.clear();
		session.close();

	}

	public void delete() throws Exception {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;

		if (siparisViewModel.getId() == null)
			return;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();

		siparisModel = (SiparisModel) session.get(SiparisModel.class, siparisViewModel.getId());
		if (siparisModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(siparisViewModel.getId() + " ait sipariþ kaydý bulunamadý!");
		}
		session.doWork(new Work() {

			@Override
			public void execute(Connection connection) throws SQLException {
				SevkAdetScript sevkAdetScript = new SevkAdetScript(connection, siparisModel.getId(), siparisModel.getSiparistipi());
				sevkAdetScript.delete();
			}
			
		});
		session.delete(siparisModel);
		tx.commit();
		session.clear();
		session.close();

	}

	private SiparisModel siparisModel;
	public void save() throws Exception {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, siparisViewModel.getHesapno());
		if (sicilModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(siparisViewModel.getHesapno() + " ait sicil kartý bulunumadý!");
		}
		// Hibernate.initialize(sicilModel);
		session.evict(sicilModel);

		siparisModel = null;
		if (siparisViewModel.getId() != null) {
			siparisModel = (SiparisModel) session.get(SiparisModel.class, siparisViewModel.getId());
			if (siparisModel == null) {
				tx.rollback();
				session.clear();
				session.close();
				throw new Exception(siparisViewModel.getId() + " ait sipariþ kaydý bulunumadý.");
			} else {
				// Hibernate.initialize(siparisModel);
				Hibernate.initialize(siparisModel.getSiparisDetayModels());
				session.evict(siparisModel);
			}
		} else 
			siparisModel = new SiparisModel();
					
		siparisModel.setHesapadi(siparisViewModel.getHesapadi());
		siparisModel.setSevksekli(siparisViewModel.getSevksekli());
		siparisModel.setSicilModel(sicilModel);
		siparisModel.setSiparisialan(siparisViewModel.getSiparisialan());
		siparisModel.setSiparisnot(siparisViewModel.getNote());
		siparisModel.setSiparistipi(siparisViewModel.getSiparistipi().ordinal());
		siparisModel.setTarih(siparisViewModel.getTarih());
		
		siparisModel.getSiparisDetayModels().clear();
		List<SiparisDetayViewModel> siparisDetayViewModels = siparisViewModel.getSiparisDetayViewModels();

		for (int i = 0; i < siparisDetayViewModels.size(); i++) {
			SiparisDetayViewModel siparisDetayViewModel = siparisDetayViewModels.get(i);
			SiparisDetayModel siparisDetayModel = new SiparisDetayModel();
			siparisDetayModel.setId(siparisDetayViewModel.getId());
			siparisDetayModel.setAdet(siparisDetayViewModel.getAdet());
			siparisDetayModel.setCinsi(siparisDetayViewModel.getParcaadi());
			siparisDetayModel.setFiyat(siparisDetayViewModel.getFiyati());
			siparisDetayModel.setIskonto1(siparisDetayViewModel.getIsk1());
			siparisDetayModel.setIskonto2(siparisDetayViewModel.getIsk2());
			siparisDetayModel.setKatno(siparisDetayViewModel.getKatno());
			siparisDetayModel.setKdv(siparisDetayViewModel.getKdv());
			siparisDetayModel.setMarka(siparisDetayViewModel.getMarka());
			siparisDetayModel.setOemno(siparisDetayViewModel.getOemno());
			siparisDetayModel.setPrcno(siparisDetayViewModel.getPrcno());
			siparisDetayModel.setStno(siparisDetayViewModel.getStno());
			siparisDetayModel.setTipi(siparisDetayViewModel.getTipi());
			siparisDetayModel.setSiparisModel(siparisModel);
			siparisModel.getSiparisDetayModels().add(siparisDetayModel);
		}
		if (siparisModel.getSiparisDetayModels().size() == 0) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception("Sipariþ hiç satýr içermiyor !!!");
		}
		
		siparisModel = (SiparisModel) session.merge(siparisModel);
		session.flush();
		/*
		 * localden giriþmiþ sipariþi web sepetid ve id leri setle
		*/
		if(siparisModel.getSepetid() == null) {
			siparisModel.setSepetid(siparisModel.getId().toString());
		}
		for(SiparisDetayModel smd : siparisModel.getSiparisDetayModels())
			if(smd.getIdwb_siparis() == null)
				smd.setIdwb_siparis(smd.getId());
		
		session.flush();

		session.doWork(new Work(){

			@Override
			public void execute(Connection connection) throws SQLException {
				
				SevkAdetScript sevkAdetScript = new SevkAdetScript(connection, siparisModel.getId(), siparisModel.getSiparistipi());
				sevkAdetScript.updateInsert();
			}
			
		});
		
		
				
		tx.commit();
		session.clear();
		session.close();
		/*
		 * Okundu yap
		 */
		String key = "siparis_" + siparisViewModel.getSiparistipi() + "_" + siparisModel.getId();
		FUserTrashModel trashModel = FPropertiesAdapter.readTrashProperty(key);
		if (trashModel == null)
			FPropertiesAdapter.writeTrashProperty(new FUserTrashModel(key));
		siparisViewModel.setId(siparisModel.getId());
	}
	
	public JasperPrint print() throws Exception {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;

		JasperReport report = null;

		report = (JasperReport) JRLoader
				.loadObject(getClass().getResource("/tr/fikopar/modules/siparis/report/SiparisReport.jasper"));

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("logopath", UIManager.getIcon("logoIcon"));
		JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters,
				new SiparisJRDataSource(siparisViewModel));
		return jasperPrint;

	}

	public void loadStokModel(int index) throws Exception {
		SiparisViewModel siparisViewModel = (SiparisViewModel) model;
		SiparisDetayViewModel siparisDetayViewModel = siparisViewModel.getSiparisDetayViewModels().get(index);

		Session session = HibernateUtil.getSessionFactory().openSession();

		
		StokModel stokModel = (StokModel) session.get(StokModel.class,
				new StokPK(siparisDetayViewModel.getKatno(), siparisDetayViewModel.getStno()));
		if (stokModel == null) {
			session.clear();
			session.close();
			throw new Exception(siparisDetayViewModel.getKatno() + "," + siparisDetayViewModel.getStno()
					+ " ait sicil kartý bulunamadý!.");
		}

		siparisDetayViewModel.setFiyati(stokModel.getFiyat());
		
		if (siparisViewModel.getSiparistipi().equals(SiparisListViewModel.Tip.VERME) && !siparisViewModel.getHesapno().equals("")) {
			for(AlimDetayModel alimDetayModel : stokModel.getAlimDetayModels()) {
				if(alimDetayModel.getAlimModel().getSicilModel().getHesapno().equals(siparisViewModel.getHesapno())) {
					siparisDetayViewModel.setIsk1(alimDetayModel.getIskonto1());
					siparisDetayViewModel.setIsk2(alimDetayModel.getIskonto2());
					siparisDetayViewModel.setKdv(alimDetayModel.getKdv());
					siparisDetayViewModel.setFiyati(alimDetayModel.getMaliyet());
					break;
				}
			}
		}		

		if (siparisViewModel.getSiparistipi().equals(SiparisListViewModel.Tip.ALMA) && siparisViewModel.getKampanya() != null && 
				siparisViewModel.getKampanya()) {
			for (KampanyaModel kampanyaModel : stokModel.getKampanyaModels())
				if (!kampanyaModel.isBitti()) {
					siparisDetayViewModel.setFiyati(kampanyaModel.getKampanya());
					siparisDetayViewModel.setIsk1(BigDecimal.ZERO);
					siparisDetayViewModel.setIsk2(BigDecimal.ZERO);
					siparisDetayViewModel.setKdv(new BigDecimal(18));
					break;
				}
		}
		siparisDetayViewModel.setKatno(stokModel.getStokPK().getKatno());
		siparisDetayViewModel.setMarka(stokModel.getMarka());
		siparisDetayViewModel.setOemno(stokModel.getOemno());
		siparisDetayViewModel.setParcaadi(stokModel.getCinsi());
		siparisDetayViewModel.setPrcno(stokModel.getPrcno());
		siparisDetayViewModel.setStno(stokModel.getStokPK().getStno());
		siparisDetayViewModel.setTipi(stokModel.getTipi());

		session.clear();
		session.close();

	}
	
}
