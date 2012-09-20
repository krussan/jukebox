package se.qxx.jukebox;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class FileCreatedHandler implements INotifyClient {

	@Override
	public void fileModified(FileRepresentation f) {
		
	}
	
	@Override
	public void fileCreated(FileRepresentation f)  {
		Log.Debug(String.format("New file found :: %s", f.getName()), Log.LogType.FIND);
		
		String filename = f.getName();
		String path = f.getPath();
		// Added ignore on all filename that contains the string sample
		if (StringUtils.containsIgnoreCase(filename, "sample")) {
			Log.Info(String.format("Ignoring %s as this appears to be a sample", filename), LogType.FIND);
		}
		else {
			if (isTvEpisode(filename)) {
				Log.Info(String.format("Ignoring %s as this appears to be a TV episode", filename), LogType.FIND);
			}
			else {
				Movie m = MovieBuilder.identifyMovie(path, filename);
				
				// TODO: Get info about movie from NFO if available
				
				if (m != null) {
					Log.Info(String.format("Movie identified by %s as :: %s", m.getIdentifier().toString(), m.getTitle()), Log.LogType.FIND);

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
						// If title is the same as the filename (except ignore pattern) then don't identify on IMDB.
						if (Arguments.get().isImdbIdentifierEnabled())
							m = getImdbInformation(m);
						
						m = DB.addMovie(m);
						
						SubtitleDownloader.get().addMovie(m);			
					}
				}
				else {
					Log.Info(String.format("Failed to identity movie with filename :: %s", f.getName()), Log.LogType.FIND);
				}
			}
		}
	}
	
	private Movie getImdbInformation(Movie m) {
		//find imdb link
		try {
			m = IMDBFinder.Search(m);
			if (m.getImdbUrl() != null && m.getImdbUrl() != "")
				Log.Info(String.format("IMDB link found for :: %s", m.getTitle()), LogType.FIND);
		}
		catch (IOException e) {
			Log.Error("Error occured when finding IMDB link", Log.LogType.FIND, e);
		}
		
		return m;
	}
	
	private boolean isTvEpisode(String filename) {
		Pattern p1 = Pattern.compile("s\\d{1,2}e\\d{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher m1 = p1.matcher(filename);
		
		Pattern p2 = Pattern.compile("\\d{1,2}x\\d{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher m2 = p2.matcher(filename);
		
		Pattern p3 = Pattern.compile("Episode", Pattern.CASE_INSENSITIVE);
		Matcher m3 = p3.matcher(filename);
		return m1.find() || m2.find() || m3.find();
	}
}
