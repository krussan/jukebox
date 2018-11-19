package se.qxx.jukebox.watcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.JukeboxThread;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.SubtitleDownloader;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.converter.FileChangedState;
import se.qxx.jukebox.converter.FileRepresentationState;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.tools.Util;

@Singleton
public class DownloadChecker extends JukeboxThread implements IDownloadChecker {
	
	private ReentrantLock lock = new ReentrantLock();
	private Map<String, FileRepresentationState> files = new ConcurrentHashMap<String, FileRepresentationState>();
	
	private IDatabase database;
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
	
	@Inject
	private DownloadChecker(IExecutor executor, IDatabase database) {
		super("DownloadChecker", 300000, LogType.CHECKER, executor);
		this.setDatabase(database);
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	@Override
	protected void initialize() {
	}
	
	@Override
	protected void execute() {		
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
			
			if (!this.isRunning())
				return;
		}
			
	}
	
	
	private void resetFile(FileRepresentationState fs, String filename) {
		Log.Debug(String.format("-- STATE -- Setting download state to INIT on :: %s",  filename), LogType.CHECKER);
		fs.setState(FileChangedState.INIT);
		store(fs, filename);
	}

	private void markCompleted(FileRepresentationState fs, String filename) {
		Log.Debug(String.format("-- STATE -- Setting download state to DONE on :: %s",  filename), LogType.CHECKER);
		fs.setState(FileChangedState.DONE);
		
		Media md = this.getDatabase().getMediaByFilename(fs.getName());
		
		// should never be null, but anyway
		if (md != null) {
			this.getDatabase().setDownloadCompleted(md.getID());
			/*
			 *TODO: Fix reenlist matroska file
			if (Util.isMatroskaFile(md)) {				
				SubtitleDownloader.get().reenlistEpisode(ep);
			}
			*/
		}
		
		store(fs, filename);
		
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.watcher.IDownloadChecker#checkFile(se.qxx.jukebox.watcher.FileRepresentation)
	 */
	@Override
	public void checkFile(FileRepresentation f)  {
		lock.lock();
		try {
			if (this.isRunning()) {
				if (!Util.isExcludedFile(f, LogType.CHECKER)) {
					FileRepresentationState fs = new FileRepresentationState(f);
					String filename = f.getFullPath().toLowerCase();
					
					if (files.containsKey(filename)) {
						checkFilePresentInMap(fs, filename);
					}
					else {
						checkFileNotPresentInMap(fs, filename);
					}
				}
			}		
		}
		finally {
			lock.unlock();
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
			Log.Debug(String.format("-- STATE -- File exists in map. Setting state to CHANGED on :: %s", filename), LogType.CHECKER);
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
		Media md = null;
		
		// this check is trigered from every file system watcher,
		// these threads quickly gets many. hence the synchronization
		
		md = getSynchronizedMedia(fs);
		
		if (md != null) {
			// media exist. set State to INIT
			// check if download is marked as complete already
			if (md.getDownloadComplete()) {
				Log.Debug(String.format("-- STATE -- Download already completed. Setting state to DONE on :: %s", filename), LogType.CHECKER);
				fs.setState(FileChangedState.DONE);
			}
			else {
				Log.Debug(String.format("-- STATE -- Download NOT completed. Setting state to INIT on :: %s", filename), LogType.CHECKER);
				fs.setState(FileChangedState.INIT);
			}
		}
		else {
			Log.Debug(String.format("-- STATE -- Setting state to WAIT on :: %s", filename), LogType.CHECKER);
			// media does not exist. Not handled by identifier yet
			// set state to wait
			fs.setState(FileChangedState.WAIT);
		}
		
		store(fs, filename);
		
	}

	private Media getSynchronizedMedia(FileRepresentationState fs) {
		lock.lock();
		try {
			return this.getDatabase().getMediaByFilename(fs.getName());
		}
		finally {
			lock.unlock();
		}
	}

	private void store(FileRepresentationState fs, String filename) {
		lock.lock();
		try {
			this.files.put(filename, fs);
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public int getJukeboxPriority() {
		return 3;
	}

	@Override
	public Runnable getRunnable() {
		return this;
	}
}
