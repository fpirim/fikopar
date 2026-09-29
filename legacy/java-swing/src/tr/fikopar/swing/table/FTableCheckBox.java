package tr.fikopar.swing.table;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class FTableCheckBox extends JPanel {
	private JCheckBox checkBox;
	public FTableCheckBox() {
		super(new BorderLayout());
		checkBox = new JCheckBox();
		add(checkBox);
		checkBox.setOpaque(false);
	}
	public boolean isSelected() {
		return checkBox.isSelected();
	}
	public void setSelected(boolean value) {
		checkBox.setSelected(value);
	}
	
	@Override
	public synchronized void addMouseListener(MouseListener l) {
		checkBox.addMouseListener(l);
	}
}