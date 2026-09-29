package tr.fikopar.modules.siparislist.controller;

import java.math.BigDecimal;
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
import tr.fikopar.modules.siparislist.model.SiparisListViewDetayModel;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.modules.siparislist.report.SiparisBakiyeJRDataSource;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.view.FController;
import tr.fikopar.swing.view.FPropertiesAdapter;

public class SiparisListController extends FController {

	private static final String hepsi_sql = "select a.id, a.tarih, x.`SC-HESAPNO`, concat(a.hesapadi,' ',x.`SC-YETKILI`) as hesapadi," 
			+ " CONCAT(x.`SC-MAHALLE`,'/',x.`SC-ILCE`,'/',x.`SC-IL`,'/',x.`SC-ULKE`) as adres, a.sevksekli, a.siparisialan,"
			+ " sum(b.adet*(b.fiyat*(1-b.iskonto1/100)*(1-b.iskonto2/100)*(1+b.kdv/100))) as bakiye, a.siparisnot"
			+ " from `WINDOWS`.`WIN_SIPARIS` a left join `COBOL`.`COB_SICIL` x on a.hesapno = x.`SC-HESAPNO`"
            + " left join `WINDOWS`.`WIN_SIPARISDETAY` b on a.id = b.idsiparis where a.siparistipi = :tipi"
			+ " group by a.id, a.tarih, x.`SC-HESAPNO`, a.hesapadi, x.`SC-YETKILI`, a.sevksekli, a.siparisialan, a.siparisnot";
	private static final String bekleyen_sql = "select a.id, a.tarih, x.`SC-HESAPNO`, concat(a.hesapadi,' ',x.`SC-YETKILI`) as hesapadi," 
			+ " CONCAT(x.`SC-MAHALLE`,'/',x.`SC-ILCE`,'/',x.`SC-IL`,'/',x.`SC-ULKE`) as adres, a.sevksekli, a.siparisialan,"
			+ " sum((b.adet-b.sevkadet)*(b.fiyat*(1-b.iskonto1/100)*(1-b.iskonto2/100)*(1+b.kdv/100))) as bakiye, a.siparisnot"
			+ " from `WINDOWS`.`WIN_SIPARISDETAY` b join `WINDOWS`.`WIN_SIPARIS` a on a.id = b.idsiparis"
			+ " left join `COBOL`.`COB_SICIL` x on a.hesapno = x.`SC-HESAPNO` where a.siparistipi = :tipi and b.adet > b.sevkadet"
			+ " group by a.id, a.tarih, x.`SC-HESAPNO`, a.hesapadi, x.`SC-YETKILI`, a.sevksekli, a.siparisialan, a.siparisnot";
	private static final String tamamlanan_sql = "select a.id, a.tarih, x.`SC-HESAPNO`, concat(a.hesapadi,' ',x.`SC-YETKILI`) as hesapadi," 
			+ " CONCAT(x.`SC-MAHALLE`,'/',x.`SC-ILCE`,'/',x.`SC-IL`,'/',x.`SC-ULKE`) as adres, a.sevksekli, a.siparisialan,"
		    + " sum(b.adet*(b.fiyat*(1-b.iskonto1/100)*(1-b.iskonto2/100)*(1+b.kdv/100))) as bakiye, a.siparisnot"
			+ " from `WINDOWS`.`WIN_SIPARISDETAY` b join `WINDOWS`.`WIN_SIPARIS` a on a.id = b.idsiparis"
			+ " left join `COBOL`.`COB_SICIL` x on a.hesapno = x.`SC-HESAPNO`"
			+ " where a.siparistipi = :tipi and b.adet = b.sevkadet"
			+ " group by a.id, a.tarih, x.`SC-HESAPNO`, a.hesapadi, x.`SC-YETKILI`, a.sevksekli, a.siparisialan, a.siparisnot";

	private SiparisListViewModel.Durum lastDurum;

	@SuppressWarnings("unchecked")
	public boolean load() {
		Session session = HibernateUtil.getSessionFactory().openSession();

		SiparisListViewModel siparisListViewModel = (SiparisListViewModel) model;

		String sql = siparisListViewModel.getDurum() == SiparisListViewModel.Durum.HEPSI ? hepsi_sql
				: (siparisListViewModel.getDurum() == SiparisListViewModel.Durum.BEKLEYEN ? bekleyen_sql
						: tamamlanan_sql);

		List<Object> list = session.createSQLQuery(sql).setParameter("tipi", siparisListViewModel.getTip().ordinal())
				.list();
		session.clear();
		session.close();

		boolean flag = false;
		if (lastDurum == null || !lastDurum.equals(siparisListViewModel.getDurum())) {
			siparisListViewModel.getSiparisListViewDetayModels().clear();
			flag = true;
			lastDurum = siparisListViewModel.getDurum();
		}
		/*
		 * check update & insert
		 */
		List<SiparisListViewDetayModel> currentList = siparisListViewModel.getSiparisListViewDetayModels();
		for (int i = 0; i < list.size(); i++) {
			Object[] obj = (Object[]) list.get(i);
			SiparisListViewDetayModel o1 = new SiparisListViewDetayModel();
			o1.setId((Integer) obj[0]);
			o1.setTarih((String) obj[1]);
			o1.setHesapno((String) obj[2]);
			o1.setHesapadi((String) obj[3]);
			o1.setAdres((String) obj[4]);
			o1.setSevksekli((String) obj[5]);
			o1.setSiparisialan((String) obj[6]);
			o1.setBakiye((BigDecimal) obj[7]);
			o1.setNote((String) obj[8]);

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
			SiparisListViewDetayModel o1 = currentList.get(i);
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
			SiparisListViewDetayModel o1 = currentList.get(index);
			String key = "siparis_" + siparisListViewModel.getTip() + "_" + o1.getId();
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
			currentList.sort(new Comparator<SiparisListViewDetayModel>() {

				@Override
				public int compare(SiparisListViewDetayModel o1, SiparisListViewDetayModel o2) {
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
			List<FUserTrashModel> trashList = FPropertiesAdapter
					.readTrashProperties("siparis_" + siparisListViewModel.getTip() + "_%");
			siparisListViewModel.getTrashMap().clear();
			for (Iterator<FUserTrashModel> itr = trashList.iterator(); itr.hasNext();) {
				FUserTrashModel trashModel = itr.next();
				siparisListViewModel.getTrashMap().put(trashModel.getValue(), trashModel);
			}
		}

		return flag;
	}

	public JasperPrint printBakiye(int index) {
		try {
			JasperReport report = (JasperReport) JRLoader
					.loadObject(getClass().getResource("/tr/fikopar/modules/siparislist/report/BekleyenReport.jasper"));
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("logopath", UIManager.getIcon("logoIcon"));

			SiparisListViewModel siparisListViewModel = (SiparisListViewModel) model;

			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new SiparisBakiyeJRDataSource(
					siparisListViewModel.getTip(), siparisListViewModel.getSiparisListViewDetayModels().get(index)));
			return jasperPrint;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
