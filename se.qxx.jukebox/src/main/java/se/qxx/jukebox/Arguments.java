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
	private boolean purgeSeries = false;
	private boolean webServerEnabled = true;
	private boolean watcherEnabled = true;
	private boolean cleanerEnabled = true;
	private boolean cleanerLogOnly = false;
	private boolean setupDatabase = false;
	private boolean downloadCheckerEnabled = false;
	private boolean mediaConverterEnabled = false;
	
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

	public boolean isWebServerEnabled() {
		return webServerEnabled;
	}

	public void setWebServerEnabled(boolean webServerEnabled) {
		this.webServerEnabled = webServerEnabled;
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

		if (arguments.contains("-dw"))
			_instance.setWebServerEnabled(false);

		if (arguments.contains("--purge"))
			_instance.setPurgeMode(true);
		
		if (arguments.contains("--help") || arguments.contains("-?"))
			_instance.setHelpRequested(true);
		
		if (arguments.contains("--purgeSubs"))
			_instance.setPurgeSubtitles(true);
		
		if (arguments.contains("--purgeSeries"))
			_instance.setPurgeSeries(true);
		
		if (arguments.contains("-df"))
			_instance.setWatcherEnabled(false);
		
		if (arguments.contains("-dc"))
			_instance.setCleanerEnabled(false);
		
		if (arguments.contains("-dcl")) {
			_instance.setCleanerEnabled(true);
			_instance.setCleanerLogOnly(true);
		}
		
		if (arguments.contains("-dd")) 
			_instance.setDownloadCheckerEnabled(false);
		
		if (arguments.contains("-dmc"))
			_instance.setMediaConverterEnabled(false);
		
		if (arguments.contains("--setupdb")) {
			_instance.setSetupDatabase(true);
		}

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

	public boolean isPurgeSeries() {
		return purgeSeries;
	}

	private void setPurgeSeries(boolean purgeSeries) {
		this.purgeSeries = purgeSeries;
	}

	public boolean isWatcherEnabled() {
		return watcherEnabled;
	}

	public void setWatcherEnabled(boolean watcherEnabled) {
		this.watcherEnabled = watcherEnabled;
	}

	public boolean isCleanerEnabled() {
		return cleanerEnabled;
	}

	public void setCleanerEnabled(boolean cleanerEnabled) {
		this.cleanerEnabled = cleanerEnabled;
	}

	public boolean isCleanerLogOnly() {
		return cleanerLogOnly;
	}

	public void setCleanerLogOnly(boolean cleanerLogOnly) {
		this.cleanerLogOnly = cleanerLogOnly;
	}

	public boolean isSetupDatabase() {
		return setupDatabase;
	}

	public void setSetupDatabase(boolean setupDatabase) {
		this.setupDatabase = setupDatabase;
	}

	public boolean isDownloadCheckerEnabled() {
		return downloadCheckerEnabled;
	}

	public void setDownloadCheckerEnabled(boolean downloadCheckerEnabled) {
		this.downloadCheckerEnabled = downloadCheckerEnabled;
	}

	public boolean isMediaConverterEnabled() {
		return mediaConverterEnabled;
	}

	public void setMediaConverterEnabled(boolean mediaConverterEnabled) {
		this.mediaConverterEnabled = mediaConverterEnabled;
	}
}
