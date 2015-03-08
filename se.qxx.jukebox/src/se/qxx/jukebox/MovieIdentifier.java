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
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

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
	protected void matchMovieWithDatabase(Movie movie, Series series, String filename) {
		Log.Info(String.format("MovieIdentifier :: Movie identified by %s as :: %s"
				, movie.getIdentifier().toString(), movie.getTitle()), Log.LogType.FIND);
		
		
		// Check if movie exists in db
		PartPattern pp = new PartPattern(filename);
		
		Log.Debug(String.format("MovieIdentifier :: Finding movie that starts with :: %s", pp.getPrefixFilename()), LogType.FIND);
		
		// Careful here! As the identification of the other movie parts could be in a 
		// different thread. Hence synchronized declaration.
		// Shouldn't be a problem no more as all identification is done on a single thread
		Media newMedia = movie.getMedia(0);
		
		if (series == null) {
			Movie dbMovie = DB.getMovieByStartOfMediaFilename(pp.getPrefixFilename());
	
			
			if (dbMovie == null) {
				Log.Debug("MovieIdentifier :: Movie not found -- adding new", LogType.FIND);
				getInfoAndSaveMovie(movie, newMedia);			
			}
			else {
				Log.Debug("MovieIdentifier :: Movie found -- checking existing media", LogType.FIND);	
				checkExistingMedia(dbMovie, newMedia);
			}
		}
		else {
			matchSeries(series, pp, newMedia);
		}
	}

	private void matchSeries(Series series, PartPattern pp, Media newMedia) {
		int season = pp.getSeason();
		int episode = pp.getEpisode();
		
		Log.Debug(String.format("MovieIndentifier :: Finding series :: %s", series.getTitle()), LogType.FIND);
		
		// find series that matches
		// do we not need to merge dbSeries and series??!!
		Series dbSeries = DB.findSeries(
				series.getTitle());
		
		//if no series found in DB. create new from the created series
		//  - if no series then no seasons and no episodes
		if (dbSeries == null) {
			// no series exist 
			Log.Debug("MovieIdentifier :: No series found! Creating new", LogType.FIND);
			getInfoAndSaveSeries(
				series, 
				season,
				episode,
				newMedia);
		}
		else {
			Log.Debug("MovieIdentifier :: Series found. Searching for season..", LogType.FIND);
			Log.Debug(String.format("MovieIdentifier :: dbSeries nr of episodes :: %s", DomainUtil.findSeason(dbSeries, season).getEpisodeCount()), LogType.FIND);
			
			//verify if dbSeries have the episode.
			//if it does then exit
			if (checkSeries(dbSeries, season, episode)) {
				Log.Debug("MovieIdentifier :: Episode already exist in DB. Exiting ... ", LogType.FIND);
				return;
			}
			
			dbSeries = mergeSeries(dbSeries, series, season, episode);
			
			getInfoAndSaveSeries(
				dbSeries, 
				season,
				episode,
				newMedia);
		}
	}

	/**
	 * Returns if the series already contains information about the season and episode specified
	 * @param dbSeries
	 * @param season
	 * @param episode
	 * @return
	 */
	private boolean checkSeries(Series series, int season, int episode) {
		Season sn = DomainUtil.findSeason(series, season);
		if (sn == null)
			return false;
		
		Episode ep = DomainUtil.findEpisode(sn, episode);
		if (ep == null)
			return false;
		
		return true;
	}

	/**
	 * Takes the season and episode (both at index 0) from the mergeFrom object and merges them
	 * into the mergeTo object. If they already exist nothing will be merged.
	 * @param mergeTo
	 * @param mergeFrom
	 * @param season
	 * @param episode
	 * @return
	 */
	private Series mergeSeries(Series mergeTo, Series mergeFrom, int season, int episode) {
		Season sn = DomainUtil.findSeason(mergeTo, season);
		if (sn == null)
			sn = mergeFrom.getSeason(0);
		
		Episode ep = DomainUtil.findEpisode(sn, episode);
		if (ep == null)
			ep = mergeFrom.getSeason(0).getEpisode(0);
		
		sn = DomainUtil.updateEpisode(sn, ep);
		return DomainUtil.updateSeason(mergeTo, sn);
	}

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * @param m
	 * @param newMedia
	 */
	protected void getInfoAndSaveMovie(Movie movie, Media media) {
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
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * @param m
	 * @param newMedia
	 */
	protected void getInfoAndSaveSeries(Series series, int season, int episode, Media media) {
		
		Series s = series;

		// construct the objects.
		// this should be done here and not in the IMDBFinder.
		// IMDBFinder should expect the objects in the hierarchy to be populated.
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);

		if (sn == null || ep == null)
			throw new IllegalArgumentException("Season or Episode not found!");
		
		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't identify on IMDB.
		if (Arguments.get().isImdbIdentifierEnabled()) 
			s = getImdbInformation(s, season, episode);
		
		sn = DomainUtil.findSeason(s, season);
		ep = DomainUtil.findEpisode(sn, episode);
		
		// Get media information from MediaInfo library
		if (Arguments.get().isMediaInfoEnabled()) {
			Media md = MediaMetadata.addMediaMetadata(media);

			ep = Episode.newBuilder(ep)
					.clearMedia()
					.addMedia(md)
					.build();	
		}
			
		if (Arguments.get().isSubtitleDownloaderEnabled()) {
			ep = SubtitleDownloader.get().addEpisode(ep);
		}
		
		sn = DomainUtil.updateEpisode(sn, ep);
		s = DomainUtil.updateSeason(s, sn);
		
		Log.Debug(String.format("MovieIdentifier :: #3 Number of episodes :: %s", sn.getEpisodeCount()), LogType.FIND);
		
		DB.save(s);
		
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
		if (!mediaExists(movie, media)) {
			// Add media metadata
			if (Arguments.get().isMediaInfoEnabled())
				media = MediaMetadata.addMediaMetadata(media);

			// If movie exist but not the media then add the media
			DB.save(Movie.newBuilder(movie).addMedia(media).build());
		}
		
		// Check if movie has subs. If not then add it to the subtitle queue.
		if (!hasSubtitles(movie)) {
			SubtitleDownloader.get().addMovie(movie);
		}		
	}

	protected void checkExistingMedia(Episode e, Media media) {
		// Check if media exists

		if (!mediaExists(e, media)) {
			// Add media metadata
			if (Arguments.get().isMediaInfoEnabled())
				media = MediaMetadata.addMediaMetadata(media);

			// If movie exist but not the media then add the media
			DB.save(Episode.newBuilder(e).addMedia(media).build());
		}
		
		if (!hasSubtitles(e)) {
			SubtitleDownloader.get().addEpisode(e);
		}
	}

	private boolean mediaExists(Movie m, Media mediaToFind) {
		for (Media md : m.getMediaList()) {
			if (StringUtils.equalsIgnoreCase(md.getFilename(), mediaToFind.getFilename()) && StringUtils.equalsIgnoreCase(md.getFilepath(), mediaToFind.getFilepath()))
				return true;
		}
		
		return false;
	}



	private boolean mediaExists(Episode e, Media mediaToFind) {		
		if (e != null) {
			for (Media md : e.getMediaList()) {
				if (StringUtils.equalsIgnoreCase(md.getFilename(), mediaToFind.getFilename()) && StringUtils.equalsIgnoreCase(md.getFilepath(), mediaToFind.getFilepath()))
					return true;
			}
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

	private Series getImdbInformation(Series series, int season, int episode) {
		//find imdb link
		Series s = null;
		try {
			s = IMDBFinder.Get(series, season, episode);
			
			if (!StringUtils.isEmpty(s.getImdbUrl()))
				Log.Info(String.format("IMDB link found for :: %s", series.getTitle()), LogType.FIND);
		}
		catch (IOException | NumberFormatException | ParseException e) {
			Log.Error("Error occured when finding IMDB link", Log.LogType.FIND, e);
		}
		
		return s;
	}

	private boolean hasSubtitles(Movie m) {
		for(Media md : m.getMediaList()) {
			if (md.getSubsCount() > 0)
				return true;
		}
		
		return false;
	}
	
	private boolean hasSubtitles(Episode e) {
		if (e != null) {
			for(Media md : e.getMediaList()) {
				if (md.getSubsCount() > 0)
					return true;
			}
		}
		return false;
	}
}