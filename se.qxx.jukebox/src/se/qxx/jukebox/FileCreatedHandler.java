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
		
		// Check if movie exists in db
		Movie dbMovie = DB.getMovie(m.getTitle());
		if (dbMovie != null) {
			// If it does but in a different path update the path
			if (!m.getFilepath().equals(dbMovie.getFilepath())) {
				Movie store = Movie.newBuilder(dbMovie).setFilepath(m.getFilepath()).build();
				DB.updateMovie(store);
			}
			// If it does but in same path exit
		}
		else {
			// If not get information and subtitles
			
			m = getImdbInformation(m);
			m = DB.addMovie(m);
			
			SubtitleDownloader.get().addMovie(m);			
		}
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
