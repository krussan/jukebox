package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.watcher.IFileCreatedHandler;

public interface IFileSystemWatcher {

	void registerClient(IFileCreatedHandler client);
	void setSleepTime(long sleep);
	Runnable getRunnable();

}