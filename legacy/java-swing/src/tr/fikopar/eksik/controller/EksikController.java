package tr.fikopar.eksik.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.fikopar.app.MyApp;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.db.models.sicil.SicilModel;
import tr.fikopar.db.models.siparis.SiparisDetayModel;
import tr.fikopar.db.models.siparis.SiparisModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.eksik.model.EksikKatalogModel;
import tr.fikopar.eksik.model.EksikSicilModel;
import tr.fikopar.eksik.model.EksikStokModel;
import tr.fikopar.eksik.view.EksikListTableModel;
import tr.fikopar.modules.siparis.controller.SiparisViewController;
import tr.fikopar.modules.siparis.view.SiparisView;

public class EksikController extends AbstractController {
	private String siparis_sql = " select x.id, x.tarih, x.hesapno, x.hesapadi, j.yetkili,"
			+ " CONCAT(j.mahalle, '/', j.ilce, '/', j.il, '/', j.ulke) as adres, x.siparisnot,"
			+ " y.katno, y.stno, y.prcno, y.oemno, y.tipi, y.cinsi, y.marka, y.iskonto1, y.iskonto2, y.kdv, y.fiyat,"
			+ " IFNULL(GROUP_CONCAT(s.rafno SEPARATOR ','),'') as rafyeri, "
			+ " y.adet-(select IFNULL(sum(b.adet),0) "
			+ "        FROM siparis x, harsic a, hardet b"
			+ "        WHERE a.tip = b.tip and a.evrakno=b.evrakno and a.hesapno=x.hesapno and x.id=idsiparis and"
			+ "              b.katno = y.katno and b.stno=y.stno and a.tarih >= x.tarih and ( (x.siparistipi=0 and a.tip in(1,2)) or (x.siparistipi=1 and a.tip in(4,5))) ) as bakiye,"
			+ " IFNULL(sum(c.gir),0)-IFNULL(sum(c.cik),0) as stok, x.sevksekli, x.siparisialan, IFNULL(h.min_adet,0) as min_adet, IFNULL(h.max_adet,0) as max_adet, x.siparistipi "
			+ " from "
			+ " siparis x"
			+ " left join sicil j on x.hesapno = j.hesapno"
			+ " join siparisdetay y on x.id=y.idsiparis "
			+ " left join stok h on h.katno = y.katno and h.stno = y.stno"
			+ " left join stkadt c on c.katno = y.katno and c.stno = y.stno and c.yil = (select max(o.yil) from stkadt o)"
			+ " left join sayimdetay s on s.katno = y.katno and s.stno = y.stno "
			+ " where x.arsiv = 0 and trim(y.stno) <> '' "
			+ " group by id, tarih, hesapno, hesapadi, yetkili, adres, siparisnot, katno,"
			+ " stno, prcno, oemno, tipi, cinsi, marka, iskonto1, iskonto2, kdv, fiyat, bakiye, sevksekli, siparisialan, min_adet, max_adet, siparistipi having bakiye > 0";

	private String minmax_sql = " select a.katno, a.stno, a.prcno, a.oemno, a.tipi, a.cinsi, a.marka, a.note, IFNULL(a.max_adet,0) as max_adet, IFNULL(a.min_adet,0) as min_adet, IFNULL(sum(b.gir),0)-IFNULL(sum(b.cik),0) as adet, a.fiyat, a.paket,"
			+ " t.katno as bkatno, t.aciklama as baciklama, t.hesapno as bhesapno, d.bstno as bstno,"
			+ " d.isk as bisk, d.kdv as bkdv, j.fiyat as bfiyat"
			+ " from stok a join katlog c on a.katno = c.katno and trim(c.hesapno) = ''"
			+ " left join stokbag d on d.katno = a.katno and a.stno = d.stno"
			+ " left join katlog t on d.bkatno = t.katno"
			+ " left join stok j on j.katno = d.bkatno and j.stno = d.bstno"
			+ " left join stkadt b on a.katno = b.katno and a.stno = b.stno and b.yil =  (select max(c.yil) from stkadt c)"
			+ " group by a.katno, a.stno, a.prcno, a.oemno, a.tipi, a.cinsi, a.marka, a.note, a.max_adet, a.min_adet, a.fiyat,"
			+ " a.paket, t.katno, t.aciklama, t.hesapno, d.bstno, d.isk, d.kdv, j.fiyat";

	private EksikStokModel findEksikStokModel(Integer katno, String stno) {
		for (Iterator<EksikStokModel> itr = eksikStokModels.iterator(); itr
				.hasNext();) {
			EksikStokModel eksikStokModel = itr.next();
			if (eksikStokModel.getKatno().equals(katno)
					&& eksikStokModel.getStno().equals(stno))
				return eksikStokModel;
		}

		EksikStokModel eksikStokModel = new EksikStokModel();
		eksikStokModel.setKatno(katno);
		eksikStokModel.setStno(stno);
		eksikStokModels.add(eksikStokModel);
		return eksikStokModel;

	}

	private List<EksikStokModel> eksikStokModels = new ArrayList<EksikStokModel>();

	public List<EksikStokModel> getEksikStokModels() {
		return eksikStokModels;
	}

	@SuppressWarnings("unchecked")
	public void refresh() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.getTransaction().begin();

		eksikStokModels.clear();
		try {
			List<Object[]> list = session.createSQLQuery(minmax_sql).list();
			for (Iterator<Object[]> itr = list.iterator(); itr.hasNext();) {
				Object[] arr = itr.next();

				EksikStokModel eksikStokModel = findEksikStokModel(
						(Integer) arr[0], (String) arr[1]);
				eksikStokModel.setPrcno((String) arr[2]);
				eksikStokModel.setOemno((String) arr[3]);
				eksikStokModel.setTipi((String) arr[4]);
				eksikStokModel.setCinsi((String) arr[5]);
				eksikStokModel.setMarka((String) arr[6]);
				eksikStokModel.setNote((String) arr[7]);
				eksikStokModel.setMaxadet((BigDecimal) arr[8]);
				eksikStokModel.setMinadet((BigDecimal) arr[9]);
				eksikStokModel.setAdet((BigDecimal) arr[10]);
				eksikStokModel.setFiyat((BigDecimal) arr[11]);
				eksikStokModel.setPaket((Integer) arr[12]);

				if (arr[13] != null) {
					EksikKatalogModel eksikKatalogModel = new EksikKatalogModel();
					eksikKatalogModel.setKatno((Integer.valueOf(arr[13]
							.toString())));
					eksikKatalogModel.setAciklama((String) arr[14]);
					eksikKatalogModel.setHesapno((String) arr[15]);
					eksikKatalogModel.setStno((String) arr[16]);
					eksikKatalogModel.setIsk((BigDecimal) arr[17]);
					eksikKatalogModel.setKdv((BigDecimal) arr[18]);
					eksikKatalogModel.setFiyat((BigDecimal) arr[19]);
					eksikKatalogModel.setEksikStokModel(eksikStokModel);

					eksikStokModel.getEksikKatalogModels().add(
							eksikKatalogModel);
				}

			}
			list.clear();
			list = session.createSQLQuery(siparis_sql).list();
			for (Iterator<Object[]> itr = list.iterator(); itr.hasNext();) {
				Object[] arr = itr.next();

				EksikStokModel eksikStokModel = findEksikStokModel(
						(Integer) arr[7], (String) arr[8]);
				eksikStokModel.setPrcno((String) arr[9]);
				eksikStokModel.setOemno((String) arr[10]);
				eksikStokModel.setTipi((String) arr[11]);
				eksikStokModel.setCinsi((String) arr[12]);
				eksikStokModel.setMarka((String) arr[13]);
				eksikStokModel.setRafyeri((String) arr[18]);
				if( ((Integer) arr[25])==SiparisViewController.SIPARIS_ALMA )
					eksikStokModel.setBakiye(eksikStokModel.getBakiye().add(
						(BigDecimal) arr[19]));
				else
					eksikStokModel.setVerbakiye(eksikStokModel.getVerbakiye().add(
							(BigDecimal) arr[19]));
				eksikStokModel.setAdet((BigDecimal) arr[20]);
				eksikStokModel.setMinadet((BigDecimal) arr[23]);
				eksikStokModel.setMaxadet((BigDecimal) arr[24]);

				EksikSicilModel eksikSicilModel = new EksikSicilModel();
				
				eksikSicilModel.setId((Integer) arr[0]);
				eksikSicilModel.setTarih((String) arr[1]);
				eksikSicilModel.setHesapno((String) arr[2]);
				eksikSicilModel.setHesapadi((String) arr[3]);
				eksikSicilModel.setYetkili((String) arr[4]);
				eksikSicilModel.setAdres((String) arr[5]);
				eksikSicilModel.setSiparisnot((String) arr[6]);
				eksikSicilModel.setIskonto1((BigDecimal) arr[14]);
				eksikSicilModel.setIskonto2((BigDecimal) arr[15]);
				eksikSicilModel.setKdv((BigDecimal) arr[16]);
				eksikSicilModel.setFiyat((BigDecimal) arr[17]);
				eksikSicilModel.setSevsekli((String) arr[21]);
				eksikSicilModel.setSiparisialan((String) arr[22]);
				eksikSicilModel.setBakiye((BigDecimal) arr[19]);
				eksikSicilModel.setTipi((Integer) arr[25]);
				
				eksikSicilModel.setEksikStokModel(eksikStokModel);

				eksikStokModel.getEksikSicilModels().add(eksikSicilModel);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.clear();
			session.close();
		}
	}
	public static final int SIPARIS_ADET_GORE = 0;
	public static final int MINIMUM_ADET_GORE = 1;
	public static final int MAXIMUM_ADET_GORE = 2;
	
	public void createSiparis(int type, String hesapno) {
		SiparisViewController siparisController = new SiparisViewController(SiparisModel.VERME);
		SiparisView siparisView = new SiparisView(siparisController);
		siparisController.setSiparisView(siparisView);
		siparisController.createNewSiparis();
		SiparisModel siparisModel = siparisController.getSiparisModel();
		
		SicilModel sicilModel = siparisController.loadSicilKart(hesapno);
		siparisModel.setSicilModel(sicilModel);
		
		for(EksikStokModel eksikStokModel : eksikStokModels) {
			if(eksikStokModel.getEksikKatalogModels().size()==0)
				continue;
			for(EksikKatalogModel eksikKatalogModel : eksikStokModel.getEksikKatalogModels()) {
				if(eksikKatalogModel.getHesapno().equals(hesapno)) {
					SiparisDetayModel siparisDetayModel = new SiparisDetayModel();
					siparisDetayModel.setKatno(eksikStokModel.getKatno());
					siparisDetayModel.setStno(eksikStokModel.getStno());
					siparisDetayModel.setOemno(eksikStokModel.getOemno());
					siparisDetayModel.setPrcno(eksikStokModel.getPrcno());
					siparisDetayModel.setTipi(eksikStokModel.getTipi());
					siparisDetayModel.setCinsi(eksikStokModel.getCinsi());
					siparisDetayModel.setMarka(eksikStokModel.getMarka());
					siparisDetayModel.setFiyat(eksikKatalogModel.getFiyat());
					siparisDetayModel.setIskonto1(eksikKatalogModel.getIsk());
					siparisDetayModel.setIskonto2(BigDecimal.ZERO);
					siparisDetayModel.setKdv(eksikKatalogModel.getKdv());
					switch(type) {
					case MINIMUM_ADET_GORE:
					siparisDetayModel.setAdet(
							eksikStokModel.getMinadet().compareTo(BigDecimal.ZERO) > 0 && eksikStokModel.getMinadet().compareTo(eksikStokModel.getAdet()) > 0 ? 
									eksikStokModel.getMinadet().subtract(eksikStokModel.getAdet()) : BigDecimal.ZERO
							);
					break;
					case MAXIMUM_ADET_GORE:
						siparisDetayModel.setAdet(
								eksikStokModel.getMaxadet().compareTo(BigDecimal.ZERO) > 0 && eksikStokModel.getMaxadet().compareTo(eksikStokModel.getAdet()) > 0 ? 
									eksikStokModel.getMaxadet().subtract(eksikStokModel.getAdet()) : BigDecimal.ZERO
										);
						break;
					case SIPARIS_ADET_GORE:
						siparisDetayModel.setAdet(
								eksikStokModel.getBakiye().compareTo(BigDecimal.ZERO) > 0 && eksikStokModel.getBakiye().compareTo(eksikStokModel.getAdet()) > 0 ? 
									eksikStokModel.getBakiye().subtract(eksikStokModel.getAdet()) : BigDecimal.ZERO
										);
					}
					siparisDetayModel.setAdet(siparisDetayModel.getAdet().subtract(eksikStokModel.getVerbakiye()));
					if(siparisDetayModel.getAdet().compareTo(BigDecimal.ZERO) > 0)
						siparisModel.addSiparisDetayModel(siparisDetayModel);
				}
			}
		}
		
		siparisView.setName("Sipariþ Verme - Yeni Sipariþ");
		MyApp.addView(siparisView);
	}
	public JasperPrint print(EksikListTableModel eksikListTableModel) {
		try {
			
			JasperReport report = (JasperReport) JRLoader.loadObject(
						getClass().getResource("/tr/fikopar/eksik/report/EksikReport.jasper"));
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, null, new EksikSiparisBakiyeJRDataSource(eksikStokModels, eksikListTableModel));
			return jasperPrint;
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
}
