package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.watcher.FileRepresentation;

public interface IDownloadChecker {

	public void checkFile(FileRepresentation f);
	public Runnable getRunnable();
}