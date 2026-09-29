package tr.fikopar.swing.table;

import java.util.concurrent.BlockingQueue;

import tr.fikopar.swing.component.FTextField;

public abstract class FTableContentAsistService {
	protected boolean interrupt;
	public void init() {
		interrupt = false;
	}
	public abstract void refresh(FTextField source, BlockingQueue<Object> queue) throws InterruptedException;
	public void interrupt() {
		interrupt = true;
	}
	public abstract int getMaxVisibleRowCount();
}
