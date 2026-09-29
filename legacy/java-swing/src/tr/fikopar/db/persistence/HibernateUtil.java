package tr.fikopar.db.persistence;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import tr.fikopar.db.models.cobol.sicil.SicilModel;
import tr.fikopar.db.models.windows.alim.AlimModel;
import tr.fikopar.db.models.windows.excel.ExcelModel;
import tr.fikopar.db.models.windows.sayim.SayimModel;
import tr.fikopar.modules.sayim.controller.SayimController;

public class HibernateUtil {
	public static SessionFactory sessionFactory;
	static {
		//emf = Persistence.createEntityManagerFactory("jpa");
		Configuration configuration = new Configuration();
		configuration.configure("META-INF/hibernate.cfg.xml");
		//configuration.configure();
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}
	public static Session getCurrentSession() {
		Session s = sessionFactory.getCurrentSession();
		if(s == null) {
			s = sessionFactory.openSession();
		}
		return s;
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		AlimModel alim = (AlimModel) session.load(AlimModel.class, 1);
		System.out.println("alim:"+alim.getSicilModel().getHesapadi());
		System.out.println("alim:"+alim.getAlimDetayModels().size());
		System.out.println("alim:"+alim.getAlimDetayModels().get(0).getStokModel().getCinsi());
		System.out.println("alim:"+alim.getAlimDetayModels().get(0).getKurModel().getKur());
		SayimModel sayim = (SayimModel) session.load(SayimModel.class, 15);
		System.out.println("sayim:"+sayim.getYil());
		System.out.println("sayim:"+sayim.getSayimDetays().size());
		session.clear();
		session.close();
		HibernateUtil.getSessionFactory().close();
	}
}
