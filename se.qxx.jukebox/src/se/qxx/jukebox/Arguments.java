package se.qxx.jukebox;

import java.util.Arrays;
import java.util.List;

public class Arguments {

	private boolean subtitleDownloaderEnabled = true;
	private boolean tcpListenerEnabled = true;
	private boolean imdbIdentitifierEnabled = true;
	private static Arguments _instance;
	
	public boolean isSubtitleDownloaderEnabled() {
		return subtitleDownloaderEnabled;
	}

	public void setImdbIdentifierEnabled(boolean imdbIdentitifierEnabled) {
		this.imdbIdentitifierEnabled = imdbIdentitifierEnabled;
	}
	
	public boolean isImdbIdentifierEnabled() {
		return this.imdbIdentitifierEnabled;
	}	

	public void setSubtitleDownloaderEnabled(boolean subtitleDownloaderEnabled) {
		this.subtitleDownloaderEnabled = subtitleDownloaderEnabled;
	}

	public boolean isTcpListenerEnabled() {
		return tcpListenerEnabled;
	}

	public void setTcpListenerEnabled(boolean tcpListenerEnabled) {
		this.tcpListenerEnabled = tcpListenerEnabled;
	}

	private Arguments() {
		
	}
	
	public static void initialize(String[] args) {
		_instance = new Arguments();
		
		List<String> arguments = Arrays.asList(args);
	
		if (arguments.contains("-dt"))
			_instance.setTcpListenerEnabled(false);
		
		if (arguments.contains("-ds"))
			_instance.setSubtitleDownloaderEnabled(false);
		
		if (arguments.contains("-di"))
			_instance.setImdbIdentifierEnabled(false);
	}
	
	public static Arguments get() {
		if (_instance == null)
			_instance = new Arguments();
		
		return _instance;
	}
}
