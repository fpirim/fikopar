package tr.fikopar.swing.binding;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tr.fikopar.swing.component.FDateField;
import tr.fikopar.swing.view.FViewModel;

public class FDateFieldBindingModel extends FBindingModel implements DocumentListener {

	public FDateFieldBindingModel(FDateField component, FViewModel model, String propertyName, int type) {
		super(component, model, propertyName, type);
		component.getDocument().addDocumentListener(this);
	}

	@Override
	public void setViewValue(Object value) {

		FDateField dateField = (FDateField) component;
		if (!dateField.getDateValue().equals((String) value))
			dateField.setText((String) value);
	}

	public void textFieldChange() {
		FDateField dateField = (FDateField) component;
		setModelValue(dateField.getDateValue());
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
		((FDateField) component).getDocument().removeDocumentListener(this);
	}
}
