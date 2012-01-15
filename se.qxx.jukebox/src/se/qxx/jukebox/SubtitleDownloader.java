package se.qxx.jukebox;

import java.util.ArrayList;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class SubtitleDownloader implements Runnable {

	//TODO: add event listeners that listens for that subtitles for a specific movie
	// has been downloaded
	
	//TODO: implement run method
	
	//TODO: somehow block thread until there are members in _listToDownload
	
	private List<Movie> _listToDownload = new ArrayList<Movie>();
	private static SubtitleDownloader _instance;
	private boolean _isRunning;
	
	private SubtitleDownloader() {
		
	}
	
	public static SubtitleDownloader get() {
		if (_instance == null)
			_instance = new SubtitleDownloader();
		
		return _instance;
	}

	@Override
	public void run() {
		
	}
	
	public void stop() {
		
	}
	
	
}
