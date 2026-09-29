package tr.fikopar.swing.view;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tr.fikopar.swing.component.FTabbedPane;
import tr.fikopar.swing.component.FToolBar;

public class FDialog extends JDialog {
	
	private FTabbedPane tabbedPane;
	private FToolBar myToolBar;
	
	public FDialog(JFrame owner, String title, boolean modal) {
		super(owner, title, modal);
		setLayout(new BorderLayout());
		tabbedPane = new FTabbedPane(FTabbedPane.TOP);
		tabbedPane.setTabShape(FTabbedPane.SHAPE_WINDOWS);
		tabbedPane.setColorTheme(FTabbedPane.COLOR_THEME_WINXP);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				FView view = (FView) tabbedPane.getSelectedComponent();
				myToolBar.removeAll();
				if (view != null) {
					for (JComponent component : view.getMyComponents())
						myToolBar.add(component);
				}
				if (view == null || view.getMyComponents().size() == 0)
					myToolBar.setVisible(false);
				else {
					myToolBar.setVisible(true);
					myToolBar.repaint();
				}
			}
		});
		add(tabbedPane, BorderLayout.CENTER);
		
		myToolBar = new FToolBar();
		getContentPane().add(myToolBar, BorderLayout.NORTH);
		myToolBar.setVisible(false);
	}
	
	public void addView(String title, FView view) {
		tabbedPane.add(title, view);
	}
	public void setSelectedView(FView view) {
		tabbedPane.setSelectedComponent(view);
	}

}
