package se.qxx.jukebox;

import java.util.ArrayList;

import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog.Extensions.Extension;
import se.qxx.jukebox.settings.Settings;

public class Main implements Runnable, INotifyClient
{
	private Boolean isRunning = false;
	private TcpListener _listener;
	ArrayList<FileSystemWatcher> watchers = new ArrayList<FileSystemWatcher>();
	
    private Runnable scanExisting = new Runnable() {

        @Override
        public void run() {
            scanExisting();
        }
    };
    
	public Main() {
		
	}
	
	java.util.concurrent.Semaphore s = new java.util.concurrent.Semaphore(1);	
	
	public void run() {
		
		try {
			setupListening();

			Thread t = new Thread(scanExisting);
			t.start();
			
			Thread subtitleDownloaderThread = new Thread(SubtitleDownloader.get());
			subtitleDownloaderThread.start();
			
			isRunning = true;
			
			ExtensionFileFilter filter = new ExtensionFileFilter();
			filter.addExtension("xml");
			FileSystemWatcher w = new  FileSystemWatcher(".", filter, false, true);
			w.registerClient(this);

			// start by acquiring the semaphore
			s.acquire();

			while (isRunning) {
				Settings.readSettings();
				
				setupCatalogs();
				
				// acquire a new to block this thread until it is released by another thread (by calling stop)
				s.acquire();
			}
		}
		catch (Exception e) {
			System.out.println("An error occured on main thread::");
			System.out.println(e.toString());
		}
	}
	
	public void stop() {
		isRunning = false;
		s.release();
		stopListening();
	}
	
	public void fileModified(FileRepresentation f) {
		s.release();
	}
	
	public void fileCreated(FileRepresentation f){
		
	}
	
	private void setupCatalogs(){
		FileCreatedHandler h = new FileCreatedHandler();
		
		for (FileSystemWatcher f : watchers) {
			f.stopListening();
		}
		
		watchers.clear();
		
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			for (Extension e : c.getExtensions().getExtension()) {
				ExtensionFileFilter ff = new ExtensionFileFilter();
				ff.addExtension(e.getValue());
				FileSystemWatcher f = new FileSystemWatcher(c.getPath(), ff, true, false);
				
				Log.Debug(String.format("Starting listening on :: %s", c.getPath()));
				
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

	private void scanExisting() {
		//scan all folders (and subfolders)
		//if a movie is found and the movie exists in DB with another path then update file path
		//if a movie is found with the same path ignore it
		//if a new movie is found add it to the database and initiate subtitle download
		
	}
}
