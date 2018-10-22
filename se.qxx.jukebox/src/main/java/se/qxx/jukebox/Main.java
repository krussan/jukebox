package se.qxx.jukebox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.converter.MediaConverter;
import se.qxx.jukebox.servercomm.TcpListener;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.ExtensionFileFilter;
import se.qxx.jukebox.watcher.FileCreatedHandler;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.watcher.FileSystemWatcher;
import se.qxx.jukebox.watcher.INotifyClient;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class Main implements Runnable, INotifyClient
{
	private Boolean isRunning;
	private List<JukeboxThread> threadPool = new ArrayList<JukeboxThread>();
	
	public List<JukeboxThread> getThreadPool() {
		return threadPool;
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
			Settings.initialize();
			
			System.out.println("Starting threads ...");
			if (Arguments.get().isTcpListenerEnabled()) {
				System.out.println("Starting TCP listener");
				startupThread(new TcpListener());;
			}
			
			if (Arguments.get().isSubtitleDownloaderEnabled()) {
				System.out.println("Starting subtitle downloader");
				startupThread(SubtitleDownloader.get());
			}
			
			if (Arguments.get().isWebServerEnabled()) {
				System.out.println("Starting web server");
				StreamingWebServer.setup("0.0.0.0", 8001);
			}
			
			if (Arguments.get().isWatcherEnabled()) {
				System.out.println("Starting watcher thread");
				startupThread(MovieIdentifier.get());
			}
			
			if (Arguments.get().isCleanerEnabled()) {
				System.out.println("Starting cleaner thread");
				startupThread(Cleaner.get());
			}
			
			if (Arguments.get().isDownloadCheckerEnabled()) {
				System.out.println("Starting download checker");
				startupThread(DownloadChecker.get());
			}
			
			if (Arguments.get().isMediaConverterEnabled()) {
				System.out.println("Starting media converter");
				startupThread(MediaConverter.get());
			}
			
			
			this.setIsRunning(true);;
			
			setupConfigurationListener();
			
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
		cleanupStopperFile();
	}

	private void setupConfigurationListener() {
		System.out.println("Setting watcher on configuration file");
		
		ExtensionFileFilter filter = new ExtensionFileFilter();
		filter.addExtension("xml");
		filter.addExtension("stp");
		
		FileSystemWatcher configurationWatcher = new FileSystemWatcher("ConfigurationWatcher", ".", filter, true, true, false);
		configurationWatcher.setSleepTime(300);
		configurationWatcher.registerClient(this);
		startupThread(configurationWatcher);
	}
	
	private void startupThread(JukeboxThread t) {
		this.getThreadPool().add(t);
		t.start();
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

		// stop web server
		try {
			System.out.println("Stopping web server ...");
			StreamingWebServer.get().stop();
		} catch(Exception e) {
			Log.Debug("StreamingWebServer error. Continue shutdown", LogType.MAIN);
		}

		// stop all jukebox threads in reverse order
		Collections.reverse(this.getThreadPool());
		
		for (JukeboxThread t : this.getThreadPool()) {
			Log.Debug(String.format("Ending thread :: %s", t.getName()), LogType.MAIN);
			t.end();
			try {
				t.join();
			} catch (InterruptedException e) {
				Log.Debug("Thread was interrupted... continuing", LogType.MAIN);
			}
		}		
		
		// stop main thread
		System.out.println("Stopping server ...");
		this.setIsRunning(false);
		s.release();
	
	}
	
	public void fileModified(FileRepresentation f) {
		Log.Debug(String.format("-- file modified :: %s", f.getName()), LogType.MAIN);
		if (f.getName().endsWith("xml"))
			s.release();
	}
	
	public void fileCreated(FileRepresentation f){
		Log.Debug(String.format("-- file created :: %s", f.getName()), LogType.MAIN);
		if (f.getName().endsWith("stp"))
			stop();
	}
	
	private void setupCatalogs(){
		FileCreatedHandler h = new FileCreatedHandler();
		
		for (JukeboxThread t : threadPool) {
			if (t instanceof FileSystemWatcher) {
				t.end();
			}
		}

		ExtensionFileFilter ff = new ExtensionFileFilter();
		ff.addExtensions(Util.getExtensions());
		
		int cc = 0;
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			cc++;
			File path = new File(c.getPath());
			
			if (path.exists()) {
				FileSystemWatcher f = new FileSystemWatcher(String.format("Catalog %s", cc),c.getPath(), ff, true, true, true);
			
				Log.Debug(String.format(
					"Starting listening on :: %s", 
					c.getPath()), 
					Log.LogType.FIND);
			
				f.registerClient(h);
				f.setSleepTime(500);
				startupThread(f);
			
			}
		}
	}
	
}
