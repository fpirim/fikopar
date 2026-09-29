package tr.fikopar.swing.table;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/*
 *	Use a JTable as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the scrollpane that
 *  contains the main table.
 */
public class FRowHeader extends JTable implements ChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private FTable main;

	public FRowHeader(FTable table) {
		// setOpaque(false);
		main = table;
		main.addPropertyChangeListener(this);
		setBackground(UIManager.getColor("ScrollPane.background"));
		setOpaque(true);
		setFocusable(false);
		setAutoCreateColumnsFromModel(false);
		setModel(main.getModel());
		setRowSorter(main.getRowSorter());
		setSelectionModel(main.getSelectionModel());

		TableColumn column = new TableColumn();
		column.setHeaderValue(" ");
		addColumn(column);
		column.setCellRenderer(new FRowNumberRenderer());

		setRowHeight(main.getRowHeight());
		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
	}
	@Override
	public void sorterChanged(RowSorterEvent e) {
		super.sorterChanged(e);
		resizeAndRepaint();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();

		Component c = getParent();

		// Keep scrolling of the row table in sync with the main table.

		if (c instanceof JViewport) {
			JViewport viewport = (JViewport) c;
			viewport.addChangeListener(this);
		}
	}

	/*
	 * This table does not use any data from the main TableModel, so just return
	 * a value based on the row parameter.
	 */
	@Override
	public Object getValueAt(int row, int column) {
		RowSorter rowSorter = main.getRowSorter();
		if (rowSorter == null)
			return Integer.toString(row + 1);
		return rowSorter.convertRowIndexToModel(row) + 1;
	}

	@Override
	public int getRowCount() {
		RowSorter rowSorter = main.getRowSorter();
		if (rowSorter == null)
			return getModel().getRowCount();
		return rowSorter.getViewRowCount();
	}

	
	/*
	 * Don't edit data in the main TableModel by mistake
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	//
	// Implement the ChangeListener
	//
	public void stateChanged(ChangeEvent e) {
		// Keep the scrolling of the row table in sync with main table
		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane) viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
	}

	//
	// Implement the PropertyChangeListener
	//
	public void propertyChange(PropertyChangeEvent e) {
		// Keep the row table in sync with the main table

		if ("selectionModel".equals(e.getPropertyName())) {
			setSelectionModel(main.getSelectionModel());
		}

		if ("model".equals(e.getPropertyName())) {
			
			setModel(main.getModel());
		}
		if ("sorter".equals(e.getPropertyName()))
			setRowSorter((RowSorter<? extends TableModel>) e.getNewValue());
		
		
	}

	/*
	 * Borrow the renderer from JDK1.4.2 table header
	 */
	private static class FRowNumberRenderer extends DefaultTableCellRenderer {

		public FRowNumberRenderer() {
			setHorizontalAlignment(SwingConstants.CENTER);
			// setOpaque(false);
			// setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setBorder(BorderFactory.createEmptyBorder());

		}

		public void updateUI() {
			super.updateUI();
			// setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setBorder(BorderFactory.createEmptyBorder());
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JTableHeader h = (table != null ? table.getTableHeader() : null);

			if (h != null) {
				setEnabled(h.isEnabled());
				// setComponentOrientation(h.getComponentOrientation());

				if (isSelected) {
					setBackground(h.getForeground());
					setForeground(h.getBackground());
				} else {
					setForeground(h.getForeground());
					setBackground(h.getBackground());
				}
			} else {
				/*
				 * Use sensible values instead of random leftover values from
				 * the last call
				 */
				setEnabled(true);
				// setComponentOrientation(ComponentOrientation.UNKNOWN);

				if (isSelected) {
					setBackground(UIManager.getColor("TableHeader.foreground"));
					setForeground(UIManager.getColor("TableHeader.background"));
				} else {
					setForeground(UIManager.getColor("TableHeader.foreground"));
					setBackground(UIManager.getColor("TableHeader.background"));
				}
				setFont(UIManager.getFont("TableHeader.font"));
			}

			setValue(value);

			setFont(h.getFont());
			return this;
		}
	}
}
