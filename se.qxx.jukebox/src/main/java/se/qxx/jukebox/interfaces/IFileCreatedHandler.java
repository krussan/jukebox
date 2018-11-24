package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.watcher.FileRepresentation;

public interface IFileCreatedHandler {
	public void fileModified(FileRepresentation f);
	public void fileCreated(FileRepresentation f);
}
