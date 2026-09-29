package tr.fikopar.modules.stokkart.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import tr.fikopar.modules.stokkart.controller.HareketController;
import tr.fikopar.modules.stokkart.controller.StokkartController;
import tr.fikopar.modules.stokkart.model.HareketViewModel;
import tr.fikopar.modules.stokkart.model.StokKartViewModel;
import tr.fikopar.swing.binding.FBindingModel;
import tr.fikopar.swing.binding.FBindings;
import tr.fikopar.swing.component.FNumericField;
import tr.fikopar.swing.component.FTabbedPane;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.view.FDialog;
import tr.fikopar.swing.view.FView;

public class StokkartView extends FView {
	private FTextField txtStokNo, txtOemNo, txtParcaNo, txtTipi, txtParcaadi, txtNot, txtMarka, txtDeg;
	private FNumericField txtFiyati, txtMin_adet, txtMax_adet, txtPaket;
	private JPanel resimPanel;
	private FTabbedPane tabbedPane;
	private BigDecimal selectYil = null;

	@Override
	public void initActions() {
		Action closeAction = new AbstractAction("Kapat") {

			@Override
			public void actionPerformed(ActionEvent e) {
				((FDialog) getRootPane().getParent()).dispose();
			}
		};
		getActionMap().put("kapatAction", closeAction);
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				"kapatAction");

	}

	@Override
	public void initComponents() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 270, 78, 122, 100, 59, 102 };
		gridBagLayout.rowHeights = new int[] { 30, 30, 30, 30, 30, 30, 30, 30, 30, 195 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		setLayout(gridBagLayout);

		resimPanel = new JPanel();
		resimPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Parça Resmi",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_resimPanel = new GridBagConstraints();
		gbc_resimPanel.fill = GridBagConstraints.BOTH;
		gbc_resimPanel.insets = new Insets(5, 4, 5, 5);
		gbc_resimPanel.gridheight = 9;
		gbc_resimPanel.gridx = 0;
		gbc_resimPanel.gridy = 0;
		add(resimPanel, gbc_resimPanel);

		JLabel lblStokNo = new JLabel("Stok No :");
		GridBagConstraints gbc_lblStokNo = new GridBagConstraints();
		gbc_lblStokNo.fill = GridBagConstraints.VERTICAL;
		gbc_lblStokNo.anchor = GridBagConstraints.WEST;
		gbc_lblStokNo.insets = new Insets(5, 0, 5, 5);
		gbc_lblStokNo.gridx = 1;
		gbc_lblStokNo.gridy = 0;
		add(lblStokNo, gbc_lblStokNo);

		txtStokNo = new FTextField();
		GridBagConstraints gbc_txtStokNo = new GridBagConstraints();
		gbc_txtStokNo.fill = GridBagConstraints.BOTH;
		gbc_txtStokNo.insets = new Insets(5, 0, 5, 5);
		gbc_txtStokNo.gridx = 2;
		gbc_txtStokNo.gridy = 0;
		add(txtStokNo, gbc_txtStokNo);

		JLabel lblOemNo = new JLabel("Oem No :");
		GridBagConstraints gbc_lblOemNo = new GridBagConstraints();
		gbc_lblOemNo.anchor = GridBagConstraints.WEST;
		gbc_lblOemNo.insets = new Insets(0, 0, 5, 5);
		gbc_lblOemNo.gridx = 1;
		gbc_lblOemNo.gridy = 1;
		add(lblOemNo, gbc_lblOemNo);

		txtOemNo = new FTextField();
		GridBagConstraints gbc_txtOemNo = new GridBagConstraints();
		gbc_txtOemNo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtOemNo.insets = new Insets(0, 0, 5, 5);
		gbc_txtOemNo.gridx = 2;
		gbc_txtOemNo.gridy = 1;
		add(txtOemNo, gbc_txtOemNo);
		JLabel lblParcaNo = new JLabel("Parça No :");
		GridBagConstraints gbc_lblParcaNo = new GridBagConstraints();
		gbc_lblParcaNo.anchor = GridBagConstraints.WEST;
		gbc_lblParcaNo.insets = new Insets(0, 0, 5, 5);
		gbc_lblParcaNo.gridx = 1;
		gbc_lblParcaNo.gridy = 2;
		add(lblParcaNo, gbc_lblParcaNo);
		txtParcaNo = new FTextField();
		GridBagConstraints gbc_txtParcaNo = new GridBagConstraints();
		gbc_txtParcaNo.gridwidth = 2;
		gbc_txtParcaNo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtParcaNo.insets = new Insets(0, 0, 5, 5);
		gbc_txtParcaNo.gridx = 2;
		gbc_txtParcaNo.gridy = 2;
		add(txtParcaNo, gbc_txtParcaNo);
		JLabel lblTipi = new JLabel("Tipi :");
		GridBagConstraints gbc_lblTipi = new GridBagConstraints();
		gbc_lblTipi.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTipi.insets = new Insets(0, 0, 5, 5);
		gbc_lblTipi.gridx = 1;
		gbc_lblTipi.gridy = 3;
		add(lblTipi, gbc_lblTipi);
		txtTipi = new FTextField();
		GridBagConstraints gbc_txtTipi = new GridBagConstraints();
		gbc_txtTipi.gridwidth = 2;
		gbc_txtTipi.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTipi.insets = new Insets(0, 0, 5, 5);
		gbc_txtTipi.gridx = 2;
		gbc_txtTipi.gridy = 3;
		add(txtTipi, gbc_txtTipi);
		JLabel lblParcaadi = new JLabel("Parça Adý :");
		GridBagConstraints gbc_lblParcaadi = new GridBagConstraints();
		gbc_lblParcaadi.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblParcaadi.insets = new Insets(0, 0, 5, 5);
		gbc_lblParcaadi.gridx = 1;
		gbc_lblParcaadi.gridy = 4;
		add(lblParcaadi, gbc_lblParcaadi);
		txtParcaadi = new FTextField();
		GridBagConstraints gbc_txtParcaadi = new GridBagConstraints();
		gbc_txtParcaadi.insets = new Insets(0, 0, 5, 5);
		gbc_txtParcaadi.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtParcaadi.gridwidth = 4;
		gbc_txtParcaadi.gridx = 2;
		gbc_txtParcaadi.gridy = 4;
		add(txtParcaadi, gbc_txtParcaadi);
		JLabel lblNot = new JLabel("Not :");
		GridBagConstraints gbc_lblNot = new GridBagConstraints();
		gbc_lblNot.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNot.insets = new Insets(0, 0, 5, 5);
		gbc_lblNot.gridx = 1;
		gbc_lblNot.gridy = 5;
		add(lblNot, gbc_lblNot);
		txtNot = new FTextField();
		GridBagConstraints gbc_txtNot = new GridBagConstraints();
		gbc_txtNot.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtNot.insets = new Insets(0, 0, 5, 5);
		gbc_txtNot.gridx = 2;
		gbc_txtNot.gridy = 5;
		add(txtNot, gbc_txtNot);
		JLabel lblMin_adet = new JLabel("Min Adet :");
		GridBagConstraints gbc_lblMin_adet = new GridBagConstraints();
		gbc_lblMin_adet.anchor = GridBagConstraints.WEST;
		gbc_lblMin_adet.insets = new Insets(0, 0, 5, 5);
		gbc_lblMin_adet.gridx = 4;
		gbc_lblMin_adet.gridy = 5;
		add(lblMin_adet, gbc_lblMin_adet);
		txtMin_adet = new FNumericField("#,##0.0000");
		GridBagConstraints gbc_txtMin_adet = new GridBagConstraints();
		gbc_txtMin_adet.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMin_adet.insets = new Insets(0, 0, 5, 5);
		gbc_txtMin_adet.gridx = 5;
		gbc_txtMin_adet.gridy = 5;
		add(txtMin_adet, gbc_txtMin_adet);
		JLabel lblMarka = new JLabel("Marka :");
		GridBagConstraints gbc_lblMarka = new GridBagConstraints();
		gbc_lblMarka.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblMarka.insets = new Insets(0, 0, 5, 5);
		gbc_lblMarka.gridx = 1;
		gbc_lblMarka.gridy = 6;
		add(lblMarka, gbc_lblMarka);
		txtMarka = new FTextField();
		GridBagConstraints gbc_txtMarka = new GridBagConstraints();
		gbc_txtMarka.gridwidth = 2;
		gbc_txtMarka.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMarka.insets = new Insets(0, 0, 5, 5);
		gbc_txtMarka.gridx = 2;
		gbc_txtMarka.gridy = 6;
		add(txtMarka, gbc_txtMarka);
		JLabel lblMax_adet = new JLabel("Max Adet :");
		GridBagConstraints gbc_lblMax_adet = new GridBagConstraints();
		gbc_lblMax_adet.anchor = GridBagConstraints.WEST;
		gbc_lblMax_adet.insets = new Insets(0, 0, 5, 5);
		gbc_lblMax_adet.gridx = 4;
		gbc_lblMax_adet.gridy = 6;
		add(lblMax_adet, gbc_lblMax_adet);
		txtMax_adet = new FNumericField("#,##0.0000");
		GridBagConstraints gbc_txtMax_adet = new GridBagConstraints();
		gbc_txtMax_adet.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMax_adet.insets = new Insets(0, 0, 5, 5);
		gbc_txtMax_adet.gridx = 5;
		gbc_txtMax_adet.gridy = 6;
		add(txtMax_adet, gbc_txtMax_adet);
		JLabel lblFiyat = new JLabel("Fiyatý :");
		GridBagConstraints gbc_lblFiyat = new GridBagConstraints();
		gbc_lblFiyat.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblFiyat.insets = new Insets(0, 0, 5, 5);
		gbc_lblFiyat.gridx = 1;
		gbc_lblFiyat.gridy = 7;
		add(lblFiyat, gbc_lblFiyat);
		txtFiyati = new FNumericField("#,##0.0000");
		GridBagConstraints gbc_txtFiyati = new GridBagConstraints();
		gbc_txtFiyati.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFiyati.insets = new Insets(0, 0, 5, 5);
		gbc_txtFiyati.gridx = 2;
		gbc_txtFiyati.gridy = 7;
		add(txtFiyati, gbc_txtFiyati);
		JLabel lblDeg = new JLabel("Deðiþim :");
		GridBagConstraints gbc_lblDeg = new GridBagConstraints();
		gbc_lblDeg.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDeg.insets = new Insets(0, 0, 5, 5);
		gbc_lblDeg.gridx = 4;
		gbc_lblDeg.gridy = 7;
		add(lblDeg, gbc_lblDeg);
		txtDeg = new FTextField();
		GridBagConstraints gbc_txtDeg = new GridBagConstraints();
		gbc_txtDeg.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDeg.insets = new Insets(0, 0, 5, 5);
		gbc_txtDeg.gridx = 5;
		gbc_txtDeg.gridy = 7;
		add(txtDeg, gbc_txtDeg);
		JLabel lblPaket = new JLabel("Paket :");
		GridBagConstraints gbc_lblPaket = new GridBagConstraints();
		gbc_lblPaket.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPaket.insets = new Insets(0, 0, 5, 5);
		gbc_lblPaket.gridx = 4;
		gbc_lblPaket.gridy = 8;
		add(lblPaket, gbc_lblPaket);
		txtPaket = new FNumericField("#,##0.0000");
		GridBagConstraints gbc_txtPaket = new GridBagConstraints();
		gbc_txtPaket.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPaket.insets = new Insets(0, 0, 5, 5);
		gbc_txtPaket.gridx = 5;
		gbc_txtPaket.gridy = 8;
		add(txtPaket, gbc_txtPaket);

		tabbedPane = new FTabbedPane(FTabbedPane.TOP);
		tabbedPane.setTabShape(FTabbedPane.SHAPE_WINDOWS);
		tabbedPane.setColorTheme(FTabbedPane.COLOR_THEME_WINXP);
		tabbedPane.setShowCloseButtonOnTab(false);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 5, 5, 5);
		gbc_tabbedPane.gridwidth = 6;
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 9;
		add(tabbedPane, gbc_tabbedPane);

	}

	public void setSelectYil(BigDecimal selectYil) {
		this.selectYil = selectYil;
	}
	private int selectedIndex = 0;
	@Override
	public void viewAdded() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					tabbedPane.setActivationFire(false);
					doProcess();
					((StokkartController) controller).loadStok();
					List<BigDecimal> list = ((StokKartViewModel) controller.getModel()).getYils();
					int i=0;
					for (Iterator<BigDecimal> itr = list.iterator(); itr.hasNext();) {
						BigDecimal yil = itr.next();
						HareketViewModel hareketViewModel = new HareketViewModel();
						
						HareketController hareketController = new HareketController();
						hareketController.setModel(hareketViewModel);
						
						HareketView hareketView = new HareketView();
						hareketController.setView(hareketView);
						hareketView.setFirstView(false);
						
						hareketViewModel.setYil(yil);
						hareketViewModel.setKatno(((StokKartViewModel) controller.getModel()).getKatno());
						hareketViewModel.setStno(((StokKartViewModel) controller.getModel()).getStno());
						
						tabbedPane.add(hareketViewModel.getYil().toString(), hareketView);
						if (selectYil != null && yil.compareTo(selectYil) == 0)
							selectedIndex = i;
						i++;
					}
					System.out.println(selectYil);
				} catch (Exception ex) {
					controller.uncaughtException(Thread.currentThread(), ex);
				} finally {
					endProcess();
					tabbedPane.setSelectedIndex(-1);
					tabbedPane.setActivationFire(true);
					tabbedPane.setSelectedIndex(selectedIndex);
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							tabbedPane.requestFocus();		
						}
					});
				}
			}
		});
		thread.start();
	}

	@Override
	public void initProperties() {
		txtStokNo.setEditable(false);
		txtOemNo.setEditable(false);
		txtParcaNo.setEditable(false);
		txtTipi.setEditable(false);
		txtParcaadi.setEditable(false);
		txtNot.setEditable(false);
		txtMin_adet.setEditable(false);
		txtMarka.setEditable(false);
		txtMax_adet.setEditable(false);
		txtFiyati.setEditable(false);
		txtDeg.setEditable(false);
		txtPaket.setEditable(false);

	}

	@Override
	public void initBindings() {

		FBindings.add(txtStokNo, controller.getModel(), "stno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtOemNo, controller.getModel(), "oemno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtParcaNo, controller.getModel(), "prcno", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtTipi, controller.getModel(), "tipi", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtParcaadi, controller.getModel(), "parcaadi", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtNot, controller.getModel(), "note", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtMarka, controller.getModel(), "marka", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtDeg, controller.getModel(), "deg", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtFiyati, controller.getModel(), "fiyat", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtMin_adet, controller.getModel(), "minadet", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtMax_adet, controller.getModel(), "maxadet", FBindingModel.MODEL_TO_VIEW);
		FBindings.add(txtPaket, controller.getModel(), "paket", FBindingModel.MODEL_TO_VIEW);

	}

	@Override
	public void viewActivated() {
	}

}
