package tr.fikopar.modules.contentasist.sicilmodel;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FTableContentAsistMessage;
import tr.fikopar.swing.table.FTableContentAsistService;

public class SicilModelContentAsistService extends FTableContentAsistService {

	@Override
	public void refresh(FTextField source, BlockingQueue<Object> queue) throws InterruptedException {

		queue.put(new FTableContentAsistMessage("Veri tabanýna baðlanýlýyor ..."));
		Session session = HibernateUtil.getSessionFactory().openSession();

		queue.put(new FTableContentAsistMessage("Sorgu hazýrlanýyor ..."));

		SQLQuery query = session.createSQLQuery("select count(*) from `COBOL`.`COB_SICIL`");
		Long total = ((BigInteger) query.uniqueResult()).longValue();

		query = session.createSQLQuery("select `SC-HESAPNO` as hesapno, `SC-HESAPADI` as hesapadi, `SC-YETKILI` as yetkili from `COBOL`.`COB_SICIL` order by hesapadi, yetkili");
		query.addScalar("hesapno", StandardBasicTypes.STRING);
		query.addScalar("hesapadi", StandardBasicTypes.STRING);
		query.addScalar("yetkili", StandardBasicTypes.STRING);
		
		query.setReadOnly(true);
		query.setFetchSize(Integer.MIN_VALUE);

		FTableContentAsistMessage message = new FTableContentAsistMessage("Yükleniyor ...", 0, total);
		ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);


		while (total!=0L && results.next() && !interrupt) {
				queue.put(results.get());
				queue.put(message.increment());
				if(message.getCurrent()%500==0)
					session.flush();
		}
		if(interrupt)
			session.cancelQuery();
		results.close();
		session.flush();
		session.close();
	}

	@Override
	public int getMaxVisibleRowCount() {
		return 10;
	}

}
