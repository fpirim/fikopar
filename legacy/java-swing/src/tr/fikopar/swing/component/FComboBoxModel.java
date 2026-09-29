package tr.fikopar.swing.component;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class FComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T>{
	
	private List<T> list;
	private Object selectedItem;
	
	public void setList(List<T> list) {
		this.list = list;
		super.fireContentsChanged(this, 0, getSize());
	}
	@Override
	public T getElementAt(int i) {
		if(list == null)
			return null;
		return list.get(i);
	}

	@Override
	public int getSize() {
		if(list == null)
			return 0;
		return list.size();
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	
	@Override
	public void setSelectedItem(Object object) {
		// No item is selected and object is null, so no change required.
		 if (selectedItem == null && object == null)
			 return;
		 // object is already selected so no change required.
		 if (selectedItem != null && selectedItem.equals(object))
			 return;
		  
		 // Simply return if object is not in the list.
		 /*
		 if (object != null && list.indexOf(object) == -1)
			 return;
		 */
		 // Here we know that object is either an item in the list or null.
		 // Handle the three change cases: selectedItem is null, object is
		 // non-null; selectedItem is non-null, object is null;
		 // selectedItem is non-null, object is non-null and they're not
		 // equal.
		 selectedItem = object;
		 fireContentsChanged(this, -1, -1);
	}
	
	
	
}
