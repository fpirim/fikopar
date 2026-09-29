package tr.fikopar.modules.sayim.view;

import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import tr.fikopar.app.MyApp;
import tr.fikopar.modules.contentasist.stokmodel.StokModelContentAsist;
import tr.fikopar.modules.sayim.controller.SayimController;
import tr.fikopar.modules.sayim.model.SayimDetayViewModel;
import tr.fikopar.modules.sayim.model.SayimViewModel;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.modules.stokkart.controller.StokkartController;
import tr.fikopar.modules.stokkart.model.StokKartViewModel;
import tr.fikopar.modules.stokkart.view.StokkartView;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FClipboardKeyAdapter;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCellEditor;
import tr.fikopar.swing.view.FView;

public class SayimView extends FView {
	private JSpinner yilSpinner;
	private FTable table;
	private SayimTableModel sayimTableModel;
	private FTextField txtCinsi, txtMarka, txtOemno, txtParcano;

	@Override
	public void initComponents() {

		sayimTableModel = new SayimTableModel();
		table = new FTable(sayimTableModel);

		txtCinsi = new FTextField();
		table.getColumnModel().getColumn(6).setCellEditor(new FTableCellEditor(txtCinsi));

		txtMarka = new FTextField();
		table.getColumnModel().getColumn(7).setCellEditor(new FTableCellEditor(txtMarka));

		txtOemno = new FTextField();
		table.getColumnModel().getColumn(4).setCellEditor(new FTableCellEditor(txtOemno));

		txtParcano = new FTextField();
		table.getColumnModel().getColumn(3).setCellEditor(new FTableCellEditor(txtParcano));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblSaymYl = new JLabel("Sayým Yýlý :");
		GridBagConstraints gbc_lblSaymYl = new GridBagConstraints();
		gbc_lblSaymYl.insets = new Insets(5, 5, 5, 5);
		gbc_lblSaymYl.anchor = GridBagConstraints.EAST;
		gbc_lblSaymYl.gridx = 0;
		gbc_lblSaymYl.gridy = 0;
		add(lblSaymYl, gbc_lblSaymYl);

		Date datenow = Calendar.getInstance().getTime();
		yilSpinner = new JSpinner(new SpinnerDateModel(datenow, null, null, Calendar.YEAR) );  //new SpinnerNumberModel(year, year-100, year+100, 1));;
		
		GridBagConstraints gbc_yilSpinner = new GridBagConstraints();
		gbc_yilSpinner.anchor = GridBagConstraints.WEST;
		gbc_yilSpinner.insets = new Insets(5, 0, 5, 0);
		gbc_yilSpinner.gridx = 1;
		gbc_yilSpinner.gridy = 0;
		add(yilSpinner, gbc_yilSpinner);

		JPanel tablePanel = table.createPanelForFTable(true, sayimTableModel.getSayimToplamTableModel());
		GridBagConstraints gbc_tablePanel = new GridBagConstraints();
		gbc_tablePanel.gridwidth = 2;
		gbc_tablePanel.insets = new Insets(0, 5, 5, 5);
		gbc_tablePanel.fill = GridBagConstraints.BOTH;
		gbc_tablePanel.gridx = 0;
		gbc_tablePanel.gridy = 1;
		add(tablePanel, gbc_tablePanel);

	}

	@Override
	public void initActions() {
		/*
		 * Table Copy&Paste Actions Install
		 */
		FClipboardKeyAdapter.install(table);

		/*
		 * Sayým Kaydet Button
		 */
		registerAction(new AbstractAction("Kaydet", UIManager.getIcon("kaydetIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						doProcess();
						try {
							((SayimController) controller).save();
							((SayimController) controller).load();
							sayimTableModel.fireTableDataChanged();
							table.requestFocus();
						} catch (Exception e) {
							controller.uncaughtException(Thread.currentThread(), e);
							table.requestFocus();
						} finally {
							endProcess();
						}

					}
				});
				thread.start();
			}
		}, "CTRL+S Kaydet", KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), "actKaydet");

		/*
		 * Sayým Sil Button
		 */
		registerAction(new AbstractAction("Sayým Sil", UIManager.getIcon("silIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int n = JOptionPane.showConfirmDialog(MyApp.getAppHandler(), "Sayým silinecek emin misiniz?",
						"Sayým Sil", JOptionPane.YES_NO_OPTION);

				if (n == JOptionPane.YES_OPTION) {

					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							doProcess();
							try {
								((SayimController) controller).delete();
								((SayimController) controller).load();
								endProcess();
								yilSpinner.requestFocus();
							} catch (Exception e) {
								controller.uncaughtException(Thread.currentThread(), e);
								table.requestFocus();
								endProcess();
							}
						}
					});
					thread.start();
				}
			}
		}, "CTRL+DELETE Sayim Sil", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK), "actSil");

		/*
		 * Table Satýr Sil
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
							sayimTableModel.getList().remove(rows[i] - i);
							sayimTableModel.fireTableRowsDeleted(rows[i] - i, rows[i] - i);
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
		 * Table Satýr Ekle
		 */
		registerAction(new AbstractAction("Satýr Ekle", UIManager.getIcon("satirekleIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] cols = table.getSelectedColumns();
				final int[] rows = table.getSelectedRows();
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					sayimTableModel.getList().add(rows[i], new SayimDetayViewModel());
					sayimTableModel.fireTableRowsInserted(rows[i], rows[i]);
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

		/*
		 * Stok Kartý Button
		 */
		registerAction(new AbstractAction("Stok Kartý", UIManager.getIcon("duzenleIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				int r = table.getSelectedRow();
				if (r != -1) {
					BigDecimal katno = (BigDecimal) sayimTableModel.getValueAt(r, 1);
					String stno = (String) sayimTableModel.getValueAt(r, 2);

					StokKartViewModel stokKartViewModel = new StokKartViewModel();

					StokkartController stokkartController = new StokkartController();
					stokkartController.setModel(stokKartViewModel);

					StokkartView stokkartView = new StokkartView();
					stokkartView.setName("Stok Kartý");

					stokkartController.setView(stokkartView);

					stokKartViewModel.setKatno(katno);
					stokKartViewModel.setStno(stno);

					stokkartView.setSelectYil(((SayimViewModel) controller.getModel()).getYil().subtract(BigDecimal.ONE));

					MyApp.getAppHandler().showDialog("Stok Kartý", stokkartView);

				}

			}
		}, "F9 - Stok Kartý", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "stokKartiAction");

		/*
		 * popup stok selected
		 */
		sayimTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				int lastRow = e.getLastRow() == Integer.MAX_VALUE ? table.getRowCount() : e.getLastRow();
				for (int r = e.getFirstRow(); r <= lastRow; r++) {
					BigDecimal katno = (BigDecimal) sayimTableModel.getValueAt(r, 1);
					String stno = (String) sayimTableModel.getValueAt(r, 2);

					if (e.getColumn() == 1 || e.getColumn() == 2) {
						if (katno != null && stno != null && !"".equals(stno) && katno.compareTo(BigDecimal.ZERO) != 0) {
							loadStokModel(r, katno, stno);
						}
					}
				}
			}
		});
		/*
		 * yil deðiþince yükle
		 */
		yilSpinner.addChangeListener(new ChangeListener() {
			
			Object lastValue;
			
			@Override
			public void stateChanged(ChangeEvent e) {
				 if (lastValue != null && !yilSpinner.getValue().equals(lastValue)) {
					 load();
			      }
			     lastValue = yilSpinner.getValue();
			}
		});

	}

	public void load() {
		Thread thread = new Thread(new Runnable() {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
			
			@Override
			public void run() {
				doProcess();
				try {
					
					SayimViewModel sayimViewModel = (SayimViewModel) controller.getModel();
					Calendar cal = Calendar.getInstance();
					cal.setTime((Date)yilSpinner.getValue());					
					 sayimViewModel.setYil(new BigDecimal(cal.get(Calendar.YEAR)) );
					((SayimController) controller).load();
					sayimTableModel.fireTableDataChanged();
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					yilSpinner.requestFocus();
				} finally {
					endProcess();
				}

			}
		});
		thread.start();
	}

	public void loadStokModel(final int index, final BigDecimal katno, final String stno) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					((SayimController) controller).loadStokModel(index);
					sayimTableModel.fireTableRowsUpdated(index, index);
					table.setRowSelectionInterval(index, index);
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
				}

			}
		});
	}

	@Override
	public void initProperties() {
		table.setDuplicateCellKeyColumns(new int[] { 1, 2 });
		table.setDuplicateCellMarkerEnable(true);
		yilSpinner.setEditor(new JSpinner.DateEditor(yilSpinner, "yyyy"));
	}

	private PropertyChangeListener stokListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("contentSelected".equals(evt.getPropertyName())) {
				table.getCellEditor().cancelCellEditing();
				Object[] obj = (Object[]) evt.getNewValue();
				sayimTableModel.setValueAt(obj[0], table.getSelectedRow(), 1);
				sayimTableModel.setValueAt(obj[1], table.getSelectedRow(), 2);
			}
		}
	};

	@Override
	public void viewAdded() {
		load();
	}

	@Override
	public void viewActivated() {
		SayimViewModel sayimViewModel = (SayimViewModel) controller.getModel();

		boolean flag = false;
		if(!"".equals(StokModelContentAsist.getStokModelContentAsist().getService().getHesapno())) { 
			StokModelContentAsist.getStokModelContentAsist().getService().setHesapno("");
			flag = true;
		}
		if(!SiparisListViewModel.Tip.ALMA.equals(StokModelContentAsist.getStokModelContentAsist().getService().getSiparistipi())) {
			StokModelContentAsist.getStokModelContentAsist().getService().setSiparistipi(SiparisListViewModel.Tip.ALMA);
			flag = true;
		}
		if (sayimViewModel.getYil() != null && !sayimViewModel.getYil().equals("") && StokModelContentAsist
				.getStokModelContentAsist().getService().getYear() != sayimViewModel.getYil().intValue() - 1) {
			StokModelContentAsist.getStokModelContentAsist().getService().setYear(sayimViewModel.getYil().intValue() - 1);
			flag = true;
		}
		if(flag)
			StokModelContentAsist.getStokModelContentAsist().getContentAsist().revalidate();
	}

	private PropertyChangeListener pcs = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("yil".equals(evt.getPropertyName()))
				viewActivated();
		}
	};

	@Override
	public void initBindings() {
		SayimViewModel sayimViewModel = (SayimViewModel) controller.getModel();
		sayimViewModel.addPropertyChangeListener(pcs);

		sayimTableModel.setList(sayimViewModel.getSayimDetayViewModels());
		
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
		SayimViewModel sayimViewModel = (SayimViewModel) controller.getModel();
		sayimViewModel.removePropertyChangeListener(pcs);

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

	/*
	 * public Action autoMaliyetAction = new AbstractAction("Otomatik Hesapla")
	 * { public void actionPerformed(ActionEvent e) { if
	 * (table.getTable().getSelectedRow() != -1) { int index =
	 * table.getTable().getSelectedRow();
	 * 
	 * SayimDetayModel sayimDetay = sayimTableModel.getSayimDetayAt(index);
	 * BigDecimal kdv = (BigDecimal) stokHareketView
	 * .getOzetTableModel().getValueAt(1, 10); BigDecimal netmaliyet =
	 * (BigDecimal) stokHareketView .getOzetTableModel().getValueAt(1, 12);
	 * sayimDetay.setKdv(kdv); sayimDetay.setMaliyet(netmaliyet);
	 * 
	 * table.getTable().getSelectionModel() .setSelectionInterval(index + 1,
	 * index + 1); } } };
	 */
	/*
	 * public Action printAction = new AbstractAction("Yazdï¿½r") { public void
	 * actionPerformed(ActionEvent e) { JasperPrint jasperPrint =
	 * sayimController.print(); if (jasperPrint == null) return;
	 * KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager
	 * .getCurrentKeyboardFocusManager(); Window window =
	 * keyboardFocusManager.getActiveWindow();
	 * 
	 * JDialog printDialog = new JDialog(window, "Yazdï¿½r",
	 * JDialog.ModalityType.APPLICATION_MODAL); JRViewer jrViewer = new
	 * JRViewer(jasperPrint); jrViewer.setPreferredSize(new Dimension(800,
	 * 600)); printDialog.getContentPane().add(jrViewer);
	 * printDialog.setSize(800, 600); printDialog.setLocationRelativeTo(window);
	 * printDialog.setVisible(true); jrViewer.setFitPageZoomRatio(); } };
	 */

}