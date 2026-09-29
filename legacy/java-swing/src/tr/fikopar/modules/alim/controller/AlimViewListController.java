package tr.fikopar.modules.alim.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import tr.fikopar.db.models.windows.alim.AlimModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.alim.model.AlimViewModel;
import tr.fikopar.modules.excel.controller.ExcelViewController;
import tr.fikopar.modules.excel.model.ExcelViewModel;
import tr.fikopar.swing.view.FController;

public class AlimViewListController extends FController {

	public List<AlimViewModel> loadAlimViewModels() {
		List<AlimViewModel> alimViewModels = new ArrayList<>();
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		List<AlimModel> alimModels = session.createQuery("from AlimModel a inner join fetch a.sicilModel left join fetch a.excelModel")
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		
		List<ExcelViewModel> excelViewModels = ExcelViewController.loadExcelViewModels();
		
		for(AlimModel alimModel : alimModels) {
			AlimViewModel alimViewModel = new AlimViewModel();
			alimViewModel.setId(alimModel.getId());
			alimViewModel.setHesapno(alimModel.getSicilModel().getHesapno());
			alimViewModel.setHesapadi(alimModel.getSicilModel().getHesapadi());
			alimViewModel.setYetkili(alimModel.getSicilModel().getYetkili());
			alimViewModel.setDusukseAktar(alimModel.getDusukseAktar());
			alimViewModel.setYuksekseAktar(alimModel.getYuksekseAktar());
			alimViewModel.setTarih(alimModel.getTarih());
			alimViewModel.setNote(alimModel.getNote());
			if(alimModel.getExcelModel() != null ){
				for(ExcelViewModel excelViewModel : excelViewModels)
					if(alimModel.getExcelModel().getId() == excelViewModel.getId()) {
						alimViewModel.setExcelViewModel(excelViewModel);
						break;
					}
			} else
				alimViewModel.setExcelViewModel(null);
			alimViewModels.add(alimViewModel);
		}
		
		session.clear();
		session.close();
		
		alimViewModels.sort(new Comparator<AlimViewModel>() {

			@Override
			public int compare(AlimViewModel o1, AlimViewModel o2) {
				return o1.getHesapadi().compareTo(o2.getHesapadi());
			}
		});
		
		return alimViewModels;
		
	}
}
