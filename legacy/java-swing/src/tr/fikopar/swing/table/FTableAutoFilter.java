package tr.fikopar.swing.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.jidesoft.popup.JidePopup;
import com.jidesoft.swing.JideScrollPane;

import tr.fikopar.swing.component.FTextField;

public class FTableAutoFilter implements
		MouseListener, MouseMotionListener, KeyListener {

	private JTable table;
	
	class MyRowFilter extends RowFilter<Object, Object> {
		private int column;
		private Object filterValue;
		private boolean filtered;
		public MyRowFilter(Object filterValue, int column) {
			this(column);
			this.filterValue = filterValue;
			filtered = true;
		}
		MyRowFilter(int column) {
			this.column = column;
			filterValue = null;
			filtered = false;
        }
		public boolean isFiltered() {
			return filtered;
		}
		
		public MyRowFilter clearFilter() {
			filtered = false;
			return this;
		}

		public int getColumn() {
			return column;
		}
		public MyRowFilter setFilterValue(Object filterValue) {
			this.filterValue = filterValue;
			this.filtered = true;
			return this;
		}
		
        @Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends Object, ? extends Object> entry) {
			if(!filtered)
        		return true;
        	Object value = entry.getValue(column);
        	if(value == null && filterValue == null)
        		return true;
        	if(value == null)
        		return false;
        	if(filterValue == null)
        		return false;
        	
            return value.equals(filterValue);
        
		}
		
	}
	class MyAutoFilterRowHeaderRenderer extends JPanel implements TableCellRenderer {
		
		private JLabel label;
		private JButton button;
		private int colIndex = -1;
		private int eventID;
		private Point point;
		private Rectangle rect;
		private Rectangle buttonRect;
		private int clickedColumn = -1;
		private ImageIcon icon;
		public void setPoint(Point point) {
			this.point = point;
		}
		public void setRect(Rectangle rect) {
			this.rect = rect;
			buttonRect = new Rectangle(rect.width-16 , (rect.height-16)/2, button.getPreferredSize().width, button.getPreferredSize().height);
		}
		public void setEventID(int eventID) {
			this.eventID = eventID;
		}
		public void setColIndex(int colIndex) {
			this.colIndex = colIndex;
		}
		
		public MyAutoFilterRowHeaderRenderer() {
			setLayout(new BorderLayout());
			label = new JLabel("");
			button = new BasicArrowButton(SwingConstants.SOUTH, 
					 UIManager.getColor("Button.background"),
					    UIManager.getColor("Button.shadow"),
					    Color.BLACK,//UIManager.getColor("Button.darkShadow"),
					    UIManager.getColor("Button.highlight"));
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			add(label, BorderLayout.CENTER);
			add(button, BorderLayout.EAST);
			
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			label.setText(String.valueOf(value));
			
			button.getModel().setPressed(false);
			button.getModel().setRollover(false);
			label.setIcon(null);
			MyRowFilter filter = (MyRowFilter) filters.get(column);
			if(filter.isFiltered())
				button.setBackground(Color.CYAN);
			else
				button.setBackground(UIManager.getColor("Button.background"));
			if(colIndex == column) {
				switch(eventID) {
					case MouseEvent.MOUSE_ENTERED:
					case MouseEvent.MOUSE_EXITED:	
					case MouseEvent.MOUSE_MOVED:
						if(point!=null)
							button.getModel().setRollover(buttonRect.contains(point));
						break;
					case MouseEvent.MOUSE_RELEASED:
						button.getModel().setPressed(false);
						break;
					case MouseEvent.MOUSE_PRESSED:
						if(point!=null)
							button.getModel().setPressed(buttonRect.contains(point));
						break;
					case MouseEvent.MOUSE_CLICKED:
						if(point!=null && buttonRect.contains(point)) {
							prepareAndShowPopup(colIndex);
							button.getModel().setPressed(true);
						}
						else {
							List<SortKey> sortKeys = new ArrayList<>();
							SortOrder order;
							if(sorter.getSortKeys().size()==0 || sorter.getSortKeys().get(0).getColumn()!=colIndex)
								order = SortOrder.ASCENDING;
							else {
								if(sorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING)
									order = SortOrder.DESCENDING;
								else if(sorter.getSortKeys().get(0).getSortOrder() == SortOrder.DESCENDING)
									order = SortOrder.UNSORTED;
								else
									order = SortOrder.ASCENDING;
							}
							icon = (ImageIcon) (order == SortOrder.ASCENDING ? UIManager.get("ascendingIcon") : (order == SortOrder.DESCENDING ? UIManager.get("descendingIcon") : null));
							clickedColumn = order == SortOrder.UNSORTED ? -1 : column; 
							sortKeys.add(new RowSorter.SortKey(colIndex, order));
							sorter.setSortable(colIndex, true);
							sorter.setSortKeys(sortKeys);
							sorter.sort();
							sorter.setSortable(colIndex, false);
						}
						break;
					}
				
				eventID = -1;
			} 
			if(clickedColumn == column)
				label.setIcon(icon);
			return this;
		}
	}
	
	private class MyListModel extends AbstractListModel<Object> {
		private SortedSet<Object> data;
		public MyListModel() {
			data = new TreeSet<Object>();
		}

		@Override
		public Object getElementAt(int index) {
			if(index == 0)
				return "(Hepsi)";
			return data.toArray()[index - 1];
		}

		@Override
		public int getSize() {
			return data.size() + 1;
		}

		public void addElement(Object value) {
			if(value==null)
				return;
			if(!data.contains(value)) {
				data.add(value);
				fireContentsChanged(this, 0, data.size() + 1);
			}
		}
		public void clear() {
			int size = data.size() + 1;
			data.clear();
			fireIntervalRemoved(this, 0, size);
		}
		
	}
	
	
	public void prepareAndShowPopup(int colIndex) {
		listModel.clear();
		for(int i=0; i<sorter.getViewRowCount(); i++) {
			listModel.addElement(table.getModel().getValueAt(sorter.convertRowIndexToModel(i), colIndex));
		}
		
		Rectangle rect = table.getTableHeader().getHeaderRect(colIndex);
		Point point = new Point(rect.x, rect.y+rect.height);
		SwingUtilities.convertPointToScreen(point, table.getTableHeader());
		popup.setPreferredPopupSize(new Dimension(rect.width, 200));
		popup.showPopup(point.x,point.y,table.getTableHeader());
		this.colIndex = colIndex;
	}
	
	private JidePopup popup;
	private JList<String> list;
	private MyListModel listModel;
	private int colIndex;
	
	private TableRowSorter<TableModel> sorter;
	private List<RowFilter<Object,Object>> filters;
	private List<RowFilter<Object,Object>> emptyFilters;
	
	private void initPopup() {
		popup = new JidePopup();
		listModel = new MyListModel();
		list = new JList(listModel);
		popup.add(new JideScrollPane(list));
		popup.setResizable(true);
		list.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				list.clearSelection();
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				int selIndex = list.locationToIndex(e.getPoint());
				switch(selIndex) {
					case 0:
						filters.set(colIndex, ((MyRowFilter)filters.get(colIndex)).clearFilter());
						sorter.setRowFilter(RowFilter.andFilter(filters));
						break;
//					case 1: case 2: case 3:
//						for(int i=0; i<table.getColumnCount(); i++) 
//							sorter.setSortable(i, true);
//						sortKeys.set(colIndex, new RowSorter.SortKey(colIndex, selIndex == 1 ? SortOrder.ASCENDING : (selIndex == 2 ? SortOrder.DESCENDING : SortOrder.UNSORTED)));
//						sorter.setSortKeys(sortKeys);
//						sorter.sort();
//						for(int i=0; i<table.getColumnCount(); i++) 
//							sorter.setSortable(i, false);
//						break;
					default:
						filters.set(colIndex, ((MyRowFilter)filters.get(colIndex)).setFilterValue(listModel.getElementAt(selIndex)));
						sorter.setRowFilter(RowFilter.andFilter(filters));
				}
				table.getTableHeader().repaint();
				popup.hidePopup();
			}
		});
		list.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				list.setSelectedIndex(list.locationToIndex(e.getPoint()));
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {}
		});
	}
	public void clearTemproraryFilters() {
		sorter.setRowFilter(RowFilter.andFilter(emptyFilters));
	}
	public void applyTemproraryFilters() {
		sorter.setRowFilter(RowFilter.andFilter(filters));
	}
	public TableRowSorter<TableModel> getSorter() {
		return sorter;
	}
	public FTableAutoFilter(FTable table) {
		this.table = table;
		
		sorter = new TableRowSorter<TableModel>(table.getModel());
		filters = new ArrayList<RowFilter<Object, Object>>();
		emptyFilters = new ArrayList<RowFilter<Object, Object>>();
		for(int i=0; i<table.getColumnCount(); i++) {
			filters.add(new MyRowFilter(i));
			emptyFilters.add(new MyRowFilter(i));
			sorter.setSortable(i, false);
		}
		
		MyAutoFilterRowHeaderRenderer autoFilterRowHeaderRenderer = new MyAutoFilterRowHeaderRenderer();
		//table.getTableHeader().setDefaultRenderer(autoFilterRowHeaderRenderer);
		
		////setTable(table);
		for(int i=0; i<table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(autoFilterRowHeaderRenderer);
		}
		table.getTableHeader().addMouseListener(this);
		table.getTableHeader().addMouseMotionListener(this);
		
		//setDefaultRenderer(autoFilterRowHeaderRenderer);
		
		sorter.setRowFilter(RowFilter.andFilter(filters));
		table.setRowSorter(sorter);
		
		initPopup();

	}
	Map<FTextField, Integer> map = new HashMap<FTextField, Integer>();
	public void addFilter(FTextField field) {
		map.put(field, filters.size());
		filters.add(filters.size(), RowFilter.regexFilter(field.getText()));
		field.addKeyListener(this);
	}
	public void removeFilters() {
		for(Iterator<FTextField> itr =map.keySet().iterator(); itr.hasNext();) {
			FTextField field = itr.next();
			field.removeKeyListener(this);
			filters.remove(map.get(field));
		}
		map.clear();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		fireMouseEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		fireMouseEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		fireMouseEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		fireMouseEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		fireMouseEvent(e);

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		fireMouseEvent(e);
		
	}

	private void fireMouseEvent(MouseEvent e) {
		int viewIndexCol = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
		if(viewIndexCol==-1)
			return;
		Point p = e.getPoint();
		for(int i=0; i<viewIndexCol; i++) {
			Rectangle rect = table.getTableHeader().getHeaderRect(i);
			p.x -= rect.width;
		}
		MyAutoFilterRowHeaderRenderer _renderer = (MyAutoFilterRowHeaderRenderer) table.getColumnModel().getColumn(viewIndexCol).getHeaderRenderer();
		_renderer.setRect(table.getTableHeader().getHeaderRect(viewIndexCol));
		_renderer.setColIndex(viewIndexCol);
		_renderer.setEventID(e.getID());
		_renderer.setPoint(p);
		table.getTableHeader().repaint();
		e.consume();
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		fireMouseEvent(e);
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(!e.isActionKey()) {
			FTextField field = (FTextField) e.getSource();
			Integer index = map.get(field);
			filters.set(index, RowFilter.regexFilter(field.getText()));
			sorter.setRowFilter(RowFilter.andFilter(filters));
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

}
