package se.qxx.jukebox.watcher;

public interface INotifyClient {
	public void fileModified(FileRepresentation f);
	public void fileCreated(FileRepresentation f);
}
