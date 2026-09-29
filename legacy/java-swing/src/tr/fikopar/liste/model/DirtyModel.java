package tr.fikopar.liste.model;

public class DirtyModel {
	private boolean dirty;

	public DirtyModel() {
		dirty = false;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
}
