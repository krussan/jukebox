package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IFileSystemWatcher;
import se.qxx.jukebox.watcher.ExtensionFileFilter;

public interface FileSystemWatcherFactory {
	IFileSystemWatcher create(
			@Assisted("Name") String name, 
			@Assisted("Directory") String directoryName, 
			ExtensionFileFilter filter, 
			@Assisted("WatchCreated") boolean watchCreated,
			@Assisted("WatchModified") boolean watchModified, 
			@Assisted("Recurse") boolean recurse, 
			int waitTime);
}
