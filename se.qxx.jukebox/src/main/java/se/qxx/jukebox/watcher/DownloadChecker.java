package se.qxx.jukebox.watcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.MovieIdentifier;
import se.qxx.jukebox.converter.FileChangedState;
import se.qxx.jukebox.converter.FileRepresentationState;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.tools.Util;

public class DownloadChecker implements Runnable {
	private static DownloadChecker _instance;
	private boolean isRunning;
	
	private Map<String, FileRepresentationState> files = new HashMap<String, FileRepresentationState>();
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
			// check all files in map that is not marked as DONE
			for (String filename : this.files.keySet()) {
				FileRepresentationState fs = this.files.get(filename);
				switch (fs.getState()) {
				case CHANGED:
					// reset state to INIT 
					resetFile(fs, filename);
					break;
				case INIT:
					// if nothing happened on the file then mark it as completed
					markCompleted(fs, filename);
					break;
				case WAIT:
					// check DB again. If present change to INIT
					checkFileNotPresentInMap(fs, filename);
					break;
				case DONE:
					// do nothing. Done files are not handled
					break;					
				default:
					break;
				
				}
			}
			
			try { Thread.sleep(300000); } 
			catch (InterruptedException e) {
				this.setRunning(false);
			}
		}
			
	}
	
	
	private void resetFile(FileRepresentationState fs, String filename) {
		Log.Debug(String.format("-- STATE -- Setting download state to INIT on :: %s",  filename), LogType.FIND);
		fs.setState(FileChangedState.INIT);
		store(fs, filename);
	}

	private void markCompleted(FileRepresentationState fs, String filename) {
		Log.Debug(String.format("-- STATE -- Setting download state to DONE on :: %s",  filename), LogType.FIND);
		fs.setState(FileChangedState.DONE);
		
		Media md = DB.getMediaByFilename(fs.getName());
		
		// should never be null, but anywhay
		if (md != null) {
			DB.save(
				Media.newBuilder(md)
				.setDownloadComplete(true)
				.build());
		}
		
		store(fs, filename);
		
	}

	public void checkFile(FileRepresentation f)  {
		FileRepresentationState fs = new FileRepresentationState(f);
		String filename = f.getFullPath().toLowerCase();
		
		if (files.containsKey(filename)) {
			checkFilePresentInMap(fs, filename);
		}
		else {
			checkFileNotPresentInMap(fs, filename);
		}
	}

	/***
	 * File exist in map and has changed. Set to CHANGED and replace it in MAP.
	 * @param fs
	 * @param filename
	 */
	private void checkFilePresentInMap(FileRepresentationState fs, String filename) {
		FileRepresentationState fsOld = this.files.get(filename);
		
		if (fsOld.getState() != FileChangedState.DONE && fsOld.getState() != FileChangedState.WAIT) {
			Log.Debug(String.format("-- STATE -- File exists in map. Setting state to CHANGED on :: %s", filename), LogType.FIND);
			fs.setState(FileChangedState.CHANGED);
			store(fs, filename);
		}
		
	}

	
	/***
	 * File was not present in map.
	 * If media exist in DB then set state to INIT
	 * else set it to WAIT
	 * Store the representation in the map
	 * 
	 * @param fs
	 * @param filename
	 */
	private void checkFileNotPresentInMap(FileRepresentationState fs, String filename) {
		//check DB flag
		Media md = DB.getMediaByFilename(fs.getName());
		if (md != null) {
			// media exist. set State to INIT
			// check if download is marked as complete already
			if (md.getDownloadComplete()) {
				Log.Debug(String.format("-- STATE -- Download already completed. Setting state to DONE on :: %s", filename), LogType.FIND);
				fs.setState(FileChangedState.DONE);
			}
			else {
				Log.Debug(String.format("-- STATE -- Download NOT completed. Setting state to INIT on :: %s", filename), LogType.FIND);
				fs.setState(FileChangedState.INIT);
			}
		}
		else {
			Log.Debug(String.format("-- STATE -- Setting state to WAIT on :: %s", filename), LogType.FIND);
			// media does not exist. Not handled by identifier yet
			// set state to wait
			fs.setState(FileChangedState.WAIT);
		}
		
		store(fs, filename);
		
	}

	private void store(FileRepresentationState fs, String filename) {
		synchronized(_instance) {
			this.files.put(filename, fs);
		}
	}

}
