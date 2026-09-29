package tr.fikopar.eksik.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;
import tr.fikopar.app.MyApp;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.common.view.AbstractView;
import tr.fikopar.eksik.controller.EksikController;
import tr.fikopar.swing.TableHeaderCheckBoxCellRenderer;
import tr.fikopar.swing.db.models.FTableCellStyle;
import tr.fikopar.swing.table.FTable;
import tr.fikopar.swing.table.FTableCheckBoxCellRenderer;
import tr.fikopar.swing.table.MyTableCellRenderer;
import tr.fikopar.swing.unused._CheckBoxTableCellEditor;
import tr.fikopar.swing.ButtonDropdownFactory;

import com.itextpdf.text.Font;

public class EksikListView extends AbstractView {

	private EksikListTableModel eksikListTableModel;
	private EksikTableModel eksikTableModel;
	private FTable topTable;
	private FTable bottomTable;

	public EksikListView(AbstractController controller) {
		super(controller);

		eksikListTableModel = new EksikListTableModel(
				(EksikController) controller);
		eksikTableModel = new EksikTableModel((EksikController) controller);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(
				Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 426,
								Short.MAX_VALUE).addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
				Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 276,
								Short.MAX_VALUE).addContainerGap()));

		JPanel topPanel = new JPanel();
		splitPane.setLeftComponent(topPanel);
		topPanel.setLayout(new BorderLayout(0, 0));

		topTable = new FTable(eksikListTableModel);
		topPanel.add(topTable);
		topTable.getTable().setOpaque(true);
		topTable.getTable().setCellSelectionEnabled(false);
		topTable.getTable().setRowSelectionAllowed(true);
		topTable.getTable().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		JPanel bottomPanel = new JPanel();
		splitPane.setRightComponent(bottomPanel);
		bottomPanel.setLayout(new BorderLayout(0, 0));

		bottomTable = new FTable(eksikTableModel);
		bottomTable.getTable().setOpaque(true);
		bottomTable.getTable().setCellSelectionEnabled(false);
		bottomTable.getTable().setRowSelectionAllowed(true);
		bottomTable.getTable().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		bottomPanel.add(bottomTable);

		topTable.getTable().getColumnModel().getColumn(0).setPreferredWidth(50); // selected
		topTable.getTable().getColumnModel().getColumn(1).setPreferredWidth(50); // katno
		topTable.getTable().getColumnModel().getColumn(2).setPreferredWidth(100); // hesapno
		topTable.getTable().getColumnModel().getColumn(3).setPreferredWidth(220); // aciklama
		topTable.getTable().getColumnModel().getColumn(4).setPreferredWidth(70); // min adet
		topTable.getTable().getColumnModel().getColumn(5).setPreferredWidth(70); // max adet
		topTable.getTable().getColumnModel().getColumn(6).setPreferredWidth(70); // sip adet
		topTable.getTable().getColumnModel().getColumn(7).setPreferredWidth(70); // ver adet
		topTable.getTable().getColumnModel().getColumn(8).setPreferredWidth(100); // min tutar
		topTable.getTable().getColumnModel().getColumn(9).setPreferredWidth(100); // max tutar
		topTable.getTable().getColumnModel().getColumn(10).setPreferredWidth(100); // sip tutar
		topTable.getTable().getColumnModel().getColumn(11).setPreferredWidth(100); // ver tutar
		topTable.getTable().getColumnModel().getColumn(12).setPreferredWidth(70); // ver tutar

		topTable.getTable().getColumnModel().getColumn(4).setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD, "#,##0")));
		topTable.getTable().getColumnModel().getColumn(5).setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD, "#,##0")));
		topTable.getTable().getColumnModel().getColumn(6).setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD, "#,##0", Color.RED)));
		topTable.getTable().getColumnModel().getColumn(7).setCellRenderer(
				new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD, "#,##0", Color.BLUE)));
		topTable.getTable().getColumnModel().getColumn(8).setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,	"#,##0.00")));
		topTable.getTable().getColumnModel().getColumn(9).setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.00")));
		topTable.getTable().getColumnModel().getColumn(10).setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,	"#,##0.00", Color.RED)));
		topTable.getTable().getColumnModel().getColumn(11).setCellRenderer(
				new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,	"#,##0.00", Color.BLUE)));
		topTable.getTable().getColumnModel().getColumn(12).setCellRenderer(
				new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,	"#,##0", Color.MAGENTA)));

		
		
		
		TableColumn tc = topTable.getTable().getColumnModel().getColumn(0);
		tc.setCellRenderer(new FTableCheckBoxCellRenderer());
		tc.setCellEditor(new _CheckBoxTableCellEditor());
		tc.setHeaderRenderer(new TableHeaderCheckBoxCellRenderer(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Object source = e.getSource();
				if (source instanceof AbstractButton == false)
					return;
				boolean checked = e.getStateChange() == ItemEvent.SELECTED;
				for (int x = 0, y = eksikListTableModel.getRowCount(); x < y; x++) {
					eksikListTableModel.setValueAt(new Boolean(checked), x, 0);
				}
			}
		}).setIntialValue(true));

		setLayout(groupLayout);

		bottomTable.getTable().getColumnModel().getColumn(0)
				.setPreferredWidth(50); // katno
		bottomTable.getTable().getColumnModel().getColumn(1)
				.setPreferredWidth(90); // stokno
		bottomTable.getTable().getColumnModel().getColumn(2)
				.setPreferredWidth(120); // parcano
		bottomTable.getTable().getColumnModel().getColumn(3)
				.setPreferredWidth(120); // oemno
		bottomTable.getTable().getColumnModel().getColumn(4)
				.setPreferredWidth(150); // tipi
		bottomTable.getTable().getColumnModel().getColumn(5)
				.setPreferredWidth(300); // cinsi
		bottomTable.getTable().getColumnModel().getColumn(6)
				.setPreferredWidth(120); // marka
		bottomTable.getTable().getColumnModel().getColumn(7)
				.setPreferredWidth(30); // note
		bottomTable.getTable().getColumnModel().getColumn(8)
				.setPreferredWidth(90); // fiyat
		bottomTable.getTable().getColumnModel().getColumn(9)
				.setPreferredWidth(50); // min
		bottomTable.getTable().getColumnModel().getColumn(10)
				.setPreferredWidth(50); // max
		bottomTable.getTable().getColumnModel().getColumn(11)
				.setPreferredWidth(50); // sipariþ
		bottomTable.getTable().getColumnModel().getColumn(12)
				.setPreferredWidth(90); // adet
		bottomTable.getTable().getColumnModel().getColumn(13)
				.setPreferredWidth(50); // min_eksik
		bottomTable.getTable().getColumnModel().getColumn(14)
				.setPreferredWidth(50); // max_eksik
		bottomTable.getTable().getColumnModel().getColumn(15)
				.setPreferredWidth(50); // siparis_eksik
		bottomTable.getTable().getColumnModel().getColumn(16)
				.setPreferredWidth(50); // ver_eksik
		bottomTable.getTable().getColumnModel().getColumn(17)
				.setPreferredWidth(90); // maliyeti
		bottomTable.getTable().getColumnModel().getColumn(18)
				.setPreferredWidth(60); // iskonto
		bottomTable.getTable().getColumnModel().getColumn(19)
				.setPreferredWidth(60); // kdv
		bottomTable.getTable().getColumnModel().getColumn(20)
				.setPreferredWidth(90); // rafyeri
		bottomTable.getTable().getColumnModel().getColumn(21)
				.setPreferredWidth(40); // paket

		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(8)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.NORMAL,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(9)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.NORMAL,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(10)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.NORMAL,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(11)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(12)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.NORMAL,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(13)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(14)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(15)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.00", Color.RED)));
		bottomTable
		.getTable()
		.getColumnModel()
		.getColumn(16)
		.setCellRenderer(
				new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
						"#,##0.00", Color.BLUE)));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(17)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.00")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(18)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.0000")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(19)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD,
								"#,##0.0000")));
		bottomTable
				.getTable()
				.getColumnModel()
				.getColumn(21)
				.setCellRenderer(
						new MyTableCellRenderer(new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD, "#,##0")));

		TableRowSorter<EksikTableModel> sorter = new TableRowSorter<EksikTableModel>(
				eksikTableModel);
		bottomTable.getTable().setRowSorter(sorter);

		sorter.setRowFilter(new RowFilter<EksikTableModel, Integer>() {

			@Override
			public boolean include(
					javax.swing.RowFilter.Entry<? extends EksikTableModel, ? extends Integer> entry) {
				EksikTableModel eksikTableModel = entry.getModel();

				BigDecimal minAdet = (BigDecimal) eksikTableModel.getValueAt(
						entry.getIdentifier(), 13);
				BigDecimal maxAdet = (BigDecimal) eksikTableModel.getValueAt(
						entry.getIdentifier(), 14);
				BigDecimal sipAdet = (BigDecimal) eksikTableModel.getValueAt(
						entry.getIdentifier(), 15);
				if (minAdet.compareTo(BigDecimal.ZERO) == 0
						&& maxAdet.compareTo(BigDecimal.ZERO) == 0
						&& sipAdet.compareTo(BigDecimal.ZERO) == 0)
					return false;
				return true;
			}

		});

		topTable.getTable().getSelectionModel()
				.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						int row = topTable.getTable().getSelectedRow();
						Integer value = 0;
						if (row != -1)
							value = (Integer) eksikListTableModel.getValueAt(
									row, 1);
						eksikTableModel.refresh(value);
					}
				});

		registerComponent(new JButton(new AbstractAction("Refresh") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				((EksikController) EksikListView.this.controller).refresh();
				eksikListTableModel.refresh();
			}
		}));
		registerComponent(new JButton(new AbstractAction("Bakiye Sipariþ") {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JasperPrint jasperPrint = ((EksikController) EksikListView.this.controller)
						.print(eksikListTableModel);
				JRViewer jrViewer = new JRViewer(jasperPrint);

				AbstractView abstractView = new AbstractView(null) {

				};
				abstractView.setName("Eksik Bakiye Sipariþ");
				abstractView.setLayout(new BorderLayout());
				abstractView.add(jrViewer, BorderLayout.CENTER);
				MyApp.addView(abstractView);

			}
		}));
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new AbstractAction("Sipariþ Adet'e göre sipariþ oluþtur") {
				@Override
				public void actionPerformed(ActionEvent e) {
					createSiparis(EksikController.SIPARIS_ADET_GORE);
				}
			});
		popupMenu.add(new AbstractAction("Minimum Adet'e göre sipariþ oluþtur") {
			@Override
			public void actionPerformed(ActionEvent e) {
				createSiparis(EksikController.MINIMUM_ADET_GORE);
			}
		});
		popupMenu.add(new AbstractAction("Maximum Adet'e göre sipariþ oluþtur") {
			@Override
			public void actionPerformed(ActionEvent e) {
				createSiparis(EksikController.MAXIMUM_ADET_GORE);
			}
		});
		registerComponent(ButtonDropdownFactory.createDropDownButton(new ImageIcon("tr/fikopar/swing/icons/1428611432_ark2.png"),popupMenu));
	}
	private void createSiparis(int type) {
		int row = topTable.getTable().getSelectedRow();
		if (row != -1) {
			((EksikController) EksikListView.this.controller).createSiparis(type, (String) eksikListTableModel.getValueAt(
					row, 2));
		}
	}
}
