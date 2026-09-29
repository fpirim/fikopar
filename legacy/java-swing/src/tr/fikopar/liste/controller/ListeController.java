package tr.fikopar.liste.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.UIManager;

import org.hibernate.Session;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.models.windows.stok.BolumModel;
import tr.fikopar.db.models.windows.stok.KampanyaModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.liste.model.ListeBolumModel;
import tr.fikopar.liste.model.ListeModel;

public class ListeController extends AbstractController {

	public static final int STNO = 0;
	public static final int PRCNO = 1;
	public static final int OEMNO = 2;
	public static final int TIPI = 3;
	public static final int CINSI = 4;
	public static final int NOTE = 5;
	public static final int MARKA = 6;
	public static final int ADET = 7;
	public static final int FIYAT = 8;
	public static final int DEG = 9;
	public static final int PAKET = 10;
	public static final int MIN_ADET = 11;
	public static final int MAX_ADET = 12;
	public static final int ALINAN_ADET = 13;
	public static final int VERILEN_ADET = 14;
	public static final int SIRA = 15;
	public static final int KAMPANYA = 16;
	public static final int BITINCE = 17;
	public static final int BOLUMSIRA = 18;
	public static final int BOLUM = 19;

	private TreeMap<Integer, ListeBolumModel> map = new TreeMap<Integer, ListeBolumModel>();

	public ListeController() {
		map = new TreeMap<Integer, ListeBolumModel>();
	}

	/*
	 * private StokModel findStokModel(List<StokModel> list, Integer katno,
	 * String stno) { for(Iterator<StokModel> itr = list.iterator();
	 * itr.hasNext();) { StokModel stokModel = itr.next();
	 * if(stokModel.getStokPK().getKatno() == katno &&
	 * stokModel.getStokPK().getStno().equals(stno)) return stokModel; } return
	 * null; }
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	public void save() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();

		for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext();) {
			Integer key = itr.next();
			ListeBolumModel bolumModel = map.get(key);
			TreeMap<Integer, ListeModel> listeModels = bolumModel.getMap();
			for (Iterator<Integer> itr2 = listeModels.keySet().iterator(); itr2.hasNext();) {
				Integer key2 = itr2.next();
				ListeModel listeModel = listeModels.get(key2);
				StokModel stokModel = null;

				if (listeModel.isDirty() || bolumModel.isDirty()) {
					stokModel = (StokModel) session.get(StokModel.class, new StokPK(listeModel.getKatno(), listeModel.getStno()));
					System.out.println(stokModel.getStokPK().getKatno() + ":" + stokModel.getStokPK().getStno());
				}

				if (listeModel.isDirty() && stokModel != null) {

					KampanyaModel kampanyaModel = findKampanyaModel(stokModel, "00000000");
					if (kampanyaModel == null) {
						kampanyaModel = new KampanyaModel();
						kampanyaModel.setKatno(stokModel.getStokPK().getKatno());
						kampanyaModel.setStno(stokModel.getStokPK().getStno());
						kampanyaModel.setTarih("00000000");
						stokModel.getKampanyaModels().add(kampanyaModel);
					}

					kampanyaModel.setFiyati(listeModel.getKampanya());
					kampanyaModel.setBitince(listeModel.getKampanya().compareTo(BigDecimal.ZERO) == 0 ? false : listeModel.isBitince());

					String curDate = dateFormat.format(new Date());
					kampanyaModel = findKampanyaModel(stokModel, curDate);
					if (kampanyaModel == null && listeModel.getKampanya().compareTo(BigDecimal.ZERO) != 0) {
						kampanyaModel = new KampanyaModel();
						kampanyaModel.setKatno(stokModel.getStokPK().getKatno());
						kampanyaModel.setStno(stokModel.getStokPK().getStno());
						kampanyaModel.setTarih(curDate);
						stokModel.getKampanyaModels().add(kampanyaModel);
					}

					if (listeModel.getKampanya().compareTo(BigDecimal.ZERO) != 0) {
						kampanyaModel.setFiyati(listeModel.getKampanya());
						kampanyaModel.setBitince(listeModel.isBitince());
					} else {
						if (kampanyaModel != null)
							stokModel.getKampanyaModels().remove(kampanyaModel);

					}

					BolumModel bolum_Model = stokModel.getBolumModel() == null ? new BolumModel() : stokModel.getBolumModel();
					bolum_Model.setStokModel(stokModel);
					bolum_Model.setSira(key2);
					bolum_Model.setBolumsira(key);
					bolum_Model.setBolum(bolumModel.getBolum());
					stokModel.setBolumModel(bolum_Model);
				}

			}
		}

		session.getTransaction().commit();
		session.clear();
		session.close();

	}

	private KampanyaModel findKampanyaModel(StokModel stokModel, String tarih) {
		for (Iterator<KampanyaModel> itrK = stokModel.getKampanyaModels().iterator(); itrK.hasNext();) {
			KampanyaModel kampanyaModel = itrK.next();
			if (kampanyaModel.getTarih().equals(tarih)) {
				return kampanyaModel;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public TreeMap<Integer, ListeBolumModel> loadMap() throws Exception {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();

		map.clear();

		ListeBolumModel bolumModel = null;
		List<Object[]> list = session.createSQLQuery("call PRC_LISTE()").list();
		int bolum_sira = -1;
		for (Object[] arr : list) {
			if (bolum_sira != (Integer) arr[BOLUMSIRA]) {
				bolumModel = new ListeBolumModel();
				bolumModel.setBolum((String) arr[BOLUM]);
				map.put((Integer) arr[BOLUMSIRA], bolumModel);
				bolum_sira = (Integer) arr[BOLUMSIRA];
			}
			ListeModel listeModel = new ListeModel();
			listeModel.setKatno(1);
			listeModel.setStno((String) arr[STNO]);
			listeModel.setPrcno((String) arr[PRCNO]);
			listeModel.setOemno((String) arr[OEMNO]);
			listeModel.setTipi((String) arr[TIPI]);
			listeModel.setCinsi((String) arr[CINSI]);
			listeModel.setNote((String) arr[NOTE]);
			listeModel.setMarka((String) arr[MARKA]);
			listeModel.setAdet((BigDecimal) arr[ADET]);
			listeModel.setFiyat((BigDecimal) arr[FIYAT]);
			listeModel.setDeg((String) arr[DEG]);
			listeModel.setPaket(new BigDecimal(String.valueOf(arr[PAKET])));
			listeModel.setMin_adet((BigDecimal) arr[MIN_ADET]);
			listeModel.setMax_adet((BigDecimal) arr[MAX_ADET]);
			listeModel.setAlinan_adet((BigDecimal) arr[ALINAN_ADET]);
			listeModel.setVerilen_adet((BigDecimal) arr[VERILEN_ADET]);
			listeModel.setKampanya((BigDecimal) arr[KAMPANYA]);
			listeModel.setBitince(arr[BITINCE] == null ? false : ((Byte) arr[BITINCE]) != 0);

			bolumModel.addListeModel((Integer) arr[SIRA], listeModel);

			// max_sira = (Integer)arr[SIRA];
		}
		session.clear();
		session.close();
		return map;
	}

	@SuppressWarnings("unchecked")
	public List<Object> loadKampanya(Integer katno, String stno) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();

		List<Object> list = session.createSQLQuery("select tarih,fiyati from kampanya where katno = :katno and stno = :stno and tarih <> '00000000' order by tarih desc").setParameter("katno", katno)
				.setParameter("stno", stno).list();

		session.clear();
		session.close();
		return list;

	}

	public JasperPrint print(List<Object> arr, String reportinfo, boolean kampanya) {
		try {

			JasperReport report = null;

			report = (JasperReport) JRLoader.loadObject(getClass().getResource("/tr/fikopar/liste/report/listereport.jasper"));

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("companyname", UIManager.getString("companyname"));
			parameters.put("companylogo", UIManager.getIcon("logoIcon"));
			parameters.put("reportinfo", reportinfo);
			parameters.put("totalRecordsOnReport", arr.size());
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new ListeJRDataSource(arr, kampanya));
			return jasperPrint;
		} catch (JRException e1) {
			e1.printStackTrace();
		}
		return null;

	}
}
