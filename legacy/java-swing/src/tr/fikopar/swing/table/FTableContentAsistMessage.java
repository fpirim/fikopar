package tr.fikopar.swing.table;

public class FTableContentAsistMessage {

	private String message;
	private long current;
	private long total;
	
	public FTableContentAsistMessage(String message) {
		this(message, 0, 0);
	}
	public FTableContentAsistMessage(String message, long current, long total) {
		this.message = message;
		this.current = current;
		this.total = total;
	}
	
	public FTableContentAsistMessage increment() {
		current++;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getCurrent() {
		return current;
	}
	public void setCurrent(long current) {
		this.current = current;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	
	
	
	
}
