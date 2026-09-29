package tr.fikopar.makbuz.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import org.hibernate.Session;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.db.persistence.HibernateUtil;

public class MakbuzController extends AbstractController {
	// private static Logger log = Logger.getLogger(SiparisController.class);
	// private EntityManager em = null;
	// private Siparis siparis;

	@SuppressWarnings("unchecked")
	public List<Object> loadList(Calendar g1, Calendar g2, Object [] selectedList) throws Exception {
		
		String temp = buildTipQueryFromArray(selectedList);
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		List<Object> tlist = session
					.createSQLQuery(
							"select a.tarih, a.evrakno,t.aciklama as tip,o.sekiladi as sekil,y.adi as depo, b.hesapno, concat(b.hesapadi,' ',b.yetkili) as hesapadi,"
									+ " concat(b.mahalle,', ',b.ilce,', ',b.il,', ',b.ulke) as adres,a.aciklama,a.tutar from makbuz a "
									+ " left join sicil b on a.hesapno = b.hesapno "
									+ " left join sicilkod sk on a.hesapno = sk.hesapno "
									+ " left join sicilkod k on k.kod = sk.kod "
									+ " left join tipler t on t.tip = a.tip and t.tipadi = 'MAKBUZ'"
									+ " left join depo y on a.depono = y.dpno"
									+ " left join odeme o on o.sekilno = a.sekilno where k.kod in ("
									+ (temp.equals("") ? "null" : temp)
									+ ")"
									+ " and "
									+ (g1 != null ? String.format(
											"a.tarih >= '%04d%02d%02d'",
											g1.get(Calendar.YEAR),
											g1.get(Calendar.MONTH) + 1,
											g1.get(Calendar.DATE)) : "1")
									+ " and "
									+ (g2 != null ? String.format(
											"a.tarih <= '%04d%02d%02d'",
											g2.get(Calendar.YEAR),
											g2.get(Calendar.MONTH) + 1,
											g2.get(Calendar.DATE)) : "1")
									+ " group by tarih,evrakno,tip,sekil,depo,hesapno,hesapadi,adres,aciklama,tutar")
					.list();
		
		session.clear();
		session.close();
		
		return tlist;

	}

	public JasperPrint print(List<Object[]> list, Calendar g1, Calendar g2) {
		try {

			JasperReport report = null;

			report = (JasperReport) JRLoader.loadObject(getClass().getResource(
					"/tr/fikopar/makbuz/report/makbuzreport.jasper"));

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("logopath", UIManager.getIcon("logoIcon"));
			JasperPrint jasperPrint = JasperFillManager.fillReport(report,
					parameters, new MakbuzJRDataSource(list, g1, g2));
			// JasperReport report =
			// JasperCompileManager.compileReport("report/StokDetayReport.jrxml");
			return jasperPrint;
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		return null;

	}

}
