package tr.fikopar.swing.component;

import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


public class FNumericField extends FTextField implements FocusListener {

	final static String validchars = "0123456789-,";

	private DecimalFormat format;
	public FNumericDocument document;

	public FNumericField() {
		this("#,###.00");
	}
	public FNumericField(String ft) {

		format = new DecimalFormat(ft);
		document = new FNumericDocument();
		setDocument(document);
		setHorizontalAlignment(RIGHT);
		addFocusListener(this);
		// setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	public DecimalFormat getFormat() {
		return format;
	}

	public class FNumericDocument extends PlainDocument {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offs, String str, AttributeSet atr)
				throws BadLocationException {

			if (str == "") {
				super.remove(0, getLength());
				return;
			}
			if (str.length() > 1)
				return;
			if (validchars.indexOf(str) == -1)
				return;

			String text = super.getText(0, getLength());
			if (str.equals(",")) {
				if (text.indexOf(",") == -1)
					return;
				setCaretPosition(text.indexOf(",") + 1);
				return;
			} else if (str.equals("-")) {
				if (text.indexOf("-") > -1) {
					super.remove(0, 1);
					setForeground(UIManager.getColor("TextField.foreground"));
				} else {
					super.insertString(0, "-", atr);
					setForeground(UIManager
							.getColor("NumericField.minusForeground"));
				}
				return;
			}
			int c = getCaretPosition();
			boolean flag = false;
			if (c > text.indexOf(",") && text.indexOf(",") != -1)
				flag = true;
			if (c == 0 && text.indexOf("-") > -1)
				return;

			if (c == 0)
				text = str + text;
			else if (getCaretPosition() == getLength()) {
				if (flag)
					return;
				else
					text += str;
			} else if (flag)
				text = text.substring(0, c) + str
						+ text.substring(c + 1, text.length());
			else
				text = text.substring(0, c) + str
						+ text.substring(c, text.length());

			text = text.replace(".", "");
			text = text.replace(",", ".");

			String formattext = format.format(Double.parseDouble(text));

			super.remove(0, getLength());
			super.insertString(0, formattext, atr);
			if (!flag)
				setCaretPosition(formattext.indexOf(",") == -1 ? formattext
						.length() : formattext.indexOf(","));
			else
				setCaretPosition(c + 1 > formattext.length() ? formattext
						.length() : c + 1);
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {
			if (len == getLength()) {
				super.remove(0, getLength());
				String formattext = format.format(0);
				super.insertString(0, formattext, null);
				if (formattext.indexOf(",") > -1)
					setCaretPosition(formattext.indexOf(","));
				setForeground(UIManager.getColor("TextField.foreground"));
				return;
			}
			String text = super.getText(0, getLength());
			int c = getCaretPosition();
			boolean flag = false;
			if (c > text.indexOf(",") && text.indexOf(",") != -1)
				flag = true;

			if (text.charAt(offs) == ',') {
				setCaretPosition(c == 0 ? c : c - 1);
				return;
			}
			if (c == 0)
				return;
			else if (getCaretPosition() == getLength())
				text = text.substring(0, getLength() - 1) + (flag ? "0" : "");
			else if (flag)
				text = text.substring(0, c - 1) + "0"
						+ text.substring(c, text.length());
			else
				text = text.substring(0, c - 1)
						+ text.substring(c, text.length());

			text = text.replace(".", "");
			text = text.replace(",", ".");

			String formattext = format.format(Double.parseDouble(text
					.equals("-") ? "0" : text));
			if (text.equals("-"))
				insertString(0, "-", null);

			super.remove(0, getLength());
			super.insertString(0, formattext, null);
			if (!flag)
				setCaretPosition(formattext.indexOf(",") == -1 ? formattext
						.length() : formattext.indexOf(","));
			else
				setCaretPosition(c - 1 < 0 ? 0 : c - 1);

		}

		public void setText(String value) {
			try {
				super.remove(0, getLength());
				double val = Double.parseDouble(value.equals("") ? "0" : value);
				if (val != 0) {
					String formattext = format
							.format(Double.parseDouble(value));
					super.insertString(0, formattext, null);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		public BigDecimal getBigDecimal() {
			if (getLength() == 0)
				return BigDecimal.ZERO;
			double val = 0;
			String text;
			try {
				text = super.getText(0, getLength());
				val = format.parse(text).doubleValue();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			return BigDecimal.valueOf(val);
		}

		public int getInt() {
			int val = 0;
			if (getLength() == 0)
				return val;
			String text;
			try {
				text = super.getText(0, getLength());
				val = format.parse(text).intValue();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			return val;
		}

		public void setFocusGained() {
			String text;
			try {
				text = super.getText(0, getLength());
				if (text.equals("")) {
					String formattext = format.format(0);
					super.insertString(0, formattext, null);
					if (formattext.indexOf(",") > -1)
						setCaretPosition(formattext.indexOf(","));
				} else {
					boolean flag = true;
					Container con = getParent();
					while(con != null) {
						if(con instanceof JTable)
							flag = false;
						con = con.getParent();
					}
					if (flag)
						select(0, getLength());
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

		}

		public void setFocusLost() {
			Number num;
			try {
				num = format
						.parse(super.getText(0, getLength()).equals("") ? "0"
								: super.getText(0, getLength()));
				if (num.doubleValue() == 0)
					super.remove(0, getLength());
				else {
					super.remove(0, getLength());
					super.insertString(0, format.format(num.doubleValue()),
							null);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	public BigDecimal getBigDecimal() {
		return document.getBigDecimal();
	}

	public int getInt() {
		return document.getInt();
	}

	public void setText(BigDecimal value) {
		if (value == null)
			document.setText("0.0");
		else
			document.setText(value.toString());
	}

	public void setText(int value) {
		document.setText(String.valueOf(value));
	}

	public void focusGained(FocusEvent e) {
		document.setFocusGained();
		super.focusGained(e);
	}

	@Override
	public String getText() {
		return document.getBigDecimal().toString();
	}

	@Override
	public void setText(String t) {
		document.setText(t);
	}

	public void focusLost(FocusEvent e) {
		document.setFocusLost();
		super.focusLost(e);
	}

	@Override
	public Document getDocument() {
		if (document == null)
			return super.getDocument();
		return document;
	}
}
