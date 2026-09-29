package tr.fikopar.swing.component;

import javax.swing.JComboBox;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class FComboBox<T> extends JComboBox<T> {

	private JTextComponent component;

	public FComboBox(FComboBoxModel<T> model) {
		super(model);
		component = (JTextComponent) this.getEditor().getEditorComponent();
	}
	
	public FComboBox() {
		super();
		component = (JTextComponent) this.getEditor().getEditorComponent();
	}
	
	public FComboBox(T[] items) {
		super(items);
		component = (JTextComponent) this.getEditor().getEditorComponent();
	}
	
	public String getText() {
		return component.getText();
	}

	public void setText(String text) {
		component.setText(text);
	}

	public Document getDocument() {
		return component.getDocument();
	}

}
