package tr.fikopar.swing.component;

import java.awt.Component;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.JideTabbedPane;

import tr.fikopar.swing.view.FView;

public class FTabbedPane extends JideTabbedPane implements ChangeListener {

	private boolean activationFire = true;
	public FTabbedPane() {
		super();
		addChangeListener(this);
	}

	public FTabbedPane(int tabPlacement) {
		super(tabPlacement);
		addChangeListener(this);
	}
	public void setActivationFire(boolean activationFire) {
		this.activationFire = activationFire;
	}
	public boolean isActivationFire() {
		return activationFire;
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		Component comp = getSelectedComponent();
		if (activationFire && comp instanceof FView) {
			if (((FView) comp).isFirstView())
				((FView) comp).setFirstView(false);
			else
				((FView) comp).viewActivated();
		}
	}

}
