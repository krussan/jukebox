package se.qxx.jukebox;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.builders.PartPattern;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MovieIdentifier implements Runnable {
	
	private static MovieIdentifier _instance;
	private Queue<FileRepresentation> files;
	private boolean isRunning;
	
	private MovieIdentifier() {
		this.files = new LinkedList<FileRepresentation>();
	}
	
	public static MovieIdentifier get() {
		if (_instance == null)
			_instance = new MovieIdentifier();
		
		return _instance;
	}
	
	@Override
	public void run() {
		this.setRunning(true);
		Util.waitForSettings();

		mainLoop();
	}
	
	private void mainLoop() {
		while(this.isRunning()) {	
			if (this.files.isEmpty()) {
				synchronized (_instance) {
					try { _instance.wait(); } catch (InterruptedException e) {}
				}
			}
			FileRepresentation f = this.files.poll();
			
			if (f != null)
				identify(f);
		}
		
		
	}

	public void addFile(FileRepresentation f)  {
		if (!files.contains(f)) {
			synchronized(_instance) {
				this.files.add(f);
				_instance.notify();
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	private void identify(FileRepresentation f) {
		Log.Debug(String.format("Identifying :: %s", f.getName()), Log.LogType.FIND);		

		String filename = f.getName();
		String path = f.getPath();
		
		// Added ignore on all filename that contains the string sample
		if (StringUtils.containsIgnoreCase(filename, "sample")) {
			Log.Info(String.format("Ignoring %s as this appears to be a sample", filename), LogType.FIND);
		}
		else {
			
			Movie m = MovieBuilder.identifyMovie(path, filename);
			Series s = MovieBuilder.identifySeries(m, path, filename);
					
			if (m != null) {
				matchMovieWithDatabase(m, s, filename);
			}
			else {
				Log.Info(String.format("Failed to identity movie with filename :: %s", f.getName()), Log.LogType.FIND);
			}
			
		}		
	}
	

	
	/**
	 * Checks if the media present in a movie exists in database.
	 * If the movie does not exist then get media information and add it to the database
	 * If the movie exist then check existing media and add it if it does not exist.
	 * @param movie
	 * @param filename
	 */
	protected void matchMovieWithDatabase(Movie movie, Series s, String filename) {
		Log.Info(String.format("MovieIdentifier :: Movie identified by %s as :: %s"
				, movie.getIdentifier().toString(), movie.getTitle()), Log.LogType.FIND);
		
		
		// Check if movie exists in db
		PartPattern pp = new PartPattern(filename);
		
		Log.Debug(String.format("MovieIdentifier :: Finding movie that starts with :: %s", pp.getPrefixFilename()), LogType.FIND);
		
		// Careful here! As the identification of the other movie parts could be in a 
		// different thread. Hence synchronized declaration.
		// Shouldn't be a problem no more as all identification is done on a single thread
		Movie dbMovie = DB.getMovieByStartOfMediaFilename(pp.getPrefixFilename());

		Media newMedia = movie.getMedia(0);
		if (dbMovie == null) {
			Log.Debug("MovieIdentifier :: Movie not found -- adding new", LogType.FIND);
			getInfoAndSaveMovie(movie, newMedia, s);			
		}
		else {
			Log.Debug("MovieIdentifier :: Movie found -- checking existing media", LogType.FIND);	
			checkExistingMedia(dbMovie, newMedia);
		}
		
		//TODO: make the same match for Series/Episodes and add them if they don't exist.
		// we should have an ID on the existing movie - no?
		// Then it should be easy to insert it into the Series object
		DB.findSeriesEpisode(movie.getTitle(), s);
		
		
		throw new NotImplementedException();

	}

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * @param m
	 * @param newMedia
	 */
	protected void getInfoAndSaveMovie(Movie movie, Media media, Series serie) {
		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't identify on IMDB.
		if (Arguments.get().isImdbIdentifierEnabled()) 
			movie = getImdbInformation(movie);

		// Get media information from MediaInfo library
		if (Arguments.get().isMediaInfoEnabled()) {
			Media md = MediaMetadata.addMediaMetadata(media);

			movie = Movie.newBuilder(movie)
					.clearMedia()
					.addMedia(md)
					.build();
		}
		
		movie = DB.save(movie);
		
		SubtitleDownloader.get().addMovie(movie);
	}

	/**
	 * Checks if a media exist in a movie.
	 * If it does then add the movie to the SubtitleDownloader if no subtitles exist.
	 * Otherwise add metadata information to the media and add it to database
	 * @param m
	 * @param md
	 */
	protected void checkExistingMedia(Movie movie, Media media) {
		// Check if media exists
		if (mediaExists(movie, media)) {
			// Check if movie has subs. If not then add it to the subtitle queue.
			if (hasSubtitles(movie)) {
				SubtitleDownloader.get().addMovie(movie);
			}							
		}
		else {
			// Add media metadata
			if (Arguments.get().isMediaInfoEnabled())
				media = MediaMetadata.addMediaMetadata(media);

			// If movie exist but not the media then add the media
			DB.save(Movie.newBuilder(movie).addMedia(media).build());
		}
	}
	
	private boolean mediaExists(Movie m, Media mediaToFind) {
		for (Media md : m.getMediaList()) {
			if (StringUtils.equalsIgnoreCase(md.getFilename(), mediaToFind.getFilename()) && StringUtils.equalsIgnoreCase(md.getFilepath(), mediaToFind.getFilepath()))
				return true;
		}
		
		return false;
	}
   
	private Movie getImdbInformation(Movie m) {
		//find imdb link
		try {
			m = IMDBFinder.Get(m);
			
			if (!StringUtils.isEmpty(m.getImdbUrl()))
				Log.Info(String.format("IMDB link found for :: %s", m.getTitle()), LogType.FIND);
		}
		catch (IOException | NumberFormatException | ParseException e) {
			Log.Error("Error occured when finding IMDB link", Log.LogType.FIND, e);
		}
		
		return m;
	}

	private boolean hasSubtitles(Movie m) {
		for(Media md : m.getMediaList()) {
			if (md.getSubsCount() > 0)
				return true;
		}
		
		return false;
	}
}