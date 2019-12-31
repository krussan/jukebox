package se.qxx.jukebox.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import se.qxx.jukebox.concurrent.JukeboxPriorityQueue;
import se.qxx.jukebox.concurrent.JukeboxThreadPoolExecutor;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.*;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IDownloadChecker;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFileCreatedHandler;
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
	private ExecutorService executorService;

	private WebServerFactory webServerFactory;

	private TcpListenerFactory tcpListenerFactory;

	@Inject
	public Main(IArguments arguments,
				IExecutor executor,
				ISubtitleDownloader subtitleDownloader,
				ICleaner cleaner,
				WebServerFactory webServerFactory,
				ISettings settings,
				TcpListenerFactory tcpListenerFactory,
				IMovieIdentifier movieIdentifier,
				IDownloadChecker downloadChecker,
				IMediaConverter mediaConverter,
				LoggerFactory loggerFactory,
				IFileCreatedHandler fileCreatedHandler,
				FileSystemWatcherFactory fileSystemWatcherFactory,
				ExecutorService executorService) {

		this.setTcpListenerFactory(tcpListenerFactory);
		this.setWebServerFactory(webServerFactory);
		this.setFileSystemWatcherFactory(fileSystemWatcherFactory);
		this.setFileCreatedHandler(fileCreatedHandler);
		this.setArguments(arguments);	
		this.setExecutor(executor);
		this.setSubtitleDownloader(subtitleDownloader);
		this.setCleaner(cleaner);
		//this.setWebServer(webServer);
		this.setSettings(settings);
		//this.setTcpListener(tcpListener);
		this.setMovieIdentifier(movieIdentifier);
		this.setDownloadChecker(downloadChecker);
		this.setMediaConverter(mediaConverter);
		this.setExecutorService(executorService);

		this.setLog(loggerFactory.create(LogType.MAIN));
	}	
	
	public TcpListenerFactory getTcpListenerFactory() {
		return tcpListenerFactory;
	}

	public void setTcpListenerFactory(TcpListenerFactory tcpListenerFactory) {
		this.tcpListenerFactory = tcpListenerFactory;
	}

	public WebServerFactory getWebServerFactory() {
		return webServerFactory;
	}

	public void setWebServerFactory(WebServerFactory webServerFactory) {
		this.webServerFactory = webServerFactory;
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

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	java.util.concurrent.Semaphore s = new java.util.concurrent.Semaphore(1);	
	
	public void run() {
		
		try {
			cleanupStopperFile();			
			
			consoleLog("Initializing settings");
			this.getSettings().initialize();
			
			// Create instances of tcp listener and web server
			setupFactoryInstances();

			this.getWebServer().initializeMappings(this.getSettings());
			
			setupConfigurationListener();
			startupThreads();
			
			this.setIsRunning(true);;
			
			// start by acquiring the semaphore
			s.acquire();

			while (this.getIsRunning()) {
				if (this.getArguments().isWatcherEnabled()) {
					consoleLog("Starting up watcher thread");
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
		
		
		logAllThreads();
		this.getLog().Info("Shutdown completed!");
		cleanupStopperFile();
	}
	
	private void logAllThreads() {
		this.getLog().Debug("-------------- Logging all threads on exit ----------------");
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		 
		for (Thread t : threads) {
		    String name = t.getName();
		    Thread.State state = t.getState();
		    int priority = t.getPriority();
		    String type = t.isDaemon() ? "Daemon" : "Normal";
		    this.getLog().Debug(String.format("%-20s \t %s \t %d \t %s\n", name, state, priority, type));
		}
	}

	private void setupFactoryInstances() {
		//TODO: Extract webserver port to settings
		if (this.getWebServer() == null)
			this.setWebServer(this.getWebServerFactory().create(8001));

		if (this.getTcpListener() == null)
			this.setTcpListener(this.getTcpListenerFactory().create(
					this.getWebServer(),
					this.getExecutorService(),
					this.getSettings().getSettings().getTcpListener().getPort().getValue()));

	}
	
	private void consoleLog(String message) {
		System.out.println(message);
		this.getLog().Info(message);
	}

	private void startupThreads() {
		consoleLog("Starting threads ...");
		if (this.getArguments().isTcpListenerEnabled()) {
			consoleLog("Starting TCP listener");
			this.getExecutor().start(this.getTcpListener().getRunnable());
		}
		
		if (this.getArguments().isSubtitleDownloaderEnabled()) {
			consoleLog("Starting subtitle downloader");
			this.getExecutor().start(this.getSubtitleDownloader().getRunnable());
		}
		
		if (this.getArguments().isWebServerEnabled()) {
			consoleLog("Starting web server");
			this.getWebServer().initialize();
			this.getExecutor().start(this.getWebServer().getRunnable());
		}
		
		if (this.getArguments().isWatcherEnabled()) {
			consoleLog("Starting watcher thread");
			this.getExecutor().start(this.getMovieIdentifier().getRunnable());
		}
		
		if (this.getArguments().isCleanerEnabled()) {
			consoleLog("Starting cleaner thread");
			this.getExecutor().start(this.getCleaner().getRunnable());				
		}
		
		if (this.getArguments().isDownloadCheckerEnabled()) {
			consoleLog("Starting download checker");
			this.getExecutor().start(this.getDownloadChecker().getRunnable());
		}
		
		if (this.getArguments().isMediaConverterEnabled()) {
			consoleLog("Starting media converter");
			this.getExecutor().start(this.getMediaConverter().getRunnable());
		}
	}

	private void setupConfigurationListener() {
		consoleLog("Setting watcher on configuration file");
		
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
		consoleLog("Server is shutting down ...");		
		consoleLog(String.format("Number of threads :: %s", Thread.activeCount()));
		
		try {
			
			this.getExecutor().stop(10);

			// stop main thread
			consoleLog("Stopping server ...");
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
