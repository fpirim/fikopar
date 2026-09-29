package tr.fikopar.modules.stokkart.controller;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.stokkart.model.StokKartViewModel;
import tr.fikopar.swing.view.FController;

public class StokkartController extends FController  {

	public void loadStok() throws Exception {

		StokKartViewModel stokKartViewModel = (StokKartViewModel) model;
		Session session = HibernateUtil.getSessionFactory().openSession();

		StokModel stokModel = (StokModel) session.get(StokModel.class,
				new StokPK(stokKartViewModel.getKatno(), stokKartViewModel.getStno()));
		if(stokModel!=null) {
			stokKartViewModel.setPrcno(stokModel.getPrcno());
			stokKartViewModel.setOemno(stokModel.getOemno());
			stokKartViewModel.setTipi(stokModel.getTipi());
			stokKartViewModel.setParcaadi(stokModel.getCinsi());
			stokKartViewModel.setMarka(stokModel.getMarka());
			stokKartViewModel.setNote(stokModel.getNote());
			stokKartViewModel.setPaket(stokModel.getPaket());
			stokKartViewModel.setMinadet(stokModel.getMin_adet());
			stokKartViewModel.setMaxadet(stokKartViewModel.getMaxadet());
			stokKartViewModel.setDeg(stokModel.getDeg());
			stokKartViewModel.setFiyat(stokModel.getFiyat());
		}
		session.clear();

		@SuppressWarnings("unchecked")
		List<Object> list = session
				.createSQLQuery(
						"select distinct substring(evraktarihi,1,4) as yil from isletme.fw_alfssici_2012 union "
								+ "select distinct substring(evraktarihi,1,4) from isletme.fw_alftsici_2012 union select distinct substring(evraktarihi,1,4) from isletme.fw_alifsici_2012 union "
								+ "select distinct substring(evraktarihi,1,4) from isletme.fw_stfssici_2012 union select distinct substring(evraktarihi,1,4) from isletme.fw_stftsici_2012 union "
								+ "select distinct substring(evraktarihi,1,4) from isletme.fw_stifsici_2012 union select distinct substring(evraktarihi,1,4) from isletme.fw_alfssici_2011 union "
								+ "select distinct substring(evraktarihi,1,4) from isletme.fw_alftsici_2011 union select distinct substring(evraktarihi,1,4) from isletme.fw_alifsici_2011 union "
								+ "select distinct substring(evraktarihi,1,4) from isletme.fw_stfssici_2011 union select distinct substring(evraktarihi,1,4) from isletme.fw_stftsici_2011 union "
								+ "select distinct convert(`SY-YIL` using ascii) from `COBOL`.`COB_SAYIM` union "
								+ "select distinct substring(evraktarihi,1,4) from isletme.fw_stifsici_2011 order by yil desc")
				.list();

		stokKartViewModel.getYils().clear();
		for (Iterator<Object> itr = list.iterator(); itr.hasNext();)
			stokKartViewModel.getYils().add(new BigDecimal((String)itr.next()));

		session.clear();
		session.close();

	}

}
