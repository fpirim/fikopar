package tr.fikopar.modules.depo.view;

import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.jasperreports.engine.JasperPrint;
import tr.fikopar.app.MyApp;
import tr.fikopar.modules.contentasist.sicilmodel.SicilModelContentAsist;
import tr.fikopar.modules.contentasist.stokmodel.StokModelContentAsist;
import tr.fikopar.modules.depo.controller.DepoViewController;
import tr.fikopar.modules.depo.model.DepoDetayViewModel;
import tr.fikopar.modules.depo.model.DepoViewModel;
import tr.fikopar.modules.stokkart.controller.StokkartController;
import tr.fikopar.modules.stokkart.model.StokKartViewModel;
import tr.fikopar.modules.stokkart.view.StokkartView;
import tr.fikopar.swing.binding.FBindingModel;
import tr.fikopar.swing.binding.FBindings;
import tr.fikopar.swing.component.FComboBox;
import tr.fikopar.swing.component.FComboBoxModel;
import tr.fikopar.swing.component.FDateField;
import tr.fikopar.swing.component.FTextArea;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FClipboardKeyAdapter;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCellEditor;
import tr.fikopar.swing.view.FPrintPreview;
import tr.fikopar.swing.view.FView;

public class DepoView extends FView {
	private FTextField txtVno;
	private FTextField txtTanim;
	private FTextField txtVda;
	private FTextField txtYetkili;
	private FTextField txtHesapAdi;
	private FTextField txtHesapNo;
	private FTextField txtDepoNo;
	private FTextField txtTCNo;
	private FTextField txtTel;
	private FDateField txtTarih;
	private FTextArea txtAdres;
	private FTextArea txtNot;
	private FComboBox<String> comboDepoAlan;
	private FComboBoxModel<String> comboDepoAlanModel;
	private FTable table;
	private DepoTableModel depoTableModel;
	
	private FTextField txtCinsi, txtMarka, txtParcano, txtOemno;
	
	@Override
	public void initActions() {

		FClipboardKeyAdapter.install(table);

		// Kaydet Button
		registerAction(new AbstractAction("Kaydet", UIManager.getIcon("kaydetIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				save();

			}
		}, "CTRL+S Kaydet", KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), "actKaydet");

		// Sil Button
		registerAction(new AbstractAction("Depo Sil", UIManager.getIcon("silIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int dialogResult = JOptionPane.showOptionDialog(
						MyApp.getAppHandler(), "Depo silinecek emin misiniz?","Depo Sil", 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String [] {"Evet", "Hayýr"}, "Hayýr");
				if(dialogResult == JOptionPane.YES_OPTION)
					delete();
			}
		}, "CTRL+DELETE Depo Sil", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK), "actSil");

		// Yazdýr Button
		registerAction(new AbstractAction("Depo Yazdýr", UIManager.getIcon("yazdirIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}
		}, "CTRL+P Depo Yazdýr", KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "actYazdir");

		// Satýr Sil
		registerAction(new AbstractAction("Satýr Sil", UIManager.getIcon("satirsilIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] cols = table.getSelectedColumns();
				final int[] rows = table.getSelectedRows();
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					if (rows[i] - i != table.getRowCount() - 1) {
						depoTableModel.getList().remove(rows[i] - i);
						depoTableModel.fireTableRowsDeleted(rows[i] - i, rows[i] - i);
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
		}, "ALT+DELETE Satýr Sil", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.ALT_MASK), "actSatirSil");

		// Satýr Ekle
		registerAction(new AbstractAction("Satýr Ekle", UIManager.getIcon("satirekleIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] cols = table.getSelectedColumns();
				final int[] rows = table.getSelectedRows();
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					depoTableModel.getList().add(rows[i], new DepoDetayViewModel());
					depoTableModel.fireTableRowsInserted(rows[i], rows[i]);
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

		// Stok Kartý Button
		registerAction(new AbstractAction("Stok Kartý", UIManager.getIcon("duzenleIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int r = table.getSelectedRow();
				if (r != -1) {
					BigDecimal katno = (BigDecimal) depoTableModel.getValueAt(r, 1);
					String stno = (String) depoTableModel.getValueAt(r, 2);

					StokKartViewModel stokKartViewModel = new StokKartViewModel();

					StokkartController stokkartController = new StokkartController();
					stokkartController.setModel(stokKartViewModel);

					StokkartView stokkartView = new StokkartView();
					stokkartView.setName("Stok Kartý");

					stokkartController.setView(stokkartView);

					stokKartViewModel.setKatno(katno);
					stokKartViewModel.setStno(stno);

					stokkartView.setSelectYil(new BigDecimal(((DepoViewModel) controller.getModel()).getTarih().substring(0, 4)));

					MyApp.getAppHandler().showDialog("Stok Kartý", stokkartView);
					
				}
			}
		}, "F9 - Stok Kartý", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "stokKartiAction");

		// GUI Actions
		txtHesapNo.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				loadSicilModel();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		depoTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				int lastRow = e.getLastRow() == Integer.MAX_VALUE ? table.getRowCount() : e.getLastRow();
				for (int r = e.getFirstRow(); r <= lastRow; r++) {
					BigDecimal katno = (BigDecimal) depoTableModel.getValueAt(r, 1);
					String stno = (String) depoTableModel.getValueAt(r, 2);

					if (e.getColumn() == 1 || e.getColumn() == 2) {
						if (katno != null && stno != null && !"".equals(stno) && katno.compareTo(BigDecimal.ZERO) != 0) {
							loadStokModel(r);
						}
					}
				}
			}
		});

	}

	@Override
	public void viewAdded() {
		load();
	}

	@Override
	public void viewActivated() {
		DepoViewModel depoViewModel = (DepoViewModel) controller.getModel();

		boolean flag = false;
		
		if (depoViewModel.getTarih() != null && !depoViewModel.getTarih().equals("")
				&& StokModelContentAsist.getStokModelContentAsist().getService().getYear() != Integer
						.parseInt(depoViewModel.getTarih().substring(0, 4))) {
			StokModelContentAsist.getStokModelContentAsist().getService()
					.setYear(Integer.parseInt(depoViewModel.getTarih().substring(0, 4)));
			flag = true;
		}
		
		if(flag)
			StokModelContentAsist.getStokModelContentAsist().getContentAsist().revalidate();

	}

	private void load() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					((DepoViewController) controller).load();
					depoTableModel.fireTableDataChanged();
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtDepoNo.requestFocus();
				} finally {
					endProcess();
				}

			}
		});
		thread.start();
	}

	private void loadStokModel(final int index) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					((DepoViewController) controller).loadStokModel(index);
					depoTableModel.fireTableRowsUpdated(index, index);
					table.setRowSelectionInterval(index, index);
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
				}

			}
		});
	}

	private void loadSicilModel() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					((DepoViewController) controller).loadSicilModel();
					comboDepoAlan.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtHesapNo.setText("");
					txtHesapAdi.requestFocus();
				} finally {
					endProcess();
				}

			}
		});
		thread.start();
	}

	private void save() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					((DepoViewController) controller).save();
					((DepoViewController) controller).load();
					depoTableModel.fireTableDataChanged();
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtDepoNo.requestFocus();
				} finally {
					endProcess();
				}

			}
		});
		thread.start();

	}

	private void delete() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					((DepoViewController) controller).delete();
					endProcess();
					MyApp.removeView(DepoView.this);
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtDepoNo.requestFocus();
					endProcess();
				}
			}
		});
		thread.start();
	}

	private void print() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					JasperPrint jasperPrint = ((DepoViewController) controller).print();
					endProcess();
					
					MyApp.getAppHandler().showDialog("Depo Yazdýr", new FPrintPreview(jasperPrint));
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtDepoNo.requestFocus();
					endProcess();
				}
			}
		});
		thread.start();
	}

	@Override
	public void initProperties() {
		txtVno.setEditable(false);
		txtTanim.setEditable(false);
		txtVda.setEditable(false);
		txtDepoNo.setEditable(false);
		txtTCNo.setEditable(false);
		txtTel.setEditable(false);
		txtAdres.setEditable(false);
		comboDepoAlan.setEditable(true);

	}

	private PropertyChangeListener sicilListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("contentSelected".equals(evt.getPropertyName())) {
				txtHesapNo.setText((String) ((Object[]) evt.getNewValue())[0]);
				loadSicilModel();
			}
		}
	};

	PropertyChangeListener stokListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("contentSelected".equals(evt.getPropertyName())) {
				table.getCellEditor().cancelCellEditing();
				Object[] obj = (Object[]) evt.getNewValue();
				depoTableModel.setValueAt(obj[0], table.getSelectedRow(), 1);
				depoTableModel.setValueAt(obj[1], table.getSelectedRow(), 2);
			}

		}
	};

	private PropertyChangeListener pcs = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("hesapno".equals(evt.getPropertyName()) || "tarih".equals(evt.getPropertyName()))
				viewActivated();
		}
	};

	@Override
	public void initBindings() {
		DepoViewModel depoViewModel = (DepoViewModel) controller.getModel();
		depoTableModel.setList(depoViewModel.getDepoDetayViewModels());
		comboDepoAlanModel.setList(depoViewModel.getPlasiyers());
		depoViewModel.addPropertyChangeListener(pcs);
		txtHesapAdi.addPropertyChangeListener(sicilListener);
		txtYetkili.addPropertyChangeListener(sicilListener);

		SicilModelContentAsist.addFTextFieldContentListener(txtHesapAdi);
		SicilModelContentAsist.addFTextFieldContentListener(txtYetkili);

		txtCinsi.addPropertyChangeListener(stokListener);
		txtMarka.addPropertyChangeListener(stokListener);
		txtOemno.addPropertyChangeListener(stokListener);
		txtParcano.addPropertyChangeListener(stokListener);

		StokModelContentAsist.addFTextFieldContentListener(txtCinsi);
		StokModelContentAsist.addFTextFieldContentListener(txtMarka);
		StokModelContentAsist.addFTextFieldContentListener(txtOemno);
		StokModelContentAsist.addFTextFieldContentListener(txtParcano);

		FBindings.add(txtHesapNo, depoViewModel, "hesapno", FBindingModel.BOTH);
		FBindings.add(txtHesapAdi, depoViewModel, "hesapadi", FBindingModel.BOTH);
		FBindings.add(txtYetkili, depoViewModel, "yetkili", FBindingModel.BOTH);
		FBindings.add(txtTarih, depoViewModel, "tarih", FBindingModel.BOTH);
		FBindings.add(comboDepoAlan, depoViewModel, "depoalan", FBindingModel.BOTH);
		FBindings.add(txtNot, depoViewModel, "note", FBindingModel.BOTH);
		
		FBindings.add(txtVno, depoViewModel, "vno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtTanim, depoViewModel, "tanim", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtVda, depoViewModel, "vda", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtDepoNo, depoViewModel, "id", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtTCNo, depoViewModel, "tcno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtAdres, depoViewModel, "adres", FBindingModel.MODEL_TO_VIEW);

	}

	@Override
	public void removeBindings() {
		DepoViewModel depoViewModel = (DepoViewModel) controller.getModel();
		depoViewModel.removePropertyChangeListener(pcs);
		txtHesapAdi.removePropertyChangeListener(sicilListener);
		txtYetkili.removePropertyChangeListener(sicilListener);

		SicilModelContentAsist.removeFTextFieldContentListener(txtHesapAdi);
		SicilModelContentAsist.removeFTextFieldContentListener(txtYetkili);

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

	@Override
	public void initComponents() {

		depoTableModel = new DepoTableModel();
		table = new FTable(depoTableModel);

		txtCinsi = new FTextField();
		table.getColumnModel().getColumn(6).setCellEditor(new FTableCellEditor(txtCinsi));

		txtMarka = new FTextField();
		table.getColumnModel().getColumn(7).setCellEditor(new FTableCellEditor(txtMarka));

		txtOemno = new FTextField();
		table.getColumnModel().getColumn(4).setCellEditor(new FTableCellEditor(txtOemno));

		txtParcano = new FTextField();
		table.getColumnModel().getColumn(3).setCellEditor(new FTableCellEditor(txtParcano));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 75, 120, 120, 100, 0, 0, 150, 70, 0 };
		gridBagLayout.rowHeights = new int[] { 20, 25, 25, 20, 55, 24, 20, 20, 305, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblDepoNo = new JLabel("Depo No :");
		GridBagConstraints gbc_lblDepoNo = new GridBagConstraints();
		gbc_lblDepoNo.anchor = GridBagConstraints.WEST;
		gbc_lblDepoNo.insets = new Insets(5, 5, 5, 5);
		gbc_lblDepoNo.gridx = 0;
		gbc_lblDepoNo.gridy = 0;
		add(lblDepoNo, gbc_lblDepoNo);

		txtDepoNo = new FTextField();
		txtDepoNo.setColumns(10);
		GridBagConstraints gbc_txtDepoNo = new GridBagConstraints();
		gbc_txtDepoNo.anchor = GridBagConstraints.NORTH;
		gbc_txtDepoNo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDepoNo.insets = new Insets(5, 0, 5, 5);
		gbc_txtDepoNo.gridx = 1;
		gbc_txtDepoNo.gridy = 0;
		add(txtDepoNo, gbc_txtDepoNo);
		JLabel lblTarih = new JLabel("Depo Tarihi :");
		GridBagConstraints gbc_lblTarih = new GridBagConstraints();
		gbc_lblTarih.anchor = GridBagConstraints.WEST;
		gbc_lblTarih.insets = new Insets(5, 0, 5, 5);
		gbc_lblTarih.gridx = 5;
		gbc_lblTarih.gridy = 0;
		add(lblTarih, gbc_lblTarih);
		txtTarih = new FDateField();
		GridBagConstraints gbc_txtTarih = new GridBagConstraints();
		gbc_txtTarih.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTarih.anchor = GridBagConstraints.NORTH;
		gbc_txtTarih.insets = new Insets(5, 0, 5, 5);
		gbc_txtTarih.gridx = 6;
		gbc_txtTarih.gridy = 0;
		add(txtTarih, gbc_txtTarih);
		JLabel lblHesapNo = new JLabel("Hesap No :");
		GridBagConstraints gbc_lblHesapNo = new GridBagConstraints();
		gbc_lblHesapNo.anchor = GridBagConstraints.WEST;
		gbc_lblHesapNo.insets = new Insets(0, 5, 5, 5);
		gbc_lblHesapNo.gridx = 0;
		gbc_lblHesapNo.gridy = 1;
		add(lblHesapNo, gbc_lblHesapNo);
		txtHesapNo = new FTextField();
		GridBagConstraints gbc_txtHesapNo = new GridBagConstraints();
		gbc_txtHesapNo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtHesapNo.insets = new Insets(0, 0, 5, 5);
		gbc_txtHesapNo.gridx = 1;
		gbc_txtHesapNo.gridy = 1;
		add(txtHesapNo, gbc_txtHesapNo);
		JLabel lblHesapAdi = new JLabel("Hesap Adý :");
		GridBagConstraints gbc_lblHesapAdi = new GridBagConstraints();
		gbc_lblHesapAdi.anchor = GridBagConstraints.WEST;
		gbc_lblHesapAdi.insets = new Insets(0, 5, 5, 5);
		gbc_lblHesapAdi.gridx = 0;
		gbc_lblHesapAdi.gridy = 2;
		add(lblHesapAdi, gbc_lblHesapAdi);
		txtHesapAdi = new FTextField();
		GridBagConstraints gbc_txtHesapAdi = new GridBagConstraints();
		gbc_txtHesapAdi.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtHesapAdi.insets = new Insets(0, 0, 5, 5);
		gbc_txtHesapAdi.gridwidth = 3;
		gbc_txtHesapAdi.gridx = 1;
		gbc_txtHesapAdi.gridy = 2;
		add(txtHesapAdi, gbc_txtHesapAdi);
		JLabel lblDepoAlan = new JLabel("Depo Alan :");
		GridBagConstraints gbc_lblDepoAlan = new GridBagConstraints();
		gbc_lblDepoAlan.anchor = GridBagConstraints.WEST;
		gbc_lblDepoAlan.insets = new Insets(0, 0, 5, 5);
		gbc_lblDepoAlan.gridx = 5;
		gbc_lblDepoAlan.gridy = 2;
		add(lblDepoAlan, gbc_lblDepoAlan);
		comboDepoAlanModel = new FComboBoxModel<>();
		comboDepoAlan = new FComboBox<String>(comboDepoAlanModel);
		GridBagConstraints gbc_comboDepoAlan = new GridBagConstraints();
		gbc_comboDepoAlan.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboDepoAlan.anchor = GridBagConstraints.NORTH;
		gbc_comboDepoAlan.insets = new Insets(0, 0, 5, 5);
		gbc_comboDepoAlan.gridx = 6;
		gbc_comboDepoAlan.gridy = 2;
		add(comboDepoAlan, gbc_comboDepoAlan);
		JLabel lblYetkili = new JLabel("Yetkili :");
		GridBagConstraints gbc_lblYetkili = new GridBagConstraints();
		gbc_lblYetkili.anchor = GridBagConstraints.WEST;
		gbc_lblYetkili.insets = new Insets(0, 5, 5, 5);
		gbc_lblYetkili.gridx = 0;
		gbc_lblYetkili.gridy = 3;
		add(lblYetkili, gbc_lblYetkili);
		txtYetkili = new FTextField();
		GridBagConstraints gbc_txtYetkili = new GridBagConstraints();
		gbc_txtYetkili.anchor = GridBagConstraints.NORTH;
		gbc_txtYetkili.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtYetkili.insets = new Insets(0, 0, 5, 5);
		gbc_txtYetkili.gridwidth = 3;
		gbc_txtYetkili.gridx = 1;
		gbc_txtYetkili.gridy = 3;
		add(txtYetkili, gbc_txtYetkili);
		JLabel lblNot = new JLabel("Not :");
		GridBagConstraints gbc_lblNot = new GridBagConstraints();
		gbc_lblNot.anchor = GridBagConstraints.WEST;
		gbc_lblNot.insets = new Insets(0, 0, 5, 5);
		gbc_lblNot.gridx = 5;
		gbc_lblNot.gridy = 3;
		add(lblNot, gbc_lblNot);
		JScrollPane paneNote = new JScrollPane(txtNot = new FTextArea());
		GridBagConstraints gbc_paneNote = new GridBagConstraints();
		gbc_paneNote.gridwidth = 2;
		gbc_paneNote.fill = GridBagConstraints.BOTH;
		gbc_paneNote.insets = new Insets(0, 0, 5, 0);
		gbc_paneNote.gridheight = 2;
		gbc_paneNote.gridx = 6;
		gbc_paneNote.gridy = 3;
		add(paneNote, gbc_paneNote);
		JLabel lblAdres = new JLabel("Adres :");
		GridBagConstraints gbc_lblAdres = new GridBagConstraints();
		gbc_lblAdres.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblAdres.insets = new Insets(0, 5, 5, 5);
		gbc_lblAdres.gridx = 0;
		gbc_lblAdres.gridy = 4;
		add(lblAdres, gbc_lblAdres);
		JScrollPane paneAdres = new JScrollPane(txtAdres = new FTextArea());
		GridBagConstraints gbc_paneAdres = new GridBagConstraints();
		gbc_paneAdres.fill = GridBagConstraints.BOTH;
		gbc_paneAdres.insets = new Insets(0, 0, 5, 5);
		gbc_paneAdres.gridwidth = 3;
		gbc_paneAdres.gridx = 1;
		gbc_paneAdres.gridy = 4;
		add(paneAdres, gbc_paneAdres);
		JLabel lblTanim = new JLabel("Tanim :");
		GridBagConstraints gbc_lblTanim = new GridBagConstraints();
		gbc_lblTanim.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblTanim.insets = new Insets(0, 5, 5, 5);
		gbc_lblTanim.gridx = 0;
		gbc_lblTanim.gridy = 5;
		add(lblTanim, gbc_lblTanim);
		txtTanim = new FTextField();
		GridBagConstraints gbc_txtTanim = new GridBagConstraints();
		gbc_txtTanim.anchor = GridBagConstraints.NORTH;
		gbc_txtTanim.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTanim.insets = new Insets(0, 0, 5, 5);
		gbc_txtTanim.gridwidth = 3;
		gbc_txtTanim.gridx = 1;
		gbc_txtTanim.gridy = 5;
		add(txtTanim, gbc_txtTanim);

		JLabel lvlVdaVno = new JLabel("Vergi Da./No :");
		GridBagConstraints gbc_lvlVdaVno = new GridBagConstraints();
		gbc_lvlVdaVno.anchor = GridBagConstraints.WEST;
		gbc_lvlVdaVno.insets = new Insets(0, 5, 5, 5);
		gbc_lvlVdaVno.gridx = 0;
		gbc_lvlVdaVno.gridy = 6;
		add(lvlVdaVno, gbc_lvlVdaVno);
		txtVda = new FTextField();
		GridBagConstraints gbc_txtVda = new GridBagConstraints();
		gbc_txtVda.anchor = GridBagConstraints.NORTH;
		gbc_txtVda.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVda.insets = new Insets(0, 0, 5, 5);
		gbc_txtVda.gridx = 1;
		gbc_txtVda.gridy = 6;
		add(txtVda, gbc_txtVda);
		txtVno = new FTextField();
		GridBagConstraints gbc_txtVno = new GridBagConstraints();
		gbc_txtVno.anchor = GridBagConstraints.NORTH;
		gbc_txtVno.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVno.insets = new Insets(0, 0, 5, 5);
		gbc_txtVno.gridx = 2;
		gbc_txtVno.gridy = 6;
		add(txtVno, gbc_txtVno);
		JLabel lblTcNo = new JLabel("T.C. No /Tel :");
		GridBagConstraints gbc_lblTcNo = new GridBagConstraints();
		gbc_lblTcNo.anchor = GridBagConstraints.WEST;
		gbc_lblTcNo.insets = new Insets(0, 5, 5, 5);
		gbc_lblTcNo.gridx = 0;
		gbc_lblTcNo.gridy = 7;
		add(lblTcNo, gbc_lblTcNo);
		txtTCNo = new FTextField();
		GridBagConstraints gbc_txtTCNo = new GridBagConstraints();
		gbc_txtTCNo.anchor = GridBagConstraints.NORTH;
		gbc_txtTCNo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTCNo.insets = new Insets(0, 0, 5, 5);
		gbc_txtTCNo.gridx = 1;
		gbc_txtTCNo.gridy = 7;
		add(txtTCNo, gbc_txtTCNo);
		txtTel = new FTextField();
		GridBagConstraints gbc_txtTel = new GridBagConstraints();
		gbc_txtTel.anchor = GridBagConstraints.NORTH;
		gbc_txtTel.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTel.insets = new Insets(0, 0, 5, 5);
		gbc_txtTel.gridx = 2;
		gbc_txtTel.gridy = 7;
		add(txtTel, gbc_txtTel);

		JPanel tablePanel = table.createPanelForFTable(true);
		GridBagConstraints gbc_tablePanel = new GridBagConstraints();
		gbc_tablePanel.insets = new Insets(0, 5, 0, 0);
		gbc_tablePanel.fill = GridBagConstraints.BOTH;
		gbc_tablePanel.gridwidth = 8;
		gbc_tablePanel.gridx = 0;
		gbc_tablePanel.gridy = 8;
		add(tablePanel, gbc_tablePanel);

	}

}
