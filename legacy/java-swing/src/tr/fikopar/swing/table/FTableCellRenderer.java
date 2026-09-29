package tr.fikopar.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import tr.fikopar.swing.db.adapter.FTableCellAdapter;
import tr.fikopar.swing.db.models.FTableCellStyle;

public class FTableCellRenderer extends DefaultTableCellRenderer {

	private Color color = new Color(250, 88, 88);

	public FTableCellRenderer() {
	}

	private void convertNumericTypeComponent(Component comp, Object value, FTableCellStyle fTableCellStyle) {
		BigDecimal tempValue = BigDecimal.ZERO;
		if (value instanceof Integer)
			tempValue = new BigDecimal((Integer) value);
		else if (value instanceof BigDecimal)
			tempValue = (BigDecimal) value;

		JLabel label = (JLabel) comp;
		label.setHorizontalAlignment(JLabel.RIGHT);
		if (tempValue.compareTo(BigDecimal.ZERO) == 0)
			label.setText("");
		else
			label.setText(String.valueOf(FTableCellAdapter.applyFormat(tempValue, fTableCellStyle)));

	}

	private FTableCheckBox convertBooleanTypeComponent(Object value) {
		FTableCheckBox checkBox = new FTableCheckBox();
		checkBox.setSelected((Boolean) value);
		return checkBox;

	}

	private JLabel convertTextTypeComponent(Component comp, Object value) {
		JLabel label = (JLabel) comp;
		label.setHorizontalAlignment(JLabel.LEFT);
		label.setText(value == null ? "" : String.valueOf(value));
		return label;
	}

	private void convertDateTypeComponent(Component comp, Object value, FTableCellStyle fTableCellStyle) {
		JLabel label = (JLabel) comp;
		label.setHorizontalAlignment(JLabel.CENTER);
		try {
			if (value == null)
				label.setText("");
			else
				label.setText(FTableCellAdapter.maskDate(String.valueOf(value)));
		} catch (Exception e) {
			label.setText(String.valueOf(value));
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		FTable fTable = (FTable) table;
		FTableCellStyle fTableCellStyle = ((FTableModel) fTable.getModel()).getFTableCellStyle(column);
		switch (fTableCellStyle.getType()) {
		case FTableCellStyle.NUMERIC:
			convertNumericTypeComponent(comp, value, fTableCellStyle);
			break;
		case FTableCellStyle.DATE:
			convertDateTypeComponent(comp, value, fTableCellStyle);
			break;
		case FTableCellStyle.BOOLEAN:
			if (value != null) {
				comp = convertBooleanTypeComponent(value);
				break;
			}
		default:
			convertTextTypeComponent(comp, value);
			break;
		}

		comp.setFont(comp.getFont().deriveFont(fTableCellStyle.getFontStyle()));

		if (fTableCellStyle.getBackgroundColor() != null)
			comp.setBackground(FTableCellAdapter.stringToColor(fTableCellStyle.getBackgroundColor()));
		else {
			if (isSelected)
				comp.setBackground(table.getSelectionBackground());
			else
				comp.setBackground(table.getBackground());
		}

		if (fTableCellStyle.getForegroundColor() != null)
			comp.setForeground(FTableCellAdapter.stringToColor(fTableCellStyle.getForegroundColor()));
		else {
			if (isSelected)
				comp.setForeground(table.getSelectionForeground());
			else
				comp.setForeground(table.getForeground());
		}

		((JComponent) comp).setToolTipText("");

		if (fTable.isDuplicateCellMarkerEnable()) {
			int[] duplicateRows = fTable.getDuplicateRows(row, 1);
			if (duplicateRows != null && duplicateRows.length > 1) {
				if (!isSelected)
					comp.setBackground(this.color);
				((JComponent) comp).setToolTipText(Arrays.toString(duplicateRows) + " satýrlar ayný kodlara sahip!");
			} else
				((JComponent) comp).setToolTipText("");

		} else
			((JComponent) comp).setToolTipText("");
		return comp;

	}
}
