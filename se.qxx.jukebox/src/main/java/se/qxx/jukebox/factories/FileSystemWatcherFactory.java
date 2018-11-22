package se.qxx.jukebox.factories;

import se.qxx.jukebox.interfaces.IFileSystemWatcher;
import se.qxx.jukebox.watcher.ExtensionFileFilter;

public interface FileSystemWatcherFactory {
	IFileSystemWatcher create(String name, 
			String directoryName, 
			ExtensionFileFilter filter, 
			boolean watchCreated,
			boolean watchModified, 
			boolean recurse, 
			int waitTime);
}
