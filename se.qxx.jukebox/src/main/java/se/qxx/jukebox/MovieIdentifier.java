package se.qxx.jukebox;

import java.io.IOException;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.tools.MediaMetadata;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.watcher.FileRepresentation;

public class MovieIdentifier extends JukeboxThread {
	private static MovieIdentifier _instance;
	private Queue<FileRepresentation> files;

	private MovieIdentifier() {
		super("MovieIdentifier", 0, LogType.FIND);
		this.files = new ConcurrentLinkedQueue<FileRepresentation>();
	}

	public static MovieIdentifier get() {
		if (_instance == null)
			_instance = new MovieIdentifier();

		return _instance;
	}

	@Override
	protected void initialize() {

	}

	@Override
	protected void execute() {
		while (!this.files.isEmpty()) {
			FileRepresentation f = this.files.poll();

			if (f != null)
				identify(f);

			if (!this.isRunning())
				break;
		}
	}

	public void addFile(FileRepresentation f) {
		Log.Debug(String.format("Adding file %s", f.getName()), LogType.FIND);
		
		if (!files.contains(f)) {
			this.files.add(f);
			signal();
		} else {
			Log.Debug("File already added", LogType.FIND);
		}
	}

	private void identify(FileRepresentation f) {
		Log.Debug(String.format("Identifying :: %s", f.getName()), Log.LogType.FIND);

		String filename = f.getName();
		String path = f.getPath();

		// Added ignore on all filename that contains the string sample
		if (!Util.isExcludedFile(f, LogType.FIND)) {
			// check if the same media already exist in db
			Media dbMedia = DB.getMediaByFilename(filename);
			if (dbMedia != null && StringUtils.equalsIgnoreCase(dbMedia.getFilepath(), path)) {
				Log.Info("Media already exist in DB. Continuing...", LogType.FIND);
				return;
			} else {
				MovieOrSeries mos = MovieBuilder.identify(path, filename);

				if (mos != null) {
					matchMovieWithDatabase(mos);
				} else {
					Log.Info(String.format("Failed to identity movie with filename :: %s", f.getName()),
							Log.LogType.FIND);
				}
			}

		}
	}

	/**
	 * Checks if the media present in a movie exists in database. If the movie does
	 * not exist then get media information and add it to the database If the movie
	 * exist then check existing media and add it if it does not exist.
	 * 
	 * @param movie
	 * @param filename
	 */
	protected void matchMovieWithDatabase(MovieOrSeries mos) {
		Log.Info(String.format("MovieIdentifier :: Object identified by %s as :: %s", mos.getIdentifier().toString(),
				mos.getTitle()), Log.LogType.FIND);

		// Check if movie exists in db
		// PartPattern pp = new PartPattern(filename); // ABOMINATION!

		// Careful here! As the identification of the other movie parts could be in a
		// different thread. Hence synchronized declaration.
		// Shouldn't be a problem no more as all identification is done on a single
		// thread
		Media newMedia = mos.getMedia();

		if (!mos.isSeries()) {
			matchMovie(mos, newMedia);
		} else {
			matchSeries(mos, newMedia);
		}
	}

	private void matchMovie(MovieOrSeries mos, Media newMedia) {
		// Movie dbMovie = DB.getMovieByStartOfMediaFilename(mos.getTitle());
		Movie dbMovie = DB.findMovie(mos.getTitle());

		if (dbMovie == null) {
			saveNewMovie(mos, newMedia);
		} else {
			Log.Debug("MovieIdentifier :: Movie found -- checking existing media", LogType.FIND);
			checkExistingMedia(dbMovie, newMedia);
		}
	}

	private void saveNewMovie(MovieOrSeries mos, Media newMedia) {
		
		Log.Debug("MovieIdentifier :: Movie not found -- adding new", LogType.FIND);
		
		Thread t = new Thread(() -> {
			Movie movie = getMovieInfo(mos.getMovie(), newMedia);
			movie = DB.save(movie);
			if (Arguments.get().isSubtitleDownloaderEnabled())
				SubtitleDownloader.get().addMovie(movie);			
		});
		t.start();
	}

	private void matchSeries(MovieOrSeries mos, Media newMedia) {
		Series series = mos.getSeries();
		
		// verify that we have season and episode info!
		int season = series.getSeason(0).getSeasonNumber();
		int episode = series.getSeason(0).getEpisode(0).getEpisodeNumber();
		
		if (season == 0 && episode == 0) {
			Log.Error("MovieIdentifier :: Series identified but season and episode info not found!", LogType.FIND);
		} else {
			Log.Debug(String.format("MovieIndentifier :: Finding series :: %s", series.getTitle()), LogType.FIND);
		}

		// find series that matches
		// do we not need to merge dbSeries and series??!!
		Series dbSeries = DB.findSeries(series.getTitle());

		// if no series found in DB. create new from the created series
		// - if no series then no seasons and no episodes
		if (dbSeries == null) {
			saveNewSeries(series, newMedia, season, episode);			
		} else {
			mergeExistingSeries(series, newMedia, season, episode, dbSeries);
		}
	}

	private void enlistToSubtitleDownloader(Series s, int season, int episode) {
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);
				
		if (Arguments.get().isSubtitleDownloaderEnabled()) 
			SubtitleDownloader.get().addEpisode(ep);
	}

	private void mergeExistingSeries(Series series, Media newMedia, int season, int episode, Series dbSeries) {
		Log.Debug("MovieIdentifier :: Series found. Searching for season..", LogType.FIND);
		// Log.Debug(String.format("MovieIdentifier :: dbSeries nr of episodes :: %s",
		// DomainUtil.findSeason(dbSeries, season).getEpisodeCount()), LogType.FIND);

		// verify if dbSeries have the episode.
		// if it does then exit
		if (checkSeries(dbSeries, season, episode)) {
			Log.Debug("MovieIdentifier :: Episode already exist in DB. Exiting ... ", LogType.FIND);
		} else {
			Series mergedSeries = mergeSeries(dbSeries, series, season, episode);

			forkSeriesUpdate(mergedSeries, newMedia, season, episode);
		}
	}

	private void forkSeriesUpdate(Series series, Media media, int season, int episode) {
		Thread t = new Thread(() -> {
			Series s = getSeriesInfo(series, season, episode, media);
			s = DB.save(s);
			enlistToSubtitleDownloader(s, season, episode);

		});
		t.start();
	}

	private void saveNewSeries(Series series, Media newMedia, int season, int episode) {
		// no series exist
		Log.Debug("MovieIdentifier :: No series found! Creating new", LogType.FIND);
		forkSeriesUpdate(series, newMedia, season, episode);
	}

	/**
	 * Returns if the series already contains information about the season and
	 * episode specified
	 * 
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
	 * Takes the season and episode (both at index 0) from the mergeFrom object and
	 * merges them into the mergeTo object. If they already exist nothing will be
	 * merged.
	 * 
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
	 * 
	 * @param m
	 * @param newMedia
	 */
	public Movie getMovieInfo(Movie movie, Media media) {
		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't
		// identify on IMDB.
		if (Arguments.get().isImdbIdentifierEnabled())
			movie = getImdbInformation(movie);

		// Get media information from MediaInfo library
		if (Arguments.get().isMediaInfoEnabled()) {
			Media md = MediaMetadata.addMediaMetadata(media);

			movie = Movie.newBuilder(movie).clearMedia().addMedia(md).build();
		}

		Log.Debug(String.format("Saving movie. Image length :: %s, Thumbnail length :: %s", movie.getImage().size(), movie.getThumbnail().size()), LogType.FIND);
		return movie;
	}

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * 
	 * @param m
	 * @param newMedia
	 */
	public Series getSeriesInfo(Series series, int season, int episode, Media media) {
		Series s = series;

		// construct the objects.
		// this should be done here and not in the IMDBFinder.
		// IMDBFinder should expect the objects in the hierarchy to be populated.
		validateSeriesStructure(s, season, episode);

		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't
		// identify on IMDB.
		if (Arguments.get().isImdbIdentifierEnabled())
			s = getImdbInformation(s, season, episode);

		// Get metadata and enlist to subtitle downloader
		s = getAdditionalInfo(media, s, season, episode);

		return s;
	}

	private Series getAdditionalInfo(Media media, Series s, int season, int episode) {
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);

		// Get media information from MediaInfo library
		if (Arguments.get().isMediaInfoEnabled()) {
			Media md = MediaMetadata.addMediaMetadata(media);

			ep = Episode.newBuilder(ep).clearMedia().addMedia(md).build();
		}

		sn = DomainUtil.updateEpisode(sn, ep);
		s = DomainUtil.updateSeason(s, sn);

		Log.Debug(String.format("MovieIdentifier :: #3 Number of episodes :: %s", sn.getEpisodeCount()), LogType.FIND);
		return s;
	}

	private void validateSeriesStructure(Series s, int season, int episode) {
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);

		if (sn == null || ep == null)
			throw new IllegalArgumentException("Season or Episode not found!");
	}

	/**
	 * Checks if a media exist in a movie. If it does then add the movie to the
	 * SubtitleDownloader if no subtitles exist. Otherwise add metadata information
	 * to the media and add it to database
	 * 
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
			if (StringUtils.equalsIgnoreCase(md.getFilename(), mediaToFind.getFilename())
					&& StringUtils.equalsIgnoreCase(md.getFilepath(), mediaToFind.getFilepath()))
				return true;
		}

		return false;
	}

	private boolean mediaExists(Episode e, Media mediaToFind) {
		if (e != null) {
			for (Media md : e.getMediaList()) {
				if (StringUtils.equalsIgnoreCase(md.getFilename(), mediaToFind.getFilename())
						&& StringUtils.equalsIgnoreCase(md.getFilepath(), mediaToFind.getFilepath()))
					return true;
			}
		}

		return false;
	}

	private Movie getImdbInformation(Movie m) {
		// find imdb link
		try {
			m = IMDBFinder.Get(m);

			if (!StringUtils.isEmpty(m.getImdbUrl()))
				Log.Info(String.format("IMDB link found for :: %s", m.getTitle()), LogType.FIND);
		} catch (IOException | NumberFormatException | ParseException e) {
			Log.Error("Error occured when finding IMDB link", Log.LogType.FIND, e);
		}

		return m;
	}

	private Series getImdbInformation(Series series, int season, int episode) {
		// find imdb link
		Series s = null;
		try {
			s = IMDBFinder.Get(series, season, episode);

			if (!StringUtils.isEmpty(s.getImdbUrl()))
				Log.Info(String.format("IMDB link found for :: %s", series.getTitle()), LogType.FIND);
		} catch (IOException | NumberFormatException | ParseException e) {
			Log.Error("Error occured when finding IMDB link", Log.LogType.FIND, e);
		}

		return s;
	}

	private boolean hasSubtitles(Movie m) {
		for (Media md : m.getMediaList()) {
			if (md.getSubsCount() > 0)
				return true;
		}

		return false;
	}

	private boolean hasSubtitles(Episode e) {
		if (e != null) {
			for (Media md : e.getMediaList()) {
				if (md.getSubsCount() > 0)
					return true;
			}
		}
		return false;
	}

	@Override
	public int getJukeboxPriority() {
		return Thread.NORM_PRIORITY;
	}
}