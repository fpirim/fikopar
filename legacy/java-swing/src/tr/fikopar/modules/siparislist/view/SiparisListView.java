package tr.fikopar.modules.siparislist.view;

import java.awt.Component;
import java.awt.Event;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import net.sf.jasperreports.engine.JasperPrint;
import sun.audio.AudioPlayer;
import tr.fikopar.app.MyApp;
import tr.fikopar.app.MyProperties;
import tr.fikopar.modules.siparis.controller.SiparisViewController;
import tr.fikopar.modules.siparis.model.SiparisViewModel;
import tr.fikopar.modules.siparis.view.SiparisView;
import tr.fikopar.modules.siparislist.controller.SiparisListController;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.db.models.FUserTrashModel;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCellRenderer;
import tr.fikopar.swing.view.FPrintPreview;
import tr.fikopar.swing.view.FPropertiesAdapter;
import tr.fikopar.swing.view.FView;

public class SiparisListView extends FView implements ActionListener {

	private FTable table;
	private SiparisListTableModel siparisListTableModel;
	private FTextField araText;
	private ButtonGroup group;
	private JRadioButton bekleyenButton, tamamlananButton, hepsiButton;

	public void initComponents() {
		siparisListTableModel = new SiparisListTableModel();
		table = new FTable(siparisListTableModel);
		JPanel tablePanel = table.createPanelForFTable(true, siparisListTableModel.getSiparisListToplamTableModel());

		table.setDefaultRenderer(new FTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				SiparisListViewModel siparisListViewModel = (SiparisListViewModel) controller.getModel();
				if (siparisListViewModel.isRead(siparisListViewModel.getSiparisListViewDetayModels().get(row)))
					comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
				else
					comp.setFont(comp.getFont().deriveFont(Font.BOLD));
				return comp;

			}
		});

		group = new ButtonGroup();

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel siparislerLabel = new JLabel("Sipariþler :");

		bekleyenButton = new JRadioButton("Bekleyen");
		group.add(bekleyenButton);

		tamamlananButton = new JRadioButton("Tamamlanan");
		group.add(tamamlananButton);

		hepsiButton = new JRadioButton("Hepsi");
		group.add(hepsiButton);

		JLabel araLabel = new JLabel("Ara :");
		araText = new FTextField();

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING,
				groupLayout.createSequentialGroup().addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(tablePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
								.addGroup(Alignment.LEADING,
										groupLayout.createSequentialGroup().addComponent(siparislerLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(bekleyenButton)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(tamamlananButton).addPreferredGap(ComponentPlacement.RELATED).addComponent(hepsiButton))
				.addGroup(Alignment.LEADING,
						groupLayout.createSequentialGroup().addComponent(araLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(araText, GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)))
				.addContainerGap()));
		groupLayout
				.setVerticalGroup(
						groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addContainerGap()
										.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(araLabel)
												.addComponent(araText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(bekleyenButton).addComponent(tamamlananButton).addComponent(hepsiButton)
										.addComponent(siparislerLabel)).addPreferredGap(ComponentPlacement.RELATED).addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
				.addContainerGap()));
		setLayout(groupLayout);

	}

	public void initActions() {
		/*
		 * YENÝ
		 */
		registerAction(new AbstractAction("Yeni Kayýt", UIManager.getIcon("yeniIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				SiparisListViewModel siparisListViewModel = (SiparisListViewModel) controller.getModel();

				SiparisViewModel siparisViewModel = new SiparisViewModel();

				SiparisViewController siparisController = new SiparisViewController();
				siparisController.setModel(siparisViewModel);

				SiparisView siparisView = new SiparisView();

				siparisViewModel.setSiparistipi(siparisListViewModel.getTip());
				siparisController.setView(siparisView);

				if (siparisListViewModel.getTip().equals(SiparisListViewModel.Tip.ALMA))
					siparisView.setName("Sipariþ Alma - Yeni Sipariþ");
				else
					siparisView.setName("Sipariþ Verme - Yeni Sipariþ");

				MyApp.addView(siparisView.getName(), siparisView);
			}

		}, "F2 - Yeni", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "yeniAction");

		/*
		 * DÜZENLE
		 */
		registerAction(new AbstractAction("Düzenle", UIManager.getIcon("duzenleIcon")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (table.getSelectedRow() != -1) {

					SiparisListViewModel siparisListViewModel = (SiparisListViewModel) controller.getModel();

					SiparisViewModel siparisViewModel = new SiparisViewModel();

					SiparisViewController siparisController = new SiparisViewController();
					siparisController.setModel(siparisViewModel);

					SiparisView siparisView = new SiparisView();

					siparisViewModel.setSiparistipi(siparisListViewModel.getTip());

					siparisController.setView(siparisView);

					if (siparisListViewModel.getTip().equals(SiparisListViewModel.Tip.ALMA))
						siparisView.setName("Sipariþ Alma - Düzenle");
					else
						siparisView.setName("Sipariþ Verme - Düzenle");
					
					int index = table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
					siparisViewModel.setId(siparisListViewModel.getSiparisListViewDetayModels().get(index).getId());
					
					String key = "siparis_"+siparisViewModel.getSiparistipi()+"_"+siparisViewModel.getId();
					if(!siparisListViewModel.getTrashMap().containsKey(key)) {
						FUserTrashModel trashModel = new FUserTrashModel(key);
						FPropertiesAdapter.writeTrashProperty(trashModel);
						siparisListViewModel.getTrashMap().put(key, trashModel);
					}
					MyApp.addView(siparisView.getName(), siparisView);

				}

			}
		}, "ENTER - Düzenle", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "duzenleAction");

		/*
		 * YAZDIR
		 */
		registerAction(new AbstractAction("Bakiye Yazdýr", UIManager.getIcon("yazdirIcon")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (table.getSelectedRow() == -1)
					return;
				int index = table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
				JasperPrint jasperPrint = ((SiparisListController) controller).printBakiye(index);
				if (jasperPrint == null)
					return;
				MyApp.getAppHandler().showDialog("Bakiye Yazdýr", new FPrintPreview(jasperPrint));

			}
		}, "CTRL+P - Bakiye Yazdýr", KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "printAction");
	}

	private ActionListener timerAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			if (actionPerformed)
				return;
			loadContent();
		};

	};

	private void loadContent() {
		SiparisListViewModel siparisListViewModel = (SiparisListViewModel) controller.getModel();
		siparisListViewModel.setDurum(Enum.valueOf(SiparisListViewModel.Durum.class, group.getSelection().getActionCommand()));
		if (((SiparisListController) controller).load()) {
			siparisListTableModel.fireTableDataChanged();
			if(!siparisListViewModel.isAllRead())
				AudioPlayer.player.start(MyApp.class.getResourceAsStream("/tr/fikopar/resources/newmessage.wav"));
		}
	}

	private Boolean actionPerformed = true;

	@Override
	public void actionPerformed(ActionEvent e) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				actionPerformed = true;
				doProcess();
				loadContent();
				endProcess();
				actionPerformed = false;
			}
		});
		t.start();
	}

	@Override
	public void viewActivated() {
		actionPerformed(null);
	}

	private Timer timer = new Timer(Integer.parseInt(MyApp.getProperties().getUserProperties(MyProperties.SIPARIS_REFRESH_RATE)), timerAction);

	public void viewAdded() {
		timer.setRepeats(true);
		timer.start();
		actionPerformed(null);
	}

	@Override
	public void initProperties() {
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFocusable(false);
		table.setAutoFilter(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2)
					((JButton) getComponentByActionCommand("duzenleAction")).doClick();
			}
		});

		table.getRowSorter().addRowSorterListener(siparisListTableModel.getSiparisListToplamTableModel());

		bekleyenButton.setActionCommand(SiparisListViewModel.Durum.BEKLEYEN.toString());
		bekleyenButton.setName("bekleyenButton");
		bekleyenButton.addActionListener(this);
		bekleyenButton.setSelected(true);
		bekleyenButton.setFocusable(false);

		tamamlananButton.setActionCommand(SiparisListViewModel.Durum.TAMAMLANAN.toString());
		tamamlananButton.setName("tamamlananButton");
		tamamlananButton.addActionListener(this);
		tamamlananButton.setFocusable(false);

		hepsiButton.setActionCommand(SiparisListViewModel.Durum.HEPSI.toString());
		hepsiButton.setName("hepsiButton");
		hepsiButton.addActionListener(this);
		hepsiButton.setSelected(false);
		hepsiButton.setFocusable(false);

		araText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.isActionKey()) {
					KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
					Action action = (Action) table.getActionForKeyStroke(keyStroke);
					if (action != null) {
						SwingUtilities.notifyAction((Action) table.getActionForKeyStroke(keyStroke), keyStroke, e, table, e.getModifiers());
						e.consume();
					}
					e.consume();
				}
			}
		});
		table.addFilter(araText);

	}

	@Override
	public void initBindings() {
		siparisListTableModel.setList(((SiparisListViewModel) controller.getModel()).getSiparisListViewDetayModels());
	}

	@Override
	public void removeBindings() {
		timer.stop();
		super.removeBindings();
	}

}
