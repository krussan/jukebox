package se.qxx.jukebox.vlc;

public class VLCConnectionNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4627359950724174808L;

	public VLCConnectionNotFoundException(String hostName) {
		super(String.format("%s is not a recognized host name", hostName));
	}
}
