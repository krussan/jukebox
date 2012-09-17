package se.qxx.jukebox.builders;

public class NFOLine {
	private NFOClass type;
	private String value;
	private String line;
	public NFOClass getType() {
		return type;
	}
	private void setType(NFOClass type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	private void setValue(String value) {
		this.value = value;
	}
	public String getLine() {
		return line;
	}
	private void setLine(String line) {
		this.line = line;
	}
	
	public NFOLine(NFOClass type, String value, String line) {
		this.setType(type);
		this.setValue(value);
		this.setLine(line);
	}
}
