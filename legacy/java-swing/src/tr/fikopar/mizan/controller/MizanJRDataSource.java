package tr.fikopar.mizan.controller;

import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class MizanJRDataSource implements JRDataSource{

	private Iterator<Object []> itr;
	private Object [] obj;
	
	public MizanJRDataSource(List<Object []> arr) {
		itr = arr.iterator();
		
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if("hesapno".equals(field.getName()))
			return String.valueOf(obj[0]);
		else if("hesapadi".equals(field.getName()))
			return String.valueOf(obj[1])+"/"+String.valueOf(obj[2]);
		else if("adres".equals(field.getName()))
			return String.valueOf(obj[3])+"/"+String.valueOf(obj[4])+"/"+String.valueOf(obj[5])+"/"+String.valueOf(obj[6]);
		else if("plasiyer".equals(field.getName()))
			return obj[7];
		else if("tipi".equals(field.getName()))
			return obj[8];
		else if("borc".equals(field.getName()))
			return obj[9];
		else if("alacak".equals(field.getName()))
			return obj[10];
			
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
