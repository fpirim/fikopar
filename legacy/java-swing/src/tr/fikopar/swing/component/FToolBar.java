package tr.fikopar.swing.component;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class FToolBar extends JToolBar {

	private List<JButton> abstractButtons = new ArrayList<JButton>();
	
	public FToolBar() {
		super();
		setRollover(false);
		setFloatable(false);
	}
	
	public void registerAction(AbstractAction action) {
		JButton button = new JButton(action);
		//button.setMaximumSize(new Dimension(70, 50));
		//button.setMinimumSize(new Dimension(70, 50));
		//button.setPreferredSize(new Dimension(70, 50));
		//button.setVerticalTextPosition(SwingConstants.BOTTOM);
		//button.setHorizontalTextPosition(SwingConstants.CENTER);
		add(button);
		abstractButtons.add(button);
		this.setVisible(true);
	}
	
	public void clearAll() {
		for(JButton button : abstractButtons)
			button.removeAll();
		this.setVisible(false);
	}
	
	
}
