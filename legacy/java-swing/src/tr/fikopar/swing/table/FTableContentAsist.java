package tr.fikopar.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import com.jidesoft.popup.JidePopup;
import com.jidesoft.swing.JideScrollPane;

import tr.fikopar.swing.component.FTextField;

public class FTableContentAsist implements DocumentListener, KeyListener, FocusListener, MouseListener, AncestorListener {

	protected JidePopup popup;
	protected FTable table;
	private TableRowSorter<FTableModel> sorter;
	private FTableContentAsistService fTableContentAsistService;
	private JideScrollPane scrollPane;
	private JLabel statusLabel;
	private JPanel statusPanel;
	private JProgressBar progressBar;
	private boolean firstTimeToShow = true;
	public FTableContentAsist(FTableModel tableModel, FTableContentAsistService fTableContentAsistService) {
		
		this.table = new FTable(tableModel);
		this.fTableContentAsistService = fTableContentAsistService;

		this.table.setCellSelectionEnabled(false);
		this.table.setRowSelectionAllowed(true);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.setFocusable(false);
		this.table.addMouseListener(this);
		this.table.addAncestorListener(this);
		
		sorter = new TableRowSorter<FTableModel>((FTableModel) table.getModel());
		this.table.setRowSorter(sorter);

		popup = new JidePopup();
		popup.setResizable(true);
		popup.setMovable(true);
		scrollPane = new JideScrollPane(table);
		
		
		statusLabel = new JLabel();
		statusPanel = new JPanel();
		progressBar = new JProgressBar();
		statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		statusPanel.add(statusLabel);
		statusPanel.add(progressBar);
		progressBar.setVisible(false);
		statusPanel.setVisible(false);
		
		popup.getContentPane().add(scrollPane, BorderLayout.CENTER);
		popup.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		popup.setReturnFocusToOwner(false); // fix infinite focus
	}

	private void showPopup() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (scrollPane.isVisible()) {
					int width = ((FTableModel)table.getModel()).getTotalColumnWidth() + +UIManager.getInt("ScrollBar.width") + 2
							+ (table.getScrollPane().getRowHeader() == null ? 0 : table.getScrollPane().getRowHeader().getWidth());

					int height = (table.getRowHeight() + table.getRowMargin())
							* fTableContentAsistService.getMaxVisibleRowCount()
							+ table.getTableHeader().getHeight() + table.getRowMargin() * 2 + statusPanel.getPreferredSize().height + 20;

					popup.setPreferredPopupSize(new Dimension(width, height));
				}
				popup.showPopup();
			}
		});

	}

	private void hidePopup() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (popup.isPopupVisible()) {
					popup.setOwner(null);
					popup.hidePopup();
				}
			}
		});

	}

	public void addFTextFieldContentListener(FTextField textField) {
		textField.getDocument().addDocumentListener(this);
		textField.addKeyListener(this);
		textField.addFocusListener(this);
	}

	public void removeFTextFieldContentListener(FTextField textField) {
		textField.getDocument().removeDocumentListener(this);
		textField.removeKeyListener(this);
		textField.removeFocusListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keyChanged(e);

	}

	@Override
	public void keyTyped(KeyEvent e) {
		keyChanged(e);

	}

	@Override
	public void keyReleased(KeyEvent e) {
		keyChanged(e);

	}

	private void keyChanged(KeyEvent e) {
		if(!e.getComponent().hasFocus())
			return;
		
		if (e.isActionKey() && e.getID() == KeyEvent.KEY_PRESSED) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				if (!popup.isPopupVisible()) {
					popup.setOwner((Component) e.getSource());
					showPopup();
					e.consume();
				} else
					keyPropertyChange(e);
				break;
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown() && e.getID() == KeyEvent.KEY_PRESSED) {
			refresh((FTextField) e.getComponent());
			e.consume();
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && popup.isPopupVisible() && e.getID() == KeyEvent.KEY_PRESSED) {
			hidePopup();
			e.consume();
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER && popup.isPopupVisible() && e.getID() == KeyEvent.KEY_PRESSED) {
			hidePopup();
			((FTextField) e.getComponent()).firePropertyChange("contentSelected", null, ((FTableModel)table.getModel()).getObjectAt(sorter.convertRowIndexToModel(table.getSelectedRow())));
			e.consume();
		}
	}

	private void keyPropertyChange(KeyEvent e) {
		if (e.isActionKey()) {
			KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
			Action action = (Action) table.getActionForKeyStroke(keyStroke);
			if (action != null) {
				SwingUtilities.notifyAction((Action) table.getActionForKeyStroke(keyStroke), keyStroke, e, table, e.getModifiers());
				e.consume();
			}
			e.consume();
		}
	}
	private void textChanged(FTextField textfield) {
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if(c != textfield)
			return;
		sorter.setRowFilter(RowFilter.regexFilter(caseInSensitive(textfield.getText())));
		table.clearSelection();
		table.getSelectionModel().addSelectionInterval(0, 0);
		showPopup();		
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		textChanged((FTextField)e.getDocument().getProperty("owner"));
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		textChanged((FTextField)e.getDocument().getProperty("owner"));
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textChanged((FTextField)e.getDocument().getProperty("owner"));
	}

	public static String caseInSensitive(String text) {
		text = text.replace("Ð", "[ð|Ð]");
		text = text.replace("Ü", "[ü|Ü]");
		text = text.replace("Þ", "[þ|Þ]");
		text = text.replace("Ý", "[i|Ý]");
		text = text.replace("Ö", "[ö|Ö]");
		text = text.replace("Ç", "[ç|Ç]");
		text = text.replace("ð", "[ð|Ð]");
		text = text.replace("ü", "[ü|Ü]");
		text = text.replace("þ", "[þ|Þ]");
		text = text.replace("i", "[i|Ý]");
		text = text.replace("ö", "[ö|Ö]");
		text = text.replace("ç", "[ç|Ç]");
		text = text.replace("ý", "[ý|I]");
		text = "(?i)" + text;
		return text;
	}

	@Override
	public void focusGained(final FocusEvent e) {
		popup.setOwner(e.getComponent());
		textChanged(((FTextField) e.getComponent()));
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (!containsFocusListener(e.getOppositeComponent())) // fix after
																// gained focus
																// also have
																// conent asist
			hidePopup();

	}

	private boolean containsFocusListener(Component comp) {
		if (comp == null)
			return false;
		for (FocusListener listener : comp.getFocusListeners()) {
			if (listener == FTableContentAsist.this)
				return true;
		}
		return false;
	}

	private SwingWorker<Void, Object> refreshWorker;
	private BlockingQueue<Object> queue = new ArrayBlockingQueue<>(500);
	public void refresh(final FTextField textfield) {
		
		while(refreshWorker != null && refreshWorker.getState()!=StateValue.DONE);
			
		
		if (refreshWorker == null || refreshWorker.getState() == StateValue.DONE) {
			refreshWorker = new SwingWorker<Void, Object>() {
				@Override
				protected Void doInBackground() throws Exception {
					publish(new FTableContentAsistMessage("Yükleniyor ..."));
					((FTableModel) table.getModel()).removeAll();
					fTableContentAsistService.init();
					Thread thread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								fTableContentAsistService.refresh(textfield, queue);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							try {
								queue.put("exit");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
					thread.start();
					while(true) {
						Object obj = queue.take();
						if(obj instanceof String && "exit".equals((String)obj))
							break;
						publish(obj);
					}
					thread.join();
					publish(new FTableContentAsistMessage("Yüklendi."));
					return null;
				}

				@Override
				protected void process(List<Object> chunks) {
					for(int i=0; i<chunks.size(); i++) {
						Object obj = chunks.get(i);
						if(obj instanceof FTableContentAsistMessage) {
							statusPanel.setVisible(true);
							FTableContentAsistMessage message = (FTableContentAsistMessage) obj;
							if(message.getTotal()!=0) {
								statusLabel.setText(message.getMessage()+" "+message.getCurrent()+"/"+message.getTotal());
								progressBar.setMinimum(1);
								progressBar.setMaximum(Long.valueOf(message.getTotal()).intValue());
								progressBar.setValue(Long.valueOf(message.getCurrent()).intValue());
								progressBar.setVisible(true);
							} else if(message.getCurrent()!=0) {
								progressBar.setVisible(false);
								statusLabel.setText(message.getMessage()+" "+message.getCurrent());
							} else {
								progressBar.setVisible(false);
								statusLabel.setText(message.getMessage());
							}
						} else { 
							((FTableModel) table.getModel()).addObject(chunks.get(i));
						}
					}
					sorter.allRowsChanged();
					
				}

				protected void done() {
					statusPanel.setVisible(false);
				}
			};

		}

		refreshWorker.execute();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(table.getSelectedRow() != -1 && e.getClickCount() >= 2) {
			Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			((FTextField) c).firePropertyChange("contentSelected", null, ((FTableModel)table.getModel()).getObjectAt(sorter.convertRowIndexToModel(table.getSelectedRow())));
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		if(firstTimeToShow) {
			Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			refresh((FTextField) c);
			firstTimeToShow = !firstTimeToShow;
		}
		
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {}

	public void revalidate() {
		if(refreshWorker != null && refreshWorker.getState() == StateValue.STARTED)
			fTableContentAsistService.interrupt();
		firstTimeToShow = true;
	}

}
