package tr.fikopar.ceksenet.controller;

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

public class CekSenetController extends AbstractController{
	
	@SuppressWarnings("unchecked")
	public List<Object> loadList(Calendar g1, Calendar g2, Calendar v1, Calendar v2, Object [] selectedList) {
		String temp = buildTipQueryFromArray(selectedList);
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Object> list = session.createSQLQuery(
			"select a.vade, a.tarih, a.evrakno,y.adi,tx.aciklama as evraktipi,a.hesapno,concat(b.hesapadi,' ',b.yetkili) as hesapadi,"+
			" concat(b.mahalle,', ',b.ilce,', ',b.il,', ',b.ulke) as adres, a.tutar,"+
			" e.hesapno as chesapno, concat(e.hesapadi,' ',e.yetkili) as chesapadi, t.aciklama from ceksenet a"+
			" left join depo y on a.depono = y.dpno"+
			" left join csnlog c on a.evrakno = c.evrakno and a.tip = c.ctip and c.optime = (select max(x.optime) from csnlog x where x.evrakno = a.evrakno and x.ctip = a.tip)"+
			" and c.sira = (select max(y.sira) from csnlog y where y.evrakno = a.evrakno and y.ctip = a.tip)"+
			" left join tipler tx on tx.tip = a.tip and tx.tipadi = 'CEKSENET'"+
			" left join tipler t on t.tip = c.sonuc and t.tipadi = 'CEKSENETSONUC'"+
			" left join makbuz d on d.evrakno = c.makbuzno and c.tip = d.tip"+ 
			" left join sicil e on d.hesapno = e.hesapno"+
			" left join sicilkod sk on a.hesapno = sk.hesapno "+
			" left join tipkod k on k.kod = sk.kod "+
			" join sicil b on a.hesapno = b.hesapno where k.kod in ("+ (temp.equals("") ? "null" : temp)+")"+
			" and "+(g1 != null ? String.format("a.tarih >= '%04d%02d%02d'", g1.get(Calendar.YEAR), g1.get(Calendar.MONTH)+1, g1.get(Calendar.DATE)) : "1")+
			" and "+(g2 != null ? String.format("a.tarih <= '%04d%02d%02d'", g2.get(Calendar.YEAR), g2.get(Calendar.MONTH)+1, g2.get(Calendar.DATE)) : "1")+
			" and "+(v1 != null ? String.format("a.vade >= '%04d%02d%02d'", v1.get(Calendar.YEAR), v1.get(Calendar.MONTH)+1, v1.get(Calendar.DATE)) : "1")+
			" and "+(v2 != null ? String.format("a.vade <= '%04d%02d%02d'", v2.get(Calendar.YEAR), v2.get(Calendar.MONTH)+1, v2.get(Calendar.DATE)) : "1")+
			" group by vade,tarih,evrakno,adi,evraktipi,hesapadi,hesapadi,adres,tutar,chesapno,chesapadi,aciklama").list();
		
		session.clear();
		session.close();
		
		return list;
		
	}
		
	public JasperPrint print(List<Object []> list, Calendar g1, Calendar g2, Calendar v1, Calendar v2) {
		try {
			
			JasperReport report = null;
			
			report = (JasperReport) JRLoader.loadObject(
					getClass().getResource("/tr/fikopar/ceksenet/report/ceksenetreport.jasper"));
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("logopath", UIManager.getIcon("logoIcon"));
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new CekSenetJRDataSource(list,g1,g2,v1,v2));
			return jasperPrint;
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		return null;
		

	}
	
}
