package se.qxx.jukebox;

import java.util.Arrays;
import java.util.List;

public class Arguments {

	private boolean subtitleDownloaderEnabled = true;
	private boolean tcpListenerEnabled = true;
	
	public boolean isSubtitleDownloaderEnabled() {
		return subtitleDownloaderEnabled;
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

	public Arguments() {
		
	}
	
	public static Arguments parse(String[] args) {
		Arguments a = new Arguments();
		
		List<String> arguments = Arrays.asList(args);
	
		if (arguments.contains("-dt"))
			a.setTcpListenerEnabled(false);
		
		if (arguments.contains("-ds"))
			a.setSubtitleDownloaderEnabled(false);

		return a;
	}
}
