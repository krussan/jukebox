package se.qxx.jukebox.interfaces;

public interface IArguments {
	public boolean isSubtitleDownloaderEnabled();
	public boolean isImdbIdentifierEnabled();
	public boolean isTcpListenerEnabled();
	public boolean isMediaInfoEnabled();
	public boolean isWebServerEnabled();
	public boolean isPurgeMode();
	public boolean isHelpRequested();
	public boolean isPurgeSubtitles();
	public boolean isPurgeSeries();
	public boolean isWatcherEnabled();
	public boolean isCleanerEnabled();
	public boolean isCleanerLogOnly();
	public boolean isSetupDatabase();
	public boolean isDownloadCheckerEnabled();
	public boolean isMediaConverterEnabled();
}
