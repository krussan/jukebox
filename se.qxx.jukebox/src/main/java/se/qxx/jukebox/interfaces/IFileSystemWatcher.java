package se.qxx.jukebox.interfaces;

public interface IFileSystemWatcher {

	void registerClient(IFileCreatedHandler client);
	void setSleepTime(long sleep);
	Runnable getRunnable();

}