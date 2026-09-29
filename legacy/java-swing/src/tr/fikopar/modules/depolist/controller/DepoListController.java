package tr.fikopar.modules.depolist.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.UIManager;

import org.hibernate.Session;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.depolist.model.DepoListViewDetayModel;
import tr.fikopar.modules.depolist.model.DepoListViewModel;
import tr.fikopar.modules.depolist.report.DepoBakiyeJRDataSource;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.view.FController;
import tr.fikopar.swing.view.FPropertiesAdapter;

public class DepoListController extends FController {

	private static final String sql = "select a.id, a.tarih, x.`SC-HESAPNO`, concat(a.hesapadi,' ',x.`SC-YETKILI`) as hesapadi," 
			+ " CONCAT(x.`SC-MAHALLE`,'/',x.`SC-ILCE`,'/',x.`SC-IL`,'/',x.`SC-ULKE`) as adres, a.depoalan, a.deponot"
			+ " from `WINDOWS`.`WIN_DEPO` a left join `COBOL`.`COB_SICIL` x on a.hesapno = x.`SC-HESAPNO`";
	
	private DepoListViewModel.Durum lastDurum;

	@SuppressWarnings("unchecked")
	public boolean load() {
		Session session = HibernateUtil.getSessionFactory().openSession();

		DepoListViewModel depoListViewModel = (DepoListViewModel) model;

//		String sql = depoListViewModel.getDurum() == DepoListViewModel.Durum.HEPSI ? hepsi_sql
//				: (depoListViewModel.getDurum() == DepoListViewModel.Durum.BEKLEYEN ? bekleyen_sql
//						: tamamlanan_sql);

		List<Object> list = session.createSQLQuery(sql).list();
		session.clear();
		session.close();

		boolean flag = false;
		if (lastDurum == null || !lastDurum.equals(depoListViewModel.getDurum())) {
			depoListViewModel.getDepoListViewDetayModels().clear();
			flag = true;
			lastDurum = depoListViewModel.getDurum();
		}
		/*
		 * check update & insert
		 */
		List<DepoListViewDetayModel> currentList = depoListViewModel.getDepoListViewDetayModels();
		for (int i = 0; i < list.size(); i++) {
			Object[] obj = (Object[]) list.get(i);
			DepoListViewDetayModel o1 = new DepoListViewDetayModel();
			o1.setId((Integer) obj[0]);
			o1.setTarih((String) obj[1]);
			o1.setHesapno((String) obj[2]);
			o1.setHesapadi((String) obj[3]);
			o1.setAdres((String) obj[4]);
			o1.setDepoalan((String) obj[5]);
			o1.setNote((String) obj[6]);

			int index = currentList.indexOf(o1);
			if (index == -1) {
				flag = true;
				currentList.add(o1);
			} else if (!o1.isFullyEqual(currentList.get(index))) {
				flag = true;
				currentList.set(index, o1);
			}
		}
		/*
		 * end update & insert
		 */

		/*
		 * check delete
		 */
		Stack<Integer> stack = new Stack<>();
		for (int i = 0; i < currentList.size(); i++) {
			DepoListViewDetayModel o1 = currentList.get(i);
			boolean found = false;
			for (int j = 0; j < list.size(); j++) {
				Object[] obj = (Object[]) list.get(j);
				if (o1.getId().equals(((Integer) obj[0]))) {
					found = true;
					break;
				}
			}
			if (!found)
				stack.push(i);
		}

		while (!stack.isEmpty()) {
			int index = stack.pop();
			DepoListViewDetayModel o1 = currentList.get(index);
			String key = "depo_" + o1.getId();
			FPropertiesAdapter.deleteTrashProperty(key);
			currentList.remove(index);
			flag = true;
		}
		/*
		 * end delete
		 */

		/*
		 * if change resort again
		 */
		if (flag) {
			currentList.sort(new Comparator<DepoListViewDetayModel>() {

				@Override
				public int compare(DepoListViewDetayModel o1, DepoListViewDetayModel o2) {
					int cmpTarih = o1.getTarih().compareTo(o2.getTarih());
					int cmpId = o1.getId().compareTo(o2.getId());
					if (cmpTarih != 0)
						cmpTarih = cmpTarih > 0 ? -1 : 1;
					if (cmpId != 0)
						cmpId = cmpId > 0 ? -1 : 1;
					return cmpTarih == 0 ? cmpId : cmpTarih;
				}
			});
		}
		if (flag) {
			List<FUserTrashModel> trashList = FPropertiesAdapter.readTrashProperties("depo_%");
			depoListViewModel.getTrashMap().clear();
			for (Iterator<FUserTrashModel> itr = trashList.iterator(); itr.hasNext();) {
				FUserTrashModel trashModel = itr.next();
				depoListViewModel.getTrashMap().put(trashModel.getValue(), trashModel);
			}
		}

		return flag;
	}

	public JasperPrint printBakiye(int index) {
		try {
			JasperReport report = (JasperReport) JRLoader
					.loadObject(getClass().getResource("/tr/fikopar/modules/depolist/report/BekleyenReport.jasper"));
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("logopath", UIManager.getIcon("logoIcon"));

			DepoListViewModel depoListViewModel = (DepoListViewModel) model;

			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new DepoBakiyeJRDataSource(depoListViewModel.getDepoListViewDetayModels().get(index)));
			return jasperPrint;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
