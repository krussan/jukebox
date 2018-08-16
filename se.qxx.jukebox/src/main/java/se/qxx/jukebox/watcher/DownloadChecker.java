package se.qxx.jukebox.watcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import se.qxx.jukebox.MovieIdentifier;
import se.qxx.jukebox.tools.Util;

public class DownloadChecker implements Runnable {
	private static DownloadChecker _instance;
	private boolean isRunning;
	
	private Map<String, FileRepresentation> files = new HashMap<String, FileRepresentation>();
	// <string, string>
	// <filename - FileRepresentation, state, exist in db?, downloadflag from db>
	
	// initialization -- setup all
	// file, initialized
	// file changed
	
	// when addFile is triggered store the filerepresentation. state INIT.
	
	// check if file exist in mapping
	// -- yes ->
	// ---- state = WATCH? ->
	// ------ 
	
	// -- no ->
	// -- check if file exist in DB
	// ----no -> state => WAIT_FOR_WATCHER
	// ----yes ->
	// ------state WATCH
	
	
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	private DownloadChecker() {
	}
	
	public static DownloadChecker get() {
		if (_instance == null)
			_instance = new DownloadChecker();
		
		return _instance;
	}
	
	@Override
	public void run() {
		this.setRunning(true);
		Util.waitForSettings();

		mainLoop();
	}
	
	private void mainLoop() {
		while(this.isRunning()) {	
				try { 
					// check file sizes every 5 minutes
					Thread.sleep(300000);
				} 
				catch (InterruptedException e) {}
			}
		
		
//			FileRepresentation f = this.files.poll();
//			
//			if (f != null)
//				identify(f);
			
	}
	
	
	public void addFile(FileRepresentation f)  {
//		if (!files.contains(f)) {
//			synchronized(_instance) {
//				this.files.add(f);
//				_instance.notify();
//			}
//		}
	}


}
