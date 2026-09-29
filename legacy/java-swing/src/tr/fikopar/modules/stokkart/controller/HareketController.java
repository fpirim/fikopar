package tr.fikopar.modules.stokkart.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.hibernate.Session;

import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.stokkart.model.HareketDetayModel;
import tr.fikopar.modules.stokkart.model.HareketViewModel;
import tr.fikopar.swing.view.FController;

public class HareketController extends FController {

	public void loadHarekets() {
		HareketViewModel hareketViewModel = (HareketViewModel) model;

		hareketViewModel.getGirishareketDetayModels().clear();
		hareketViewModel.getCikishareketDetayModels().clear();

		if (hareketViewModel.getYil().compareTo(new BigDecimal(2015)) >= 0)
			fill_2015_ve_sonrasi();
		else
			fill_2014_ve_oncesi();

		prepare(hareketViewModel.getGirishareketDetayModels());
		prepare(hareketViewModel.getCikishareketDetayModels());
	}

	private void prepare(List<HareketDetayModel> list) {
		list.sort(new Comparator<HareketDetayModel>() {

			@Override
			public int compare(HareketDetayModel o1, HareketDetayModel o2) {
				int tarih_result = o1.getTarih().compareTo(o2.getTarih());
				return tarih_result == 0 ? o1.getHesapno().compareTo(o2.getHesapno()) : tarih_result;
			}
		});
		Stack<HareketDetayModel> stack = new Stack<HareketDetayModel>();

		for (int i = 0; i < list.size(); i++) {
			HareketDetayModel o1 = list.get(i);
			if (o1.getTip().compareTo(HareketDetayModel.FAT_CIKIS) == 0 
					|| o1.getTip().compareTo(HareketDetayModel.FAT_GIRIS) == 0 
					|| o1.getTip().compareTo(HareketDetayModel.FAT_IADE_CIKIS) == 0
					|| o1.getTip().compareTo(HareketDetayModel.FAT_IADE_GIRIS) == 0) {
				if (o1.getBag1().compareTo(BigDecimal.ZERO) != 0 && !o1.getBevrakno1().equals("") && !stack.contains(o1)) {
					HareketDetayModel b1 = find(list, o1.getBag1(), o1.getBevrakno1());
					if (b1 != null) {
						if (o1.bagEkle(b1))
							stack.push(b1);
					}
				}
			}
		}

		while (!stack.isEmpty())
			list.remove(stack.pop());

		for (int i = 0; i < list.size(); i++) {
			HareketDetayModel o1 = list.get(i);
			if (o1.getBag2().compareTo(BigDecimal.ZERO) != 0 && !o1.getBevrakno2().equals("") && !stack.contains(o1)) {
				HareketDetayModel b1 = find(list, o1.getBag2(), o1.getBevrakno2());
				if (b1 != null) {
					if (o1.bagEkle(b1))
						stack.push(b1);
				}
			}
		}

		while (!stack.isEmpty())
			list.remove(stack.pop());

	}

	private HareketDetayModel find(List<HareketDetayModel> list, BigDecimal tip, String evrakno) {
		for (int i = 0; i < list.size(); i++) {
			HareketDetayModel o1 = list.get(i);
			if (o1.getTip().equals(tip) && o1.getEvrakno().indexOf(evrakno) != -1)
				return o1;
		}
		return null;
	}

	private static final String _2015_ve_sonrasi_sql = 
			"select concat(convert(a.`SY-YIL` using ascii),'0101') as tarih, 0 as tip, a.`SY-RAF` as evrakno, 0 as depono, 'DEVÝR' as hesapno, 'ÖNCEKÝ DÖNEMDEN DEVÝR' as hesapadi,"
			+" a.`SY-ADET` as adet, 0.0 as iskonto1,0.0 as iskonto2, a.`SY-KDV` as kdv,a.`SY-FIYAT` as fiyat, 0 as bag1, '' as bevrakno1, 0 as bag2, '' as bevrakno2"
			+" from `COBOL`.`COB_SAYIM` a where a.`SY-YIL` like :yil and a.`SY-KATNO` = :katno and a.`SY-STNO` = :stno union all"
			+" select a.`HS-TARIH` as tarih, a.`HS-TIP` as tip, a.`HS-EVRAKNO` as evrakno, a.`HS-DEPONO` as depono, a.`HS-HESAPNO` as hesapno," 
			+" concat(a.`HS-HESAPADI`,' ',a.`HS-YETKILI`) as hesapadi, b.`HD-ADET` as adet, b.`HD-ISKONTO1` as iskonto1, b.`HD-ISKONTO2` as iskonto2," 
			+" b.`HD-KDV` as kdv, b.`HD-FIYAT` as fiyat, a.`HS-BAGTIPI1` as bag1, a.`HS-BEVRAKNO1` as bevrakno1, a.`HS-BAGTIPI2` as bag2, a.`HS-BEVRAKNO2` as bevrakno2"
			+" from `COBOL`.`COB_HAREKET-SICIL` a, `COBOL`.`COB_HAREKET-DETAY` b where a.`HS-TIP` = b.`HD-TIP` and a.`HS-EVRAKNO` = b.`HD-EVRAKNO`"
			+" and b.`HD-KATNO` = :katno and b.`HD-STNO` = :stno and a.`HS-TARIH` like :yil";

	private void fill_2015_ve_sonrasi() {
		HareketViewModel hareketViewModel = (HareketViewModel) model;
		Session session = HibernateUtil.getSessionFactory().openSession();

		@SuppressWarnings("unchecked")
		List<Object> list = session.createSQLQuery(_2015_ve_sonrasi_sql).setParameter("katno", hareketViewModel.getKatno()).setParameter("yil", hareketViewModel.getYil() + "%")
				.setParameter("stno", hareketViewModel.getStno()).list();

		for (Iterator<Object> itr = list.iterator(); itr.hasNext();) {
			Object[] obj = (Object[]) itr.next();
			HareketDetayModel hareketDetayModel = new HareketDetayModel();
			hareketDetayModel.setTarih(String.valueOf(obj[0]));
			hareketDetayModel.setTip(new BigDecimal(String.valueOf(obj[1])));
			hareketDetayModel.setEvrakno(String.valueOf(obj[2]));
			hareketDetayModel.setDepono(new BigDecimal(String.valueOf(obj[3])));
			hareketDetayModel.setHesapno(String.valueOf(obj[4]));
			hareketDetayModel.setHesapadi(String.valueOf(obj[5]));
			hareketDetayModel.setAdet(new BigDecimal(String.valueOf(obj[6])));
			hareketDetayModel.setIskonto1(new BigDecimal(String.valueOf(obj[7])));
			hareketDetayModel.setIskonto2(new BigDecimal(String.valueOf(obj[8])));
			hareketDetayModel.setKdv(new BigDecimal(String.valueOf(obj[9])));
			hareketDetayModel.setFiyat(new BigDecimal(String.valueOf(obj[10])));
			hareketDetayModel.setBag1(new BigDecimal(String.valueOf(obj[11])));
			hareketDetayModel.setBevrakno1(String.valueOf(obj[12]));
			hareketDetayModel.setBag2(new BigDecimal(String.valueOf(obj[13])));
			hareketDetayModel.setBevrakno2(String.valueOf(obj[14]));

			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.DEVIR) == 0
				|| hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_GIRIS) == 0
				|| hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_IADE_GIRIS) == 0
				|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_GIRIS) == 0
				|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_IADE_GIRIS) == 0
				|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_GIRIS) == 0
				|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_IADE_GIRIS) == 0)
				hareketViewModel.getGirishareketDetayModels().add(hareketDetayModel);
			
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_CIKIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_IADE_CIKIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_CIKIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_IADE_CIKIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_CIKIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_IADE_CIKIS) == 0)
				hareketViewModel.getCikishareketDetayModels().add(hareketDetayModel);
				
		}

		session.clear();
		session.close();

	}

	private static final String _2014_ve_oncesi_sql = "select a.tarih, 0 as tip, a.num as evrakno, 0 as depono, 'DEVÝR' as hesapno, a.adi as hesapadi,"
			+ " a.adet, a.isk as iskonto1, 0.0 as iskonto2, 0.0 as iskonto, a.kdv, 0.0 as kdv1, a.fiyat," + "  cast(0 AS SIGNED) as bag1," + "  cast('' as CHAR(10)) as bevrakno1,"
			+ "  cast(0 AS SIGNED) as bag2," + "  cast('' as CHAR(10)) as bevrakno2" + "  from isletme.stokdvr a where a.tarih like :yil and a.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 4 as tip, a.evrakno, 2 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(0 AS SIGNED) as bag1," + "  cast('' as CHAR(10)) as bevrakno1," + "  cast(0 AS SIGNED) as bag2," + "  cast('' as CHAR(10)) as bevrakno2"
			+ "  from isletme.fw_alfssici_2012 a, isletme.fw_alfsdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 4 as tip, a.evrakno, 1 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(0 AS SIGNED) as bag1," + "  cast('' as CHAR(10)) as bevrakno1," + "  cast(4 AS SIGNED) as bag2,"
			+ "  cast(IFNULL((select GROUP_CONCAT(x.num) from isletme.fw_bag_2012 x where x.dosadi2 = 'ALIFSICI.DAT' and x.num2 = a.evrakno and x.tip = 'O'),'') as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_alifsici_2012 a, isletme.fw_alifdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 6 as tip, a.evrakno, 1 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(5 as SIGNED) as bag1,"
			+ "  cast(IFNULL((select substring(x.stokadi,10,10) from isletme.fw_alftdata_2012 x where x.evrakno = b.evrakno and trim(x.stokno) = '' and x.adet = 0),'') as CHAR(10)) as bevrakno1,"
			+ "  cast(4 as SIGNED) as bag2,"
			+ "  cast(IFNULL((select GROUP_CONCAT(x.num) from isletme.fw_bag_2012 x where x.dosadi2 = 'ALFTSICI.DAT' and x.num2 = a.evrakno and x.tip = 'O'),'') as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_alftsici_2012 a, isletme.fw_alftdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 5 as tip, a.evrakno, 1 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(0 AS SIGNED) as bag1," + "  cast('' as CHAR(10)) as bevrakno1," + "  cast(0 AS SIGNED) as bag2," + "  cast('' as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_alirsici_2012 a, isletme.fw_alirdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 1 as tip, a.evrakno, 2 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(0 AS SIGNED) as bag1," + "  cast('' as CHAR(10)) as bevrakno1," + "  cast(0 AS SIGNED) as bag2," + "  cast('' as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_stfssici_2012 a, isletme.fw_stfsdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 1 as tip, a.evrakno, 1 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(0 as signed) as bag1," + "  cast('' as CHAR(10)) as bevrakno1," + "  cast(1 as signed) as bag2,"
			+ "  cast(IFNULL((select GROUP_CONCAT(x.num) from isletme.fw_bag_2012 x where x.dosadi2 = 'STIFSICI.DAT' and x.num2 = a.evrakno and x.tip = 'O'),'') as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_stifsici_2012 a, isletme.fw_stifdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 3 as tip, a.evrakno, 1 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(2 as signed) as bag1,"
			+ "  cast(IFNULL((select substring(x.stokadi,10,10) from isletme.fw_stftdata_2012 x where x.evrakno = b.evrakno and trim(x.stokno) = '' and x.adet = 0),'') as CHAR(10)) as bevrakno1,"
			+ "  cast(1 as signed) as bag2,"
			+ "  cast(IFNULL((select GROUP_CONCAT(x.num) from isletme.fw_bag_2012 x where x.dosadi2 = 'STFTSICI.DAT' and x.num2 = a.evrakno and x.tip = 'O'),'') as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_stftsici_2012 a, isletme.fw_stftdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno" + " union all"
			+ " select a.evraktarihi as tarih, 2 as tip, a.evrakno, 1 as depono, a.hesapno, a.hesapadi, b.adet, a.iskonto1, a.iskonto2, b.iskonto, a.kdv, b.kdv as kdv1, b.fiyati as fiyat,"
			+ "  cast(0 AS SIGNED) as bag1," + "  cast('' as CHAR(10)) as bevrakno1," + "  cast(0 AS SIGNED) as bag2," + "  cast('' as CHAR(10)) as bevrakno2"
			+ " from isletme.fw_stirsici_2012 a, isletme.fw_stirdata_2012 b where a.evrakno = b.evrakno and a.evraktarihi like :yil and b.stokno = :stno";

	private BigDecimal aHundred = new BigDecimal(100);

	private void fill_2014_ve_oncesi() {
		HareketViewModel hareketViewModel = (HareketViewModel) model;
		Session session = HibernateUtil.getSessionFactory().openSession();

		@SuppressWarnings("unchecked")
		List<Object> list = session.createSQLQuery((hareketViewModel.getYil().compareTo(new BigDecimal(2012)) < 0 ? _2014_ve_oncesi_sql.replace("_2012", "_2011") : _2014_ve_oncesi_sql))
				.setParameter("yil", hareketViewModel.getYil() + "%").setParameter("stno", hareketViewModel.getStno()).list();

		for (Iterator<Object> itr = list.iterator(); itr.hasNext();) {
			Object[] obj = (Object[]) itr.next();
			HareketDetayModel hareketDetayModel = new HareketDetayModel();
			hareketDetayModel.setTarih(String.valueOf(obj[0]));
			hareketDetayModel.setTip(new BigDecimal(String.valueOf(obj[1])));
			hareketDetayModel.setEvrakno(String.valueOf(obj[2]));
			hareketDetayModel.setDepono(new BigDecimal(String.valueOf(obj[3])));
			hareketDetayModel.setHesapno(String.valueOf(obj[4]));
			hareketDetayModel.setHesapadi(String.valueOf(obj[5]));
			hareketDetayModel.setAdet(new BigDecimal(String.valueOf(obj[6])));
			hareketDetayModel.setIskonto1(new BigDecimal(String.valueOf(obj[7])));
			hareketDetayModel.setIskonto2(new BigDecimal(String.valueOf(obj[8])));
			BigDecimal kalemIskonto = new BigDecimal(String.valueOf(obj[9]));
			hareketDetayModel.setKdv(new BigDecimal(String.valueOf(obj[10])));
			BigDecimal kalemKdv = new BigDecimal(String.valueOf(obj[11]));
			hareketDetayModel.setFiyat(new BigDecimal(String.valueOf(obj[12])));

			hareketDetayModel.setFiyat(hareketDetayModel.getFiyat().multiply(BigDecimal.ONE.subtract(kalemIskonto.divide(aHundred))).multiply(BigDecimal.ONE.add(kalemKdv.divide(aHundred))).setScale(2,
					RoundingMode.HALF_UP));

			hareketDetayModel.setBag1(new BigDecimal(String.valueOf(obj[13])));
			hareketDetayModel.setBevrakno1(String.valueOf(obj[14]));
			hareketDetayModel.setBag2(new BigDecimal(String.valueOf(obj[15])));
			hareketDetayModel.setBevrakno2(String.valueOf(obj[16]));

			
			if(hareketDetayModel.getTip().compareTo(HareketDetayModel.DEVIR) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_GIRIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_IADE_GIRIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_GIRIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_IADE_GIRIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_GIRIS) == 0
					|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_IADE_GIRIS) == 0)
					hareketViewModel.getGirishareketDetayModels().add(hareketDetayModel);
				
				if(hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_CIKIS) == 0
						|| hareketDetayModel.getTip().compareTo(HareketDetayModel.FAT_IADE_CIKIS) == 0
						|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_CIKIS) == 0
						|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IFT_IADE_CIKIS) == 0
						|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_CIKIS) == 0
						|| hareketDetayModel.getTip().compareTo(HareketDetayModel.IRS_IADE_CIKIS) == 0)
					hareketViewModel.getCikishareketDetayModels().add(hareketDetayModel);
		}

		session.clear();
		session.close();

	}

}
