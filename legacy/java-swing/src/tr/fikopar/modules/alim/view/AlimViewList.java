package tr.fikopar.modules.alim.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tr.fikopar.modules.alim.controller.AlimViewListController;
import tr.fikopar.modules.alim.model.AlimViewModel;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.view.FView;

public class AlimViewList extends FView {

	private AlimViewListTableModel alimViewListTableModel;
	private FTable table;

	@Override
	public void viewAdded() {
		alimViewListTableModel.setList(((AlimViewListController) controller).loadAlimViewModels());
		table.requestFocus();
	}

	@Override
	public void viewActivated() {
	}

	@Override
	public void initComponents() {

		alimViewListTableModel = new AlimViewListTableModel();
		table = new FTable(alimViewListTableModel);

		setLayout(new BorderLayout());
		add(table.createPanelForFTable(true), BorderLayout.CENTER);
	}

	@Override
	public void initActions() {
		// Seç Action
		registerAction(new AbstractAction("Seç", UIManager.getIcon("secIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() == -1)
					return;

				controller.setModel((AlimViewModel) alimViewListTableModel.getList().get(table.getSelectedRow()));
				SwingUtilities.getWindowAncestor(AlimViewList.this).dispose();
			}
		}, "ENTER - Seç", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "actSec");
		// Kapat Action
		registerAction(new AbstractAction("Kapat", UIManager.getIcon("kapatIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setModel(null);
				SwingUtilities.getWindowAncestor(AlimViewList.this).dispose();
			}
		}, "ESC - Kapat", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "actKapat");

	}

	@Override
	public void initProperties() {
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2)
					((JButton) getComponentByActionCommand("actSec")).doClick();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					e.consume();
					((JButton) getComponentByActionCommand("actSec")).doClick();
				}
				super.keyReleased(e);
			}
		});
	}

	@Override
	public void initBindings() {
	}

}
