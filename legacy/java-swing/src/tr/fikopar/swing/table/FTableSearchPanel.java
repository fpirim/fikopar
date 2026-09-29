package tr.fikopar.swing.table;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import tr.fikopar.swing.component.FTextField;

public class FTableSearchPanel extends JPanel implements KeyListener, FocusListener, ActionListener, AncestorListener {

	private FTextField searchField;
	private JButton closeButton, nextButton, previousButton;
	private FTable fTable;

	public FTableSearchPanel(FTable fTable) {

		this.fTable = fTable;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, fTable.getGridColor()));
		searchField = new FTextField();
		searchField.setPreferredSize(new Dimension(200, searchField.getPreferredSize().height));
		searchField.addKeyListener(this);
		searchField.addFocusListener(this);

		closeButton = new JButton(UIManager.getIcon("closeIcon"));
		closeButton.setMargin(new Insets(1, 1, 1, 1));
		closeButton.addActionListener(this);
		closeButton.setFocusable(false);

		nextButton = new JButton("Sonraki", UIManager.getIcon("nextIcon"));
		nextButton.addActionListener(this);
		nextButton.setFocusable(false);

		previousButton = new JButton("Önceki", UIManager.getIcon("previousIcon"));
		previousButton.addActionListener(this);
		previousButton.setFocusable(false);

		add(closeButton);
		add(new JLabel("Ara :"));
		add(searchField);
		add(previousButton);
		add(nextButton);
		addAncestorListener(this);
	}

	public void focusGained(FocusEvent e) {
		searchField.selectAll();
	};

	public void focusLost(FocusEvent e) {
		setVisible(false);
		transferFocusBackward();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
			transferFocusBackward();
			return;
		}

		int r = fTable.getSelectedRow() == -1 ? 0 : fTable.getSelectedRow();
		int c = fTable.getSelectedColumn() == -1 ? 0 : fTable.getSelectedColumn();

		int inc = 0;

		if (e.getSource() == nextButton) {
			c++;
			if (c == fTable.getColumnCount()) {
				r++;
				if (r == fTable.getRowCount())
					r = 0;
				c = 0;
			}
			inc = 1;
		} else {
			c--;
			if (c == -1) {
				r--;
				if (r == -1)
					r = fTable.getRowCount() - 1;
				c = fTable.getColumnCount() - 1;
			}
			inc = -1;
		}

		for (; r < fTable.getModel().getRowCount() && r >= 0; r += inc) {
			for (; c < fTable.getModel().getColumnCount() && c >= 0; c += inc) {
				Object value = fTable.getModel().getValueAt(r, c);
				if (String.valueOf(value).contains(searchField.getText())) {
					fTable.clearSelection();
					fTable.getSelectionModel().addSelectionInterval(r, r);
					fTable.getColumnModel().getSelectionModel().addSelectionInterval(c, c);
					Rectangle cellLocation = fTable.getCellRect(r, c, false);
					fTable.getScrollPane().getVerticalScrollBar().setValue(cellLocation.y);
					return;
				}
			}
			if (inc == 1)
				c = 0;
			else
				c = fTable.getColumnCount() - 1;
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setVisible(false);
			transferFocusBackward();
			e.consume();
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isControlDown()) {
			nextButton.doClick();
			e.consume();
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
			previousButton.doClick();
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		searchField.requestFocus();
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
	};

}
