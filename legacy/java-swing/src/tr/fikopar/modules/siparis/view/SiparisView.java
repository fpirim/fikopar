package tr.fikopar.modules.siparis.view;

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
import tr.fikopar.modules.siparis.controller.SiparisViewController;
import tr.fikopar.modules.siparis.model.SiparisDetayViewModel;
import tr.fikopar.modules.siparis.model.SiparisViewModel;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.modules.stokkart.controller.StokkartController;
import tr.fikopar.modules.stokkart.model.StokKartViewModel;
import tr.fikopar.modules.stokkart.view.StokkartView;
import tr.fikopar.swing.binding.FBindingModel;
import tr.fikopar.swing.binding.FBindings;
import tr.fikopar.swing.component.FCheckBox;
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

public class SiparisView extends FView {
	private FTextField txtVno;
	private FTextField txtTanim;
	private FTextField txtVda;
	private FTextField txtYetkili;
	private FTextField txtHesapAdi;
	private FTextField txtHesapNo;
	private FTextField txtSiparisNo;
	private FTextField txtTCNo;
	private FTextField txtTel;
	private FDateField txtTarih;
	private FTextArea txtAdres;
	private FTextArea txtNot;
	private FComboBox<String> comboSevkSekli;
	private FComboBox<String> comboSiparisiAlan;
	private FComboBoxModel<String> comboSiparisiAlanModel;
	private FComboBoxModel<String> comboSevkSekliModel;
	private FCheckBox chkKampanyaliSatis;
	private FTable table;
	private SiparisTableModel siparisTableModel;
	
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
		registerAction(new AbstractAction("Sipariþi Sil", UIManager.getIcon("silIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int dialogResult = JOptionPane.showOptionDialog(
					MyApp.getAppHandler(), "Sipariþ silinecek emin misiniz?","Sipariþ Sil", 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String [] {"Evet", "Hayýr"}, "Hayýr");
				if(dialogResult == JOptionPane.YES_OPTION)
					delete();
				
			}
		}, "CTRL+DELETE Sipariþ Sil", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK), "actSil");

		// Yazdýr Button
		registerAction(new AbstractAction("Sipariþi Yazdýr", UIManager.getIcon("yazdirIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}
		}, "CTRL+P Sipariþi Yazdýr", KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "actYazdir");

		// Satýr Sil
		registerAction(new AbstractAction("Satýr Sil", UIManager.getIcon("satirsilIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] cols = table.getSelectedColumns();
				final int[] rows = table.getSelectedRows();
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					if (rows[i] - i != table.getRowCount() - 1) {
						siparisTableModel.getList().remove(rows[i] - i);
						siparisTableModel.fireTableRowsDeleted(rows[i] - i, rows[i] - i);
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
					siparisTableModel.getList().add(rows[i], new SiparisDetayViewModel());
					siparisTableModel.fireTableRowsInserted(rows[i], rows[i]);
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
					BigDecimal katno = (BigDecimal) siparisTableModel.getValueAt(r, 1);
					String stno = (String) siparisTableModel.getValueAt(r, 2);

					StokKartViewModel stokKartViewModel = new StokKartViewModel();

					StokkartController stokkartController = new StokkartController();
					stokkartController.setModel(stokKartViewModel);

					StokkartView stokkartView = new StokkartView();
					stokkartView.setName("Stok Kartý");

					stokkartController.setView(stokkartView);

					stokKartViewModel.setKatno(katno);
					stokKartViewModel.setStno(stno);

					stokkartView.setSelectYil(new BigDecimal(((SiparisViewModel) controller.getModel()).getTarih().substring(0, 4)));

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

		siparisTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				int lastRow = e.getLastRow() == Integer.MAX_VALUE ? table.getRowCount() : e.getLastRow();
				for (int r = e.getFirstRow(); r <= lastRow; r++) {
					BigDecimal katno = (BigDecimal) siparisTableModel.getValueAt(r, 1);
					String stno = (String) siparisTableModel.getValueAt(r, 2);

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
		SiparisViewModel siparisViewModel = (SiparisViewModel) controller.getModel();

		boolean flag = false;
		if(!siparisViewModel.getSiparistipi().equals(StokModelContentAsist.getStokModelContentAsist().getService().getSiparistipi())) {
			StokModelContentAsist.getStokModelContentAsist().getService().setHesapno("");
			StokModelContentAsist.getStokModelContentAsist().getService().setYear(0);
			StokModelContentAsist.getStokModelContentAsist().getService().setSiparistipi(siparisViewModel.getSiparistipi());
			flag = true;
		}
		if (siparisViewModel.getHesapno() != null
				&& siparisViewModel.getSiparistipi().equals(SiparisListViewModel.Tip.VERME)
				&& !StokModelContentAsist.getStokModelContentAsist().getService().getHesapno()
						.equals(siparisViewModel.getHesapno())) {
			StokModelContentAsist.getStokModelContentAsist().getService().setHesapno(siparisViewModel.getHesapno());
			flag = true;
		}
		if (siparisViewModel.getTarih() != null && !siparisViewModel.getTarih().equals("")
				&& StokModelContentAsist.getStokModelContentAsist().getService().getYear() != Integer
						.parseInt(siparisViewModel.getTarih().substring(0, 4))) {
			StokModelContentAsist.getStokModelContentAsist().getService()
					.setYear(Integer.parseInt(siparisViewModel.getTarih().substring(0, 4)));
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
					((SiparisViewController) controller).load();
					siparisTableModel.fireTableDataChanged();
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtSiparisNo.requestFocus();
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
					((SiparisViewController) controller).loadStokModel(index);
					siparisTableModel.fireTableRowsUpdated(index, index);
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
					((SiparisViewController) controller).loadSicilModel();
					comboSevkSekli.requestFocus();
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
					((SiparisViewController) controller).save();
					((SiparisViewController) controller).load();
					siparisTableModel.fireTableDataChanged();
					table.requestFocus();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtSiparisNo.requestFocus();
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
					((SiparisViewController) controller).delete();
					endProcess();
					MyApp.removeView(SiparisView.this);
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtSiparisNo.requestFocus();
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
					JasperPrint jasperPrint = ((SiparisViewController) controller).print();
					endProcess();
					
					MyApp.getAppHandler().showDialog("Sipariþ Yazdýr", new FPrintPreview(jasperPrint));
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
					txtSiparisNo.requestFocus();
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
		txtSiparisNo.setEditable(false);
		txtTCNo.setEditable(false);
		txtTel.setEditable(false);
		txtAdres.setEditable(false);
		comboSevkSekli.setEditable(true);
		comboSiparisiAlan.setEditable(true);

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
				siparisTableModel.setValueAt(obj[0], table.getSelectedRow(), 1);
				siparisTableModel.setValueAt(obj[1], table.getSelectedRow(), 2);
			}

		}
	};

	private PropertyChangeListener pcs = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("siparistipi".equals(evt.getPropertyName()) ||"hesapno".equals(evt.getPropertyName()) || "tarih".equals(evt.getPropertyName()))
				viewActivated();
		}
	};

	@Override
	public void initBindings() {
		SiparisViewModel siparisViewModel = (SiparisViewModel) controller.getModel();
		siparisTableModel.setList(siparisViewModel.getSiparisDetayViewModels());
		comboSevkSekliModel.setList(siparisViewModel.getSevkseklis());
		comboSiparisiAlanModel.setList(siparisViewModel.getPlasiyers());
		siparisViewModel.addPropertyChangeListener(pcs);
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

		FBindings.add(txtHesapNo, siparisViewModel, "hesapno", FBindingModel.BOTH);
		FBindings.add(txtHesapAdi, siparisViewModel, "hesapadi", FBindingModel.BOTH);
		FBindings.add(txtYetkili, siparisViewModel, "yetkili", FBindingModel.BOTH);
		FBindings.add(txtTarih, siparisViewModel, "tarih", FBindingModel.BOTH);
		FBindings.add(comboSevkSekli, siparisViewModel, "sevksekli", FBindingModel.BOTH);
		FBindings.add(comboSiparisiAlan, siparisViewModel, "siparisialan", FBindingModel.BOTH);
		FBindings.add(txtNot, siparisViewModel, "note", FBindingModel.BOTH);
		if (((SiparisViewModel) controller.getModel()).getSiparistipi().equals(SiparisListViewModel.Tip.ALMA))
			FBindings.add(chkKampanyaliSatis, siparisViewModel, "kampanya", FBindingModel.BOTH);

		FBindings.add(txtVno, siparisViewModel, "vno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtTanim, siparisViewModel, "tanim", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtVda, siparisViewModel, "vda", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtSiparisNo, siparisViewModel, "id", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtTCNo, siparisViewModel, "tcno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtAdres, siparisViewModel, "adres", FBindingModel.MODEL_TO_VIEW);

	}

	@Override
	public void removeBindings() {
		SiparisViewModel siparisViewModel = (SiparisViewModel) controller.getModel();
		siparisViewModel.removePropertyChangeListener(pcs);
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

		siparisTableModel = new SiparisTableModel();
		table = new FTable(siparisTableModel);

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

		JLabel lblSiparisNo = new JLabel("Sipariþ No :");
		GridBagConstraints gbc_lblSiparisNo = new GridBagConstraints();
		gbc_lblSiparisNo.anchor = GridBagConstraints.WEST;
		gbc_lblSiparisNo.insets = new Insets(5, 5, 5, 5);
		gbc_lblSiparisNo.gridx = 0;
		gbc_lblSiparisNo.gridy = 0;
		add(lblSiparisNo, gbc_lblSiparisNo);

		txtSiparisNo = new FTextField();
		txtSiparisNo.setColumns(10);
		GridBagConstraints gbc_txtSiparisNo = new GridBagConstraints();
		gbc_txtSiparisNo.anchor = GridBagConstraints.NORTH;
		gbc_txtSiparisNo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSiparisNo.insets = new Insets(5, 0, 5, 5);
		gbc_txtSiparisNo.gridx = 1;
		gbc_txtSiparisNo.gridy = 0;
		add(txtSiparisNo, gbc_txtSiparisNo);
		JLabel lblTarih = new JLabel("Sipariþ Tarihi :");
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
		JLabel lblSevk = new JLabel("Sevk Þekli :");
		GridBagConstraints gbc_lblSevk = new GridBagConstraints();
		gbc_lblSevk.anchor = GridBagConstraints.WEST;
		gbc_lblSevk.insets = new Insets(0, 0, 5, 5);
		gbc_lblSevk.gridx = 5;
		gbc_lblSevk.gridy = 1;
		add(lblSevk, gbc_lblSevk);
		comboSevkSekliModel = new FComboBoxModel<>();
		comboSevkSekli = new FComboBox<String>(comboSevkSekliModel);
		GridBagConstraints gbc_comboSevkSekli = new GridBagConstraints();
		gbc_comboSevkSekli.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboSevkSekli.anchor = GridBagConstraints.NORTH;
		gbc_comboSevkSekli.insets = new Insets(0, 0, 5, 5);
		gbc_comboSevkSekli.gridx = 6;
		gbc_comboSevkSekli.gridy = 1;
		add(comboSevkSekli, gbc_comboSevkSekli);
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
		JLabel lblSiparisiAlan = new JLabel("Sipariþi Alan :");
		GridBagConstraints gbc_lblSiparisiAlan = new GridBagConstraints();
		gbc_lblSiparisiAlan.anchor = GridBagConstraints.WEST;
		gbc_lblSiparisiAlan.insets = new Insets(0, 0, 5, 5);
		gbc_lblSiparisiAlan.gridx = 5;
		gbc_lblSiparisiAlan.gridy = 2;
		add(lblSiparisiAlan, gbc_lblSiparisiAlan);
		comboSiparisiAlanModel = new FComboBoxModel<>();
		comboSiparisiAlan = new FComboBox<String>(comboSiparisiAlanModel);
		GridBagConstraints gbc_comboSiparisiAlan = new GridBagConstraints();
		gbc_comboSiparisiAlan.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboSiparisiAlan.anchor = GridBagConstraints.NORTH;
		gbc_comboSiparisiAlan.insets = new Insets(0, 0, 5, 5);
		gbc_comboSiparisiAlan.gridx = 6;
		gbc_comboSiparisiAlan.gridy = 2;
		add(comboSiparisiAlan, gbc_comboSiparisiAlan);
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

		if (((SiparisViewModel)controller.getModel()).getSiparistipi().equals(SiparisListViewModel.Tip.ALMA)) {
			chkKampanyaliSatis = new FCheckBox("Kampanyalý Satýþ");
			GridBagConstraints gbc_chkKampanyaliSatis = new GridBagConstraints();
			gbc_chkKampanyaliSatis.fill = GridBagConstraints.HORIZONTAL;
			gbc_chkKampanyaliSatis.gridwidth = 2;
			gbc_chkKampanyaliSatis.anchor = GridBagConstraints.NORTH;
			gbc_chkKampanyaliSatis.insets = new Insets(0, 0, 5, 0);
			gbc_chkKampanyaliSatis.gridx = 6;
			gbc_chkKampanyaliSatis.gridy = 5;
			add(chkKampanyaliSatis, gbc_chkKampanyaliSatis);
		}
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

		JPanel tablePanel = table.createPanelForFTable(true, siparisTableModel.getSiparisToplamTableModel());
		GridBagConstraints gbc_tablePanel = new GridBagConstraints();
		gbc_tablePanel.insets = new Insets(0, 5, 0, 0);
		gbc_tablePanel.fill = GridBagConstraints.BOTH;
		gbc_tablePanel.gridwidth = 8;
		gbc_tablePanel.gridx = 0;
		gbc_tablePanel.gridy = 8;
		add(tablePanel, gbc_tablePanel);

	}

}
