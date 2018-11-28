package se.qxx.jukebox.watcher;

import com.google.inject.Inject;

import se.qxx.jukebox.interfaces.IDownloadChecker;
import se.qxx.jukebox.interfaces.IFileCreatedHandler;
import se.qxx.jukebox.interfaces.IMovieIdentifier;

public class FileCreatedHandler implements IFileCreatedHandler {

	private IDownloadChecker downloadChecker;
	private IMovieIdentifier movieIdentifier;

	@Inject
	public FileCreatedHandler(IDownloadChecker downloadChecker, IMovieIdentifier movieIdentifier) {
		this.setDownloadChecker(downloadChecker);
		this.setMovieIdentifier(movieIdentifier);
		
	}
	public IMovieIdentifier getMovieIdentifier() {
		return movieIdentifier;
	}
	public void setMovieIdentifier(IMovieIdentifier movieIdentifier) {
		this.movieIdentifier = movieIdentifier;
	}
	public IDownloadChecker getDownloadChecker() {
		return downloadChecker;
	}
	public void setDownloadChecker(IDownloadChecker downloadChecker) {
		this.downloadChecker = downloadChecker;
	}
	@Override
	public void fileModified(FileRepresentation f) {
		this.getDownloadChecker().checkFile(f);
	}
 
	@Override
	public void fileCreated(FileRepresentation f)  {
		this.getMovieIdentifier().addFile(f);
		this.getDownloadChecker().checkFile(f);
	}


	
}
