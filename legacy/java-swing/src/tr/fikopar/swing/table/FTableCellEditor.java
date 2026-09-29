package tr.fikopar.swing.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

import tr.fikopar.swing.component.FComboBox;
import tr.fikopar.swing.component.FDateField;
import tr.fikopar.swing.component.FNumericField;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.db.adapter.FTableCellAdapter;
import tr.fikopar.swing.db.models.FTableCellStyle;

public class FTableCellEditor extends AbstractCellEditor implements TableCellEditor {

	private JComponent component;
	private boolean autoCreateComponent;

	public FTableCellEditor() {
		autoCreateComponent = true;
	}

	public FTableCellEditor(JComponent component) {
		this.component = component;
		autoCreateComponent = false;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getCellEditorValue() {

		if (component instanceof FNumericField)
			return ((FNumericField) component).getBigDecimal();
		else if (component instanceof FTableCheckBox)
			return ((FTableCheckBox) component).isSelected();
		else if (component instanceof FDateField)
			return ((FDateField) component).getText();
		else if (component instanceof FComboBox)
			return ((FComboBox) component).getSelectedItem();
		
		return ((JTextField) component).getText();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		FTable fTable = (FTable) table;
		FTableCellStyle fTableCellStyle = ((FTableModel) fTable.getModel()).getFTableCellStyle(column);

		switch (fTableCellStyle.getType()) {
		case FTableCellStyle.NUMERIC:
			if (autoCreateComponent)
				component = new FNumericField(fTableCellStyle.getFormat());
			if(component instanceof FNumericField) {
				((FNumericField) component).setText(value == null ? "0" : value.toString());
				((FNumericField) component).selectAll();
			}
			break;
		case FTableCellStyle.DATE:
			if (autoCreateComponent)
				component = new FDateField();
			if(component instanceof FDateField)
				((FDateField) component).setText(value == null ? null : String.valueOf(value));
			break;
		case FTableCellStyle.BOOLEAN:
			if (autoCreateComponent)
				component = new FTableCheckBox();
			if(component instanceof FTableCheckBox)
				((FTableCheckBox) component).setSelected(value == null ? false : (Boolean) value);
			break;
		default:
			if (autoCreateComponent)
				component = new FTextField();
			if(component instanceof FTextField) {
				((FTextField) component).setText(value == null ? "" : value.toString());
				((FTextField) component).selectAll();
			}
			break;
		}

		component.setBorder(BorderFactory.createEmptyBorder());
		component.setOpaque(false);

		if (component instanceof FTableCheckBox) {
			component.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "spacePressed");
			component.getActionMap().put("spacePressed", new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					FTableCheckBox checkBox = (FTableCheckBox) FTableCellEditor.this.component;
					checkBox.setSelected(!checkBox.isSelected());
					stopCellEditing();
				}
			});
			component.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					fireEditingStopped();
					System.out.println(e.toString());
				}
			});
		}
		if (fTableCellStyle.getBackgroundColor() != null)
			component.setBackground(FTableCellAdapter.stringToColor(fTableCellStyle.getBackgroundColor()));
		else {
			if (isSelected)
				component.setBackground(table.getSelectionBackground());
			else
				component.setBackground(table.getBackground());
		}

		if (fTableCellStyle.getForegroundColor() != null)
			component.setForeground(FTableCellAdapter.stringToColor(fTableCellStyle.getForegroundColor()));
		else {
			if (isSelected)
				component.setForeground(table.getSelectionForeground());
			else
				component.setForeground(table.getForeground());
		}

		return component;

	}

	@Override
	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent) {
			FTable table = (FTable) e.getSource();
			int colIndex = table.columnAtPoint(((MouseEvent) e).getPoint());
			if (((FTableModel) table.getModel()).getFTableCellStyle(colIndex).getType() == FTableCellStyle.BOOLEAN)
				return ((MouseEvent) e).getClickCount() >= 1;
			return ((MouseEvent) e).getClickCount() >= 2;
		}
		return true;

	}
}