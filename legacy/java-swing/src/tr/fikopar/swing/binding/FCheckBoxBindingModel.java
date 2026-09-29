package tr.fikopar.swing.binding;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tr.fikopar.swing.component.FCheckBox;
import tr.fikopar.swing.view.FViewModel;

public class FCheckBoxBindingModel extends FBindingModel implements ActionListener {

	public FCheckBoxBindingModel(FCheckBox component, FViewModel model, String propertyName, int type) {
		super(component, model, propertyName, type);
		component.addActionListener(this);
	}

	@Override
	public void setViewValue(Object value) {
		FCheckBox checkBox = (FCheckBox) component;
		if (checkBox.isSelected() != (Boolean) value)
			checkBox.setSelected((Boolean) value);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		FCheckBox checkBox = (FCheckBox) component;
		setModelValue(checkBox.isSelected());
	}
	@Override
	public void removeViewListeners() {
		((FCheckBox) component).removeActionListener(this);
	}

}
