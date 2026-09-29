package tr.fikopar.swing.container;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;

import javax.swing.JPanel;
import javax.swing.JRootPane;

public abstract class FPanel extends JPanel {
	
	
	DisabledGlassPane glassPane = new DisabledGlassPane();
	
	public void doInnerProcess() {
		//AWTAccessor.getComponentAccessor().setMixingCutoutShape(glassPane, new Rectangle());

		boolean visible = false;
		if (glassPane != null && glassPane.getParent() == this) {
			this.remove(glassPane);
			visible = glassPane.isVisible();
		}
		
		glassPane.setVisible(visible);
		this.add(glassPane, 0);
		if (visible) {
			repaint();
		}
		glassPane.activate("Yükleniyor ...", true);

	}
	public void doProcess() {
		doProcess("Yükleniyor ...");
	}
	private Component comp;
	public void doProcess(String msg) {
		comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		JRootPane rootPane = this.getRootPane();
		rootPane.setGlassPane(glassPane);
		glassPane.activate(msg, false);

	}

	public void endProcess() {
		glassPane.deactivate();
		if(comp!=null)
			comp.requestFocus();
	}

	private Component getComponentByName(Container cont, String name) {
		for (Component comp : cont.getComponents()) {
			if (name.equals(comp.getName()))
				return comp;
			else if (comp instanceof Container) {
				Component rtnComp = getComponentByName((Container) comp, name);
				if (rtnComp != null)
					return rtnComp;
			}
		}
		return null;
	}

	public Component getComponentByName(String name) {
		return getComponentByName(this, name);
	}
}
