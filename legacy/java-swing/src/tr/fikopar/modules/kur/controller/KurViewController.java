package tr.fikopar.modules.kur.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Session;

import tr.fikopar.db.models.windows.kur.KurModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.kur.model.KurViewModel;
import tr.fikopar.swing.view.FController;

public class KurViewController extends FController {

	public static List<KurViewModel> loadKurViewModels() {
		
		List<KurViewModel> kurViewModels = new ArrayList<>();
		
		Session session = HibernateUtil.getSessionFactory().openSession();

		List<KurModel> kurModels = session.createQuery("from KurModel h").list();
		
		for (KurModel kurModel : kurModels) {
			KurViewModel kurViewModel = new KurViewModel();
			kurViewModel.setKur(kurModel.getKur());
			kurViewModel.setFiyat(kurModel.getFiyat());
			kurViewModels.add(kurViewModel);
		}

		session.clear();
		session.close();
		
		kurViewModels.sort(new Comparator<KurViewModel>() {

			@Override
			public int compare(KurViewModel o1, KurViewModel o2) {
				return o1.getKur().compareTo(o2.getKur());
			}
		});
		return kurViewModels;
		
	}

}
