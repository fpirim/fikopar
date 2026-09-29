package tr.fikopar.swing.table;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class FTableEditorPanel extends JPanel {

	public FTableEditorPanel() {
		super(new BorderLayout());
		setOpaque(false);
		setRequestFocusEnabled(true);
	}
	
	
	@Override
	public void requestFocus() {
		getComponent(0).requestFocus();
	}
	
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		InputMap map = ((JComponent)getComponent(0)).getInputMap(condition);
		ActionMap am = ((JComponent)getComponent(0)).getActionMap();

		if (map != null && am != null && isEnabled()) {
			Object binding = map.get(ks);
			Action action = (binding == null) ? null : am.get(binding);
			if (action != null) {
				return SwingUtilities.notifyAction(action, ks, e, getComponent(0), e.getModifiers());
			}
		}
		return false;
	}
}
