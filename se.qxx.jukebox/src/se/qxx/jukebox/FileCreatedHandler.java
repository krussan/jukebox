package se.qxx.jukebox;

import java.io.IOException;
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
		
		m = getImdbInformation(m);
		m = addMovieToDB(m);
		
		SubtitleDownloader.get().addMovie(m);
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
