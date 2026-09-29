package tr.fikopar.modules.excel.controller;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import tr.fikopar.db.models.windows.excel.ExcelDetayModel;
import tr.fikopar.db.models.windows.excel.ExcelModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.excel.model.ExcelViewDetayModel;
import tr.fikopar.modules.excel.model.ExcelViewModel;
import tr.fikopar.swing.view.FController;

public class ExcelViewController extends FController {

	public static List<ExcelViewModel> loadExcelViewModels() {
		
		List<ExcelViewModel> excelViewModels = new ArrayList<>();
		
		Session session = HibernateUtil.getSessionFactory().openSession();

		List<ExcelModel> excelModels = session.createQuery("from ExcelModel h inner join fetch h.excelDetayModels k")
							.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		
		for (ExcelModel excelModel : excelModels) {
			ExcelViewModel excelViewModel = new ExcelViewModel();
			excelViewModel.setId(excelModel.getId());
			excelViewModel.setExcelFileName(excelModel.getExcelFileName());
			for(ExcelDetayModel excelDetayModel : excelModel.getExcelDetayModels()) {
				ExcelViewDetayModel excelViewDetayModel = new ExcelViewDetayModel();
				excelViewDetayModel.setId(excelDetayModel.getId());
				excelViewDetayModel.setKatno(excelDetayModel.getKatno());
				excelViewDetayModel.setSheetName(excelDetayModel.getSheetName());
				excelViewDetayModel.setKeyColumn(excelDetayModel.getKeyColumn());
				excelViewDetayModel.setFiyatColumn(excelDetayModel.getFiyatColumn());
				excelViewModel.getExcelViewDetayModels().add(excelViewDetayModel);
			}
			excelViewModels.add(excelViewModel);
		}

		session.clear();
		session.close();
		
		return excelViewModels;
		
	}

}
