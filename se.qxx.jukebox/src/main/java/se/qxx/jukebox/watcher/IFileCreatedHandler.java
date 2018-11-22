package se.qxx.jukebox.watcher;

public interface IFileCreatedHandler {
	public void fileModified(FileRepresentation f);
	public void fileCreated(FileRepresentation f);
}
