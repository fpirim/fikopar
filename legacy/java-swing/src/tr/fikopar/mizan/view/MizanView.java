package tr.fikopar.mizan.view;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;

import net.sf.jasperreports.engine.JasperPrint;
import tr.fikopar.app.MyApp;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.common.view.AbstractView;
import tr.fikopar.common.view.PrintPreview;
import tr.fikopar.mizan.controller.MizanController;
import tr.fikopar.swing.table.FTable;

public class MizanView extends AbstractView {
	
	private MizanTableModel mizanTableModel;
	private FTable table;
	public MizanView(AbstractController controller) {
		super(controller);
		
		mizanTableModel = new MizanTableModel();
		
		table = new FTable(mizanTableModel);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoFilter(true);
		
		JPanel tablePanel = table.createPanelForFTable(true, mizanTableModel.getMizanToplamTableModel());
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
					.addContainerGap())
		);
		setLayout(groupLayout);
		
		
		registerComponent(new JButton(baskiOnizlemeAction));

		
	}
	public void refresh() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				doProcess();
				try {
					mizanTableModel.setList(((MizanController) controller).loadList());
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
	
	@Override
	public void viewSelected() {
		refresh();
	}
	
	private AbstractAction baskiOnizlemeAction = new AbstractAction(
			"Baský Önizleme") {

		@SuppressWarnings("rawtypes")
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			RowSorter sorter = table.getRowSorter();
			List<Object []> arr = new ArrayList<Object []>();
			
			for(int i=0; i<sorter.getViewRowCount(); i++) {
				arr.add((Object[]) mizanTableModel.getObjectAt(sorter.convertRowIndexToModel(i)));
				
			}
			JasperPrint jasperPrint = ((MizanController) controller).print(arr);
			if (jasperPrint == null)
				return;
			MyApp.addView(new PrintPreview(jasperPrint));
		}
	};
}
