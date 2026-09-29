package tr.fikopar.mizan.controller;

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

public class MizanController extends AbstractController{
//	private static Logger log = Logger.getLogger(SiparisController.class);
	//private EntityManager em = null;
	// private Siparis siparis;
	
	@SuppressWarnings("unchecked")
	public List<Object> loadList() throws Exception {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		List<Object> list = session.createSQLQuery(
			"select a.hesapno, a.hesapadi, a.yetkili, a.mahalle, a.ilce, a.il, a.ulke, p.pladi,"+
		    " GROUP_CONCAT(distinct d.tip SEPARATOR ',') as tipi, sum(b.borc) as borc, sum(b.alacak) as alacak"+
			" from sicil a left join sicilkod c on a.hesapno = c.hesapno left join tipkod d on d.kod = c.kod"+
			" left join plasiyer p on p.plno = a.plkod, carhar b where a.hesapno = b.hesapno and b.sira > 0"+
			" group by hesapno, hesapadi, yetkili, mahalle, ilce, il, ulke, pladi").list();
		session.clear();
		session.close();
		return list;
	}
	
		
	public JasperPrint print(List<Object []> arr) {
		try {
			
			JasperReport report = null;
			
			report = (JasperReport) JRLoader.loadObject(
					getClass().getResource("/tr/fikopar/mizan/report/mizanreport.jasper"));
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("logopath", UIManager.getIcon("logoIcon"));
			parameters.put("totalRecordsOnReport", arr.size());
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new MizanJRDataSource(arr));
			//JasperReport report = JasperCompileManager.compileReport("report/StokDetayReport.jrxml");
			return jasperPrint;
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		return null;
		

	}
	
}
