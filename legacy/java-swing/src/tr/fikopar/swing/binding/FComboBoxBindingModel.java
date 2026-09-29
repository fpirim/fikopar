package tr.fikopar.swing.binding;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tr.fikopar.swing.component.FComboBox;
import tr.fikopar.swing.view.FViewModel;

public class FComboBoxBindingModel extends FBindingModel implements DocumentListener, ActionListener {

	public FComboBoxBindingModel(FComboBox<?> component, FViewModel model, String propertyName, int type) {
		super(component, model, propertyName, type);
		//component.getDocument().addDocumentListener(this);
		component.addActionListener(this);
	}

	@Override
	public void setViewValue(Object value) {

		FComboBox<?> comboField = (FComboBox<?>) component;
		//if (comboField.isEditable()) {
		//	if (comboField.getText().equals((String) value))
		//		comboField.setText((String) value);
		//} else {
		if (!(comboField.getSelectedItem() == null ? value == null
					: comboField.getSelectedItem().equals(value)))
				comboField.setSelectedItem(value);
		//}
	}

	public void textFieldChange() {
		FComboBox<?> comboField = (FComboBox<?>) component;
		//if (comboField.isEditable())
		//	setModelValue(comboField.getText());
		//else
		setModelValue(comboField.getSelectedItem());
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		textFieldChange();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		textFieldChange();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textFieldChange();
	}

	@Override
	public void removeViewListeners() {
		//((FComboBox<?>) component).getDocument().removeDocumentListener(this);
		((FComboBox<?>) component).removeActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		textFieldChange();
	}

}
