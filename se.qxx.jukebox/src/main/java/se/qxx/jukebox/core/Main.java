package se.qxx.jukebox.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.factories.FileSystemWatcherFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFileSystemWatcher;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMain;
import se.qxx.jukebox.interfaces.IMediaConverter;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.ITcpListener;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.watcher.ExtensionFileFilter;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.watcher.IDownloadChecker;
import se.qxx.jukebox.watcher.IFileCreatedHandler;

public class Main implements IMain, IFileCreatedHandler
{
	private Boolean isRunning;

	private IArguments arguments;
	private IExecutor executor;
	private ISubtitleDownloader subtitleDownloader;
	private ICleaner cleaner;
	private IStreamingWebServer webServer;
	private ISettings settings;
	private ITcpListener tcpListener;
	private IMovieIdentifier movieIdentifier;
	private IDownloadChecker downloadChecker;
	private IMediaConverter mediaConverter;
	private IJukeboxLogger log;
	private IFileCreatedHandler fileCreatedHandler;
	private FileSystemWatcherFactory fileSystemWatcherFactory;
	
	@Inject
	public Main(IArguments arguments, 
			IExecutor executor, 
			ISubtitleDownloader subtitleDownloader, 
			ICleaner cleaner, 
			IStreamingWebServer webServer, 
			ISettings settings,
			ITcpListener tcpListener,
			IMovieIdentifier movieIdentifier,
			IDownloadChecker downloadChecker,
			IMediaConverter mediaConverter,
			LoggerFactory loggerFactory,
			IFileCreatedHandler fileCreatedHandler,
			FileSystemWatcherFactory fileSystemWatcherFactory) {
		
		this.setFileSystemWatcherFactory(fileSystemWatcherFactory);
		this.setFileCreatedHandler(fileCreatedHandler);
		this.setArguments(arguments);	
		this.setExecutor(executor);
		this.setSubtitleDownloader(subtitleDownloader);
		this.setCleaner(cleaner);
		this.setWebServer(webServer);
		this.setSettings(settings);
		this.setTcpListener(tcpListener);
		this.setMovieIdentifier(movieIdentifier);
		this.setDownloadChecker(downloadChecker);
		this.setMediaConverter(mediaConverter);
		
		this.setLog(loggerFactory.create(LogType.MAIN));
	}	
	
	public FileSystemWatcherFactory getFileSystemWatcherFactory() {
		return fileSystemWatcherFactory;
	}

	public void setFileSystemWatcherFactory(FileSystemWatcherFactory fileSystemWatcherFactory) {
		this.fileSystemWatcherFactory = fileSystemWatcherFactory;
	}

	public IFileCreatedHandler getFileCreatedHandler() {
		return fileCreatedHandler;
	}

	public void setFileCreatedHandler(IFileCreatedHandler fileCreatedHandler) {
		this.fileCreatedHandler = fileCreatedHandler;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public IMediaConverter getMediaConverter() {
		return mediaConverter;
	}

	public void setMediaConverter(IMediaConverter mediaConverter) {
		this.mediaConverter = mediaConverter;
	}

	public IDownloadChecker getDownloadChecker() {
		return downloadChecker;
	}

	public void setDownloadChecker(IDownloadChecker downloadChecker) {
		this.downloadChecker = downloadChecker;
	}

	public IMovieIdentifier getMovieIdentifier() {
		return movieIdentifier;
	}

	public void setMovieIdentifier(IMovieIdentifier movieIdentifier) {
		this.movieIdentifier = movieIdentifier;
	}

	public ITcpListener getTcpListener() {
		return tcpListener;
	}

	public void setTcpListener(ITcpListener tcpListener) {
		this.tcpListener = tcpListener;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public IStreamingWebServer getWebServer() {
		return webServer;
	}

	public void setWebServer(IStreamingWebServer webServer) {
		this.webServer = webServer;
	}

	public ICleaner getCleaner() {
		return cleaner;
	}

	public void setCleaner(ICleaner cleaner) {
		this.cleaner = cleaner;
	}

	public ISubtitleDownloader getSubtitleDownloader() {
		return subtitleDownloader;
	}

	public void setSubtitleDownloader(ISubtitleDownloader subtitleDownloader) {
		this.subtitleDownloader = subtitleDownloader;
	}

	public IExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(IExecutor executor) {
		this.executor = executor;
	}

	public IArguments getArguments() {
		return arguments;
	}
	public void setArguments(IArguments arguments) {
		this.arguments = arguments;
	}
	
	public Boolean getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	java.util.concurrent.Semaphore s = new java.util.concurrent.Semaphore(1);	
	
	public void run() {
		
		try {
			cleanupStopperFile();			
			
			System.out.println("Initializing settings");
			this.getSettings().initialize();
			
			startupThreads();
			
			this.setIsRunning(true);;
			setupConfigurationListener();
			
			// start by acquiring the semaphore
			s.acquire();

			while (this.getIsRunning()) {
				if (this.getArguments().isWatcherEnabled()) {
					System.out.println("Starting up watcher thred");
					setupCatalogs();
				}

				// acquire a new to block this thread until it is released by another thread (by calling stop)
				s.acquire();
			
				this.getSettings().initialize();	
			}
		}
		catch (Exception e) {
			this.getLog().Error("An error occured on main thread::", e);
		}
		
		this.getLog().Info("Shutdown completed!");
		cleanupStopperFile();
	}

	private void startupThreads() {
		System.out.println("Starting threads ...");
		if (this.getArguments().isTcpListenerEnabled()) {
			System.out.println("Starting TCP listener");
			this.getExecutor().start(this.getTcpListener().getRunnable());
		}
		
		if (this.getArguments().isSubtitleDownloaderEnabled()) {
			System.out.println("Starting subtitle downloader");
			this.getExecutor().start(this.getSubtitleDownloader().getRunnable());
		}
		
		if (this.getArguments().isWebServerEnabled()) {
			System.out.println("Starting web server");
			this.getExecutor().start(this.getWebServer().getRunnable());
		}
		
		if (this.getArguments().isWatcherEnabled()) {
			System.out.println("Starting watcher thread");
			this.getExecutor().start(this.getMovieIdentifier().getRunnable());
		}
		
		if (this.getArguments().isCleanerEnabled()) {
			System.out.println("Starting cleaner thread");
			this.getExecutor().start(this.getCleaner().getRunnable());				
		}
		
		if (this.getArguments().isDownloadCheckerEnabled()) {
			System.out.println("Starting download checker");
			this.getExecutor().start(this.getDownloadChecker().getRunnable());
		}
		
		if (this.getArguments().isMediaConverterEnabled()) {
			System.out.println("Starting media converter");
			this.getExecutor().start(this.getMediaConverter().getRunnable());
		}
	}

	private void setupConfigurationListener() {
		System.out.println("Setting watcher on configuration file");
		
		ExtensionFileFilter filter = new ExtensionFileFilter();
		filter.addExtension("xml");
		filter.addExtension("stp");
		
		IFileSystemWatcher configurationWatcher =
				this.getFileSystemWatcherFactory().create("ConfigurationWatcher", ".", filter, true, true, false, 500);
		configurationWatcher.setSleepTime(300);
		configurationWatcher.registerClient(this);
		this.getExecutor().start(configurationWatcher.getRunnable());
	}
	

	private void cleanupStopperFile() {
		File f = new File("stopper.stp");
		if (f.exists()) {
			this.getLog().Debug("Cleaning up stopper file");
			f.delete();
		}
	}

	public void stop() {
		this.getLog().Info("Server is shutting down ...");

		try {
			this.getExecutor().stop();

			// stop main thread
			System.out.println("Stopping server ...");
			this.setIsRunning(false);
			s.release();

		} catch (InterruptedException e) {
			
		}

	
	}
	
	public void fileModified(FileRepresentation f) {
		this.getLog().Debug(String.format("-- file modified :: %s", f.getName()));
		if (f.getName().endsWith("xml"))
			s.release();
	}
	
	public void fileCreated(FileRepresentation f){
		this.getLog().Debug(String.format("-- file created :: %s", f.getName()));
		if (f.getName().endsWith("stp"))
			stop();
	}
	
	private void setupCatalogs(){
		this.getExecutor().stopWatchers();

		ExtensionFileFilter ff = new ExtensionFileFilter();
		ff.addExtensions(getExtensions());
		
		int cc = 0;
		for (Catalog c : this.getSettings().getSettings().getCatalogs().getCatalog()) {
			cc++;
			File path = new File(c.getPath());
			
			if (path.exists()) {
				IFileSystemWatcher f =
						this.getFileSystemWatcherFactory().create(String.format("Catalog %s", cc),c.getPath(), ff, true, true, true, 10000);

				this.getLog().Debug(String.format(
					"Starting listening on :: %s", 
					c.getPath()));
			
				f.registerClient(this.getFileCreatedHandler());
				f.setSleepTime(500);
				
				this.getExecutor().start(f.getRunnable());
			
			}
		}
	}


	private List<String> getExtensions() {
		List<String> list = new ArrayList<String>();
		
		for (JukeboxListenerSettings.Catalogs.Catalog c : this.getSettings().getSettings().getCatalogs().getCatalog()) {
			for (JukeboxListenerSettings.Catalogs.Catalog.Extensions.Extension e : c.getExtensions().getExtension()) {
				if (!list.contains(e.getValue()))
					list.add(e.getValue());
			}
		}
		
		return list;
	}
}