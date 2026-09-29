package tr.fikopar.swing.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import tr.fikopar.swing.binding.FBindings;
import tr.fikopar.swing.container.FPanel;

public abstract class FView extends FPanel {
	protected List<JComponent> myComponents = new ArrayList<>();
	protected FController controller;
	protected boolean firstView = true;
	
	public boolean isFirstView() {
		return firstView;
	}
	
	public void setFirstView(boolean firstView) {
		this.firstView = firstView;
	}
	
	public void setController(FController controller) {
		this.controller = controller;
	}

	public FController getController() {
		return controller;
	}

	public abstract void viewAdded();
	public abstract void viewActivated();
	
	public void registerAction(AbstractAction action, String tooltip, KeyStroke keyStroke, String actionCommand) {
		JButton button = new JButton(action);
		button.setToolTipText(tooltip);
		button.getActionMap().put(actionCommand, button.getAction());
		button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionCommand);
		button.setName(actionCommand);
		registerComponent(button);

	}

	public void registerComponent(JComponent component) {
		if (component instanceof JButton) {
			((JButton) component).setHorizontalTextPosition(SwingConstants.CENTER);
			((JButton) component).setVerticalTextPosition(SwingConstants.BOTTOM);
		}
		myComponents.add(component);
	}

	public JComponent getComponentByActionCommand(String actionCommand) {
		for (JComponent component : myComponents) {
			if (component.getName().equals(actionCommand))
				return component;
		}
		return null;
	}

	public List<JComponent> getMyComponents() {
		return myComponents;
	}

	public abstract void initComponents();

	public abstract void initActions();

	public abstract void initProperties();

	public abstract void initBindings();

	public void removeBindings() {
		FBindings.remove(controller.getModel());
	}
	
}
