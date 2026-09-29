package tr.fikopar.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import tr.fikopar.swing.component.FTextField;

public class FTable extends JTable implements KeyListener {

	private FTableCellRenderer fTableCellRenderer = new FTableCellRenderer();
	private FTableCellEditor fTableCellEditor = new FTableCellEditor();
	private FTableSearchPanel fTableSearchPanel;
	private boolean searchPanelEnable = true;
	private boolean autoFilter = false;
	private FTableAutoFilter fTableAutoFilter;
	private FRowHeader fRowHeader;
	private boolean duplicateCellMarkerEnable = false;
	private int[] duplicateCellKeyColumns;
	private Object [][] duplicateCellExceptions;

	public FTable(FTableModel fTableModel) {
		super(fTableModel);

		tableHeader.setReorderingAllowed(false);
		setSurrendersFocusOnKeystroke(true);
		setRowSelectionAllowed(false);
		setCellSelectionEnabled(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		addKeyListener(this);

		fTableSearchPanel = new FTableSearchPanel(this);
		fTableSearchPanel.setVisible(false);
		setRowHeight(20);
		getColumnModel().addColumnModelListener(new TableColumnModelListener() {

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				if (getModel() != null) {
					for (int i = 0; i < getColumnCount(); i++)
						((FTableModel) getModel()).setColumnWidth(i, getColumnModel().getColumn(i).getWidth());
				}
			}

			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});

	}
	
	public boolean isDuplicateCellMarkerEnable() {
		return duplicateCellMarkerEnable;
	}
	public void setDuplicateCellKeyColumns(int [] duplicateCellKeyColumns, Object [][] duplicateCellExceptions) {
		this.duplicateCellKeyColumns = duplicateCellKeyColumns;
		this.duplicateCellExceptions = duplicateCellExceptions;
	}
	public void setDuplicateCellKeyColumns(int[] duplicateCellKeyColumns) {
		setDuplicateCellKeyColumns(duplicateCellKeyColumns, null);
	}

	public void setDuplicateCellMarkerEnable(boolean duplicateCellMarkerEnable) {
		indexSet.clear();
		keySet.clear();
		if(this.duplicateCellMarkerEnable = duplicateCellMarkerEnable) {
			for(int i=0; i<getRowCount(); i++)
				applyDuplicateMap(i, TableModelEvent.INSERT);
		}
	}

	private String createKeyCode(int index) {
		Object [] code = new Object[duplicateCellKeyColumns.length];
		
		for (int i = 0; i < duplicateCellKeyColumns.length; i++)
			code[i] = getValueAt(index, duplicateCellKeyColumns[i]);
		
		boolean flag = false;
		if(duplicateCellExceptions!=null) {
			for(Object [] o1 : duplicateCellExceptions) {
				boolean inFlag = true;   //exception da say
				for(int i=0; i<code.length; i++) {
					if (!(o1[i] == null ? code[i] == null : o1[i].equals(code[i]))) {
						inFlag = false;
						break;
					}
				}
				flag = inFlag;
				if(inFlag) 
					break; //exception da
			}
		}
		
		if(flag) return null;
		
		String temp = "";
		for(Object o : code)
			temp += String.valueOf(o);
		
		return temp;
	}

	public int[] getDuplicateRows(int index, int base) {
		String key = createKeyCode(index);
		if(key == null)
			return null;
		
		List<Integer> list = new ArrayList<>();
		
		String currentKey = keySet.ceiling(key);
		while (currentKey != null) {
			if (currentKey.startsWith(key)) {
				list.add(Integer.parseInt(currentKey.substring(currentKey.length() - 10))+base);
			} else
				break;
			currentKey = keySet.higher(currentKey);
		}
		int[] ret = new int[list.size()];
		Iterator<Integer> iter = list.iterator();
		for (int i = 0; iter.hasNext(); i++) {
			ret[i] = iter.next();
		}
		return ret;

	}

	TreeSet<String> indexSet = new TreeSet<>(); // indexSet --> index+key
	TreeSet<String> keySet = new TreeSet<>(); // keySet --> key+index

	private static final int ROWS_DOWN = -1;
	private static final int ROWS_UP = 1;

	private void shiftSets(int curIndex, int type) {

		String oldKey = indexSet.ceiling(String.format("%010d", curIndex));
		Stack<String> stack = new Stack<>();
		while (oldKey != null) {
			int index = Integer.parseInt(oldKey.substring(0, 10));
			String key = oldKey.substring(10);

			indexSet.remove(String.format("%010d%s", index, key));
			keySet.remove(String.format("%s%010d", key, index));

			stack.push(String.format("%010d%s", index + type, key));
			oldKey = indexSet.higher(oldKey);
		}
		String temp = null;
		while (!stack.isEmpty()) {
			temp = stack.pop();
			int index = Integer.parseInt(temp.substring(0, 10));
			String key = temp.substring(10);
			indexSet.add(String.format("%010d%s", index, key));
			keySet.add(String.format("%s%010d", key, index));
		}

	}

	private void applyDuplicateMap(int index, int type) {
		String key = createKeyCode(index);
		if (key != null && (type == TableModelEvent.DELETE || type == TableModelEvent.UPDATE)) {
			String oldKey = indexSet.ceiling(String.format("%010d", index));
			if (oldKey != null && Integer.parseInt(oldKey.substring(0, 10)) == index) {
				indexSet.remove(oldKey);
				keySet.remove(oldKey.substring(10) + oldKey.substring(0, 10));
				if (type == TableModelEvent.DELETE)
					shiftSets(index, ROWS_DOWN);
			}
		}
		if (key !=null && (type == TableModelEvent.INSERT || type == TableModelEvent.UPDATE)) {
			if (type == TableModelEvent.INSERT)
				shiftSets(index, ROWS_UP);
			indexSet.add(String.format("%010d%s", index, key));
			keySet.add(String.format("%s%010d", key, index));
		}

	}
	
	private class DuplicateEvent implements Runnable {
		int firstRow;
		int lastRow;
		int type;
		public DuplicateEvent(int firstRow, int lastRow, int type) {
			this.firstRow = firstRow;
			this.lastRow = lastRow == Integer.MAX_VALUE ? getRowCount() : lastRow;
			this.type = type;
			if(lastRow == Integer.MAX_VALUE) {
				keySet.clear();
				indexSet.clear();
			}
		}
		
		
		@Override
		public void run() {
			for (int i = this.firstRow; i <= this.lastRow; i++)
				applyDuplicateMap(i, this.type);		
		}
		
	}
	@Override
	public void tableChanged(TableModelEvent e) {
		if (duplicateCellMarkerEnable && duplicateCellKeyColumns != null)
			SwingUtilities.invokeLater(new DuplicateEvent(e.getFirstRow(), e.getLastRow(), e.getType()));
		super.tableChanged(e);
	}

	public JScrollPane getScrollPane() {
		Component p = getParent();
		while (p != null && !(p instanceof JScrollPane))
			p = p.getParent();
		return (JScrollPane) p;
	}

	public JPanel createPanelForFTable(boolean rowHeader, FTableModel footerTableModel) {
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());

		JPanel mainPanel = createPanelForFTable(rowHeader);
		JPanel footerPanel = FTableFooter.createColumnFooterPanel(this, footerTableModel);

		mainPanel.remove(getfTableSearchPanel());
		mainPanel.add(footerPanel, BorderLayout.SOUTH);

		containerPanel.add(mainPanel, BorderLayout.CENTER);
		containerPanel.add(getfTableSearchPanel(), BorderLayout.SOUTH);

		return containerPanel;
	}

	public JPanel createPanelForFTable(boolean rowHeader) {
		JPanel container = new JPanel();
		container.setBorder(BorderFactory.createLineBorder(getGridColor()));
		container.setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane(this);

		if (rowHeader) {
			fRowHeader = new FRowHeader(this);
			scrollPane.setRowHeaderView(fRowHeader);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, fRowHeader.getTableHeader());
		}
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		container.add(scrollPane, BorderLayout.CENTER);
		container.add(getfTableSearchPanel(), BorderLayout.SOUTH);
		return container;

	}

	public FTableSearchPanel getfTableSearchPanel() {
		return fTableSearchPanel;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_F && searchPanelEnable) {
			fTableSearchPanel.setVisible(true);
			fTableSearchPanel.requestFocus();
			e.consume();
		}

	}

	@Override
	public void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		if (fRowHeader != null)
			fRowHeader.setRowHeight(rowHeight);
	}

	@Override
	public void setRowHeight(int row, int rowHeight) {
		super.setRowHeight(row, rowHeight);
		if (fRowHeader != null)
			fRowHeader.setRowHeight(row, rowHeight);
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	public void setDefaultRenderer(FTableCellRenderer fTableCellRenderer) {
		this.fTableCellRenderer = fTableCellRenderer;
	}

	@Override
	public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
		return fTableCellRenderer;
	}

	@Override
	public TableCellEditor getDefaultEditor(Class<?> columnClass) {
		return fTableCellEditor;
	}

	@Override
	public void createDefaultColumnsFromModel() {
		super.createDefaultColumnsFromModel();

		for (int i = 0; i < this.getColumnCount(); i++) {
			int width = ((FTableModel) dataModel).getColumnWidth(i);
			if (width != -1)
				columnModel.getColumn(i).setPreferredWidth(width);
		}
	}

	public void setSearchPanelEnable(boolean searchPanelEnable) {
		this.searchPanelEnable = searchPanelEnable;
	}

	public boolean isSearchPanelEnable() {
		return searchPanelEnable;
	}

	@Override
	public void setRowSorter(RowSorter<? extends TableModel> sorter) {
		if (getRowSorter() != null)
			getRowSorter().removeRowSorterListener((FTableModel) getModel());
		super.setRowSorter(sorter);
		if (sorter != null)
			sorter.addRowSorterListener((FTableModel) getModel());
	}
	
	public FTableAutoFilter getfTableAutoFilter() {
		return fTableAutoFilter;
	}

	public void setAutoFilter(boolean autoFilter) {
		this.autoFilter = autoFilter;
		if (this.autoFilter)
			fTableAutoFilter = new FTableAutoFilter(this);
		else {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setHeaderRenderer(getTableHeader().getDefaultRenderer());
			}
			getTableHeader().removeMouseListener(fTableAutoFilter);
			getTableHeader().removeMouseMotionListener(fTableAutoFilter);
			setRowSorter(null);
			fTableAutoFilter.removeFilters();
			fTableAutoFilter = null;
		}
		getTableHeader().revalidate();

	}

	public void addFilter(FTextField textField) {
		if (fTableAutoFilter != null)
			fTableAutoFilter.addFilter(textField);
	}

}
