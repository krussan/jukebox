package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.watcher.FileRepresentation;

public interface IDownloadChecker {

	void checkFile(FileRepresentation f);
	Runnable getRunnable();
}