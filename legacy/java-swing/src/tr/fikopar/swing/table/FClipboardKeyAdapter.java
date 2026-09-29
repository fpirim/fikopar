package tr.fikopar.swing.table;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import tr.fikopar.swing.db.adapter.FTableCellAdapter;
import tr.fikopar.swing.db.models.FTableCellStyle;

public class FClipboardKeyAdapter extends KeyAdapter {

	private static final String LINE_BREAK = "\n";
	private static final String CELL_BREAK = "\t";
	private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

	private static FClipboardKeyAdapter instance = new FClipboardKeyAdapter();

	public static void install(FTable table) {
		table.addKeyListener(instance);
	}

	public static void uninstall(FTable table) {
		table.removeKeyListener(instance);
	}

	@Override
	public void keyReleased(KeyEvent event) {
		if ((event.isControlDown() && event.getKeyCode() == KeyEvent.VK_C)
				|| (event.isControlDown() && event.getKeyCode() == KeyEvent.VK_INSERT)) { // Copy
			cancelEditing((FTable) event.getSource());
			copyToClipboard((FTable) event.getSource(), false);
		} else if ((event.isControlDown() && event.getKeyCode() == KeyEvent.VK_X)
				|| (event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_DELETE)) { // Cut
			cancelEditing((FTable) event.getSource());
			copyToClipboard((FTable) event.getSource(), true);
		} else if ((event.isControlDown() && event.getKeyCode() == KeyEvent.VK_V)
				|| (event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_INSERT)) { // Paste
			cancelEditing((FTable) event.getSource());
			pasteFromClipboard((FTable) event.getSource());
		}
	}

	private void copyToClipboard(FTable table, boolean isCut) {
		int numCols = table.getSelectedColumnCount();
		int numRows = table.getSelectedRowCount();
		int[] rowsSelected = table.getSelectedRows();
		int[] colsSelected = table.getSelectedColumns();
		if (numRows != rowsSelected[rowsSelected.length - 1] - rowsSelected[0] + 1 || numRows != rowsSelected.length
				|| numCols != colsSelected[colsSelected.length - 1] - colsSelected[0] + 1
				|| numCols != colsSelected.length) {

			JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		StringBuffer excelStr = new StringBuffer();
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				FTableCellStyle style = ((FTableModel) table.getModel()).getFTableCellStyle(colsSelected[j]);
				Object value = table.getValueAt(rowsSelected[i], colsSelected[j]);
				if (style.getType() == FTableCellStyle.NUMERIC)
					value = FTableCellAdapter.applyFormat(table.getValueAt(rowsSelected[i], colsSelected[j]), style);
				excelStr.append(escape(value));
				if (isCut) {
					table.setValueAt(null, rowsSelected[i], colsSelected[j]);
				}
				if (j < numCols - 1) {
					excelStr.append(CELL_BREAK);
				}
			}
			excelStr.append(LINE_BREAK);
		}

		StringSelection sel = new StringSelection(excelStr.toString());
		CLIPBOARD.setContents(sel, sel);
	}

	private void pasteFromClipboard(FTable table) {
		int startRow = table.getSelectedRows()[0];
		int startCol = table.getSelectedColumns()[0];

		String pasteString = "";
		try {
			pasteString = (String) (CLIPBOARD.getContents(this).getTransferData(DataFlavor.stringFlavor));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Invalid Paste Type", "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String[] lines = pasteString.split(LINE_BREAK);
		for (int i = 0; i < lines.length; i++) {
			String[] cells = lines[i].split(CELL_BREAK);
			for (int j = 0; j < cells.length; j++) {
				if (table.getRowCount() > startRow + i && table.getColumnCount() > startCol + j) {
					FTableCellStyle style = ((FTableModel) table.getModel()).getFTableCellStyle(startCol + j);
					Object value = null;
					if (style.getType() == FTableCellStyle.NUMERIC) {
						value = FTableCellAdapter.applyParse(cells[j], style);
					} else
						value = cells[j];
					table.setValueAt(value, startRow + i, startCol + j);
				}
			}
		}
	}

	private void cancelEditing(FTable table) {
		if (table.getCellEditor() != null) {
			table.getCellEditor().cancelCellEditing();
		}
	}

	private String escape(Object cell) {
		return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
	}
}