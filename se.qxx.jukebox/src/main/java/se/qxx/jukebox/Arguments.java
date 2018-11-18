package se.qxx.jukebox;
  
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import se.qxx.jukebox.interfaces.IArguments;

public class Arguments implements IArguments {

	
	@Inject
	public Arguments(@Named("Commandline arguments") List<String> args) {
		this.initialize(args);
	}
	
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
	private boolean downloadCheckerEnabled = true;
	private boolean mediaConverterEnabled = true;
	
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

	
	public void initialize(List<String> arguments) {
		if (arguments.contains("-dt"))
			this.setTcpListenerEnabled(false);
		
		if (arguments.contains("-ds"))
			this.setSubtitleDownloaderEnabled(false);
		
		if (arguments.contains("-di"))
			this.setImdbIdentifierEnabled(false);

		if (arguments.contains("-dm"))
			this.setMediaInfoEnabled(false);

		if (arguments.contains("-dw"))
			this.setWebServerEnabled(false);

		if (arguments.contains("--purge"))
			this.setPurgeMode(true);
		
		if (arguments.contains("--help") || arguments.contains("-?"))
			this.setHelpRequested(true);
		
		if (arguments.contains("--purgeSubs"))
			this.setPurgeSubtitles(true);
		
		if (arguments.contains("--purgeSeries"))
			this.setPurgeSeries(true);
		
		if (arguments.contains("-df"))
			this.setWatcherEnabled(false);
		
		if (arguments.contains("-dc"))
			this.setCleanerEnabled(false);
		
		if (arguments.contains("-dcl")) {
			this.setCleanerEnabled(true);
			this.setCleanerLogOnly(true);
		}
		
		if (arguments.contains("-dd")) 
			this.setDownloadCheckerEnabled(false);
		
		if (arguments.contains("-dmc"))
			this.setMediaConverterEnabled(false);
		
		if (arguments.contains("--setupdb")) {
			this.setSetupDatabase(true);
		}

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
