package tr.fikopar.ceksenet.controller;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import tr.fikopar.swing.component.FDatefield;

public class CekSenetJRDataSource implements JRDataSource{

	private static final int VADE = 0;
	private static final int TARIH = 1;
	private static final int EVRAKNO = 2;
	private static final int DEPOADI = 3;
	private static final int EVRAKTIPI = 4;
	private static final int HESAPNO = 5;
	private static final int HESAPADI = 6;
	private static final int ADRES = 7;
	private static final int TUTAR = 8;
	private static final int CHESAPNO = 9;
	private static final int CHESAPADI = 10;
	private static final int DURUM = 11;
	private Iterator<Object[]> itr;
	private Object [] obj;
	private Calendar g1, g2, v1, v2;
	public CekSenetJRDataSource(List<Object []> list, Calendar g1, Calendar g2, Calendar v1, Calendar v2) {
		itr = list.iterator();
		this.g1 = g1;
		this.g2 = g2;
		this.v1 = v1;
		this.v2 = v2;
		
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("vadefil".equals(field.getName()))
			return (v1 == null ? "" : String.format("%02d/%02d/%04d", v1.get(Calendar.DATE), v1.get(Calendar.MONTH)+1, v1.get(Calendar.YEAR)))
					+" - "+
				   (v2 == null ? "" : String.format("%02d/%02d/%04d", v2.get(Calendar.DATE), v2.get(Calendar.MONTH)+1, v2.get(Calendar.YEAR)));
		else if("girisfil".equals(field.getName()))
			return (g1 == null ? "" : String.format("%02d/%02d/%04d", g1.get(Calendar.DATE), g1.get(Calendar.MONTH)+1, g1.get(Calendar.YEAR)))
					+" - "+
				   (g2 == null ? "" : String.format("%02d/%02d/%04d", g2.get(Calendar.DATE), g2.get(Calendar.MONTH)+1, g2.get(Calendar.YEAR)));
		else if("vade".equals(field.getName()))
			return FDatefield.checkDate((String) obj[VADE]);
		else if("tarih".equals(field.getName()))
			return FDatefield.checkDate((String) obj[TARIH]);
		else if("evrakno".equals(field.getName()))
			return obj[EVRAKNO];
		else if("depoadi".equals(field.getName()))
			return obj[DEPOADI];
		else if("evraktipi".equals(field.getName()))
			return obj[EVRAKTIPI];
		else if("hesapno".equals(field.getName()))
			return obj[HESAPNO];
		else if("hesapadi".equals(field.getName()))
			return obj[HESAPADI];
		else if("adres".equals(field.getName()))
			return obj[ADRES];
		else if("tutar".equals(field.getName()))
			return obj[TUTAR];
		else if("chesapno".equals(field.getName()))
			return obj[CHESAPNO];
		else if("chesapadi".equals(field.getName()))
			return obj[CHESAPADI];
		else if("durum".equals(field.getName()))
			return obj[DURUM];
				
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
