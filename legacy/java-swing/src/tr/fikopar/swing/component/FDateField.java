package tr.fikopar.swing.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

import tr.fikopar.swing.jdatepicker.DefaultComponentFactory;
import tr.fikopar.swing.jdatepicker.impl.JDatePanelImpl;

public class FDateField extends JPanel implements AncestorListener, FocusListener {
	private FTextField textField;
	private JButton button;
	private JDialog dialog;
	private JDatePanelImpl datePanelImpl;
	private Vector<ActionListener> actionListeners = new Vector<ActionListener>();

	public FDateField() {
		textField = new FTextField();
		textField.setColumns(10);
		button = new JButton("...");
		button.setMargin(new Insets(0, 0, 0, 0));
		setLayout(new BorderLayout());
		add(textField, BorderLayout.CENTER);
		add(button, BorderLayout.EAST);
		this.setPreferredSize(new Dimension(textField.getPreferredSize()));

		dialog = new JDialog();
		dialog.setUndecorated(true);
		dialog.setModal(false);
		dialog.setFocusableWindowState(false);
		dialog.setAlwaysOnTop(true);
		dialog.setLayout(new BorderLayout());

		datePanelImpl = (JDatePanelImpl) new DefaultComponentFactory().createJDatePanel();
		datePanelImpl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (datePanelImpl.getModel().isSelected())
					textField.setText(String.format("%02d/%02d/%04d", datePanelImpl.getModel().getDay(),
							datePanelImpl.getModel().getMonth() + 1, datePanelImpl.getModel().getYear()));
				else
					textField.setText("");
				fireActions();
				dialog.setVisible(false);
			}
		});
		dialog.add(datePanelImpl, BorderLayout.CENTER);
		dialog.pack();

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Calendar cal = Calendar.getInstance();
					cal.setTime(dateParser.parse(textField.getText()));
					datePanelImpl.getModel().setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
							cal.get(Calendar.DATE));
					datePanelImpl.getModel().setSelected(true);
				} catch (Exception ex) {
					Calendar cal = Calendar.getInstance();
					datePanelImpl.getModel().setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
							cal.get(Calendar.DATE));
					datePanelImpl.getModel().setSelected(true);
				}
				dialog.setVisible(!dialog.isVisible());
				positionChanged();
			}
		});
		textField.addFocusListener(this);
		button.addFocusListener(this);
		addAncestorListener(this);
		Calendar cal = Calendar.getInstance();
		setText(String.format("%04d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE)));
	}

	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}

	public Calendar getCalendar() {
		if (getText().equals(""))
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, getYear());
		calendar.set(Calendar.MONTH, getMonth() - 1);
		calendar.set(Calendar.DATE, getDay());
		return calendar;
	}

	public String getDateValue() {
		if (textField.getText().equals(""))
			return "";
		return String.format("%04d%02d%02d", getYear(), getMonth(), getDay());
	}

	public void setCalendar(Calendar cal) {
		setText(String.format("%04d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE)));
	}

	private void positionChanged() {
		if (dialog.isVisible()) {
			int maxWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			int maxHeight = Toolkit.getDefaultToolkit().getScreenSize().height
					- (Toolkit.getDefaultToolkit().getScreenSize().height
							- GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height);
			int x = this.getLocationOnScreen().x;
			int y = this.getLocationOnScreen().y;
			if (dialog.getSize().width + x > maxWidth)
				x = maxWidth - dialog.getSize().width;
			if (dialog.getSize().height + y > maxHeight)
				y -= dialog.getSize().height;
			else
				y += this.getHeight();
			dialog.setBounds(x, y, dialog.getSize().width, dialog.getSize().height);
			dialog.revalidate();
		}
	}

	private static SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat rtnDate = new SimpleDateFormat("yyyyMMdd");

	public int getYear() {
		if (!getText().equals(""))
			return Integer.parseInt(getText().substring(0, 4));
		return 0;
	}

	public int getMonth() {
		if (!getText().equals(""))
			return Integer.parseInt(getText().substring(4, 6));
		return 0;
	}

	public int getDay() {
		if (!getText().equals(""))
			return Integer.parseInt(getText().substring(6));
		return 0;
	}

	public void setYear(int year) {
		if (getYear() != 0 && getMonth() != 0 && getDay() != 0)
			setText(String.format("%04d%02d%02d", year, getMonth(), getDay()));
	}

	public void setMonth(int month) {
		if (getYear() != 0 && getMonth() != 0 && getDay() != 0)
			setText(String.format("%04d%02d%02d", getYear(), month, getDay()));
	}

	public void setDay(int day) {
		if (getYear() != 0 && getMonth() != 0 && getDay() != 0)
			setText(String.format("%04d%02d%02d", getYear(), getMonth(), day));
	}

	private void fireActions() {
		for (ActionListener listener : actionListeners)
			listener.actionPerformed(new ActionEvent(this, 0, "date changed"));

	}

	public void setText(String text) {
		try {
			textField.setText(dateParser.format(rtnDate.parse(text)));
			fireActions();
		} catch (ParseException e) {
			textField.setText("");
		}
	}

	public String getText() {
		if (textField.getText().equals(""))
			return "";
		try {
			return rtnDate.format(dateParser.parse(textField.getText()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void ancestorAdded(AncestorEvent arg0) {
		positionChanged();
	}

	@Override
	public void ancestorMoved(AncestorEvent arg0) {
		positionChanged();

	}

	@Override
	public void ancestorRemoved(AncestorEvent arg0) {
	}

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource().equals(button))
			dialog.setVisible(false);
		else {
			try {
				dateParser.parse(textField.getText());
			} catch (ParseException e1) {
				textField.setText("");
			}
		}

	}

	public Document getDocument() {
		return textField.getDocument();
	}

	public static String checkDate(String value) {
		try {
			return dateParser.format(rtnDate.parse(value));
		} catch (ParseException e) {
		}
		return value;
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame("text datefield");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		
		Date datenow = Calendar.getInstance().getTime();
		final JSpinner spinner = new JSpinner(new SpinnerDateModel(datenow, null, null, Calendar.YEAR) );  //new SpinnerNumberModel(year, year-100, year+100, 1));
		
		spinner.addChangeListener(new ChangeListener() {
			
			Object lastValue;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
			@Override
			public void stateChanged(ChangeEvent e) {
				 if (lastValue != null && !spinner.getValue().equals(lastValue)) {
			           // expensive code calls here!
					 System.out.println(Integer.parseInt(dateFormat.format(((Date)spinner.getValue()))));
			      }
			     lastValue = spinner.getValue();
				
			}
		});
		spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy"));
		
		frame.add(spinner);
		
		
		frame.setVisible(true);
		
	}
}
