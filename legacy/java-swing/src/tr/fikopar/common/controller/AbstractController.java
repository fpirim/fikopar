package tr.fikopar.common.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import tr.fikopar.app.MyApp;
import tr.fikopar.common.model.TipDataItem;
import tr.fikopar.db.persistence.HibernateUtil;

public abstract class AbstractController {

	static Logger log = Logger.getLogger(AbstractController.class.getName());

	public AbstractController() {
	}

	protected String buildTipQueryFromArray(Object [] selectedList) {
		String temp = "";
		for(Object obj : selectedList)
			temp += ((TipDataItem)obj).getKod()+",";
		temp = temp.length() == 0 ? "" : temp.substring(0, temp.length()-1);
		return temp;
	}
	
	@SuppressWarnings("unchecked")
	public List<TipDataItem> loadTipList() {
		List<TipDataItem> rtnList = new ArrayList<TipDataItem>();
		try {
			Session session = HibernateUtil.getSessionFactory().openSession();
			
			List<Object[]> list = session.createSQLQuery("select kod, tip from tipkod order by kod").list();
			for (Iterator<Object[]> itr = list.iterator(); itr.hasNext();) {
				Object[] obj = itr.next();
				rtnList.add(new TipDataItem((Integer) obj[0], (String) obj[1]));
			}

			session.clear();
			session.close();
		} catch (Exception ex) {
			uncaughtException(Thread.currentThread(), ex);
		}
		return rtnList;

	}

	public void uncaughtException(Thread t, Throwable ex) {
		System.out.print(t.getName() + " : ");
		ex.printStackTrace();

		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));

		JTextArea ta = new JTextArea(10, 60);
		ta.append(t.getName() + " : " + sw.toString());
		JOptionPane.showMessageDialog(MyApp.appHandler, new JScrollPane(ta), "Exception", JOptionPane.ERROR_MESSAGE);
	}
}
