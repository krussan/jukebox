package se.qxx.jukebox.watcher;

import se.qxx.jukebox.MovieIdentifier;

public class FileCreatedHandler implements INotifyClient {

	@Override
	public void fileModified(FileRepresentation f) {
		DownloadChecker.get().checkFile(f);
	}
 
	@Override
	public void fileCreated(FileRepresentation f)  {
		MovieIdentifier.get().addFile(f);
		DownloadChecker.get().checkFile(f);
	}


	
}
