package se.qxx.jukebox.watcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.converter.FileChangedState;
import se.qxx.jukebox.converter.FileRepresentationState;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDownloadChecker;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;
import se.qxx.jukebox.tools.MediaMetadata;
import se.qxx.jukebox.tools.Util;

@Singleton
public class DownloadChecker extends JukeboxThread implements IDownloadChecker {

	private ReentrantLock lock = new ReentrantLock();
	private Map<String, FileRepresentationState> files = new ConcurrentHashMap<String, FileRepresentationState>();

	private IDatabase database;
	// <string, string>
	// <filename - FileRepresentation, state, exist in db?, downloadflag from db>
	private IFilenameChecker filenameChecker;
	private IMediaMetadataHelper metaDataHelper;
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
	public DownloadChecker(IExecutor executor, IDatabase database, LoggerFactory loggerFactory,
			IFilenameChecker filenameChecker, IMediaMetadataHelper metaDataHelper) {
		super("DownloadChecker", 300000, loggerFactory.create(LogType.CHECKER), executor);
		this.setMetaDataHelper(metaDataHelper);
		this.setFilenameChecker(filenameChecker);
		this.setDatabase(database);
	}

	public Map<String, FileRepresentationState> getFiles() {
		return files;
	}

	public void setFiles(Map<String, FileRepresentationState> files) {
		this.files = files;
	}

	public IMediaMetadataHelper getMetaDataHelper() {
		return metaDataHelper;
	}

	public void setMetaDataHelper(IMediaMetadataHelper metaDataHelper) {
		this.metaDataHelper = metaDataHelper;
	}

	public IFilenameChecker getFilenameChecker() {
		return filenameChecker;
	}

	public void setFilenameChecker(IFilenameChecker filenameChecker) {
		this.filenameChecker = filenameChecker;
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
		checkCachedFiles();
	}
	
	
	public void checkCachedFiles() {
		// check all files in map that is not marked as DONE
		for (String filename : this.getFiles().keySet()) {
			FileRepresentationState fs = this.getFiles().get(filename);
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
				// check DB again. If present change WAIT->INIT
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

	public void resetFile(FileRepresentationState fs, String filename) {
		this.getLog().Debug(String.format("-- STATE -- Setting download state to INIT on :: %s", filename));
		fs.setState(FileChangedState.INIT);
		store(fs, filename);
	}

	public void markCompleted(FileRepresentationState fs, String filename) {
		this.getLog().Debug(String.format("-- STATE -- Setting download state to DONE on :: %s", filename));
		fs.setState(FileChangedState.DONE);

		Media md = this.getDatabase().getMediaByFilename(fs.getName());

		// should never be null, but anyway
		if (md != null) {
			setMediaComplete(md);
		}

		store(fs, filename);

	}

	public void setMediaComplete(Media md) {
		MediaMetadata meta = this.getMetaDataHelper().getMediaMetadata(md);

		Media newMedia = Media.newBuilder(md).setDownloadComplete(true).setConverterState(MediaConverterState.Queued)
				.setMetaDuration(meta.getDurationSeconds()).setMetaFramerate(meta.getFramerate()).build();

		this.getDatabase().save(newMedia);
	}

	public void markInProgress(FileRepresentationState fs, String filename) {
		this.getLog().Debug(String.format("-- STATE -- %s -> CHANGED on :: %s", fs.getState(), filename));
		fs.setState(FileChangedState.CHANGED);

		Media md = this.getDatabase().getMediaByFilename(fs.getName());

		// should never be null, but anyway
		if (md != null) {
			this.getDatabase().setDownloadInProgress(md.getID());
		}

		store(fs, filename);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.qxx.jukebox.watcher.IDownloadChecker#checkFile(se.qxx.jukebox.watcher.
	 * FileRepresentation)
	 */
	@Override
	public void checkFile(FileRepresentation f) {
		lock.lock();
		try {
			if (this.isRunning()) {
				if (!this.getFilenameChecker().isExcludedFile(f)) {
					FileRepresentationState fs = new FileRepresentationState(f);
					String filename = f.getFullPath().toLowerCase();

					if (getFiles().containsKey(filename)) {
						checkFilePresentInMap(fs, filename);
					} else {
						checkFileNotPresentInMap(fs, filename);
					}
				}
			}
		} finally {
			lock.unlock();
		}

	}

	/***
	 * File exist in map and has changed. Set to CHANGED and replace it in MAP.
	 * 
	 * @param fs
	 * @param filename
	 */
	public void checkFilePresentInMap(FileRepresentationState fs, String filename) {
		FileRepresentationState fsOld = this.getFiles().get(filename);

		if (fsOld.getState() == FileChangedState.DONE) {
			// A file that was supposedly done has changed
			// reset the file and update database

			markInProgress(fs, filename);
		} else if (fsOld.getState() != FileChangedState.WAIT) {
			// A file was changed. Set state to changed.
			// Ignore those in WAIT state until watcher finishes
			this.getLog().Debug(String.format("-- STATE -- File exists in map. State %s -> CHANGED on :: %s",
					fsOld.getState(), filename));
			fs.setState(FileChangedState.CHANGED);
			store(fs, filename);
		}

	}

	/***
	 * File was not present in map. If media exist in DB then set state to INIT else
	 * set it to WAIT Store the representation in the map
	 * 
	 * @param fs
	 * @param filename
	 */
	public void checkFileNotPresentInMap(FileRepresentationState fs, String filename) {
		// check DB flag
		Media md = null;

		// this check is trigered from every file system watcher,
		// these threads quickly gets many. hence the synchronization

		md = getSynchronizedMedia(fs);

		if (md != null) {
			// media exist. set State to INIT
			// check if download is marked as complete already
			if (md.getDownloadComplete()) {
				this.getLog().Debug(String
						.format("-- STATE -- Download already completed. Setting state to DONE on :: %s", filename));
				fs.setState(FileChangedState.DONE);
			} else {
				this.getLog().Debug(
						String.format("-- STATE -- Download NOT completed. Setting state to INIT on :: %s", filename));
				fs.setState(FileChangedState.INIT);
			}
		} else {
			this.getLog().Debug(String.format("-- STATE -- Setting state to WAIT on :: %s", filename));
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
		} finally {
			lock.unlock();
		}
	}

	private void store(FileRepresentationState fs, String filename) {
		lock.lock();
		try {
			this.getFiles().put(filename, fs);
		} finally {
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
