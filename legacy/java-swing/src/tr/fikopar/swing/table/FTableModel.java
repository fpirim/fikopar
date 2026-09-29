package tr.fikopar.swing.table;

import java.util.List;

import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;

import tr.fikopar.swing.db.models.FTableCellStyle;
import tr.fikopar.swing.view.FApp;

public abstract class FTableModel extends AbstractTableModel implements RowSorterListener {

	protected List<FTableCellStyle> fTableCellStyles;
	@SuppressWarnings("rawtypes")
	protected List list;
	
	public FTableModel() {
		fTableCellStyles = FApp.getProperties().getTableStyleMap().get(this.getClass().getName()).getfTableCellStyles();
	}
	
	public int getColumnWidth(int columnIndex) {
		if (fTableCellStyles == null)
			return -1;
		FTableCellStyle fTableCellStyle = getFTableCellStyle(columnIndex);
		return fTableCellStyle.getWidth();
		
	}
	public void setColumnWidth(int columnIndex, int width) {
		fTableCellStyles.get(columnIndex).setWidth(width);
		FApp.getProperties().getTableStyleMap().get(this.getClass().getName()).getfTableCellStyles().get(columnIndex).setWidth(width);
	}
	
	
	
	public int getTotalColumnWidth() {
		if(fTableCellStyles == null)
			return 0;
		int total = 0;
		for(FTableCellStyle fTableCellStyle : fTableCellStyles)
			total += fTableCellStyle.getWidth();
		return total;
	}
	@Override
	public String getColumnName(int column) {
		FTableCellStyle fTableCellStyle = getFTableCellStyle(column);
		return fTableCellStyle.getTitle();
	}

	@SuppressWarnings("rawtypes")
	public List getList() {
		return list;
	}

	@SuppressWarnings("rawtypes")
	public void setList(List list) {
		if (this.list != null)
			this.list.clear();
		this.list = list;
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		if (fTableCellStyles == null)
			return 0;
		return fTableCellStyles.size();
	}

	@SuppressWarnings("unchecked")
	public void addObject(Object obj) {
		list.add(obj);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
	}

	public Object getObjectAt(int index) {
		if (list == null || index >= list.size())
			return null;
		return list.get(index);
	}

	public void removeObject(Object obj) {
		int index = list.indexOf(obj);
		if (index != -1) {
			list.remove(index);
			fireTableRowsDeleted(index, index);
		}
	}

	@Override
	public int getRowCount() {
		if(list == null)
			return 0;
		return list.size();
	}

	public FTableCellStyle getFTableCellStyle(int columnIndex) {
		if (fTableCellStyles == null)
			return null;
		return fTableCellStyles.get(columnIndex);
	}

	public void removeAll() {
		if(list!=null && list.size()!=0) {
			int size = list.size();
			list.clear();
			fireTableRowsDeleted(0, size-1);
		}
		
	}
	@Override
	public void sorterChanged(RowSorterEvent e) {
	}

}
