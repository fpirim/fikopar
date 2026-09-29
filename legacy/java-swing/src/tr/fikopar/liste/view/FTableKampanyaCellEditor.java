package tr.fikopar.liste.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;

import com.jidesoft.popup.JidePopup;
import com.jidesoft.swing.JideScrollPane;

import tr.fikopar.swing.db.models.FTableCellStyle;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCellEditor;
import tr.fikopar.swing.table.FTableEditorPanel;
import tr.fikopar.swing.table.FTableModel;

public class FTableKampanyaCellEditor extends FTableCellEditor {

	private JButton button;
	private JidePopup popup;
	private FTable table;

	private KampanyaTableModel kampanyaTableModel;

	public KampanyaTableModel getKampanyaTableModel() {
		return kampanyaTableModel;
	}

	public JidePopup getPopup() {
		return popup;
	}

	public FTableKampanyaCellEditor() {
		button = new JButton(new AbstractAction("...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				togglePopup();
			}
		});
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);

		table = new FTable(kampanyaTableModel = new KampanyaTableModel());

		popup = new JidePopup();
		popup.setResizable(true);
		popup.getContentPane().add(new JideScrollPane(table));
	}

	protected void togglePopup() {
		if (popup.isPopupVisible())
			popup.hidePopup();
		else {
			int width = kampanyaTableModel.getTotalColumnWidth() + +UIManager.getInt("ScrollBar.width") + 2;
			int height = (table.getRowHeight() + table.getRowMargin()) * 10 + table.getTableHeader().getHeight() + table.getRowMargin() * 2;
			popup.setPreferredPopupSize(new Dimension(width, height));
			popup.showPopup(button.getParent());
		}
	}

	protected class KampanyaTableModel extends FTableModel {

		public KampanyaTableModel() {
			fTableCellStyles = new FTableCellStyle[] { new FTableCellStyle(FTableCellStyle.DATE, "Tarih", Font.PLAIN, 80), new FTableCellStyle(FTableCellStyle.NUMERIC, "Kampanya", Font.BOLD, 100) };
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			Object[] obj = (Object[]) list.get(rowIndex);
			return obj[columnIndex];
		}

	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
		FTableEditorPanel editorPanel = new FTableEditorPanel();
		editorPanel.add(component, BorderLayout.CENTER);
		editorPanel.add(button, BorderLayout.EAST);
		return editorPanel;
	}

}
