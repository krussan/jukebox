package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.watcher.FileRepresentation;

public interface IFilenameChecker {

	public boolean isConvertedFile(String filename);
	public boolean isExcludedFile(FileRepresentation file);
	public boolean isSampleFile(String filename);
	public boolean isSmallFile(long size);
}
