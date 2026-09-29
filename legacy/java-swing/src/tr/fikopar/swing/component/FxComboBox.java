package tr.fikopar.swing.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jidesoft.popup.JidePopup;
import com.jidesoft.swing.CheckBoxList;
import com.jidesoft.swing.JideScrollPane;

public class FxComboBox<T> extends JPanel {
	private FTextField textField;
	private JButton button;
	private JidePopup popup;
	private CheckBoxList checkBoxList;
	private ListModel<T> aModel;

	protected class DefaultListModel implements ListModel<T> {
		private T[] items;

		public DefaultListModel(T[] items) {
			this.items = items;
		}

		@Override
		public int getSize() {
			return items.length;
		}

		@Override
		public T getElementAt(int index) {
			return items[index];
		}

		@Override
		public void addListDataListener(ListDataListener l) {
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
		}

	};
	@SuppressWarnings("unchecked")
	public FxComboBox(List<T> list) {
		aModel = new DefaultListModel((T[]) list.toArray());
		init();
		initPopup();
	}
	public FxComboBox(T[] items) {
		aModel = new DefaultListModel(items);
		init();
		initPopup();
	}

	public FxComboBox(ListModel<T> aModel) {
		this.aModel = aModel;
		init();
		initPopup();
	}

	private void init() {
		textField = new FTextField();
		textField.setEditable(false);
		button = new JButton(new AbstractAction("...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				togglePopup();
			}
		});
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		setLayout(new BorderLayout());
		add(textField, BorderLayout.CENTER);
		add(button, BorderLayout.EAST);
	}

	private void initPopup() {
		checkBoxList = new CheckBoxList(aModel);
		checkBoxList.setClickInCheckBoxOnly(false);
		checkBoxList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int[] selected = checkBoxList.getCheckBoxListSelectedIndices();
				String temp = "";
				for (int i = 0; i < selected.length; i++)
					temp += aModel.getElementAt(selected[i]) + (i - 1 == selected.length ? "" : ";");
				textField.setText(temp);

			}
		});
		popup = new JidePopup();
		popup.setFocusable(true);
		popup.setResizable(true);
		popup.getContentPane().add(new JideScrollPane(checkBoxList));

	}
	public Object [] getSelectedList() {
		return checkBoxList.getCheckBoxListSelectedValues();
	}

	protected void togglePopup() {
		if (popup.isPopupVisible())
			popup.hidePopup();
		else {
			popup.setPreferredPopupSize(new Dimension(getWidth(), popup.getPreferredSize().height));
			popup.showPopup(this);

			checkBoxList.requestFocus();
		}

	}

}
