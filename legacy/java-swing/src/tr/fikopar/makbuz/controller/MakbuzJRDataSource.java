package tr.fikopar.makbuz.controller;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.swing.component.FDatefield;

public class MakbuzJRDataSource implements JRDataSource{

	private static final int TARIH = 0;
	private static final int EVRAKNO = 1;
	private static final int EVRAKTIPI = 2;
	private static final int SEKIL = 3;
	private static final int DEPO = 4;
	private static final int HESAPNO = 5;
	private static final int HESAPADI = 6;
	private static final int ADRES = 7;
	private static final int ACIKLAMA = 8;
	private static final int TUTAR = 9;
	private Iterator<Object[]> itr;
	private Object [] obj;
	private Calendar g1, g2;
	public MakbuzJRDataSource(List<Object []> list, Calendar g1, Calendar g2) {
		itr = list.iterator();
		this.g1 = g1;
		this.g2 = g2;
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("tarihfil".equals(field.getName()))
			return (g1 == null ? "" : String.format("%02d/%02d/%04d", g1.get(Calendar.DATE), g1.get(Calendar.MONTH)+1, g1.get(Calendar.YEAR)))
					+" - "+
				   (g2 == null ? "" : String.format("%02d/%02d/%04d", g2.get(Calendar.DATE), g2.get(Calendar.MONTH)+1, g2.get(Calendar.YEAR)));
		else if("tarih".equals(field.getName()))
			return FDatefield.checkDate((String) obj[TARIH]);
		else if("evrakno".equals(field.getName()))
			return obj[EVRAKNO];
		else if("evraktipi".equals(field.getName()))
			return obj[EVRAKTIPI];
		else if("sekil".equals(field.getName()))
			return obj[SEKIL];
		else if("depo".equals(field.getName()))
			return obj[DEPO];
		else if("hesapno".equals(field.getName()))
			return obj[HESAPNO];
		else if("hesapadi".equals(field.getName()))
			return obj[HESAPADI];
		else if("adres".equals(field.getName()))
			return obj[ADRES];
		else if("aciklama".equals(field.getName()))
			return obj[ACIKLAMA];
		else if("tutar".equals(field.getName()))
			return obj[TUTAR];
				
		return null;
	}

	@Override
	public boolean next() throws JRException {
		if(itr.hasNext()) {
			obj = itr.next();
			return true;
		}
		return false;
	}

}
