package tr.fikopar.modules.stokkart.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import tr.fikopar.modules.stokkart.controller.HareketController;
import tr.fikopar.modules.stokkart.model.HareketViewModel;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.view.FView;

public class HareketView extends FView {
	
	private FTable girisTable, cikisTable;
	private HareketTableModel girisTableModel, cikisTableModel;
	
	@Override
	public void initComponents() {
		girisTableModel = new HareketTableModel(HareketTableModel.GIRIS);
		cikisTableModel = new HareketTableModel(HareketTableModel.CIKIS);

		girisTable = new FTable(girisTableModel);
		cikisTable = new FTable(cikisTableModel);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JPanel girisTablePanel = girisTable.createPanelForFTable(true, girisTableModel.getHareketToplamTableModel());
		GridBagConstraints gbc_girisTable = new GridBagConstraints();
		gbc_girisTable.insets = new Insets(0, 0, 5, 0);
		gbc_girisTable.fill = GridBagConstraints.BOTH;
		gbc_girisTable.gridx = 0;
		gbc_girisTable.gridy = 0;
		add(girisTablePanel, gbc_girisTable);

		JPanel cikisTablePanel = cikisTable.createPanelForFTable(true, cikisTableModel.getHareketToplamTableModel());
		GridBagConstraints gbc_cikisTable = new GridBagConstraints();
		gbc_cikisTable.fill = GridBagConstraints.BOTH;
		gbc_cikisTable.gridx = 0;
		gbc_cikisTable.gridy = 1;
		add(cikisTablePanel, gbc_cikisTable);
	}
	private void load() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HareketViewModel hareketViewModel = (HareketViewModel) controller.getModel();
					doProcess(hareketViewModel.getYil()+" yýlý stok hareketleri yükleniyor ...");
					((HareketController) controller).loadHarekets();
					girisTableModel.fireTableDataChanged();
					cikisTableModel.fireTableDataChanged();
				} catch (Exception ex) {
					controller.uncaughtException(Thread.currentThread(), ex);
				} finally {
					endProcess();
				}
			}
		});
		thread.start();

	}
	
	@Override
	public void viewAdded() {
		//load();
	}
	
	public void viewActivated() {
		load();
		HareketViewModel hareketViewModel = (HareketViewModel) controller.getModel();
		System.out.println(hareketViewModel.getYil()+" activated.");
	}

	@Override
	public void initActions() {}

	@Override
	public void initProperties() {
		girisTable.setColumnSelectionAllowed(false);
		girisTable.setRowSelectionAllowed(true);
		cikisTable.setColumnSelectionAllowed(false);
		cikisTable.setRowSelectionAllowed(true);
	}

	@Override
	public void initBindings() {
		HareketViewModel hareketViewModel = (HareketViewModel) controller.getModel();
		girisTableModel.setList(hareketViewModel.getGirishareketDetayModels());
		cikisTableModel.setList(hareketViewModel.getCikishareketDetayModels());
	}


}
