package tr.fikopar.swing.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class FViewModel {
	
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	

}
