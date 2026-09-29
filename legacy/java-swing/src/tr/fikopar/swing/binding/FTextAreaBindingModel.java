package tr.fikopar.swing.binding;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tr.fikopar.swing.component.FTextArea;
import tr.fikopar.swing.view.FViewModel;

public class FTextAreaBindingModel extends FBindingModel implements DocumentListener {

	public FTextAreaBindingModel(FTextArea component, FViewModel model, String propertyName, int type) {
		super(component, model, propertyName, type);
		component.getDocument().addDocumentListener(this);
	}

	@Override
	public void setViewValue(Object value) {

		FTextArea textArea = (FTextArea) component;
		if (!textArea.getText().equals((String) value))
			textArea.setText((String) value);
	}

	public void textFieldChange() {
		FTextArea textArea = (FTextArea) component;
		setModelValue(textArea.getText());
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
		((FTextArea) component).getDocument().removeDocumentListener(this);		
	}

}
