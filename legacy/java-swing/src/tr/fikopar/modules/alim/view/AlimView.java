package tr.fikopar.modules.alim.view;

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
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import tr.fikopar.app.MyApp;
import tr.fikopar.modules.alim.controller.AlimViewController;
import tr.fikopar.modules.alim.controller.AlimViewListController;
import tr.fikopar.modules.alim.model.AlimViewDetayModel;
import tr.fikopar.modules.alim.model.AlimViewModel;
import tr.fikopar.modules.contentasist.sicilmodel.SicilModelContentAsist;
import tr.fikopar.modules.contentasist.stokmodel.StokModelContentAsist;
import tr.fikopar.modules.excel.controller.ExcelViewController;
import tr.fikopar.modules.excel.model.ExcelViewModel;
import tr.fikopar.modules.kur.controller.KurViewController;
import tr.fikopar.modules.kur.model.KurViewModel;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.modules.stokkart.controller.StokkartController;
import tr.fikopar.modules.stokkart.model.StokKartViewModel;
import tr.fikopar.modules.stokkart.view.StokkartView;
import tr.fikopar.swing.binding.FBindingModel;
import tr.fikopar.swing.binding.FBindings;
import tr.fikopar.swing.component.FCheckBox;
import tr.fikopar.swing.component.FComboBox;
import tr.fikopar.swing.component.FDateField;
import tr.fikopar.swing.component.FTextArea;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FClipboardKeyAdapter;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCellEditor;
import tr.fikopar.swing.view.FView;

public class AlimView extends FView {
	private FTextField txtId;
	private FTextField txtHesapno;
	private FTextField txtHesapadi;
	private FTextField txtYetkili;
	private FDateField dateTarih;
	private FCheckBox chkDusukse;
	private FCheckBox chkYuksekse;
	private FTextArea txtNote;
	private FComboBox<ExcelViewModel> comboExcel;
	private FComboBox<KurViewModel> comboKur;
	private FTable table;
	private AlimTableModel alimTableModel;

	private FTextField txtCinsi, txtMarka, txtParcano;

	@Override
	public void initActions() {

		FClipboardKeyAdapter.install(table);

		// Yeni Button
		registerAction(new AbstractAction("Alým Liste", UIManager.getIcon("listIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				AlimViewListController alimViewListController = new AlimViewListController();

				AlimViewList alimViewList = new AlimViewList();
				alimViewList.setName("Alým Listesi");

				alimViewListController.setView(alimViewList);

				MyApp.getAppHandler().showDialog("Alým Listesi", alimViewList, 900, 600);
				if (alimViewListController.getModel() != null) {
					AlimViewModel alimViewModel = (AlimViewModel) controller.getModel();
					alimViewModel.setId(((AlimViewModel) alimViewListController.getModel()).getId());
					load();
				}

			}
		}, "F1 - Alým Liste", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "actListe");

		// Yeni Button
		registerAction(new AbstractAction("Yeni", UIManager.getIcon("yeniIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				createNew();
			}
		}, "CTRL+N Yeni", KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK), "actYeni");

		// Kaydet Button
		registerAction(new AbstractAction("Kaydet", UIManager.getIcon("kaydetIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				save(SAVE);

			}
		}, "CTRL+S Kaydet", KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), "actKaydet");

		// Kaydet ve Aktar Button
		registerAction(new AbstractAction("Kaydet ve Aktar", UIManager.getIcon("kaydetaktarIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				save(SAVE_AND_TRANSFER);

			}
		}, "CTRL+W Kaydet", KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK), "actKaydetAktar");

		// Sil Button
		registerAction(new AbstractAction("Alýmý Sil", UIManager.getIcon("silIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		}, "CTRL+DELETE Alýmý Sil", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK), "actSil");

		// Satýr Sil
		registerAction(new AbstractAction("Satýr Sil", UIManager.getIcon("satirsilIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] cols = table.getSelectedColumns();
				final int[] rows = table.getSelectedRows();
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					if (rows[i] - i != table.getRowCount() - 1) {
						alimTableModel.getList().remove(rows[i] - i);
						alimTableModel.fireTableRowsDeleted(rows[i] - i, rows[i] - i);
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
					alimTableModel.getList().add(rows[i], new AlimViewDetayModel());
					alimTableModel.fireTableRowsInserted(rows[i], rows[i]);
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
					BigDecimal katno = (BigDecimal) alimTableModel.getValueAt(r, 1);
					String stno = (String) alimTableModel.getValueAt(r, 2);

					StokKartViewModel stokKartViewModel = new StokKartViewModel();

					StokkartController stokkartController = new StokkartController();
					stokkartController.setModel(stokKartViewModel);

					StokkartView stokkartView = new StokkartView();
					stokkartView.setName("Stok Kartý");

					stokkartController.setView(stokkartView);

					stokKartViewModel.setKatno(katno);
					stokKartViewModel.setStno(stno);

					stokkartView.setSelectYil(new BigDecimal(Calendar.getInstance().get(Calendar.YEAR)));

					MyApp.getAppHandler().showDialog("Stok Kartý", stokkartView);

				}
			}
		}, "F9 - Stok Kartý", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "stokKartiAction");

		// GUI Actions
		txtHesapno.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				loadSicilModel();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		alimTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				int lastRow = e.getLastRow() == Integer.MAX_VALUE ? table.getRowCount() : e.getLastRow();
				for (int r = e.getFirstRow(); r <= lastRow; r++) {
					BigDecimal katno = (BigDecimal) alimTableModel.getValueAt(r, 1);
					String stno = (String) alimTableModel.getValueAt(r, 2);

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
		viewActivated();
	}

	@Override
	public void viewActivated() {
		comboExcel.removeAllItems();
		comboExcel.addItem(null);
		for (ExcelViewModel excelViewModel : ExcelViewController.loadExcelViewModels())
			comboExcel.addItem(excelViewModel);

		comboKur.removeAllItems();
		comboKur.addItem(null);
		for (KurViewModel kurViewModel : KurViewController.loadKurViewModels())
			comboKur.addItem(kurViewModel);

		boolean flag = false;
		if (!"".equals(StokModelContentAsist.getStokModelContentAsist().getService().getHesapno())) {
			StokModelContentAsist.getStokModelContentAsist().getService().setHesapno("");
			flag = true;
		}
		if (!SiparisListViewModel.Tip.ALMA
				.equals(StokModelContentAsist.getStokModelContentAsist().getService().getSiparistipi())) {
			StokModelContentAsist.getStokModelContentAsist().getService().setSiparistipi(SiparisListViewModel.Tip.ALMA);
			flag = true;
		}
		int year = Calendar.getInstance().get(Calendar.YEAR);
		if (StokModelContentAsist.getStokModelContentAsist().getService().getYear() != year) {
			StokModelContentAsist.getStokModelContentAsist().getService().setYear(year);
			flag = true;
		}
		if (flag)
			StokModelContentAsist.getStokModelContentAsist().getContentAsist().revalidate();

	}

	private void createNew() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					((AlimViewController) controller).createNew();
					alimTableModel.fireTableDataChanged();
					txtHesapadi.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtId.requestFocus();
				} finally {
					endProcess();
				}

			}
		});
		thread.start();
	}

	private void load() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					((AlimViewController) controller).load();
					alimTableModel.fireTableDataChanged();
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtId.requestFocus();
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
					((AlimViewController) controller).loadStokModel(index);
					alimTableModel.fireTableRowsUpdated(index, index);
					table.setRowSelectionInterval(index, index);
					table.requestFocus();
				} catch (Exception e) {
					AlimViewDetayModel o1 = (AlimViewDetayModel) alimTableModel.getList().get(index);
					o1.setFiyat(BigDecimal.ZERO);
					o1.setNote("");
					table.setRowSelectionInterval(index, index);
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
					((AlimViewController) controller).loadSicilModel();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtHesapno.setText("");
				} finally {
					endProcess();
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						comboExcel.requestFocus();
					}
				});

			}
		});
		thread.start();
	}

	private static final int SAVE = 0;
	private static final int SAVE_AND_TRANSFER = 1;

	private void save(final int type) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					if (type == SAVE) {
						((AlimViewController) controller).save();
						((AlimViewController) controller).load();
						alimTableModel.fireTableDataChanged();
					}
					if (type == SAVE_AND_TRANSFER) {
						((AlimViewController) controller).save();
						((AlimViewController) controller).transfer();
						((AlimViewController) controller).load();
						alimTableModel.fireTableDataChanged();
					}
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtId.requestFocus();
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
					((AlimViewController) controller).delete();
					endProcess();
					MyApp.removeView(AlimView.this);
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtId.requestFocus();
					endProcess();
				}
			}
		});
		thread.start();
	}

	@Override
	public void initProperties() {
		txtId.setEditable(false);
		table.setDuplicateCellKeyColumns(new int[] { 1, 2 },
				new Object[][] { { 0, "" }, { 0, null }, { "", null }, { null, null } });
		table.setDuplicateCellMarkerEnable(true);

	}

	private PropertyChangeListener sicilListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("contentSelected".equals(evt.getPropertyName())) {
				txtHesapno.setText((String) ((Object[]) evt.getNewValue())[0]);
				loadSicilModel();
			}
		}
	};

	private PropertyChangeListener stokListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("contentSelected".equals(evt.getPropertyName())) {
				table.getCellEditor().cancelCellEditing();
				Object[] obj = (Object[]) evt.getNewValue();
				alimTableModel.setValueAt(obj[0], table.getSelectedRow(), 1);
				alimTableModel.setValueAt(obj[1], table.getSelectedRow(), 2);
			}

		}
	};

	@Override
	public void initBindings() {
		AlimViewModel alimViewModel = (AlimViewModel) controller.getModel();
		alimTableModel.setList(alimViewModel.getAlimViewDetayModels());

		txtHesapadi.addPropertyChangeListener(sicilListener);
		txtYetkili.addPropertyChangeListener(sicilListener);
		SicilModelContentAsist.addFTextFieldContentListener(txtHesapadi);
		SicilModelContentAsist.addFTextFieldContentListener(txtYetkili);

		txtCinsi.addPropertyChangeListener(stokListener);
		txtMarka.addPropertyChangeListener(stokListener);
		txtParcano.addPropertyChangeListener(stokListener);

		StokModelContentAsist.addFTextFieldContentListener(txtCinsi);
		StokModelContentAsist.addFTextFieldContentListener(txtMarka);
		StokModelContentAsist.addFTextFieldContentListener(txtParcano);

		FBindings.add(txtId, alimViewModel, "id", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtHesapno, alimViewModel, "hesapno", FBindingModel.BOTH);
		FBindings.add(txtHesapadi, alimViewModel, "hesapadi", FBindingModel.BOTH);
		FBindings.add(txtYetkili, alimViewModel, "yetkili", FBindingModel.BOTH);
		FBindings.add(comboExcel, alimViewModel, "excelViewModel", FBindingModel.BOTH);
		FBindings.add(dateTarih, alimViewModel, "tarih", FBindingModel.BOTH);
		FBindings.add(chkDusukse, alimViewModel, "dusukseAktar", FBindingModel.BOTH);
		FBindings.add(chkYuksekse, alimViewModel, "yuksekseAktar", FBindingModel.BOTH);
		FBindings.add(txtNote, alimViewModel, "note", FBindingModel.BOTH);
	}

	@Override
	public void removeBindings() {
		txtHesapadi.removePropertyChangeListener(sicilListener);
		txtYetkili.removePropertyChangeListener(sicilListener);

		SicilModelContentAsist.removeFTextFieldContentListener(txtHesapadi);
		SicilModelContentAsist.removeFTextFieldContentListener(txtYetkili);

		txtCinsi.removePropertyChangeListener(stokListener);
		txtMarka.removePropertyChangeListener(stokListener);
		txtParcano.removePropertyChangeListener(stokListener);

		StokModelContentAsist.removeFTextFieldContentListener(txtCinsi);
		StokModelContentAsist.removeFTextFieldContentListener(txtMarka);
		StokModelContentAsist.removeFTextFieldContentListener(txtParcano);
		super.removeBindings();
	}

	@Override
	public void initComponents() {

		alimTableModel = new AlimTableModel();
		table = new FTable(alimTableModel);

		txtCinsi = new FTextField();
		table.getColumnModel().getColumn(5).setCellEditor(new FTableCellEditor(txtCinsi));

		txtMarka = new FTextField();
		table.getColumnModel().getColumn(6).setCellEditor(new FTableCellEditor(txtMarka));

		txtParcano = new FTextField();
		table.getColumnModel().getColumn(3).setCellEditor(new FTableCellEditor(txtParcano));

		comboKur = new FComboBox<>();
		table.getColumnModel().getColumn(14).setCellEditor(new FTableCellEditor(comboKur));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 98, 130, 222, 0, 45, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel idLabel = new JLabel("Alým Id :");
		GridBagConstraints gbc_idLabel = new GridBagConstraints();
		gbc_idLabel.anchor = GridBagConstraints.WEST;
		gbc_idLabel.insets = new Insets(5, 5, 5, 5);
		gbc_idLabel.gridx = 0;
		gbc_idLabel.gridy = 0;
		add(idLabel, gbc_idLabel);

		txtId = new FTextField();
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.insets = new Insets(5, 0, 5, 5);
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.gridx = 1;
		gbc_txtId.gridy = 0;
		add(txtId, gbc_txtId);

		JLabel lblTarih = new JLabel("Tarih :");
		GridBagConstraints gbc_lblTarih = new GridBagConstraints();
		gbc_lblTarih.anchor = GridBagConstraints.WEST;
		gbc_lblTarih.insets = new Insets(5, 0, 5, 5);
		gbc_lblTarih.gridx = 4;
		gbc_lblTarih.gridy = 0;
		add(lblTarih, gbc_lblTarih);

		dateTarih = new FDateField();
		GridBagConstraints gbc_dateTarih = new GridBagConstraints();
		gbc_dateTarih.anchor = GridBagConstraints.WEST;
		gbc_dateTarih.insets = new Insets(0, 0, 5, 0);
		gbc_dateTarih.gridx = 5;
		gbc_dateTarih.gridy = 0;
		add(dateTarih, gbc_dateTarih);

		JLabel hesapnoLabel = new JLabel("Hesap No :");
		GridBagConstraints gbc_hesapnoLabel = new GridBagConstraints();
		gbc_hesapnoLabel.anchor = GridBagConstraints.WEST;
		gbc_hesapnoLabel.insets = new Insets(0, 5, 5, 5);
		gbc_hesapnoLabel.gridx = 0;
		gbc_hesapnoLabel.gridy = 1;
		add(hesapnoLabel, gbc_hesapnoLabel);

		txtHesapno = new FTextField();
		GridBagConstraints gbc_txtHesapno = new GridBagConstraints();
		gbc_txtHesapno.insets = new Insets(0, 0, 5, 5);
		gbc_txtHesapno.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtHesapno.gridx = 1;
		gbc_txtHesapno.gridy = 1;
		add(txtHesapno, gbc_txtHesapno);

		chkDusukse = new FCheckBox("Liste düþükse yükselt");
		GridBagConstraints gbc_chkDusukse = new GridBagConstraints();
		gbc_chkDusukse.anchor = GridBagConstraints.WEST;
		gbc_chkDusukse.insets = new Insets(0, 0, 5, 5);
		gbc_chkDusukse.gridx = 5;
		gbc_chkDusukse.gridy = 1;
		add(chkDusukse, gbc_chkDusukse);

		JLabel hesapadiLabel = new JLabel("Hesap Adý :");
		GridBagConstraints gbc_hesapadiLabel = new GridBagConstraints();
		gbc_hesapadiLabel.anchor = GridBagConstraints.WEST;
		gbc_hesapadiLabel.insets = new Insets(0, 5, 5, 5);
		gbc_hesapadiLabel.gridx = 0;
		gbc_hesapadiLabel.gridy = 2;
		add(hesapadiLabel, gbc_hesapadiLabel);

		txtHesapadi = new FTextField();
		GridBagConstraints gbc_txtHesapadi = new GridBagConstraints();
		gbc_txtHesapadi.gridwidth = 2;
		gbc_txtHesapadi.insets = new Insets(0, 0, 5, 5);
		gbc_txtHesapadi.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtHesapadi.gridx = 1;
		gbc_txtHesapadi.gridy = 2;
		add(txtHesapadi, gbc_txtHesapadi);

		JLabel lblNot = new JLabel("Not :");
		GridBagConstraints gbc_lblNot = new GridBagConstraints();
		gbc_lblNot.insets = new Insets(0, 0, 5, 5);
		gbc_lblNot.gridx = 4;
		gbc_lblNot.gridy = 3;
		add(lblNot, gbc_lblNot);

		txtNote = new FTextArea();
		JScrollPane scrollPane = new JScrollPane(txtNote);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 5;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);

		chkYuksekse = new FCheckBox("Liste yüksekse düþür");
		GridBagConstraints gbc_chkYuksekse = new GridBagConstraints();
		gbc_chkYuksekse.anchor = GridBagConstraints.WEST;
		gbc_chkYuksekse.insets = new Insets(0, 0, 5, 5);
		gbc_chkYuksekse.gridx = 5;
		gbc_chkYuksekse.gridy = 2;
		add(chkYuksekse, gbc_chkYuksekse);

		JLabel lblYetkili = new JLabel("Yetkili :");
		GridBagConstraints gbc_yetkili = new GridBagConstraints();
		gbc_yetkili.anchor = GridBagConstraints.WEST;
		gbc_yetkili.insets = new Insets(0, 5, 5, 5);
		gbc_yetkili.gridx = 0;
		gbc_yetkili.gridy = 3;
		add(lblYetkili, gbc_yetkili);

		txtYetkili = new FTextField();
		GridBagConstraints gbc_txtYetkili = new GridBagConstraints();
		gbc_txtYetkili.gridwidth = 2;
		gbc_txtYetkili.insets = new Insets(0, 0, 5, 5);
		gbc_txtYetkili.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtYetkili.gridx = 1;
		gbc_txtYetkili.gridy = 3;
		add(txtYetkili, gbc_txtYetkili);

		//
		JLabel excelLabel = new JLabel("Excel Modeli :");
		GridBagConstraints gbc_excelLabel = new GridBagConstraints();
		gbc_excelLabel.insets = new Insets(0, 5, 5, 5);
		gbc_excelLabel.anchor = GridBagConstraints.WEST;
		gbc_excelLabel.gridx = 0;
		gbc_excelLabel.gridy = 4;
		add(excelLabel, gbc_excelLabel);

		comboExcel = new FComboBox<>();
		GridBagConstraints gbc_comboExcel = new GridBagConstraints();
		gbc_comboExcel.gridwidth = 2;
		gbc_comboExcel.insets = new Insets(0, 0, 5, 5);
		gbc_comboExcel.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboExcel.gridx = 1;
		gbc_comboExcel.gridy = 4;
		add(comboExcel, gbc_comboExcel);

		JPanel tablePanel = table.createPanelForFTable(true);
		GridBagConstraints gbc_tablePanel = new GridBagConstraints();
		gbc_tablePanel.insets = new Insets(0, 5, 5, 5);
		gbc_tablePanel.gridwidth = 6;
		gbc_tablePanel.fill = GridBagConstraints.BOTH;
		gbc_tablePanel.gridx = 0;
		gbc_tablePanel.gridy = 5;
		add(tablePanel, gbc_tablePanel);

	}

}
