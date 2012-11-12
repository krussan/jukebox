package se.qxx.jukebox;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.builders.PartPattern;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
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
						
				if (m != null) {
					matchMovieWithDatabase(m, filename, path);
				}
				else {
					Log.Info(String.format("Failed to identity movie with filename :: %s", f.getName()), Log.LogType.FIND);
				}
			}
		}
	}

	protected void matchMovieWithDatabase(Movie m, String filename, String path) {
		Log.Info(String.format("Movie identified by %s as :: %s", m.getIdentifier().toString(), m.getTitle()), Log.LogType.FIND);
		
		// Check if movie exists in db
		PartPattern pp = new PartPattern(filename);
		Movie dbMovie = DB.getMovieByStartOfFilename(pp.getPrefixFilename());
		Media newMedia = m.getMedia(0);

		if (dbMovie == null)
			getInfoAndSaveMovie(m, newMedia);			
		else 
			checkExistingMedia(dbMovie, newMedia);
	}

	protected void getInfoAndSaveMovie(Movie m, Media newMedia) {
		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't identify on IMDB.
		if (Arguments.get().isImdbIdentifierEnabled()) 
			m = getImdbInformation(m);

		// Get media information from MediaInfo library
		Media md = MediaMetadata.addMediaMetadata(newMedia);
		m = Movie.newBuilder(m)
				.clearMedia()
				.addMedia(md)
				.build();
		
		m = DB.addMovie(m);
		
		SubtitleDownloader.get().addMovie(m);
	}

	protected void checkExistingMedia(Movie dbMovie, Media newMedia) {
		// Check if media exists
		if (mediaExists(dbMovie, newMedia)) {
			// Check if movie has subs. If not then add it to the subtitle queue.
			if (!DB.hasSubtitles(dbMovie)) {
				SubtitleDownloader.get().addMovie(dbMovie);
			}							
		}
		else {
			// Add media metadata
			Media md = MediaMetadata.addMediaMetadata(newMedia);

			// If movie exist but not the media then add the media
			DB.addMedia(dbMovie.getID(), md);
		}
	}
	
	private boolean mediaExists(Movie m, Media mediaToFind) {
		for (Media md : m.getMediaList()) {
			if (StringUtils.equalsIgnoreCase(md.getFilename(), mediaToFind.getFilename()) && StringUtils.equalsIgnoreCase(md.getFilepath(), mediaToFind.getFilepath()))
				return false;
		}
		
		return true;
	}

	private Movie getImdbInformation(Movie m) {
		//find imdb link
		try {
			m = IMDBFinder.Get(m);
			if (!StringUtils.isEmpty(m.getImdbUrl()))
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
