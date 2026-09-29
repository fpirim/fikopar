package tr.fikopar.makbuz.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.sf.jasperreports.engine.JasperPrint;
import tr.fikopar.app.MyApp;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.common.model.TipDataItem;
import tr.fikopar.common.view.AbstractView;
import tr.fikopar.common.view.PrintPreview;
import tr.fikopar.makbuz.controller.MakbuzController;
import tr.fikopar.swing.component.FDatefield;
import tr.fikopar.swing.component.FxComboBox;
import tr.fikopar.swing.table.FTable;

public class MakbuzView extends AbstractView {
	
	private MakbuzTableModel makbuzTableModel;
	private FDatefield girisBaslangic, girisBitis;
	private FTable table;
	private FxComboBox<TipDataItem> fxComboBox;
	public MakbuzView(AbstractController controller) {
		super(controller);
		
		makbuzTableModel = new MakbuzTableModel();
		
		table = new FTable(makbuzTableModel);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoFilter(true);
		
		JPanel tablePanel = table.createPanelForFTable(true, makbuzTableModel.getMakbuzTableToplamModel());
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Filtreleme", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
					.addGap(37))
		);
		
		JLabel lblGirisTarihi = new JLabel("Giriþ Tarihi :");
		lblGirisTarihi.setBounds(13, 20, 89, 22);
		
		girisBaslangic = new FDatefield();
		girisBaslangic.setBounds(107, 20, 124, 22);
		
		girisBitis = new FDatefield();
		girisBitis.setBounds(261, 20, 124, 22);
		
		Calendar cal = girisBaslangic.getCalendar();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DATE, 1);
		girisBaslangic.setCalendar(cal);
		
		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
		girisBitis.setCalendar(cal);
		
		JButton btnListele = new JButton("Listele");
		btnListele.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		btnListele.setBounds(397, 21, 72, 49);
		panel.setLayout(null);
		panel.add(lblGirisTarihi);
		panel.add(girisBaslangic);
		panel.add(girisBitis);
		panel.add(btnListele);
		
		JLabel label_1 = new JLabel("-");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBounds(239, 20, 12, 23);
		panel.add(label_1);
		
		fxComboBox = new FxComboBox<>( controller.loadTipList() );
		fxComboBox.setBounds(107, 47, 278, 23);
		panel.add(fxComboBox);
		
		JLabel lblHesapTipi = new JLabel("Hesap Tipi :");
		lblHesapTipi.setBounds(13, 47, 89, 22);
		panel.add(lblHesapTipi);
		setLayout(groupLayout);
		
		registerComponent(new JButton(printPreviewAction));
		
	}
	private Action printPreviewAction = new AbstractAction("Baský Önizleme") {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			List<Object []> list = new ArrayList<Object []>();
			for(int i=0; i< table.getRowSorter().getViewRowCount(); i++) {
					list.add((Object[]) makbuzTableModel.getObjectAt(table.getRowSorter().convertRowIndexToModel(i)));
			}
			JasperPrint jasperPrint = ((MakbuzController)controller).print(list,
					girisBaslangic.getCalendar(), girisBitis.getCalendar());
			if(jasperPrint==null)
				return;
			
			MyApp.addView(new PrintPreview(jasperPrint));
		}
	};
	private void refresh() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				doProcess();
				try {
					makbuzTableModel.setList(((MakbuzController) controller).loadList(girisBaslangic.getCalendar(), girisBitis.getCalendar(), fxComboBox.getSelectedList()));
					endProcess();
				} catch (Exception e) {
					controller.uncaughtException(Thread.currentThread(), e);
				} finally {
					endProcess();
				}
			}
		});
		thread.start();
	}
}
