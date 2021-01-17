package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.watcher.FileRepresentation;

public interface IFileCreatedHandler {
	void fileModified(FileRepresentation f);
	void fileCreated(FileRepresentation f);
}
