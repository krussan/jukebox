package se.qxx.jukebox.watcher;

public interface IDownloadChecker {

	public void checkFile(FileRepresentation f);
	public Runnable getRunnable();
}