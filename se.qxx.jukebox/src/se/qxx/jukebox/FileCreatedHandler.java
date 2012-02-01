package se.qxx.jukebox;

import java.io.IOException;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class FileCreatedHandler implements INotifyClient {

	@Override
	public void fileModified(FileRepresentation f) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileCreated(FileRepresentation f)  {
		Log.Debug(String.format("New file found :: %s", f.getName()));
		Movie m = Util.extractMovie(f.getPath(), f.getName());
		
		// Check if movie exists in db
		if (!movieExistsInDB(m)) {
			
			// If it does but in a different path update the path
			// If it does but in same path exit
			// If not get information and subtitles
			
			m = getImdbInformation(m);
			m = addMovieToDB(m);
			
			SubtitleDownloader.get().addMovie(m);
		}
	}
	

	private boolean movieExistsInDB(Movie m) {
		try {
			return DB.movieExists(m);
		}
		catch (Exception e) {
			Log.Error("failed to get information from database", e);
			
			// return true on error to avoid triggering download of subtitles and storing a new record to DB
			return true;
		}
	}

	
	private Movie addMovieToDB(Movie m) {
		try {
			m = DB.addMovie(m);
			Log.Info("Movie added to database");
		}
		catch (Exception e) {
			Log.Error("failed to store to database", e);
		}
		
		return m;
	}

	private Movie getImdbInformation(Movie m) {
		//find imdb link
		try {
			m = IMDBFinder.Search(m);
			Log.Info(String.format("Movie identified as :: %s", m.getTitle()));
		}
		catch (IOException e) {
			Log.Error("Error occured when finding IMDB link", e);
		}
		
		return m;
	}
}
