package tr.fikopar.swing.db.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class PropertiesUtil {
	public static SessionFactory sessionFactory;

	static {
		//emf = Persistence.createEntityManagerFactory("jpa");
		Configuration configuration = new Configuration();
		configuration.configure("META-INF/properties.cfg.xml");
		//configuration.configure();
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}

	public static Session getCurrentSession() {
		Session s = sessionFactory.getCurrentSession();
		if (s == null) {
			s = sessionFactory.openSession();
		}
		return s;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
