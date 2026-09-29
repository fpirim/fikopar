package tr.fikopar.modules.etiket.view;

import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import tr.fikopar.app.MyApp;
import tr.fikopar.modules.contentasist.stokmodel.StokModelContentAsist;
import tr.fikopar.modules.etiket.controller.EtiketController;
import tr.fikopar.modules.etiket.model.EtiketViewModel;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FClipboardKeyAdapter;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCellEditor;
import tr.fikopar.swing.view.FView;

public class EtiketView extends FView {
	private EtiketTableModel etiketTableModel;
	private FTable table;
	private FTextField txtCinsi, txtMarka, txtOemno, txtParcano;

	@Override
	public void initComponents() {
		etiketTableModel = new EtiketTableModel();
		table = new FTable(etiketTableModel);

		txtCinsi = new FTextField();
		table.getColumnModel().getColumn(5).setCellEditor(new FTableCellEditor(txtCinsi));
		txtMarka = new FTextField();
		table.getColumnModel().getColumn(6).setCellEditor(new FTableCellEditor(txtMarka));
		txtOemno = new FTextField();
		table.getColumnModel().getColumn(3).setCellEditor(new FTableCellEditor(txtOemno));
		txtParcano = new FTextField();
		table.getColumnModel().getColumn(2).setCellEditor(new FTableCellEditor(txtParcano));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JPanel tablePanel = table.createPanelForFTable(true);
		GridBagConstraints gbc_tablePane = new GridBagConstraints();
		gbc_tablePane.insets = new Insets(5, 5, 5, 5);
		gbc_tablePane.fill = GridBagConstraints.BOTH;
		gbc_tablePane.gridx = 0;
		gbc_tablePane.gridy = 0;
		add(tablePanel, gbc_tablePane);
	}

	@Override
	public void initActions() {
		FClipboardKeyAdapter.install(table);

		/*
		 * Yazdýr Button
		 */

		registerAction(new AbstractAction("Yazdýr", UIManager.getIcon("yazdirIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				((EtiketController) controller).printLabels();

			}
		}, "CTRL+P Yazdýr", KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "actYazdir");

		/*
		 * Satýr Sil
		 */

		registerAction(new AbstractAction("Satýr Sil", UIManager.getIcon("satirsilIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int n = JOptionPane.showConfirmDialog(MyApp.getAppHandler(), "Satýr silinecek emin misiniz?",
						"Satýr Sil", JOptionPane.YES_NO_OPTION);

				if (n == JOptionPane.YES_OPTION) {

					final int[] cols = table.getSelectedColumns();
					final int[] rows = table.getSelectedRows();
					table.clearSelection();
					for (int i = 0; i < rows.length; i++) {
						if (rows[i] - i != table.getRowCount() - 1) {
							etiketTableModel.getList().remove(rows[i] - i);
							etiketTableModel.fireTableRowsDeleted(rows[i] - i, rows[i] - i);
						}
					}
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							table.editingCanceled(new ChangeEvent(table));
							for (int i = 0; i < rows.length; i++)
								if (rows[i] - i < table.getRowCount())
									table.getSelectionModel().addSelectionInterval(rows[i], rows[i]);
							for (int i = 0; i < cols.length; i++)
								if (cols[i] < table.getColumnCount())
									table.getColumnModel().getSelectionModel().addSelectionInterval(cols[i], cols[i]);
						}
					});
				}
			}
		}, "ALT+DELETE Satýr Sil", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.ALT_MASK), "actSatirSil");

		/*
		 * Satýr Ekle
		 */

		registerAction(new AbstractAction("Satýr Ekle", UIManager.getIcon("satirekleIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] cols = table.getSelectedColumns();
				final int[] rows = table.getSelectedRows();
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					etiketTableModel.getList().add(rows[i], new EtiketViewModel());
					etiketTableModel.fireTableRowsInserted(rows[i], rows[i]);
				}
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						table.editingCanceled(new ChangeEvent(table));
						for (int i = 0; i < rows.length; i++)
							table.getSelectionModel().addSelectionInterval(rows[i], rows[i]);
						for (int i = 0; i < cols.length; i++)
							if (cols[i] < table.getColumnCount())
								table.getColumnModel().getSelectionModel().addSelectionInterval(cols[i], cols[i]);
					}
				});
			}
		}, "ALT+INSERT Satýr Ekle", KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, Event.ALT_MASK), "actSatirEkle");

		etiketTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				int lastRow = e.getLastRow() == Integer.MAX_VALUE ? table.getRowCount() : e.getLastRow();
				for (int r = e.getFirstRow(); r <= lastRow; r++) {
					BigDecimal katno = (BigDecimal) etiketTableModel.getValueAt(r, 0);
					String stno = (String) etiketTableModel.getValueAt(r, 1);

					if (e.getColumn() == 1 || e.getColumn() == 2) {
						if (katno != null && stno != null && !"".equals(stno) && katno.compareTo(BigDecimal.ZERO) != 0) {
							loadStokModel(r, katno, stno);
						}
					}
				}
			}
		});

	}

	private PropertyChangeListener stokListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("contentSelected".equals(evt.getPropertyName())) {
				table.getCellEditor().cancelCellEditing();
				Object[] obj = (Object[]) evt.getNewValue();
				etiketTableModel.setValueAt(obj[0], table.getSelectedRow(), 0);
				etiketTableModel.setValueAt(obj[1], table.getSelectedRow(), 1);
			}

		}
	};

	@Override
	public void initBindings() {
		EtiketViewModel etiketModel = (EtiketViewModel) controller.getModel();
		etiketTableModel.setList(etiketModel.getEtiketDetayModels());

		txtCinsi.addPropertyChangeListener(stokListener);
		txtMarka.addPropertyChangeListener(stokListener);
		txtOemno.addPropertyChangeListener(stokListener);
		txtParcano.addPropertyChangeListener(stokListener);

		StokModelContentAsist.addFTextFieldContentListener(txtCinsi);
		StokModelContentAsist.addFTextFieldContentListener(txtMarka);
		StokModelContentAsist.addFTextFieldContentListener(txtOemno);
		StokModelContentAsist.addFTextFieldContentListener(txtParcano);

	}

	@Override
	public void removeBindings() {

		txtCinsi.removePropertyChangeListener(stokListener);
		txtMarka.removePropertyChangeListener(stokListener);
		txtOemno.removePropertyChangeListener(stokListener);
		txtParcano.removePropertyChangeListener(stokListener);

		StokModelContentAsist.removeFTextFieldContentListener(txtCinsi);
		StokModelContentAsist.removeFTextFieldContentListener(txtMarka);
		StokModelContentAsist.removeFTextFieldContentListener(txtOemno);
		StokModelContentAsist.removeFTextFieldContentListener(txtParcano);
		super.removeBindings();
	}

	public void loadStokModel(final int index, final BigDecimal katno, final String stno) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					((EtiketController) controller).loadStokModel(index);
					etiketTableModel.fireTableRowsUpdated(index, index);
					table.setRowSelectionInterval(index, index);
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
				}

			}
		});
	}

	@Override
	public void viewAdded() {
		viewActivated();
	}

	@Override
	public void viewActivated() {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		boolean flag = false;
		if(!"".equals(StokModelContentAsist.getStokModelContentAsist().getService().getHesapno())) { 
			StokModelContentAsist.getStokModelContentAsist().getService().setHesapno("");
			flag = true;
		}
		if(!SiparisListViewModel.Tip.ALMA.equals(StokModelContentAsist.getStokModelContentAsist().getService().getSiparistipi())) {
			StokModelContentAsist.getStokModelContentAsist().getService().setSiparistipi(SiparisListViewModel.Tip.ALMA);
			flag = true;
		}
		
		if (StokModelContentAsist.getStokModelContentAsist().getService().getYear() != currentYear) {
			StokModelContentAsist.getStokModelContentAsist().getService().setYear(currentYear);
			flag = true;
		}
		if(flag)
			StokModelContentAsist.getStokModelContentAsist().getContentAsist().revalidate();
	}

	@Override
	public void initProperties() {
	}

}
