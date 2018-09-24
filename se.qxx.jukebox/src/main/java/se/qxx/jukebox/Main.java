package se.qxx.jukebox;

import java.io.File;
import java.util.ArrayList;

import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.ExtensionFileFilter;
import se.qxx.jukebox.watcher.FileCreatedHandler;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.watcher.FileSystemWatcher;
import se.qxx.jukebox.watcher.INotifyClient;
import se.qxx.jukebox.webserver.StreamingWebServer;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.converter.MediaConverter;
import se.qxx.jukebox.servercomm.TcpListener;
import se.qxx.jukebox.settings.Settings;

public class Main implements Runnable, INotifyClient
{
	private Boolean isRunning;
	private Thread subtitleDownloaderThread;
	private Thread identifierThread; 
	private Thread cleanerThread;
	private Thread mediaConverterThread;
	private Thread downloadCheckerThread;
	
	public Boolean getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}

	private TcpListener _listener;
	
	ArrayList<FileSystemWatcher> watchers = new ArrayList<FileSystemWatcher>();
	java.util.concurrent.Semaphore s = new java.util.concurrent.Semaphore(1);	
	
	public void run() {
		
		try {
			cleanupStopperFile();
			
			System.out.println("Initializing settings");
			Settings.initialize();
			
			System.out.println("Starting threads ...");
			if (Arguments.get().isTcpListenerEnabled()) {
				System.out.println("Starting TCP listener");
				setupListening();
			}
			
			if (Arguments.get().isSubtitleDownloaderEnabled()) {
				System.out.println("Starting subtitle downloader");
				subtitleDownloaderThread = new Thread(SubtitleDownloader.get());
				subtitleDownloaderThread.start();
			}
			
			if (Arguments.get().isWebServerEnabled()) {
				System.out.println("Starting web server");
				StreamingWebServer.setup("0.0.0.0", 8001);
			}
			
			if (Arguments.get().isWatcherEnabled()) {
				System.out.println("Starting watcher thread");
				identifierThread = new Thread(MovieIdentifier.get());
				identifierThread.start();
			}
			
			if (Arguments.get().isCleanerEnabled()) {
				System.out.println("Starting cleaner thread");
				cleanerThread = new Thread(Cleaner.get());
				cleanerThread.start();
			}
			
			if (Arguments.get().isMediaConverterEnabled()) {
				System.out.println("Starting media converter");
				mediaConverterThread = new Thread(MediaConverter.get());
				mediaConverterThread.start();
			}
			
			if (Arguments.get().isDownloadCheckerEnabled()) {
				System.out.println("Starting download checker");
				downloadCheckerThread = new Thread(DownloadChecker.get());
				downloadCheckerThread.start();
			}
			
			this.setIsRunning(true);;
			
			System.out.println("Setting watcher on configuration file");
			ExtensionFileFilter filter = new ExtensionFileFilter();
			filter.addExtension("xml");
			filter.addExtension("stp");
			FileSystemWatcher w = new  FileSystemWatcher(".", filter, true, true);
			w.registerClient(this);

			// start by acquiring the semaphore
			s.acquire();

			while (this.getIsRunning()) {
				if (Arguments.get().isWatcherEnabled()) {
					System.out.println("Starting up watcher thred");
					setupCatalogs();
				}

				// acquire a new to block this thread until it is released by another thread (by calling stop)
				s.acquire();
				
				Settings.initialize();				
			}
		}
		catch (Exception e) {
			System.out.println("An error occured on main thread::");
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		Log.Info("Shutdown completed!", LogType.MAIN);
	}
	
	private void cleanupStopperFile() {
		File f = new File("stopper.stp");
		if (f.exists()) {
			Log.Debug("Cleaning up stopper file", LogType.MAIN);
			f.delete();
		}
	}

	public void stop() {
		Log.Info("Server is shutting down ...", LogType.MAIN);
		
		System.out.println("Stopping tcp listener ...");
		stopListening();

		// stop cleaner thred
		System.out.println("Stopping cleaner thread ...");
		Cleaner.get().stop();
		cleanerThread.interrupt();
		
		// stop download checker
		System.out.println("Stopping download checker ...");
		DownloadChecker.get().stop();
		downloadCheckerThread.interrupt();
		
		// stop ffmpeg processes
		System.out.println("Stopping media converter and ffmpeg threads ...");
		MediaConverter.get().stop();
		mediaConverterThread.interrupt(); 		
		
		// stop web server
		System.out.println("Stopping web server ...");
		StreamingWebServer.get().stop();
		
		// stop subtitle thread
		System.out.println("Stopping subtitle downloader ...");
		SubtitleDownloader.get().stop();
		subtitleDownloaderThread.interrupt();
		
		// stop identifier
		System.out.println("Stopping identifier ...");
		MovieIdentifier.get().stop(); 
		identifierThread.interrupt();
		
		// stop main thread
		System.out.println("Stopping server ...");
		this.setIsRunning(false);
		s.release();
	}
	
	public void fileModified(FileRepresentation f) {
		if (f.getName().endsWith("xml"))
			s.release();
	}
	
	public void fileCreated(FileRepresentation f){
		if (f.getName().endsWith("stp"))
			stop();
	}
	
	private void setupCatalogs(){
		FileCreatedHandler h = new FileCreatedHandler();
		
		for (FileSystemWatcher f : watchers) {
			f.stopListening();
		}
		
		watchers.clear();

		ExtensionFileFilter ff = new ExtensionFileFilter();
		ff.addExtensions(Util.getExtensions());
		
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			File path = new File(c.getPath());
			
			if (path.exists()) {
				FileSystemWatcher f = new FileSystemWatcher(c.getPath(), ff, true, true);
			
				Log.Debug(String.format("Starting listening on :: %s", c.getPath()), Log.LogType.FIND);
			
				f.registerClient(h);
				f.setSleepTime(500);
				f.startListening();
			
				watchers.add(f);
			}
		}
	}
	
	private void setupListening() {
		try {
			_listener = new TcpListener();
			Thread t = new Thread(_listener);
			t.start();
		}
		catch (Exception e) {
			
		}
	}
	
	private void stopListening() {
		try {
			_listener.stopListening();
		}
		catch (Exception e) {
			
		}
	}	
	
}
