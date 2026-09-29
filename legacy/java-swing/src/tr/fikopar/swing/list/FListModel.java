package tr.fikopar.swing.list;

import java.util.List;

import javax.swing.AbstractListModel;

public abstract class FListModel extends AbstractListModel<Object>{

	private List<Object> list;
	
	
	@Override
	public int getSize() {
		if(list == null)
			return 0;
		return list.size();
	}

}
