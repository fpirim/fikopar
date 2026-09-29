package tr.fikopar.swing.binding;

import java.math.BigDecimal;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.view.FViewModel;

public class FTextFieldBindingModel extends FBindingModel implements DocumentListener {

	public FTextFieldBindingModel(FTextField component, FViewModel model, String propertyName, int type) {
		super(component, model, propertyName, type);
		component.getDocument().addDocumentListener(this);
	}

	@Override
	public void setViewValue(Object value) {

		FTextField textField = (FTextField) component;
		if (!textField.getText().equals(String.valueOf(value)))
			textField.setText(String.valueOf(value));
	}

	public void textFieldChange() {
		FTextField textField = (FTextField) component;
		if(getFieldClass().isInstance(Integer.class))
			setModelValue(Integer.parseInt(textField.getText()));
		else if(getFieldClass().isInstance(BigDecimal.class))
			setModelValue(new BigDecimal(textField.getText()));
		else
			setModelValue(textField.getText());
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
		((FTextField) component).getDocument().removeDocumentListener(this);
	}

}
