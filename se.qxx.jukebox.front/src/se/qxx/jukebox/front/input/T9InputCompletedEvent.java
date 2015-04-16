package se.qxx.jukebox.front.input;

public class T9InputCompletedEvent extends java.util.EventObject {

	private static final long serialVersionUID = 6575858181792885533L;

	private String input;
	
	public T9InputCompletedEvent(Object source, String input) {
		super(source);
		
		this.setInput(input);
	}

	public String getInput() {
		return input;
	}

	private void setInput(String input) {
		this.input = input;
	}

}
