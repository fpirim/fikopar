package tr.fikopar.modules.contentasist.stokmodel;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;

import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FTableContentAsistMessage;
import tr.fikopar.swing.table.FTableContentAsistService;

public class StokModelContentAsistService extends FTableContentAsistService {

	private String hesapno = "";
	private SiparisListViewModel.Tip siparistipi = SiparisListViewModel.Tip.ALMA;
	private int year = Calendar.getInstance().get(Calendar.YEAR);

	
	public void setSiparistipi(SiparisListViewModel.Tip siparistipi) {
		this.siparistipi = siparistipi;
	}
	public SiparisListViewModel.Tip getSiparistipi() {
		return siparistipi;
	}
	
	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}

	public String getHesapno() {
		return hesapno;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getYear() {
		return year;
	}

	@Override
	public void refresh(FTextField source, BlockingQueue<Object> queue) throws InterruptedException {

		/*
		BigDecimal katno = null;

		if (siparistipi == SiparisListViewModel.Tip.VERME && !hesapno.equals("")) {
			Session sess2 = HibernateUtil.getSessionFactory().openSession();
			KatlogModel katlogModel = (KatlogModel) sess2.createQuery("from KatlogModel k where k.hesapno = :hesapno")
					.setParameter("hesapno", hesapno).uniqueResult();
			if (katlogModel != null)
				katno = katlogModel.getKatno();
			sess2.clear();
			sess2.close();
		}
         */
		//System.out.println(katno);

		queue.put(new FTableContentAsistMessage("Veri tabanýna baðlanýlýyor ..."));
		Session session = HibernateUtil.getSessionFactory().openSession();

		queue.put(new FTableContentAsistMessage("Sorgu hazýrlanýyor ..."));

		SQLQuery query = (SQLQuery) (hesapno.equals("") ? 
				session.createSQLQuery("select count(*) from `COBOL`.`COB_STOK`where `ST-KATNO` in (select distinct `KT-KATNO` from `COBOL`.`COB_KATLOG` where `KT-HESAPNO` = '')") :
				session.createSQLQuery("select count(*) from `WINDOWS`.`WIN_ALIM` a join `WINDOWS`.`WIN_ALIMDETAY` b on a.id = b.idalim where a.hesapno = :hesapno").setString("hesapno", hesapno));

		Long total = ((BigInteger) query.uniqueResult()).longValue();

		query = (SQLQuery) (hesapno.equals("") ?
				session.createSQLQuery(
					    "select a.`ST-KATNO` as katno, a.`ST-STNO` as stno, a.`ST-PRCNO` as prcno,"
					   +" GROUP_CONCAT(x.oemno) as oemno, a.`ST-TIPI` as tipi, a.`ST-CINSI` as cinsi, a.`ST-MARKA` as marka, a.`ST-NOTE` as note,"
			           +" IFNULL(sum(b.`STA-GIR`),0)-IFNULL(sum(b.`STA-CIK`),0) as adet, a.`ST-FIYAT` as fiyat, IFNULL(c.kampanya,0) as kampanya, a.`ST-PAKET` as paket"
					   +" from `COBOL`.`COB_STOK` a left join `COBOL`.`COB_STOKADET` b on b.`STA-YIL` = :year and a.`ST-KATNO` = b.`STA-KATNO` and a.`ST-STNO` = b.`STA-STNO`"
			           +" left join `WINDOWS`.`WIN_ALIMDETAY` x on x.katno = a.`ST-KATNO` and x.stno = a.`ST-STNO`"
			           +" left join `WEB_PRODUCT`.`WEB_KAMPANYA` c on a.`ST-KATNO` = c.katno and a.`ST-STNO` = c.stno and c.bitti = 0"
					   +" where a.`ST-KATNO` in (select distinct `KT-KATNO` from `COBOL`.`COB_KATLOG` where `KT-HESAPNO` = '')"
			           +" group by a.`ST-KATNO`, a.`ST-STNO`,  a.`ST-PRCNO`, a.`ST-TIPI`, a.`ST-CINSI`, a.`ST-MARKA`, a.`ST-NOTE`, a.`ST-FIYAT`, c.kampanya, a.`ST-PAKET` order by katno, a.`ST-SIRA`")
				.setParameter("year", year) :
				session.createSQLQuery(
						" select a.`ST-KATNO` as katno, a.`ST-STNO` as stno, a.`ST-PRCNO` as prcno, GROUP_CONCAT(d.oemno) as oemno," 
					   +" a.`ST-TIPI` as tipi, a.`ST-CINSI` as cinsi, a.`ST-MARKA` as marka, a.`ST-NOTE` as note,"
					   +" IFNULL(sum(b.`STA-GIR`),0)-IFNULL(sum(b.`STA-CIK`),0) as adet, a.`ST-FIYAT` as fiyat," 
				       +" IFNULL(c.kampanya,0) as kampanya, a.`ST-PAKET` as paket"
					   +" from `WINDOWS`.`WIN_ALIM` x join `WINDOWS`.`WIN_ALIMDETAY` d on d.idalim = x.id "
				       +" left join `COBOL`.`COB_STOK` a on d.katno = a.`ST-KATNO` and d.stno = a.`ST-STNO`"
					   +" left join `COBOL`.`COB_STOKADET` b on b.`STA-YIL` = :year and b.`STA-KATNO` = d.katno and b.`STA-STNO` = d.stno "
					   +" left join `WEB_PRODUCT`.`WEB_KAMPANYA` c on a.`ST-KATNO` = c.katno and a.`ST-STNO` = c.stno and c.bitti = 0"
					   +" where x.hesapno = :hesapno"
					   +" group by a.`ST-KATNO`,  a.`ST-STNO`, a.`ST-PRCNO`, a.`ST-TIPI`, a.`ST-CINSI`, a.`ST-MARKA`, a.`ST-NOTE`, a.`ST-FIYAT`, c.kampanya, a.`ST-PAKET` order by x.id, d.LINDEX")
				.setParameter("hesapno", hesapno).setParameter("year", year) );
		query.addScalar("katno", StandardBasicTypes.BIG_DECIMAL);
		query.addScalar("stno", StandardBasicTypes.STRING);
		query.addScalar("prcno", StandardBasicTypes.STRING);
		query.addScalar("oemno", StandardBasicTypes.STRING);
		query.addScalar("tipi", StandardBasicTypes.STRING);
		query.addScalar("cinsi", StandardBasicTypes.STRING);
		query.addScalar("marka", StandardBasicTypes.STRING);
		query.addScalar("note", StandardBasicTypes.STRING);
		query.addScalar("adet", StandardBasicTypes.BIG_DECIMAL);
		query.addScalar("fiyat", StandardBasicTypes.BIG_DECIMAL);
		query.addScalar("kampanya", StandardBasicTypes.BIG_DECIMAL);
		query.addScalar("paket", StandardBasicTypes.BIG_DECIMAL);

		query.setReadOnly(true);
		query.setFetchSize(Integer.MIN_VALUE);

		FTableContentAsistMessage message = new FTableContentAsistMessage("Yükleniyor ...", 0, total);

		ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

		while (total != 0L && results.next() && !interrupt) {
			queue.put(results.get());
			queue.put(message.increment());
			if (message.getCurrent() % 500 == 0)
				session.flush();

		}

		if (interrupt)
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
