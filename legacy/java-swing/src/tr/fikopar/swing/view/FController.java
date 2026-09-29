package tr.fikopar.swing.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public abstract class FController {
	
	protected FViewModel model;
	protected FView view;

	
	public void setModel(FViewModel model) {
		this.model = model;
	}
	public FViewModel getModel() {
		return model;
	}
	
	private PropertyChangeListener viewListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if("ancestor".equals(evt.getPropertyName())) {
				if(evt.getOldValue() == null && evt.getNewValue() !=null)
					fireViewVisible();
				else
					fireViewDispose();
			}	
		}
	};
	public void setView(FView view) {
		if(this.view!=null) {
			this.view.removePropertyChangeListener(viewListener);
			this.view.removeBindings();
		}
		this.view = view;
		this.view.setController(this);
		this.view.initComponents();
		this.view.initActions();
		this.view.initProperties();
		this.view.initBindings();
		this.view.addPropertyChangeListener(viewListener);
	}

	public void fireViewVisible() {
		if(this.view!=null)
			this.view.viewAdded();
	}
	public void fireViewDispose() {
		if(this.view!=null) {
			this.view.removePropertyChangeListener(viewListener);
			this.view.removeBindings();
		}
	}
	
	public void uncaughtException(Thread t, Throwable ex) {
		System.out.print(t.getName() + " : ");
		ex.printStackTrace();

		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));

		JTextArea ta = new JTextArea(10, 60);
		ta.append(t.getName() + " : " + sw.toString());
		JOptionPane.showMessageDialog(FApp.getAppHandler(), new JScrollPane(ta), "Exception", JOptionPane.ERROR_MESSAGE);
	}
}
