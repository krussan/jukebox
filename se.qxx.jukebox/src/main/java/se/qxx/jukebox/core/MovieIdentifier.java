package se.qxx.jukebox.core;

import java.io.IOException;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.concurrent.StringLockPool;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.watcher.FileRepresentation;

@Singleton
public class MovieIdentifier extends JukeboxThread implements IMovieIdentifier {
	
	private Queue<FileRepresentation> files;
	private StringLockPool seriesLocks;
	private IDatabase database;
	private IArguments arguments;
	private ISubtitleDownloader subtitleDownloader;
	private IIMDBFinder imdbFinder;
	private IMovieBuilderFactory movieBuilderFactory;
	private IMediaMetadataHelper mediaHelper;
	private IFilenameChecker filenameChecker;

	@Inject
	private MovieIdentifier(IExecutor executor, 
			IDatabase database, 
			IArguments arguments,
			ISubtitleDownloader subtitleDownloader,
			IIMDBFinder imdbFinder,
			IMovieBuilderFactory movieBuilderFactory,
			LoggerFactory loggerFactory,
			IMediaMetadataHelper mediaHelper,
			IFilenameChecker filenameChecker) {
		super("MovieIdentifier", 0, loggerFactory.create(LogType.FIND), executor);
		this.setFilenameChecker(filenameChecker);
		this.setMediaHelper(mediaHelper);
		
		this.setDatabase(database);
		this.setArguments(arguments);
		this.setSubtitleDownloader(subtitleDownloader);
		this.setImdbFinder(imdbFinder);
		this.setMovieBuilderFactory(movieBuilderFactory);
		
		this.files = new ConcurrentLinkedQueue<FileRepresentation>();
		this.seriesLocks = new StringLockPool();
	}


	public IFilenameChecker getFilenameChecker() {
		return filenameChecker;
	}


	public void setFilenameChecker(IFilenameChecker filenameChecker) {
		this.filenameChecker = filenameChecker;
	}


	public IMediaMetadataHelper getMediaHelper() {
		return mediaHelper;
	}


	public void setMediaHelper(IMediaMetadataHelper mediaHelper) {
		this.mediaHelper = mediaHelper;
	}


	public IMovieBuilderFactory getMovieBuilderFactory() {
		return movieBuilderFactory;
	}


	public void setMovieBuilderFactory(IMovieBuilderFactory movieBuilderFactory) {
		this.movieBuilderFactory = movieBuilderFactory;
	}


	public IIMDBFinder getImdbFinder() {
		return imdbFinder;
	}


	public void setImdbFinder(IIMDBFinder imdbFinder) {
		this.imdbFinder = imdbFinder;
	}


	public ISubtitleDownloader getSubtitleDownloader() {
		return subtitleDownloader;
	}


	public void setSubtitleDownloader(ISubtitleDownloader subtitleDownloader) {
		this.subtitleDownloader = subtitleDownloader;
	}


	public IArguments getArguments() {
		return arguments;
	}


	public void setArguments(IArguments arguments) {
		this.arguments = arguments;
	}


	public IDatabase getDatabase() {
		return database;
	}


	public void setDatabase(IDatabase database) {
		this.database = database;
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

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IMovieIdentifier#addFile(se.qxx.jukebox.watcher.FileRepresentation)
	 */
	@Override
	public void addFile(FileRepresentation f) {
		this.getLog().Debug(String.format("Adding file %s", f.getName()));
		
		if (!files.contains(f)) {
			this.files.add(f);
			signal();
		} else {
			this.getLog().Debug("File already added");
		}
	}

	private void identify(FileRepresentation f) {
		this.getLog().Debug(String.format("Identifying :: %s", f.getName()));

		String filename = f.getName();
		String path = f.getPath();

		// Added ignore on all filename that contains the string sample
		if (!this.getFilenameChecker().isExcludedFile(f)) {
			// check if the same media already exist in db
			Media dbMedia = this.getDatabase().getMediaByFilename(filename);
			if (dbMedia != null && StringUtils.equalsIgnoreCase(dbMedia.getFilepath(), path)) {
				this.getLog().Info("Media already exist in DB. Continuing...");
				return;
			} else {
				MovieOrSeries mos = this.getMovieBuilderFactory()
						.identify(path, filename);

				if (mos != null) {
					matchMovieWithDatabase(mos);
				} else {
					this.getLog().Info(String.format("Failed to identity movie with filename :: %s", f.getName()));
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
		this.getLog().Info(String.format("MovieIdentifier :: Object identified by %s as :: %s", mos.getIdentifier().toString(),
				mos.getTitle()));

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
		// Movie dbMovie = this.getDatabase().getMovieByStartOfMediaFilename(mos.getTitle());
		Movie dbMovie = this.getDatabase().findMovie(mos.getTitle());

		if (dbMovie == null) {
			saveNewMovie(mos, newMedia);
		} else {
			this.getLog().Debug("MovieIdentifier :: Movie found -- checking existing media");
			checkExistingMedia(dbMovie, newMedia);
		}
	}

	private void saveNewMovie(MovieOrSeries mos, Media newMedia) {
		
		this.getLog().Debug("MovieIdentifier :: Movie not found -- adding new");
		
		Thread t = new Thread(() -> {
			Movie movie = getMovieInfo(mos.getMovie(), newMedia);
			movie = this.getDatabase().save(movie);
			if (this.getArguments().isSubtitleDownloaderEnabled())
				this.getSubtitleDownloader().addMovie(movie);			
		});
		t.start();
	}

	private void matchSeries(MovieOrSeries mos, Media newMedia) {
		Series series = mos.getSeries();
		
		// verify that we have season and episode info!
		int season = series.getSeason(0).getSeasonNumber();
		int episode = series.getSeason(0).getEpisode(0).getEpisodeNumber();
		
		if (season == 0 && episode == 0) {
			this.getLog().Error("MovieIdentifier :: Series identified but season and episode info not found!");
		} else {
			this.getLog().Debug(String.format("MovieIndentifier :: Finding series :: %s", series.getTitle()));
		}

		// find series that matches
		// TODO: this goes wrong if there is an ongoing parallel identification process
		// which has not been saved yet.
		
		forkWaitForOtherSeriesObjects(newMedia, series, season, episode);
	}

	private void forkWaitForOtherSeriesObjects(Media newMedia, Series series, int season, int episode) {
		Thread t = new Thread(() -> {
			
			try {
				// wait if there is a lock on the series title
				this.getSeriesLocks().lock(series.getTitle());
				
				Series dbSeries = this.getDatabase().findSeries(series.getTitle());
		
				// if no series found in this.getDatabase(). create new from the created series
				// - if no series then no seasons and no episodes
				if (dbSeries == null) {
					//check identification process
					saveNewSeries(series, newMedia, season, episode);			
				} else {
					mergeExistingSeries(series, newMedia, season, episode, dbSeries);
				}				
			}
			finally {
				this.getSeriesLocks().unlock(series.getTitle());
			}
		});
		t.start();
	}

	private void enlistToSubtitleDownloader(Series s, int season, int episode) {
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);
				
		if (this.getArguments().isSubtitleDownloaderEnabled()) 
			this.getSubtitleDownloader().addEpisode(ep);
	}

	private void mergeExistingSeries(Series series, Media newMedia, int season, int episode, Series dbSeries) {
		this.getLog().Debug("MovieIdentifier :: Series found. Searching for season..");
		// this.getLog().Debug(String.format("MovieIdentifier :: dbSeries nr of episodes :: %s",
		// DomainUtil.findSeason(dbSeries, season).getEpisodeCount()));

		// verify if dbSeries have the episode.
		// if it does then exit
		if (checkSeries(dbSeries, season, episode)) {
			this.getLog().Debug("MovieIdentifier :: Episode already exist in this.getDatabase(). Exiting ... ");
		} else {
			Series mergedSeries = mergeSeries(dbSeries, series, season, episode);

			updateSeries(mergedSeries, newMedia, season, episode);
		}
	}

	private void updateSeries(Series series, Media media, int season, int episode) {
		Series s = getSeriesInfo(series, season, episode, media);
		s = this.getDatabase().save(s);
		enlistToSubtitleDownloader(s, season, episode);
	}

	private void saveNewSeries(Series series, Media newMedia, int season, int episode) {
		// no series exist
		this.getLog().Debug("MovieIdentifier :: No series found! Creating new");
		updateSeries(series, newMedia, season, episode);
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

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IMovieIdentifier#getMovieInfo(se.qxx.jukebox.domain.JukeboxDomain.Movie, se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public Movie getMovieInfo(Movie movie, Media media) {
		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't
		// identify on IMthis.getDatabase().
		if (this.getArguments().isImdbIdentifierEnabled())
			movie = getImdbInformation(movie);

		// Get media information from MediaInfo library
		if (this.getArguments().isMediaInfoEnabled()) {
			Media md = this.getMediaHelper().addMediaMetadata(media);

			movie = Movie.newBuilder(movie).clearMedia().addMedia(md).build();
		}

		this.getLog().Debug(String.format("Saving movie. Image length :: %s, Thumbnail length :: %s", movie.getImage().size(), movie.getThumbnail().size()));
		return movie;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IMovieIdentifier#getSeriesInfo(se.qxx.jukebox.domain.JukeboxDomain.Series, int, int, se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public Series getSeriesInfo(Series series, int season, int episode, Media media) {
		Series s = series;

		// construct the objects.
		// this should be done here and not in the IMDBFinder.
		// IMDBFinder should expect the objects in the hierarchy to be populated.
		validateSeriesStructure(s, season, episode);

		// If not get information and subtitles
		// If title is the same as the filename (except ignore pattern) then don't
		// identify on IMthis.getDatabase().
		if (this.getArguments().isImdbIdentifierEnabled())
			s = getImdbInformation(s, season, episode);

		// Get metadata and enlist to subtitle downloader
		s = getAdditionalInfo(media, s, season, episode);

		return s;
	}

	private Series getAdditionalInfo(Media media, Series s, int season, int episode) {
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);

		// Get media information from MediaInfo library
		if (this.getArguments().isMediaInfoEnabled()) {
			Media md = this.getMediaHelper().addMediaMetadata(media);

			ep = Episode.newBuilder(ep).clearMedia().addMedia(md).build();
		}

		sn = DomainUtil.updateEpisode(sn, ep);
		s = DomainUtil.updateSeason(s, sn);

		this.getLog().Debug(String.format("MovieIdentifier :: #3 Number of episodes :: %s", sn.getEpisodeCount()));
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
			if (this.getArguments().isMediaInfoEnabled())
				media = this.getMediaHelper().addMediaMetadata(media);

			// If movie exist but not the media then add the media
			this.getDatabase().save(Movie.newBuilder(movie).addMedia(media).build());
		}

		// Check if movie has subs. If not then add it to the subtitle queue.
		if (!hasSubtitles(movie)) {
			this.getSubtitleDownloader().addMovie(movie);
		}
	}

	protected void checkExistingMedia(Episode e, Media media) {
		// Check if media exists

		if (!mediaExists(e, media)) {
			// Add media metadata
			if (this.getArguments().isMediaInfoEnabled())
				media = this.getMediaHelper().addMediaMetadata(media);

			// If movie exist but not the media then add the media
			this.getDatabase().save(Episode.newBuilder(e).addMedia(media).build());
		}

		if (!hasSubtitles(e)) {
			this.getSubtitleDownloader().addEpisode(e);
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
			m = this.getImdbFinder().Get(m);

			if (!StringUtils.isEmpty(m.getImdbUrl()))
				this.getLog().Info(String.format("IMDB link found for :: %s", m.getTitle()));
		} catch (IOException | NumberFormatException | ParseException e) {
			this.getLog().Error("Error occured when finding IMDB link", e);
		}

		return m;
	}

	private Series getImdbInformation(Series series, int season, int episode) {
		// find imdb link
		Series s = null;
		try {
			s = this.getImdbFinder().Get(series, season, episode);

			if (!StringUtils.isEmpty(s.getImdbUrl()))
				this.getLog().Info(String.format("IMDB link found for :: %s", series.getTitle()));
		} catch (IOException | NumberFormatException | ParseException e) {
			this.getLog().Error("Error occured when finding IMDB link", e);
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

	public StringLockPool getSeriesLocks() {
		return this.seriesLocks;
	}


	@Override
	public Runnable getRunnable() {
		return this;
	}

}