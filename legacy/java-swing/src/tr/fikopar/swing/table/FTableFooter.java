package tr.fikopar.swing.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;

public class FTableFooter {

	private static JPanel createFooterPanel(FTable mainTable, FTable footerTable) {

		JScrollPane footerScrollPane = configureScrollPanesFooterTable(mainTable, footerTable);

		JPanel westPanel = createWestPanel(mainTable);
		JPanel eastPanel = createEastPanel(mainTable);

		// create centerPanel
		JScrollBar footerScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(footerScrollPane, BorderLayout.CENTER);
		centerPanel.add(footerScrollBar, BorderLayout.SOUTH);

		// create footer panel
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.add(westPanel, BorderLayout.WEST);
		footerPanel.add(centerPanel, BorderLayout.CENTER);
		footerPanel.add(eastPanel, BorderLayout.EAST);
		footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, mainTable.getGridColor()));

		syncScrolling(mainTable.getScrollPane(), footerScrollBar, footerScrollPane, footerPanel);

		return footerPanel;
	}

	private static JPanel createWestPanel(FTable table) {

		FRowHeader fRowHeader = (FRowHeader) table.getScrollPane().getRowHeader().getView();

		JPanel westPanel = new JPanel();
		westPanel.setMinimumSize(new Dimension(fRowHeader.getPreferredScrollableViewportSize()));
		westPanel.setPreferredSize(new Dimension(fRowHeader.getPreferredScrollableViewportSize()));
		westPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, table.getGridColor()));
		return westPanel;

	}

	private static JPanel createEastPanel(FTable table) {

		FRowHeader fRowHeader = (FRowHeader) table.getScrollPane().getRowHeader().getView();

		JPanel eastPanel = new JPanel();
		eastPanel.setMinimumSize(new Dimension(fRowHeader.getPreferredScrollableViewportSize()));
		eastPanel.setPreferredSize(new Dimension(table.getScrollPane().getVerticalScrollBar().getPreferredSize().width, fRowHeader.getPreferredScrollableViewportSize().height));
		eastPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, table.getGridColor()));
		return eastPanel;

	}

	private static JScrollPane configureScrollPanesFooterTable(FTable table, FTable footerTable) {
		JScrollPane footerScrollPane = new JScrollPane(footerTable);

		footerScrollPane.setBorder(BorderFactory.createEmptyBorder());
		footerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		footerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		Dimension footerSize = footerTable.getPreferredScrollableViewportSize();
		footerSize.height = footerTable.getPreferredSize().height;
		footerTable.setPreferredScrollableViewportSize(footerSize);

		JScrollPane mainScrollPane = table.getScrollPane();
		mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		SyncColumnWidths syncColumnWidths = new SyncColumnWidths(table, footerTable);
		table.getColumnModel().addColumnModelListener(syncColumnWidths);

		return footerScrollPane;

	}

	public static JPanel createColumnFooterPanel(FTable mainTable, FTableModel footerTablemodel) {

		FTable footerTable = new FTable(footerTablemodel);

		footerTable.setRowSelectionAllowed(false);
		footerTable.setCellSelectionEnabled(false);
		footerTable.setFocusable(false);

		footerTable.getTableHeader().setReorderingAllowed(false);
		footerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		footerTable.setTableHeader(null);

		JPanel footerPanel = createFooterPanel(mainTable, footerTable);

		return footerPanel;

	}

	private static void syncScrolling(final JScrollPane centerScrollPane, final JScrollBar footerScrollBar, final JScrollPane footerScrollPane, final JPanel footerPanel) {

		footerScrollPane.getViewport().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JViewport viewport = footerScrollPane.getViewport();
				int extentWidth = viewport.getExtentSize().width;
				int viewWidth = viewport.getViewSize().width;
				int value = Math.max(0, Math.min(viewport.getViewPosition().x, viewWidth - extentWidth));
				footerScrollBar.setValues(value, extentWidth, 0, viewWidth);
				centerScrollPane.getViewport().setViewPosition(viewport.getViewPosition());
			}
		});
		centerScrollPane.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				//int x = centerScrollPane.getViewport().getLocation().x * -1;
				JViewport viewport = centerScrollPane.getViewport();
				int extentWidth = viewport.getExtentSize().width;
				int viewWidth = viewport.getViewSize().width;
				int value = Math.max(0, Math.min(viewport.getViewPosition().x, viewWidth - extentWidth));
				footerScrollBar.setValues(value, extentWidth, 0, viewWidth);
				footerScrollBar.setVisible(extentWidth <= viewWidth);
				footerPanel.getComponent(2).setVisible(centerScrollPane.getVerticalScrollBar().isVisible());
				footerPanel.revalidate();
			}
		});

		// When the footer scroll bar (horizontal scroll bar) value changes,
		// change the position of the view in the center scroll pane to match.
		footerScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				JViewport viewport = centerScrollPane.getViewport();
				viewport.setViewPosition(new Point(e.getValue(), viewport.getViewPosition().y));
				viewport = footerScrollPane.getViewport();
				viewport.setViewPosition(new Point(e.getValue(), viewport.getViewPosition().y));
			}
		});
	}

	private static class SyncColumnWidths implements TableColumnModelListener {

		private final FTable centerTable;
		private final FTable footerTable;

		public SyncColumnWidths(FTable centerTable, FTable footerTable) {
			this.centerTable = centerTable;
			this.footerTable = footerTable;
		}

		// Change the column widths of the footer table when the column widths
		// of the center table change.
		public void columnMarginChanged(ChangeEvent e) {
			TableColumnModel centerTableColumnModel = centerTable.getColumnModel();
			TableColumnModel footerTableColumnModel = footerTable.getColumnModel();
			for (int i = 0; i < centerTableColumnModel.getColumnCount(); i++) {
				int width = centerTableColumnModel.getColumn(i).getPreferredWidth();
				footerTableColumnModel.getColumn(i).setPreferredWidth(width);
			}
		}

		public void columnSelectionChanged(ListSelectionEvent e) {
		}

		public void columnAdded(TableColumnModelEvent e) {
		}

		public void columnMoved(TableColumnModelEvent e) {
		}

		public void columnRemoved(TableColumnModelEvent e) {
		}
	}

}
