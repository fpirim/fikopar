package tr.fikopar.modules.etiket.view;

import java.math.BigDecimal;

import tr.fikopar.modules.etiket.model.EtiketDetayModel;
import tr.fikopar.swing.table.FTableModel;

public class EtiketTableModel extends FTableModel {

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public int getRowCount() {
		if (list == null)
			return 0;
		return list.size() + 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		boolean inserted = false;
		if (rowIndex == list.size()) {
			list.add(new EtiketDetayModel());
			inserted = true;
		}

		EtiketDetayModel etiketDetayModel = (EtiketDetayModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			if (aValue instanceof Integer)
				etiketDetayModel.setKatno(new BigDecimal((Integer) aValue));
			else
				etiketDetayModel.setKatno(((BigDecimal) aValue));
			break;
		case 1:
			etiketDetayModel.setStno((String) aValue);
			break;
		case 2:
			etiketDetayModel.setPrcno((String) aValue);
			break;
		case 3:
			etiketDetayModel.setOemno((String) aValue);
			break;
		case 4:
			etiketDetayModel.setTipi((String) aValue);
			break;
		case 5:
			etiketDetayModel.setCinsi((String) aValue);
			break;
		case 6:
			etiketDetayModel.setMarka((String) aValue);
			break;
		case 7:
			etiketDetayModel.setPaketAdet((BigDecimal) aValue);
			break;
		case 8:
			etiketDetayModel.setEtiketAdet((BigDecimal) aValue);
			break;
		}
		if (inserted)
			fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		EtiketDetayModel etiketDetayModel = (EtiketDetayModel) list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return etiketDetayModel.getKatno();
		case 1:
			return etiketDetayModel.getStno();
		case 2:
			return etiketDetayModel.getPrcno();
		case 3:
			return etiketDetayModel.getOemno();
		case 4:
			return etiketDetayModel.getTipi();
		case 5:
			return etiketDetayModel.getCinsi();
		case 6:
			return etiketDetayModel.getMarka();
		case 7:
			return etiketDetayModel.getPaketAdet();
		case 8:
			return etiketDetayModel.getEtiketAdet();
		}
		return null;
	}

	/*
	 * 
	 * private SayimController sayimController; private String[] columnNames = {
	 * "Id", "Sayim Zamaný","Kat.No", "Stok No", "Parça No", "Oem no", "Tipi",
	 * "Parça Adý", "Marka", "Raf No", "Kdv%", "Adet", "Maliyet", "Tutar" };
	 * 
	 * public SayimTableModel(SayimController sayimController) {
	 * this.sayimController = sayimController; }
	 * 
	 * public boolean isCellEditable(int rowIndex, int columnIndex) { if
	 * ((columnIndex == 0) || (columnIndex == 13)) return false; return true; }
	 * 
	 * public String getColumnName(int column) { return
	 * this.columnNames[column]; }
	 * 
	 * public int getColumnCount() { return this.columnNames.length; }
	 * 
	 * public int getRowCount() { if (this.sayimController.getSayim() == null)
	 * return 0; return this.sayimController.getSayim().getSayimDetays().size()
	 * + 1; }
	 * 
	 * public void fireTableRowsInserted(int firstRow, int lastRow) {
	 * rowIndexesBy(firstRow, 1, '>'); super.fireTableRowsInserted(firstRow,
	 * lastRow); }
	 * 
	 * public void fireTableRowsDeleted(int firstRow, int lastRow) {
	 * rowIndexesBy(firstRow, -1, '>'); super.fireTableRowsDeleted(firstRow,
	 * lastRow); }
	 * 
	 * public void fireTableDataChanged() { this.duplicateMap.clear(); if
	 * ((this.sayimController.getSayim() != null) &&
	 * (this.sayimController.getSayim().getSayimDetays() != null)) { Iterator
	 * itr = this.sayimController.getSayim().getSayimDetays() .iterator(); int i
	 * = 0; while (itr.hasNext()) { SayimDetayModel sayimDetay =
	 * (SayimDetayModel) itr.next();
	 * 
	 * if (sayimDetay.getKatno()!=null && sayimDetay.getKatno()!=0 &&
	 * sayimDetay.getStno() != null && !sayimDetay.getStno().equals("")) {
	 * String key = sayimDetay.getKatno() + sayimDetay.getStno();
	 * Vector<Integer> vector; if (this.duplicateMap.containsKey(key)) vector =
	 * (Vector<Integer>) this.duplicateMap.get(key); else
	 * this.duplicateMap.put(key, vector = new Vector<Integer>()); if
	 * (!vector.contains(Integer.valueOf(i))) vector.add(Integer.valueOf(i)); }
	 * i++; } } super.fireTableDataChanged(); }
	 * 
	 * public SayimDetayModel getSayimDetayAt(int rowIndex) { SayimDetayModel
	 * sayimDetay = null; if (rowIndex >=
	 * this.sayimController.getSayim().getSayimDetays().size()) { sayimDetay =
	 * new SayimDetayModel();
	 * this.sayimController.getSayim().addSayimDetay(sayimDetay);
	 * fireTableRowsInserted(this.sayimController.getSayim()
	 * .getSayimDetays().size(), this.sayimController.getSayim()
	 * .getSayimDetays().size()); } else { sayimDetay = (SayimDetayModel)
	 * this.sayimController.getSayim() .getSayimDetays().get(rowIndex); } return
	 * sayimDetay; }
	 * 
	 * public void rowIndexesBy(int value, int by, char op) { Iterator<String>
	 * itr = this.duplicateMap.keySet().iterator(); while (itr.hasNext()) {
	 * Vector<Integer> vector =
	 * (Vector<Integer>)this.duplicateMap.get(itr.next()); for(int i=0;
	 * i<vector.size(); i++) { if ((op == '<') &&
	 * (((Integer)vector.get(i)).intValue() < value)) vector.set(i,
	 * Integer.valueOf(((Integer)vector.get(i)).intValue() + by)); else if ((op
	 * == '>') && (((Integer)vector.get(i)).intValue() >= value)) vector.set(i,
	 * Integer.valueOf(((Integer)vector.get(i)).intValue() + by)); } } }
	 * 
	 * private void removeMapValue(Integer katno, String stno, Integer rowIndex)
	 * { if(katno == null || katno == 0) return; if(stno == null ||
	 * stno.equals("")) return; String key = String.valueOf(katno) + stno; if
	 * (this.duplicateMap.containsKey(key)) { Vector vector = (Vector)
	 * this.duplicateMap.get(key); vector.removeElement(rowIndex); if
	 * (vector.size() == 0) this.duplicateMap.remove(key); updatededRows(key); }
	 * }
	 * 
	 * private void updatededRows(String key) { if ((key == null) ||
	 * (key.equals(""))) return; if (this.duplicateMap.containsKey(key)) {
	 * Iterator itr = ((Vector) this.duplicateMap.get(key)).iterator(); while
	 * (itr.hasNext()) { int i = ((Integer) itr.next()).intValue();
	 * fireTableRowsUpdated(i, i); } } }
	 * 
	 * private void addMapValue(Integer katno, String stno, Integer rowIndex) {
	 * if(katno == null || katno == 0) return; if(stno == null ||
	 * stno.equals("")) return; String key = String.valueOf(katno) + stno; if
	 * (this.duplicateMap.containsKey(key)) { ((Vector)
	 * this.duplicateMap.get(key)).add(rowIndex); updatededRows(key); } else {
	 * Vector vector = new Vector(); vector.add(rowIndex);
	 * this.duplicateMap.put(key, vector); } } private void
	 * loadLine(SayimDetayModel sayimDetay, int rowIndex) {
	 * if(sayimDetay.getStno()==null || sayimDetay.getStno().trim().equals(""))
	 * return; StokPopupModel katalogModel = sayimController.loadKatalogModel(
	 * sayimDetay.getKatno(), sayimDetay.getStno()); if(katalogModel == null)
	 * return; sayimDetay.setKatno(katalogModel.getKatno()==null ? 0 :
	 * katalogModel.getKatno()); sayimDetay.setStno(katalogModel.getStno()==null
	 * ? "" : katalogModel.getStno());
	 * sayimDetay.setPrcno(katalogModel.getPrcno()==null ? "" :
	 * katalogModel.getPrcno());
	 * sayimDetay.setOemno(katalogModel.getOemno()==null ? "" :
	 * katalogModel.getOemno()); sayimDetay.setTipi(katalogModel.getTipi()==null
	 * ? "" : katalogModel.getTipi());
	 * sayimDetay.setCinsi(katalogModel.getCinsi()==null ? "" :
	 * katalogModel.getCinsi());
	 * sayimDetay.setMarka(katalogModel.getMarka()==null ? "" :
	 * katalogModel.getMarka()); fireTableRowsUpdated(rowIndex, rowIndex);
	 * 
	 * 
	 * } public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	 * SayimDetayModel sayimDetay = getSayimDetayAt(rowIndex); switch
	 * (columnIndex) { case 1:
	 * sayimDetay.setSayimzamani(String.valueOf(aValue)); break; case 2: try {
	 * sayimDetay.setKatno(Integer.parseInt(String.valueOf(aValue)));
	 * loadLine(sayimDetay, rowIndex); } catch(Exception e) {
	 * sayimDetay.setKatno(0); } break; case 3:
	 * removeMapValue(sayimDetay.getKatno(), sayimDetay.getStno(),
	 * Integer.valueOf(rowIndex)); sayimDetay.setStno(String.valueOf(aValue));
	 * loadLine(sayimDetay, rowIndex); addMapValue(sayimDetay.getKatno(),
	 * sayimDetay.getStno(), Integer.valueOf(rowIndex)); break; case 4:
	 * sayimDetay.setPrcno(String.valueOf(aValue)); break; case 5:
	 * sayimDetay.setOemno(String.valueOf(aValue)); break; case 6:
	 * sayimDetay.setTipi(String.valueOf(aValue)); break; case 7:
	 * sayimDetay.setCinsi(String.valueOf(aValue)); break; case 8:
	 * sayimDetay.setMarka(String.valueOf(aValue)); break; case 9:
	 * sayimDetay.setRafno(String.valueOf(aValue)); break; case 10:
	 * sayimDetay.setKdv(new BigDecimal(String.valueOf(aValue))); break; case
	 * 11: sayimDetay.setAdet(new BigDecimal(String.valueOf(aValue))); break;
	 * case 12: sayimDetay.setMaliyet(new BigDecimal(String.valueOf(aValue)));
	 * break; } }
	 * 
	 * public void removeAt(int rowIndex) { SayimDetayModel sayimDetay =
	 * getSayimDetayAt(rowIndex);
	 * this.sayimController.getSayim().removeSayimDetay(sayimDetay);
	 * removeMapValue(sayimDetay.getKatno(),sayimDetay.getStno(),
	 * Integer.valueOf(rowIndex)); fireTableRowsDeleted(rowIndex, rowIndex); }
	 * 
	 * public Object getValueAt(int rowIndex, int columnIndex) { if (rowIndex >=
	 * this.sayimController.getSayim().getSayimDetays().size()) return null;
	 * SayimDetayModel sayimDetay = (SayimDetayModel)
	 * this.sayimController.getSayim() .getSayimDetays().get(rowIndex); switch
	 * (columnIndex) { case 0: return sayimDetay.getId(); case 1: return
	 * sayimDetay.getSayimzamani(); case 2: return sayimDetay.getKatno(); case
	 * 3: return sayimDetay.getStno(); case 4: return sayimDetay.getPrcno();
	 * case 5: return sayimDetay.getOemno(); case 6: return
	 * sayimDetay.getTipi(); case 7: return sayimDetay.getCinsi(); case 8:
	 * return sayimDetay.getMarka(); case 9: return sayimDetay.getRafno(); case
	 * 10: return sayimDetay.getKdv(); case 11: return sayimDetay.getAdet();
	 * case 12: return sayimDetay.getMaliyet(); case 13: return
	 * sayimDetay.getMaliyet() == null ? BigDecimal.ZERO :
	 * sayimDetay.getMaliyet().multiply( sayimDetay.getAdet() == null ?
	 * BigDecimal.ZERO : sayimDetay.getAdet()); } return null; }
	 * 
	 * private class DuplicateCellRenderer extends MyTableCellRenderer { private
	 * Color color = new Color(250, 88, 88);
	 * 
	 * private DuplicateCellRenderer() { }
	 * 
	 * public Component getTableCellRendererComponent(JTable table, Object
	 * value, boolean isSelected, boolean hasFocus, int row, int column) {
	 * Component comp = super.getTableCellRendererComponent(table, value,
	 * isSelected, hasFocus, row, column); if (isSelected) {
	 * comp.setBackground(UIManager .getColor("Table.selectionBackground")); }
	 * else { comp.setBackground(UIManager.getColor("Table.background")); }
	 * ((JLabel) comp).setToolTipText(""); String key =
	 * String.valueOf(SayimTableModel.this.getValueAt(row, 2)) +
	 * String.valueOf(SayimTableModel.this.getValueAt(row, 3)); if
	 * (SayimTableModel.this.duplicateMap .containsKey(key)) { Vector vector =
	 * (Vector) SayimTableModel.this.duplicateMap.get(key); if (vector.size() >
	 * 1) { if (!isSelected) comp.setBackground(this.color); Iterator itr =
	 * vector.iterator(); String temp = "["; while (itr.hasNext()) { temp = temp
	 * + (((Integer) itr.next()).intValue() + 1) + (itr.hasNext() ? " ," : "");
	 * } temp = temp + "]"; ((JLabel) comp).setToolTipText(temp); } }
	 * 
	 * return comp; } }
	 */
}