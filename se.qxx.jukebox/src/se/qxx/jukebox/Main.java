package se.qxx.jukebox;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class Main implements Runnable, INotifyClient
{
	private Boolean isRunning = false;
	private TcpListener _listener;
	
	ArrayList<FileSystemWatcher> watchers = new ArrayList<FileSystemWatcher>();
	java.util.concurrent.Semaphore s = new java.util.concurrent.Semaphore(1);	
	
	public void run() {
		
		try {
			readSettings();
			
			if (Arguments.get().isTcpListenerEnabled())
				setupListening();
			
			if (Arguments.get().isSubtitleDownloaderEnabled()) {
				Thread subtitleDownloaderThread = new Thread(SubtitleDownloader.get());
				subtitleDownloaderThread.start();
			}
			
			Thread identifierThread = new Thread(MovieIdentifier.get());
			identifierThread.start();
			
			isRunning = true;
			
			ExtensionFileFilter filter = new ExtensionFileFilter();
			filter.addExtension("xml");
			FileSystemWatcher w = new  FileSystemWatcher(".", filter, false, true);
			w.registerClient(this);

			// start by acquiring the semaphore
			s.acquire();

			while (isRunning) {
				setupCatalogs();

				// acquire a new to block this thread until it is released by another thread (by calling stop)
				s.acquire();
				
				readSettings();				
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

		ExtensionFileFilter ff = new ExtensionFileFilter();
		ff.addExtensions(Util.getExtensions());
		
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			FileSystemWatcher f = new FileSystemWatcher(c.getPath(), ff, true, false);
			
			Log.Debug(String.format("Starting listening on :: %s", c.getPath()), Log.LogType.FIND);
			
			f.registerClient(h);
			f.setSleepTime(500);
			f.startListening();
			
			watchers.add(f);
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
	
	private void readSettings() throws IOException, JAXBException {
		Settings.readSettings();
		ImdbSettings.readSettings();		
	}
}
