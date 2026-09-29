package tr.fikopar.liste.view;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.sf.jasperreports.engine.JasperPrint;
import tr.fikopar.app.MyApp;
import tr.fikopar.common.components.FDialog;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.common.view.AbstractView;
import tr.fikopar.common.view.PrintPreview;
import tr.fikopar.liste.controller.ListeController;
import tr.fikopar.liste.model.ListeBolumModel;
import tr.fikopar.liste.model.ListeModel;
import tr.fikopar.modules.stokkart.controller.StokkartController;
import tr.fikopar.modules.stokkart.view.StokkartView;
import tr.fikopar.swing.table.FTable;

public class ListeView extends AbstractView {

	private ListeTableModel listeTableModel;
	private FTable table;
	private FTableKampanyaCellEditor kampanyaCellEditor;

	// private FTable popupTable;
	// private PopupTableModel popupTableModel;
	public ListeView(AbstractController controller) {
		super(controller);
		init();
		initActions();
	}
	private void init() {
		
		listeTableModel = new ListeTableModel();
		table = new FTable(listeTableModel);
		table.setAutoFilter(true);

		table.getColumnModel().getColumn(15).setCellEditor(kampanyaCellEditor = new FTableKampanyaCellEditor());
		kampanyaCellEditor.getPopup().addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				refreshPopup();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		JPanel tablePanel = table.createPanelForFTable(true);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
				groupLayout.createSequentialGroup().addContainerGap().addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE).addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
				groupLayout.createSequentialGroup().addContainerGap().addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE).addContainerGap()));
		setLayout(groupLayout);

		
	}
	private void initActions() {
		// Kaydet Button
		JButton btnKaydet = new JButton(new AbstractAction("Kaydet", UIManager.getIcon("kaydetIcon")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						doProcess();
						try {
							((ListeController) controller).save();
							listeTableModel.setMap(((ListeController) controller).loadMap());
							configureRowHeights();
						} catch (Exception ex) {
							controller.uncaughtException(Thread.currentThread(), ex);
						} finally {
							endProcess();
						}
					}
				});
				thread.start();
			}
		});
		btnKaydet.setToolTipText("CTRL+S - Kaydet");
		getActionMap().put("kaydetAction", btnKaydet.getAction());
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), "kaydetAction");
		btnKaydet.setName("kaydet");
		
		// Stok Kartý Button
		JButton btnStokKarti = new JButton(new AbstractAction("Stok Kartý", UIManager.getIcon("duzenleIcon")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						if (table.getSelectedRow() != -1 && listeTableModel.getObjectAt(table.getSelectedRow()) instanceof ListeModel) {
							ListeModel listeModel = (ListeModel) listeTableModel.getObjectAt(table.getSelectedRow());
							StokkartController stokkartController = new StokkartController(listeModel.getKatno(), listeModel.getStno());
							StokkartView stokkartView = new StokkartView(stokkartController);
							FDialog dialog = new FDialog(MyApp.appHandler, "Stok Kartý", true);
							dialog.addView("Stok Kartý", stokkartView);
							dialog.setSize(1200, 800);
							dialog.setVisible(true);
						}
					}
				});
				thread.start();
			}
		});
		btnStokKarti.setToolTipText("F9 - Stok Kartý");
		getActionMap().put("stokKartiAction", btnStokKarti.getAction());
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "stokKartiAction");
		btnStokKarti.setName("stokkarti");
		
		// Liste Yazdýr Button
		JButton btnListeYazdir = new JButton(new AbstractAction("Liste Yazdýr", UIManager.getIcon("yazdirIcon")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						RowSorter<?> sorter = table.getRowSorter();
						List<Object> arr = new ArrayList<Object>();
						
						for(int i=0; i<sorter.getViewRowCount(); i++) {
							arr.add((Object) listeTableModel.getObjectAt(sorter.convertRowIndexToModel(i)));
							
						}
						JasperPrint jasperPrint = ((ListeController) controller).print(arr, "FÝYAT LÝSTESÝ", false);
						if (jasperPrint == null)
							return;
						MyApp.addView(new PrintPreview(jasperPrint));
					}
				});
				thread.start();
			}
		});
		btnListeYazdir.setToolTipText("CTRL+P - Liste Yazdýr");
		getActionMap().put("listeYazdirAction", btnListeYazdir.getAction());
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "listeYazdirAction");
		btnListeYazdir.setName("listeyazdir");
		
		// Kampanya Yazdir Button
		// Liste Yazdýr Button
		JButton btnKampanyaYazdir = new JButton(new AbstractAction("Kampanya Yazdýr", UIManager.getIcon("yazdirIcon")) {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								RowSorter<?> sorter = table.getRowSorter();
								List<Object> arr = new ArrayList<Object>();
								
								for(int i=0; i<sorter.getViewRowCount(); i++) {
									Object obj = listeTableModel.getObjectAt(sorter.convertRowIndexToModel(i));
									if(obj instanceof ListeModel) {
										ListeModel model = (ListeModel) obj;
										if(model.getKampanya()!=null && model.getKampanya().compareTo(BigDecimal.ZERO) != 0)
											arr.add(obj);
									} else if(obj != null) {
										if(arr.size()!=0 && arr.get(arr.size()-1) instanceof ListeBolumModel)
											arr.remove(arr.size()-1);
										arr.add(obj);
									}
								}
								while(arr.size()!=0 && arr.get(arr.size()-1) instanceof ListeBolumModel)
										arr.remove(arr.size()-1);
								JasperPrint jasperPrint = ((ListeController) controller).print(arr, "KAMPANYA LÝSTESÝ", true);
								if (jasperPrint == null)
									return;
								MyApp.addView(new PrintPreview(jasperPrint));
							}
						});
						thread.start();
					}
				});
		btnKampanyaYazdir.setToolTipText("CTRL+K - Liste Yazdýr");
				getActionMap().put("kampanyaYazdirAction", btnKampanyaYazdir.getAction());
				getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK), "kampanyaYazdirAction");
				btnKampanyaYazdir.setName("kampanyayazdir");
				
		registerComponent(btnKaydet);
		registerComponent(btnStokKarti);
		registerComponent(btnListeYazdir);
		registerComponent(btnKampanyaYazdir);
	}
	protected void refreshPopup() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ListeModel listeModel = (ListeModel) listeTableModel.getObjectAt(table.getSelectedRow());
					kampanyaCellEditor.getKampanyaTableModel().setList(((ListeController) controller).loadKampanya(listeModel.getKatno(), listeModel.getStno()));
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
	public void viewSelected() {
		refresh();
	}

	public void refresh() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					listeTableModel.setMap(((ListeController) controller).loadMap());
					configureRowHeights();
				} catch (Exception ex) {
					controller.uncaughtException(Thread.currentThread(), ex);
				} finally {
					endProcess();
					
				}
			}
		});
		thread.start();
	}

	private void configureRowHeights() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				table.getfTableAutoFilter().clearTemproraryFilters();
				for (int i = 0; i < listeTableModel.getRowCount(); i++)
					if (listeTableModel.getObjectAt(i) != null && listeTableModel.getObjectAt(i) instanceof ListeBolumModel)
						table.setRowHeight(i, table.getRowHeight() * 2);
				table.getfTableAutoFilter().applyTemproraryFilters();
			}
		});
		
	}
	

}
