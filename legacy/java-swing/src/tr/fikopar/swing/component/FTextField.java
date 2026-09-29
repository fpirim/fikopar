package tr.fikopar.swing.component;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class FTextField extends JTextField implements FocusListener {

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		for(PropertyChangeListener listener : getPropertyChangeListeners())
			listener.propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	}
	
	private class LimitedPlainDocument extends PlainDocument {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int maxLength;

		public LimitedPlainDocument(int maxLength) {
			this.maxLength = maxLength;
		}

		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null)
				return;

			if (maxLength != 0 && (getLength() + str.length()) > maxLength)
				return;

			super.insertString(offset, str, attr);
		}
	}

	public FTextField() {
		this(0);
	}

	public FTextField(int maxLength) {
		setDocument(new LimitedPlainDocument(maxLength));
		addFocusListener(this);
		getDocument().putProperty("owner", this);
	}

	@Override
	public void focusGained(FocusEvent e) {
		setBackground(UIManager.getColor("FTextField.focusBackground"));
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (isEditable())
			setBackground(UIManager.getColor("TextField.background"));
		else
			setBackground(UIManager.getColor("TextField.inactiveBackground"));
	}

}
