package tr.fikopar.modules.alim.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import tr.fikopar.db.models.cobol.sicil.SicilModel;
import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.models.windows.alim.AlimDetayModel;
import tr.fikopar.db.models.windows.alim.AlimModel;
import tr.fikopar.db.models.windows.excel.ExcelModel;
import tr.fikopar.db.models.windows.kur.KurModel;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.alim.model.AlimViewDetayModel;
import tr.fikopar.modules.alim.model.AlimViewModel;
import tr.fikopar.modules.excel.controller.ExcelViewController;
import tr.fikopar.modules.excel.model.ExcelViewDetayModel;
import tr.fikopar.modules.excel.model.ExcelViewModel;
import tr.fikopar.modules.kur.controller.KurViewController;
import tr.fikopar.modules.kur.model.KurViewModel;
import tr.fikopar.swing.view.FController;

public class AlimViewController extends FController {

	public void createNew() {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		alimViewModel.setId(null);
		alimViewModel.setHesapno("");
		alimViewModel.setHesapadi("");
		alimViewModel.setYetkili("");
		alimViewModel.setTarih(new SimpleDateFormat("YYYYMMdd").format(new Date()));
		alimViewModel.setDusukseAktar(true);
		alimViewModel.setYuksekseAktar(false);
		alimViewModel.setExcelViewModel(null);
		alimViewModel.setNote("");
		alimViewModel.getAlimViewDetayModels().clear();
	}

	public void load() throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		if (alimViewModel.getId() == null) {
			createNew();
			return;
		}

		Session session = HibernateUtil.getSessionFactory().openSession();

		AlimModel alimModel = (AlimModel) session.get(AlimModel.class, alimViewModel.getId());

		if (alimModel == null) {
			session.clear();
			session.close();
			throw new Exception(alimViewModel.getId() + " ait alým kaydý bulunamadý!.");
		}
		List<KurViewModel> kurViewModels = KurViewController.loadKurViewModels();

		alimViewModel.getAlimViewDetayModels().clear();
		for (AlimDetayModel alimDetayModel : alimModel.getAlimDetayModels()) {
			AlimViewDetayModel alimViewDetayModel = new AlimViewDetayModel();
			alimViewDetayModel.setCinsi(alimDetayModel.getCinsi());
			alimViewDetayModel.setFiyat(alimDetayModel.getStokModel() == null ? null : alimDetayModel.getStokModel().getFiyat());
			alimViewDetayModel.setId(alimDetayModel.getId());
			alimViewDetayModel.setIskonto1(alimDetayModel.getIskonto1());
			alimViewDetayModel.setIskonto2(alimDetayModel.getIskonto2());
			alimViewDetayModel.setKatno(alimDetayModel.getKatno());
			alimViewDetayModel.setKdv(alimDetayModel.getKdv());
			alimViewDetayModel.setMaliyet(alimDetayModel.getMaliyet());
			alimViewDetayModel.setMarka(alimDetayModel.getMarka());
			alimViewDetayModel.setNote(alimDetayModel.getStokModel() == null ? null : alimDetayModel.getStokModel().getNote());
			alimViewDetayModel.setOemno(alimDetayModel.getOemno());
			alimViewDetayModel.setPrcno(alimDetayModel.getPrcno());
			alimViewDetayModel.setStno(alimDetayModel.getStno());
			alimViewDetayModel.setTipi(alimDetayModel.getTipi());
			alimViewDetayModel.setMarj(alimDetayModel.getMarj());
			if (alimDetayModel.getKurModel() == null)
				alimViewDetayModel.setKurViewModel(null);
			else {
				for (KurViewModel kurViewModel : kurViewModels) {
					if (alimDetayModel.getKurModel().getKur().equals(kurViewModel.getKur())) {
						alimViewDetayModel.setKurViewModel(kurViewModel);
						break;
					}
				}
			}
			alimViewModel.getAlimViewDetayModels().add(alimViewDetayModel);
		}

		if (alimModel.getExcelModel() != null) {
			List<ExcelViewModel> excelViewModels = ExcelViewController.loadExcelViewModels();

			for (ExcelViewModel excelViewModel : excelViewModels)
				if (alimModel.getExcelModel().getId() == excelViewModel.getId()) {
					alimViewModel.setExcelViewModel(excelViewModel);
					break;
				}
		} else
			alimViewModel.setExcelViewModel(null);

		alimViewModel.setHesapno(alimModel.getSicilModel().getHesapno());
		alimViewModel.setHesapadi(alimModel.getSicilModel().getHesapadi());
		alimViewModel.setYetkili(alimModel.getSicilModel().getYetkili());
		alimViewModel.setId(alimModel.getId());
		alimViewModel.setDusukseAktar(alimModel.getDusukseAktar());
		alimViewModel.setYuksekseAktar(alimModel.getYuksekseAktar());
		alimViewModel.setTarih(alimModel.getTarih());
		alimViewModel.setNote(alimModel.getNote());

		session.clear();
		session.close();
	}

	public void loadSicilModel() throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();

		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, alimViewModel.getHesapno());
		if (sicilModel != null) {
			Hibernate.initialize(sicilModel.getSiciltelModels());
			Hibernate.initialize(sicilModel.getSicilHarekets());
		} else {
			session.clear();
			session.close();
			throw new Exception(alimViewModel.getHesapno() + " ait sicil kartý bulunamadý!.");
		}
		alimViewModel.setHesapadi(sicilModel.getHesapadi());
		alimViewModel.setYetkili(sicilModel.getYetkili());

		session.clear();
		session.close();

	}

	public void delete() throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		if (alimViewModel.getId() == null)
			return;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();

		AlimModel alimModel = (AlimModel) session.get(AlimModel.class, alimViewModel.getId());
		if (alimModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(alimViewModel.getId() + " ait sipariþ kaydý bulunamadý!");
		}

		session.delete(alimModel);
		tx.commit();
		session.clear();
		session.close();

	}

	public void save() throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		SicilModel sicilModel = (SicilModel) session.get(SicilModel.class, alimViewModel.getHesapno());
		if (sicilModel == null) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception(alimViewModel.getHesapno() + " ait sicil kartý bulunumadý!");
		}
		session.evict(sicilModel);

		ExcelModel excelModel = null;
		if (alimViewModel.getExcelViewModel() != null) {
			excelModel = (ExcelModel) session.get(ExcelModel.class, alimViewModel.getExcelViewModel().getId());
			if (excelModel == null) {
				tx.rollback();
				session.clear();
				session.close();
				throw new Exception(alimViewModel.getExcelViewModel().getId() + " ait excel kartý bulunumadý!");
			}
			session.evict(excelModel);
		}

		AlimModel alimModel;
		if (alimViewModel.getId() != null) {
			alimModel = (AlimModel) session.get(AlimModel.class, alimViewModel.getId());
			if (alimModel == null) {
				tx.rollback();
				session.clear();
				session.close();
				throw new Exception(alimViewModel.getId() + " ait alým kaydý bulunumadý.");
			} else {
				Hibernate.initialize(alimModel.getAlimDetayModels());
				session.evict(alimModel);
			}
		} else
			alimModel = new AlimModel();

		List<KurModel> kurModels = session.createQuery("from KurModel h").list();
		for (KurModel kurModel : kurModels)
			session.evict(kurModel);

		alimModel.setDusukseAktar(alimViewModel.getDusukseAktar());
		alimModel.setYuksekseAktar(alimViewModel.getYuksekseAktar());
		alimModel.setTarih(alimViewModel.getTarih());
		alimModel.setSicilModel(sicilModel);
		alimModel.setExcelModel(excelModel);
		alimModel.setId(alimViewModel.getId());
		alimModel.setNote(alimViewModel.getNote());

		alimModel.getAlimDetayModels().clear();
		HashMap<String, Integer> map = new HashMap<>();
		for (AlimViewDetayModel alimDetayViewModel : alimViewModel.getAlimViewDetayModels()) {
			AlimDetayModel alimDetayModel = new AlimDetayModel();
			alimDetayModel.setCinsi(alimDetayViewModel.getCinsi());
			alimDetayModel.setId(alimDetayViewModel.getId());
			alimDetayModel.setIskonto1(alimDetayViewModel.getIskonto1());
			alimDetayModel.setIskonto2(alimDetayViewModel.getIskonto2());
			alimDetayModel.setKatno(alimDetayViewModel.getKatno());
			alimDetayModel.setKdv(alimDetayViewModel.getKdv());
			alimDetayModel.setMaliyet(alimDetayViewModel.getMaliyet());
			alimDetayModel.setMarka(alimDetayViewModel.getMarka());
			alimDetayModel.setOemno(alimDetayViewModel.getOemno());
			alimDetayModel.setPrcno(alimDetayViewModel.getPrcno());
			alimDetayModel.setStno(alimDetayViewModel.getStno());
			alimDetayModel.setTipi(alimDetayViewModel.getTipi());
			alimDetayModel.setMarj(alimDetayViewModel.getMarj());
			if (alimDetayViewModel.getKurViewModel() == null)
				alimDetayModel.setKurModel(null);
			else {
				for (KurModel kurModel : kurModels) {
					if (kurModel.getKur().equals(alimDetayViewModel.getKurViewModel().getKur())) {
						alimDetayModel.setKurModel(kurModel);
						break;
					}
				}
			}
			BigDecimal katno = alimDetayModel.getKatno() == null ? BigDecimal.ZERO : alimDetayModel.getKatno();
			String stno = alimDetayModel.getStno() == null ? "" : alimDetayModel.getStno();
			if (katno.compareTo(BigDecimal.ZERO) != 0 && !stno.equals("")) {
				if (map.containsKey(katno + stno)) {
					tx.rollback();
					session.clear();
					session.close();
					throw new Exception(katno + "," + stno + " birden fazla kez içeremez !!!");
				} else
					map.put(katno + stno, 1);
			}
			alimDetayModel.setAlimModel(alimModel);
			alimModel.getAlimDetayModels().add(alimDetayModel);
		}
		if (alimModel.getAlimDetayModels().size() == 0) {
			tx.rollback();
			session.clear();
			session.close();
			throw new Exception("Alým hiç satýr içermiyor !!!");
		}

		alimModel = (AlimModel) session.merge(alimModel);

		tx.commit();
		session.clear();
		session.close();

		alimViewModel.setId(alimModel.getId());
	}
	private List<StokModel> stokModels = new ArrayList<StokModel>();

	public void transfer() throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;
		ExcelViewModel excelViewModel = alimViewModel.getExcelViewModel();
		if(excelViewModel == null)
			throw new Exception("Baðlý excel listesi yok !!!");
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		tx.begin();
		
		try {
			stokModels.clear();
			HSSFWorkbook workbook = transferToExcel(session);
			if(stokModels.size() == 0)
				throw new Exception("Deðiþen stok yok!!");
			for(StokModel stokModel : stokModels)
				session.merge(stokModel);
			FileOutputStream out = new FileOutputStream(excelViewModel.getExcelFileName()); 
			workbook.write(out);
			out.close();
			tx.commit();
		} catch(Exception e) {
			tx.rollback();
			session.clear();
			session.close();
			stokModels.clear();
			throw e;
		}
		stokModels.clear();
		session.clear();
		session.close();
		
	}
	
	private HSSFWorkbook transferToExcel(Session session) throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		ExcelViewModel excelViewModel = alimViewModel.getExcelViewModel();
		
		HashMap<String, AlimViewDetayModel> map = new HashMap<>();
		
		for(AlimViewDetayModel alimViewDetayModel : alimViewModel.getAlimViewDetayModels()) {
			BigDecimal katno = alimViewDetayModel.getKatno() == null ? BigDecimal.ZERO : alimViewDetayModel.getKatno();
			String stno = alimViewDetayModel.getStno() == null ? "" : alimViewDetayModel.getStno();
			if (katno.compareTo(BigDecimal.ZERO) != 0 && !stno.equals("")) {
				if (map.containsKey(katno + stno)) {
					map.clear();
					throw new Exception(katno + "," + stno + " birden fazla kez içeremez !!!");
				}
				map.put(katno + stno, alimViewDetayModel);
			}
		}
		if(map.size() == 0)
			throw new Exception("Aktarmak istenen liste hiç satýr içermiyor !!!");
		
		FileInputStream file = new FileInputStream(new File(excelViewModel.getExcelFileName()));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		
		for(ExcelViewDetayModel excelViewDetayModel : excelViewModel.getExcelViewDetayModels()) {
			HSSFSheet sheet = workbook.getSheet(excelViewDetayModel.getSheetName());
			
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Cell keyCell = row.getCell(excelViewDetayModel.getKeyColumn());
				if(keyCell == null)
					continue;
				if(keyCell.getCellType() != Cell.CELL_TYPE_STRING)
					continue;
				Integer katno = excelViewDetayModel.getKatno();
				String stno = keyCell.getStringCellValue();
				if(map.containsKey(katno + stno)) {
					AlimViewDetayModel o1 = map.get(katno + stno);
					if(o1.getListe() != null && o1.getListe().compareTo(BigDecimal.ZERO) != 0 && o1.getMaliyet().compareTo(BigDecimal.ZERO) != 0) {
						StokModel stokModel = (StokModel) session.get(StokModel.class,	new StokPK(o1.getKatno(), o1.getStno()));
						Cell valueCell = row.getCell(excelViewDetayModel.getFiyatColumn());
						
						if(valueCell == null) 
							valueCell = row.createCell(excelViewDetayModel.getFiyatColumn());
						
						if(valueCell.getCellType() != Cell.CELL_TYPE_NUMERIC)
							valueCell.setCellType(Cell.CELL_TYPE_NUMERIC);
					
						boolean dusukse = alimViewModel.getDusukseAktar() == null ? false : alimViewModel.getDusukseAktar();
						boolean yuksekse = alimViewModel.getYuksekseAktar() == null ? false : alimViewModel.getYuksekseAktar();
						
						if(valueCell.getNumericCellValue() < o1.getListe().doubleValue() && dusukse) {
							valueCell.setCellValue(o1.getListe().doubleValue());
							stokModel.setFiyat(o1.getListe());
							stokModels.add(stokModel);
						}
						
						if(valueCell.getNumericCellValue() > o1.getListe().doubleValue() && yuksekse) {
							valueCell.setCellValue(o1.getListe().doubleValue());
							stokModel.setFiyat(o1.getListe());
							stokModels.add(stokModel);
						}
					}
					map.remove(katno + stno);
				}
			}
		}		
		file.close();
		if(map.size() != 0) {
			String temp = "";
			for(Iterator<String> itr = map.keySet().iterator(); itr.hasNext(); ) {
				AlimViewDetayModel model = map.get(itr.next());
				temp+=model.getKatno()+","+model.getStno()+" ait kayýt listede bulunmadý! \n";
			}
			throw new Exception(temp);
		}
		return workbook;
	}

	public void loadStokModel(int index) throws Exception {
		AlimViewModel alimViewModel = (AlimViewModel) model;

		AlimViewDetayModel alimViewDetayModel = alimViewModel.getAlimViewDetayModels().get(index);

		Session session = HibernateUtil.getSessionFactory().openSession();

		StokModel stokModel = (StokModel) session.get(StokModel.class,
				new StokPK(alimViewDetayModel.getKatno(), alimViewDetayModel.getStno()));
		if (stokModel == null) {
			session.clear();
			session.close();
			throw new Exception(alimViewDetayModel.getKatno() + "," + alimViewDetayModel.getStno()
					+ " ait stok kartý bulunamadý!.");
		}

		alimViewDetayModel.setKatno(stokModel.getStokPK().getKatno());
		alimViewDetayModel.setCinsi(stokModel.getCinsi());
		alimViewDetayModel.setFiyat(stokModel.getFiyat());
		alimViewDetayModel.setKatno(stokModel.getStokPK().getKatno());
		alimViewDetayModel.setMarka(stokModel.getMarka());
		alimViewDetayModel.setNote(stokModel.getNote());
		alimViewDetayModel.setPrcno(stokModel.getPrcno());
		alimViewDetayModel.setStno(stokModel.getStokPK().getStno());
		alimViewDetayModel.setTipi(stokModel.getTipi());

		session.clear();
		session.close();

	}

}
