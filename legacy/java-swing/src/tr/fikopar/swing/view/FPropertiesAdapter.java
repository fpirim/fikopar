package tr.fikopar.swing.view;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import tr.fikopar.swing.db.models.FTableCellStyle;
import tr.fikopar.swing.db.models.FTableStyle;
import tr.fikopar.swing.db.models.FUserPropModel;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.db.persistence.PropertiesUtil;

public class FPropertiesAdapter {

	@SuppressWarnings("unchecked")
	public static FProperties readProperties(FProperties defaultProperties) {
		FProperties properties = new FProperties();
		
		Session session = PropertiesUtil.getSessionFactory().openSession();
		
		List<FTableStyle> list = session.createQuery("from FTableStyle a inner join fetch a.fTableCellStyles").list();
		
		for(Iterator<FTableStyle> itr = list.iterator(); itr.hasNext(); )
			properties.putTableStyle(itr.next());
		
		List<FUserPropModel> list2 = session.createQuery("from FUserPropModel").list();
		
		for(Iterator<FUserPropModel> itr = list2.iterator(); itr.hasNext(); )
			properties.putUserProperties(itr.next());
		
		session.clear();
		session.close();
		
		
		/*
		 * check default application properties set if not initialize
		 */
		
		if(properties.getUserProperties(FProperties.APPLICATION_X) == null)
			properties.putUserProperties(FProperties.APPLICATION_X, "0");
		if(properties.getUserProperties(FProperties.APPLICATION_Y) == null)
			properties.putUserProperties(FProperties.APPLICATION_Y, "0");
		if(properties.getUserProperties(FProperties.APPLICATION_WIDTH) == null)
			properties.putUserProperties(FProperties.APPLICATION_WIDTH, "800");
		if(properties.getUserProperties(FProperties.APPLICATION_HEIGHT) == null)
			properties.putUserProperties(FProperties.APPLICATION_HEIGHT, "600");
		
		for(Iterator<String> itr = defaultProperties.getTableStyleMap().keySet().iterator(); itr.hasNext(); ) {
			FTableStyle o1 = defaultProperties.getTableStyleMap().get(itr.next());
			
			if(!properties.getTableStyleMap().containsKey(o1.getClassKey()))
				properties.putTableStyle(o1);
			else {
				boolean flag = true;
				FTableStyle o2 = properties.getTableStyleMap().get(o1.getClassKey());
				if(o1.getfTableCellStyles().size() != o2.getfTableCellStyles().size()) {
					properties.putTableStyle(o1);
					flag = false;
				}
				if(flag) {
					List<FTableCellStyle> l1 = o1.getfTableCellStyles();
					List<FTableCellStyle> l2 = o2.getfTableCellStyles();
					for(int i=0; i<l1.size(); i++) {
						FTableCellStyle f1 = l1.get(i);
						FTableCellStyle f2 = l2.get(i);
						if( !(  (f1.getType() == null ? f2.getType() == null : f1.getType().equals(f2.getType())) 
							&& (f1.getTitle() == null ? f2.getTitle() == null : f1.getTitle().equals(f2.getTitle()))
							&& (f1.getFontStyle() == null ? f2.getFontStyle() == null : f1.getFontStyle().equals(f2.getFontStyle()))
							&& (f1.getFormat() == null ? f2.getFormat() == null : f1.getFormat().equals(f2.getFormat()))
							&& (f1.getBackgroundColor() == null ? f2.getBackgroundColor() == null : f1.getBackgroundColor().equals(f2.getBackgroundColor()))
							&& (f1.getForegroundColor() == null ? f2.getForegroundColor() == null : f1.getForegroundColor().equals(f2.getForegroundColor())))) {
							properties.putTableStyle(o1);
							break;
						}
					}
				}
					
			}	
		}
		
		for(Iterator<String> itr = defaultProperties.getUserPropertiesMap().keySet().iterator(); itr.hasNext(); ) {
			FUserPropModel o1 = defaultProperties.getUserPropertiesMap().get(itr.next());
			if(!properties.getUserPropertiesMap().containsKey(o1.getPropKey()))
				properties.putUserProperties(o1);
			
		}
		
		return properties;
	}
	public static void writeProperties(FProperties properties) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		session.createQuery("from FTableStyle a inner join fetch a.fTableCellStyles").list();
		session.createQuery("from FUserPropModel").list();
		
		Iterator<FTableStyle> itr = properties.getTableStyleMap().values().iterator();
		
		while(itr.hasNext())
			session.merge(itr.next());
		
		Iterator<FUserPropModel> itr2 = properties.getUserPropertiesMap().values().iterator();
		
		while(itr2.hasNext())
			session.merge(itr2.next());
		
		tx.commit();
		session.clear();
		session.close();
	}
	public static FUserTrashModel readTrashProperty(String param) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		
		FUserTrashModel trashModel = (FUserTrashModel) session.createQuery("from FUserTrashModel h where h.value like :param").setParameter("param", param).uniqueResult();
		
		session.clear();
		session.close();
		return trashModel;
	}
	@SuppressWarnings("unchecked")
	public static List<FUserTrashModel> readTrashProperties(String param) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		
		List<FUserTrashModel> list = session.createQuery("from FUserTrashModel h where h.value like :param").setParameter("param", param).list();
		
		session.clear();
		session.close();
		return list;
	}
	public static void deleteTrashProperty(FUserTrashModel userTrashModel) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		
		session.delete(userTrashModel);
		tx.commit();
		session.clear();
		session.close();
	}
	public static void deleteTrashProperty(String param) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		
		FUserTrashModel trashModel = (FUserTrashModel) session.createQuery("from FUserTrashModel h where h.value = :param")
						.setParameter("param", param).uniqueResult();
		if(trashModel!=null)
			session.delete(trashModel);
		
		tx.commit();
		session.clear();
		session.close();
	}
	public static void writeTrashProperty(FUserTrashModel userTrashModel) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		
		session.saveOrUpdate(userTrashModel);
		
		tx.commit();
		
		session.clear();
		session.close();
		//PropertiesUtil.getSessionFactory().close();
	}
	public static void writeTrashProperties(List<FUserTrashModel> userTrashModels) {
		Session session = PropertiesUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		
		Iterator<FUserTrashModel> itr = userTrashModels.iterator();
		while(itr.hasNext())
			session.saveOrUpdate(userTrashModels);
		
		tx.commit();
		session.clear();
		session.close();
	}
}
