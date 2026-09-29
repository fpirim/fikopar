package tr.fikopar.eksik.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import tr.fikopar.eksik.controller.EksikController;
import tr.fikopar.eksik.model.EksikKatalogModel;
import tr.fikopar.eksik.model.EksikStokModel;

public class EksikListTableModel extends AbstractTableModel {

	private class ListModel {

		private Boolean selected = true;
		private Integer katno;
		private String aciklama;
		private String hesapno;
		private BigDecimal minAdet = BigDecimal.ZERO;
		private BigDecimal maxAdet = BigDecimal.ZERO;
		private BigDecimal sipAdet = BigDecimal.ZERO;
		private BigDecimal verAdet = BigDecimal.ZERO;
		private BigDecimal minTutar = BigDecimal.ZERO;
		private BigDecimal maxTutar = BigDecimal.ZERO;
		private BigDecimal sipTutar = BigDecimal.ZERO;
		private BigDecimal verTutar = BigDecimal.ZERO;
		private Integer count = 0;
		
		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public BigDecimal getVerAdet() {
			return verAdet;
		}

		public void setVerAdet(BigDecimal verAdet) {
			this.verAdet = verAdet;
		}

		public BigDecimal getVerTutar() {
			return verTutar;
		}

		public void setVerTutar(BigDecimal verTutar) {
			this.verTutar = verTutar;
		}

		public Boolean getSelected() {
			return selected;
		}

		public void setSelected(Boolean selected) {
			this.selected = selected;
		}

		public BigDecimal getMinAdet() {
			return minAdet;
		}

		public void setMinAdet(BigDecimal minAdet) {
			this.minAdet = minAdet;
		}

		public BigDecimal getMaxAdet() {
			return maxAdet;
		}

		public void setMaxAdet(BigDecimal maxAdet) {
			this.maxAdet = maxAdet;
		}

		public BigDecimal getSipAdet() {
			return sipAdet;
		}

		public void setSipAdet(BigDecimal sipAdet) {
			this.sipAdet = sipAdet;
		}

		public Integer getKatno() {
			return katno;
		}

		public void setKatno(Integer katno) {
			this.katno = katno;
		}

		public String getAciklama() {
			return aciklama;
		}

		public void setAciklama(String aciklama) {
			this.aciklama = aciklama;
		}

		public String getHesapno() {
			return hesapno;
		}

		public void setHesapno(String hesapno) {
			this.hesapno = hesapno;
		}

		public BigDecimal getMinTutar() {
			return minTutar;
		}

		public void setMinTutar(BigDecimal minTutar) {
			this.minTutar = minTutar;
		}

		public BigDecimal getMaxTutar() {
			return maxTutar;
		}

		public void setMaxTutar(BigDecimal maxTutar) {
			this.maxTutar = maxTutar;
		}

		public BigDecimal getSipTutar() {
			return sipTutar;
		}

		public void setSipTutar(BigDecimal sipTutar) {
			this.sipTutar = sipTutar;
		}
		

	}

	private List<ListModel> list = new ArrayList<ListModel>();
	private EksikController eksikController;
	private String[] columnNames = new String[] { "Selected", "Kat No.",
			"Hesap No", "Açýklama", "Min Adet", "Max Adet", "Sip.Adet", "Ver.Adet",
			"Min Tutar", "Max Tutar", "Sipariþ Tutar", "Ver.Tutar","Stok Sayýsý"};

	public EksikListTableModel(EksikController eksikController) {
		this.eksikController = eksikController;
	}

	private ListModel findListModel(Integer katno) {
		for (Iterator<ListModel> itr = list.iterator(); itr.hasNext();) {
			ListModel listModel = itr.next();
			if (listModel.getKatno().equals(katno))
				return listModel;
		}
		ListModel listModel = new ListModel();
		listModel.setKatno(katno);
		list.add(listModel);
		return listModel;
	}

	private static BigDecimal HUNDRED = BigDecimal.TEN.multiply(BigDecimal.TEN);

	private void addKatalog(Integer katno, String hesapno, String aciklama,
			BigDecimal fiyat, BigDecimal isk, BigDecimal kdv,
			EksikStokModel eksikStokModel) {

		ListModel listModel = findListModel(katno);
		listModel.setHesapno(hesapno);
		listModel.setAciklama(aciklama);

		listModel.setMinTutar(listModel.getMinTutar().add(
				(eksikStokModel.getMinadet().compareTo(BigDecimal.ZERO) > 0
						&& eksikStokModel.getMinadet().compareTo(
								eksikStokModel.getAdet()) > 0 ? eksikStokModel
						.getMinadet().subtract(eksikStokModel.getAdet())
						: BigDecimal.ZERO).multiply(fiyat)
						.multiply(BigDecimal.ONE.subtract(isk.divide(HUNDRED)))
						.multiply(BigDecimal.ONE.add(kdv.divide(HUNDRED)))));
		listModel.setMaxTutar(listModel.getMaxTutar().add(
				(eksikStokModel.getMaxadet().compareTo(BigDecimal.ZERO) > 0
						&& eksikStokModel.getMaxadet().compareTo(
								eksikStokModel.getAdet()) > 0 ? eksikStokModel
						.getMaxadet().subtract(eksikStokModel.getAdet())
						: BigDecimal.ZERO).multiply(fiyat)
						.multiply(BigDecimal.ONE.subtract(isk.divide(HUNDRED)))
						.multiply(BigDecimal.ONE.add(kdv.divide(HUNDRED)))));

		listModel.setMinAdet(listModel.getMinAdet().add(
				(eksikStokModel.getMinadet().compareTo(BigDecimal.ZERO) > 0
						&& eksikStokModel.getMinadet().compareTo(
								eksikStokModel.getAdet()) > 0 ? eksikStokModel
						.getMinadet().subtract(eksikStokModel.getAdet())
						: BigDecimal.ZERO)));

		listModel.setMaxAdet(listModel.getMaxAdet().add(
				(eksikStokModel.getMaxadet().compareTo(BigDecimal.ZERO) > 0
						&& eksikStokModel.getMaxadet().compareTo(
								eksikStokModel.getAdet()) > 0 ? eksikStokModel
						.getMaxadet().subtract(eksikStokModel.getAdet())
						: BigDecimal.ZERO)));

		
		
		listModel.setSipAdet(listModel.getSipAdet().add(
				(eksikStokModel.getBakiye().compareTo(BigDecimal.ZERO) > 0
						&& eksikStokModel.getBakiye().compareTo(
								eksikStokModel.getAdet()) > 0 ? eksikStokModel
						.getBakiye().subtract(eksikStokModel.getAdet())
						: BigDecimal.ZERO)));

		listModel.setSipTutar(listModel.getSipTutar().add(
				(eksikStokModel.getBakiye().compareTo(BigDecimal.ZERO) > 0
						&& eksikStokModel.getBakiye().compareTo(
								eksikStokModel.getAdet()) > 0 ? eksikStokModel
						.getBakiye().subtract(eksikStokModel.getAdet())
						: BigDecimal.ZERO).multiply(fiyat)
						.multiply(BigDecimal.ONE.subtract(isk.divide(HUNDRED)))
						.multiply(BigDecimal.ONE.add(kdv.divide(HUNDRED)))));

		listModel.setVerAdet(listModel.getVerAdet().add(eksikStokModel.getVerbakiye()));
		listModel.setVerTutar(listModel.getVerTutar().add(
				eksikStokModel.getVerbakiye().multiply(fiyat)
				.multiply(BigDecimal.ONE.subtract(isk.divide(HUNDRED)))
				.multiply(BigDecimal.ONE.add(kdv.divide(HUNDRED)))));
		listModel.setCount(listModel.getCount()+1);

	}

	public void refresh() {
		list.clear();

		for (Iterator<EksikStokModel> itr = eksikController
				.getEksikStokModels().iterator(); itr.hasNext();) {
			EksikStokModel eksikStokModel = itr.next();
			if (eksikStokModel.getEksikKatalogModels().size() == 0) {
				addKatalog(0, "", "Katalogsuz", BigDecimal.ZERO,
						BigDecimal.ZERO, BigDecimal.ZERO, eksikStokModel);
			} else {
				for (Iterator<EksikKatalogModel> itr2 = eksikStokModel
						.getEksikKatalogModels().iterator(); itr2.hasNext();) {
					EksikKatalogModel eksikKatalogModel = itr2.next();
					addKatalog(eksikKatalogModel.getKatno(),
							eksikKatalogModel.getHesapno(),
							eksikKatalogModel.getAciklama(),
							eksikKatalogModel.getFiyat(),
							eksikKatalogModel.getIsk(),
							eksikKatalogModel.getKdv(), eksikStokModel);
				}
			}
		}

		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		if (list == null)
			return 0;
		return list.size();
	}

	public boolean contains(EksikStokModel eksikStokModel) {

		for (Iterator<ListModel> itr = list.iterator(); itr.hasNext();) {
			ListModel listModel = itr.next();
			if (eksikStokModel.getEksikKatalogModels().size() == 0
					&& listModel.getKatno() == 0 && listModel.getSelected())
				return true;
			for (Iterator<EksikKatalogModel> itr2 = eksikStokModel
					.getEksikKatalogModels().iterator(); itr2.hasNext();) {
				EksikKatalogModel eksikKatalogModel = itr2.next();
				if (eksikKatalogModel.getKatno().equals(listModel.getKatno())
						&& listModel.getSelected())
					return true;
			}
		}
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex != 0)
			return;
		ListModel listModel = list.get(rowIndex);
		listModel.setSelected((Boolean) aValue);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		ListModel listModel = list.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return listModel.getSelected();
		case 1:
			return listModel.getKatno();
		case 2:
			return listModel.getHesapno();
		case 3:
			return listModel.getAciklama();
		case 4:
			return listModel.getMinAdet();
		case 5:
			return listModel.getMaxAdet();
		case 6:
			return listModel.getSipAdet();
		case 7:
			return listModel.getVerAdet();
		case 8:
			return listModel.getMinTutar();
		case 9:
			return listModel.getMaxTutar();
		case 10:
			return listModel.getSipTutar();
		case 11:
			return listModel.getVerTutar();
		case 12:
			return "("+listModel.getCount()+")";
		}

		return null;
	}
	

}
