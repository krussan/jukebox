package se.qxx.jukebox;
  
import java.util.Arrays;
import java.util.List;

public class Arguments {

	private boolean subtitleDownloaderEnabled = true;
	private boolean tcpListenerEnabled = true;
	private boolean imdbIdentitifierEnabled = true;
	private boolean mediaInfoEnabled = true;
	private boolean purgeMode = false;
	private boolean helpRequested = false;
	private boolean purgeSubtitles = false;
	
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
	
	public boolean isMediaInfoEnabled() {
		return mediaInfoEnabled;
	}

	public void setMediaInfoEnabled(boolean mediaInfoEnabled) {
		this.mediaInfoEnabled = mediaInfoEnabled;
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

		if (arguments.contains("-dm"))
			_instance.setMediaInfoEnabled(false);

		if (arguments.contains("--purge"))
			_instance.setPurgeMode(true);
		
		if (arguments.contains("--help") || arguments.contains("-?"))
			_instance.setHelpRequested(true);
		
		if (arguments.contains("--purgeSubs"))
			_instance.setPurgeSubtitles(true);
	}
	
	public static Arguments get() {
		if (_instance == null)
			_instance = new Arguments();
		
		return _instance;
	}

	public boolean isPurgeMode() {
		return purgeMode;
	}

	public void setPurgeMode(boolean purgeMode) {
		this.purgeMode = purgeMode;
	}

	public boolean isHelpRequested() {
		return helpRequested;
	}

	public void setHelpRequested(boolean helpRequested) {
		this.helpRequested = helpRequested;
	}

	public boolean isPurgeSubtitles() {
		return purgeSubtitles;
	}

	public void setPurgeSubtitles(boolean purgeSubtitles) {
		this.purgeSubtitles = purgeSubtitles;
	}
}
