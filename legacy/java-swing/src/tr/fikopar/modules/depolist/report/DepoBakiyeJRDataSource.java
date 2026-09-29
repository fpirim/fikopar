package tr.fikopar.modules.depolist.report;

import java.util.Iterator;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.depolist.model.DepoListViewDetayModel;
import tr.fikopar.swing.component.FDateField;

public class DepoBakiyeJRDataSource implements JRDataSource {

	public static String BAKIYE_QUERY = 
			" select x.id, x.tarih, x.hesapno, x.hesapadi, j.`SC-YETKILI`,  j.`SC-ADRES1`, j.`SC-ADRES2`, j.`SC-ADRES3`, j.`SC-VDA`, j.`SC-VNO`, j.`SC-MAHALLE`, j.`SC-ILCE`, j.`SC-IL`, j.`SC-ULKE`, x.deponot," 
			+" y.katno, y.stno, y.prcno, y.oemno, y.tipi, y.cinsi, y.marka,"
			+" GROUP_CONCAT(s.`SY-RAF` SEPARATOR ',') as rafyeri, y.adet-y.sevkadet as bakiye, sum(c.`STA-GIR`)-sum(c.`STA-CIK`) as stok, x.depoalan" 
			+" from `WINDOWS`.`WIN_DEPO` x left join `COBOL`.`COB_SICIL` j on x.hesapno = j.`SC-HESAPNO`"
			+" join `WINDOWS`.`WIN_DEPODETAY` y on x.id = y.iddepo"
			+" left join `COBOL`.`COB_STOKADET` c on c.`STA-KATNO` = y.katno and c.`STA-STNO` = y.stno and c.`STA-YIL` = substring(CURDATE(),1,4)" 
			+" left join `COBOL`.`COB_SAYIM` s on s.`SY-YIL` = substring(CURDATE(),1,4) and s.`SY-KATNO` = y.katno and s.`SY-STNO` = y.stno"
			+" where x.hesapno = :hesapno and x.depotipi = :tipi"
			+" group by id, tarih, hesapno, hesapadi, j.`SC-YETKILI`,  j.`SC-ADRES1`, j.`SC-ADRES2`, j.`SC-ADRES3`, j.`SC-VDA`, j.`SC-VNO`, j.`SC-MAHALLE`, j.`SC-ILCE`, j.`SC-IL`, j.`SC-ULKE`, x.deponot,"
			+" y.katno, y.stno, y.prcno, y.oemno, y.tipi, y.cinsi, y.marka, y.adet, x.depoalan having bakiye > 0;";

	private static int ID = 0;
	private static int TARIH = 1;
	private static int HESAPNO = 2;
	private static int HESAPADI = 3;
	private static int YETKILI = 4;
	private static int ADRES1 = 5;
	private static int ADRES2 = 6;
	private static int ADRES3 = 7;
	private static int VDA = 8;
	private static int VNO = 9;
	private static int MAHALLE = 10;
	private static int ILCE = 11;
	private static int IL = 12;
	private static int ULKE = 13;
	private static int DEPONOT = 14;
	private static int KATNO = 15;
	private static int STNO = 16;
	private static int PRCNO = 17;
	private static int OEMNO = 18;
	private static int TIPI = 19;
	private static int CINSI = 20;
	private static int MARKA = 21;
	private static int RAFYERI = 22;
	private static int BAKIYE = 23;
	private static int STOK = 24;
	private static int DEPOALAN = 25;

	private Iterator<Object[]> itr;
	private Object[] obj;

	public DepoBakiyeJRDataSource(DepoListViewDetayModel depoListViewDetayModel) {
		Session session = HibernateUtil.getSessionFactory().openSession();

		SQLQuery query = session.createSQLQuery(BAKIYE_QUERY);
		query.setParameter("hesapno", depoListViewDetayModel.getHesapno());
		
		@SuppressWarnings("unchecked")
		List<Object[]> list = query.list();

		session.clear();
		session.close();
		itr = list.iterator();
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if ("id".equals(field.getName()))
			return obj[ID];
		else if ("hesapno".equals(field.getName()))
			return obj[HESAPNO];
		else if ("hesapadi".equals(field.getName()))
			return obj[HESAPADI];
		else if ("yetkili".equals(field.getName()))
			return obj[YETKILI];
		else if ("adres1".equals(field.getName()))
			return obj[ADRES1];
		else if ("adres2".equals(field.getName()))
			return obj[ADRES2];
		else if ("adres3".equals(field.getName()))
			return obj[ADRES3];
		else if ("vda".equals(field.getName()))
			return obj[VDA];
		else if ("vno".equals(field.getName()))
			return obj[VNO];
		else if ("mahalle".equals(field.getName()))
			return obj[MAHALLE];
		else if ("ilce".equals(field.getName()))
			return obj[ILCE];
		else if ("il".equals(field.getName()))
			return obj[IL];
		else if ("ulke".equals(field.getName()))
			return obj[ULKE];
		else if ("deponot".equals(field.getName()))
			return obj[DEPONOT];
		else if ("tarih".equals(field.getName()))
			return FDateField.checkDate((String) obj[TARIH]);
		else if ("katno".equals(field.getName()))
			return obj[KATNO];
		else if ("stno".equals(field.getName()))
			return obj[STNO];
		else if ("prcno".equals(field.getName()))
			return obj[PRCNO];
		else if ("oemno".equals(field.getName()))
			return obj[OEMNO];
		else if ("tipi".equals(field.getName()))
			return obj[TIPI];
		else if ("cinsi".equals(field.getName()))
			return obj[CINSI];
		else if ("marka".equals(field.getName()))
			return obj[MARKA];
		else if ("stok".equals(field.getName()))
			return obj[STOK];
		else if ("rafyeri".equals(field.getName()))
			return obj[RAFYERI];
		else if ("bakiye".equals(field.getName()))
			return obj[BAKIYE];
		else if ("depoalan".equals(field.getName()))
			return obj[DEPOALAN];

		return null;
	}

	@Override
	public boolean next() throws JRException {
		if (itr.hasNext()) {
			obj = itr.next();
			return true;
		} else
			return false;

	}

}
