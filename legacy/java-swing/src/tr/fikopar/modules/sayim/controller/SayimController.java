package tr.fikopar.modules.sayim.controller;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.models.windows.sayim.SayimDetayModel;
import tr.fikopar.db.models.windows.sayim.SayimModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.sayim.model.SayimDetayViewModel;
import tr.fikopar.modules.sayim.model.SayimViewModel;
import tr.fikopar.swing.view.FController;

public class SayimController extends FController {

	public void load() throws Exception {

		SayimViewModel sayimViewModel = (SayimViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		Integer id = (Integer) session.createSQLQuery("select id from `WINDOWS`.`WIN_SAYIM` where yil = :yil")
				.setParameter("yil", sayimViewModel.getYil()).uniqueResult();
		SayimModel sayimModel = null;

		if (id != null)
			sayimModel = (SayimModel) session.get(SayimModel.class, id);

		if (sayimModel == null) {
			sayimViewModel.getSayimDetayViewModels().clear();
			session.clear();
			session.close();
			return;
		}
		sayimViewModel.getSayimDetayViewModels().clear();
		for (SayimDetayModel sayimDetayModel : sayimModel.getSayimDetays()) {
			SayimDetayViewModel sayimDetayViewModel = new SayimDetayViewModel();
			sayimDetayViewModel.setAdet(sayimDetayModel.getAdet());
			sayimDetayViewModel.setMaliyet(sayimDetayModel.getMaliyet());
			sayimDetayViewModel.setId(sayimDetayModel.getId());
			sayimDetayViewModel.setKatno(sayimDetayModel.getKatno());
			sayimDetayViewModel.setKdv(sayimDetayModel.getKdv());
			sayimDetayViewModel.setMarka(sayimDetayModel.getMarka());
			sayimDetayViewModel.setOemno(sayimDetayModel.getOemno());
			sayimDetayViewModel.setCinsi(sayimDetayModel.getCinsi());
			sayimDetayViewModel.setPrcno(sayimDetayModel.getPrcno());
			sayimDetayViewModel.setStno(sayimDetayModel.getStno());
			sayimDetayViewModel.setTipi(sayimDetayModel.getTipi());
			sayimDetayViewModel.setRafno(sayimDetayModel.getRafno());
			sayimDetayViewModel.setSayimzamani(sayimDetayModel.getSayimzamani());
			sayimViewModel.getSayimDetayViewModels().add(sayimDetayViewModel);
		}
		sayimViewModel.setYil(sayimModel.getYil());
		session.clear();
		session.close();
	}

	public void delete() throws Exception {
		SayimViewModel sayimViewModel = (SayimViewModel) model;

		if (sayimViewModel.getYil() == null)
			return;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();

		Integer id = (Integer) session.createSQLQuery("select id from `WINDOWS`.`WIN_SAYIM` where yil = :yil")
				.setParameter("yil", sayimViewModel.getYil()).uniqueResult();

		SayimModel sayimModel = null;

		if (id != null)
			sayimModel = (SayimModel) session.get(SayimModel.class, id);

		if (sayimModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(sayimViewModel.getYil() + " ait sayým kaydý bulunamadý!");
		}
		session.delete(sayimModel);
		tx.commit();
		session.clear();
		session.close();

	}

	public void save() throws Exception {
		SayimViewModel sayimViewModel = (SayimViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		if (sayimViewModel.getYil() == null || sayimViewModel.getYil().equals(""))
			throw new Exception("Yýl boþ olamaz");

		SayimModel sayimModel = null;

		Integer id = (Integer) session.createSQLQuery("select id from `WINDOWS`.`WIN_SAYIM` where yil = :yil")
				.setParameter("yil", sayimViewModel.getYil()).uniqueResult();

		if (id != null) {

			sayimModel = (SayimModel) session.get(SayimModel.class, id);

			if (sayimModel == null) {
				tx.rollback();
				session.clear();
				session.clear();
				throw new Exception(sayimViewModel.getYil() + " ait sayým kaydý bulunumadý.");
			} else {
				// Hibernate.initialize(siparisModel);
				Hibernate.initialize(sayimModel.getSayimDetays());
				session.evict(sayimModel);
			}
		} else
			sayimModel = new SayimModel();

		sayimModel.setYil(sayimViewModel.getYil());

		sayimModel.getSayimDetays().clear();
		List<SayimDetayViewModel> sayimDetayViewModels = sayimViewModel.getSayimDetayViewModels();

		for (int i = 0; i < sayimDetayViewModels.size(); i++) {
			SayimDetayViewModel sayimDetayViewModel = sayimDetayViewModels.get(i);
			SayimDetayModel sayimDetayModel = new SayimDetayModel();
			sayimDetayModel.setAdet(sayimDetayViewModel.getAdet());
			sayimDetayModel.setMaliyet(sayimDetayViewModel.getMaliyet());
			sayimDetayModel.setId(sayimDetayViewModel.getId());
			sayimDetayModel.setKatno(sayimDetayViewModel.getKatno());
			sayimDetayModel.setKdv(sayimDetayViewModel.getKdv());
			sayimDetayModel.setMarka(sayimDetayViewModel.getMarka());
			sayimDetayModel.setOemno(sayimDetayViewModel.getOemno());
			sayimDetayModel.setCinsi(sayimDetayViewModel.getCinsi());
			sayimDetayModel.setPrcno(sayimDetayViewModel.getPrcno());
			sayimDetayModel.setStno(sayimDetayViewModel.getStno());
			sayimDetayModel.setTipi(sayimDetayViewModel.getTipi());
			sayimDetayModel.setRafno(sayimDetayViewModel.getRafno());
			sayimDetayModel.setSayimzamani(sayimDetayViewModel.getSayimzamani());
			sayimDetayModel.setSayimModel(sayimModel);
			sayimModel.getSayimDetays().add(sayimDetayModel);
		}

		SayimModel rtnSayimModel = (SayimModel) session.merge(sayimModel);

		tx.commit();
		session.clear();
		session.close();

	}

	public void loadStokModel(int index) throws Exception {
		SayimViewModel sayimViewModel = (SayimViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		SayimDetayViewModel sayimDetayViewModel = sayimViewModel.getSayimDetayViewModels().get(index);
		StokModel stokModel = (StokModel) session.get(StokModel.class,
				new StokPK(sayimDetayViewModel.getKatno(), sayimDetayViewModel.getStno()));
		if (stokModel == null) {
			session.clear();
			session.close();
			throw new Exception(sayimDetayViewModel.getKatno() + "," + sayimDetayViewModel.getStno()
					+ " ait stok kartý bulunamadý!.");
		}

		sayimDetayViewModel.setKatno(stokModel.getStokPK().getKatno());
		sayimDetayViewModel.setMarka(stokModel.getMarka());
		sayimDetayViewModel.setOemno(stokModel.getOemno());
		sayimDetayViewModel.setCinsi(stokModel.getCinsi());
		sayimDetayViewModel.setPrcno(stokModel.getPrcno());
		sayimDetayViewModel.setStno(stokModel.getStokPK().getStno());
		sayimDetayViewModel.setTipi(stokModel.getTipi());

		if (sayimDetayViewModel.getRafno().equals("") && index != 0) {
			sayimDetayViewModel.setRafno(sayimViewModel.getSayimDetayViewModels().get(index - 1).getRafno());
		}
		if (sayimDetayViewModel.getSayimzamani().equals("") && index != 0) {
			sayimDetayViewModel
					.setSayimzamani(sayimViewModel.getSayimDetayViewModels().get(index - 1).getSayimzamani());
		}

		session.clear();
		session.close();

	}

}