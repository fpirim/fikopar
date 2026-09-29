package tr.fikopar.swing.view;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class FPrintPreview extends FView implements AncestorListener{
	private JRViewer jrViewer;
	private JasperPrint jasperPrint;

	public FPrintPreview(JasperPrint jasperPrint) {
		this.jasperPrint = jasperPrint;
		initComponents();
		initActions();
		initProperties();
		initBindings();
		addAncestorListener(this);
	}

	@Override
	public void initComponents() {
		this.setName("Baský Önizleme");
		setLayout(new BorderLayout());
		jrViewer = new JRViewer(jasperPrint);
		add(jrViewer);
		

	}

	@Override
	public void initActions() {}

	@Override
	public void initProperties() {}

	@Override
	public void initBindings() {}
	
	@Override
	public void viewAdded() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				jrViewer.setFitPageZoomRatio();

			}
		});	
	}
	@Override
	public void viewActivated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		if(firstView) {
			viewAdded();
			firstView = false;
		}
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
	}


}
