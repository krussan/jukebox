package se.qxx.jukebox.interfaces;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public interface IDatabase {

	List<Movie> searchMoviesByTitle(String searchString, boolean excludeImages, boolean excludeTextData);

	List<Movie> searchMoviesByTitle(String searchString, int numberOfResults, int offset, boolean excludeImages, boolean excludeTextData);

	Movie searchMoviesByID(int id, boolean excludeImages, boolean excludeTextData);

	List<Series> searchSeriesByTitle(String searchString, boolean excludeImages);

	List<Series> searchSeriesByTitle(String searchString, int numberOfResults, int offset, boolean excludeImages);

	Movie findMovie(String identifiedTitle);

	Series findSeries(String identifiedTitle);

	/***
	 * Uses search method to find a specific Series by ID.
	 * Includes episodes but exludes images on series and season
	 * 
	 * @param id
	 * @return
	 */
	Series searchSeriesById(int id, boolean excludeImages);

	Season searchSeasonById(int id, boolean excludeImages, boolean excludeTextData);

	Episode searchEpisodeById(int id, boolean excludeImages, boolean excludeTextData);

	Movie getMovie(int id);

	Series getSeries(int id);

	Season getSeason(int id);

	Episode getEpisode(int id);

	Movie save(Movie m);

	Media save(Media md);

	Episode save(Episode episode);

	Series save(Series series);

	void saveAsync(Series series);

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Delete
	//---------------------------------------------------------------------------------------
	void delete(Movie m) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException;

	void delete(Series s) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException;

	void delete(Season sn) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException;

	void delete(Episode ep) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException;

	void toggleWatched(Movie m);

	List<Media> getConverterQueue();

	void setDownloadCompleted(int id);
	void setDownloadInProgress(int id);
	
	void cleanupConverterQueue();

	Movie addMovieToSubtitleQueue(Movie m);

	Episode addEpisodeToSubtitleQueue(Episode e);

	long getCurrentUnixTimestamp();

	/***
	 * Retrieves the subtitle queue as a list of MovieOrSeries objects
	 * Due to the nature of the MovieOrSeries class all episodes needs
	 * to be represented by a single series and a single season object
	 * 
	 * @return
	 */
	List<MovieOrSeries> getSubtitleQueue();

	ProtoDB getProtoDBInstance() throws DatabaseNotSupportedException;

	ProtoDB getProtoDBInstance(boolean populateBlobs) throws DatabaseNotSupportedException;

	ProtoDB getProtoDBInstance(String driver, String connectionString, boolean populateBlobs)
			throws DatabaseNotSupportedException;

	void addToBlacklist(Movie m);

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Version
	//---------------------------------------------------------------------------------------
	Version getVersion() throws ClassNotFoundException;

	void setVersion(Version ver) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException;

	String backup() throws IOException, DatabaseNotSupportedException;

	void restore(String backupFilename) throws IOException;

	boolean executeUpgradeStatements(String[] statements);

	boolean purgeDatabase();

	boolean setupDatabase()
			throws ClassNotFoundException, SQLException, IDFieldNotFoundException, DatabaseNotSupportedException;

	Movie getMovieByMediaID(int mediaID);

	Media getMediaByFilename(String filename, boolean excludeSubs);
	Media getMediaByStartOfFilename(String startOfFilename);

	Media getMediaById(int mediaId);

	Movie getMovieBySubfilename(String subsFilename) throws DatabaseNotSupportedException;

	void purgeSeries();

	/***
	 * This purges the subtitle queue from all items that are not present in
	 * the Episode and the Movie objects any more
	 */
	void cleanSubtitleQueue();

	int getTotalNrOfMovies();

	int getTotalNrOfSeries();

	int getTotalNrOfSeasons(int seriesID);

	int getTotalNrOfEpisodes(int seasonID);

	void saveConversion(int id, String newFilename, int value);

	void saveConversion(int id, int completedValue);

	Series getSeriesByEpisode(int id);

	Season save(Season season);

	void forceConversion(int mediaID);

	List<MovieOrSeries> decoupleSeries(List<Series> series);
}