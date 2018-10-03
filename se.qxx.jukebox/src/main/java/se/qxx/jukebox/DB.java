package se.qxx.jukebox;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.protodb.DBType;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;
import se.qxx.protodb.ProtoDBSort;
import se.qxx.protodb.SearchOptions;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;
import se.qxx.protodb.exceptions.ProtoDBParserException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;
import se.qxx.protodb.exceptions.SearchOptionsNotInitializedException;
import se.qxx.protodb.model.ProtoDBSearchOperator;

import com.google.protobuf.Message;
public class DB {

	private static Object syncObject = new Object();
	
	private DB() {
		
	} 

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Search
	//---------------------------------------------------------------------------------------

	public static List<Movie> searchMoviesByTitle(String searchString) {
		return searchMoviesByTitle(searchString, -1, -1);
	}
	
	public static List<Movie> searchMoviesByTitle(String searchString, int numberOfResults, int offset) {
		List<String> excludedObjects = new ArrayList<String>();
		excludedObjects.add("media.subs");
		excludedObjects.add("image");
		
		return searchByTitle(JukeboxDomain.Movie.getDefaultInstance()
				, "title"
				, searchString
				, numberOfResults
				, offset
				, excludedObjects
				, "identifiedTitle");
	}
	
	public static Movie searchMoviesByID(int id) {
		try {
			List<String> excludedObjects = new ArrayList<String>();
			excludedObjects.add("media.subs");
			excludedObjects.add("image");
	
			ProtoDB db = getProtoDBInstance(true);
	
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Movie> result = db.search(
					SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
						.addFieldName("ID")
						.addSearchArgument(id)
						.addOperator(ProtoDBSearchOperator.Equals)
						.setShallow(false)
						.addAllExcludedObjects(excludedObjects));				

				if (result.size() > 0)
					return result.get(0);
				else
					return null;
				
			}
		
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve series listing from DB", Log.LogType.MAIN, e);
			return null;
		}		

	}
	

	public static List<Series> searchSeriesByTitle(String searchString) {
		return searchSeriesByTitle(searchString, -1, -1);		
	}

	public static List<Series> searchSeriesByTitle(String searchString, int numberOfResults, int offset) {
		List<String> excludedObjects = new ArrayList<String>();
		excludedObjects.add("season"); // exclude underlying seasons
		excludedObjects.add("image"); // exclude full size image
		
		return searchByTitle(JukeboxDomain.Series.getDefaultInstance()
				, "title"
				, searchString
				, numberOfResults
				, offset
				, excludedObjects	
				, "title");
	}
	
	private static <T extends Message> List<T> searchByTitle(
			T instance, 
			String searchField, 
			String searchString, 
			int numberOfResults,
			int offset,
			List<String> excludedObjects,
			String sortField) {
		try {
			ProtoDB db = getProtoDBInstance(true);

			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
			
				
				if (StringUtils.isEmpty(searchString))
					searchField = "";
				else
					searchString = "%" + searchString + "%"; 
				
				return db.search(
					SearchOptions.newBuilder(instance)
						.addFieldName(searchField)
						.addSearchArgument(searchString)
						.addOperator(ProtoDBSearchOperator.Like)
						.setShallow(false)
						.setOffset(offset)
						.setNumberOfResults(numberOfResults)
						.setSortField(sortField)
						.setSortOrder(ProtoDBSort.Asc)
						.addAllExcludedObjects(excludedObjects));				

			}
			
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve series listing from DB", Log.LogType.MAIN, e);
			return new ArrayList<T>();
		}		
	}


	private static String replaceSearchString(String searchString) {
		String ret = searchString;
		ret = StringUtils.replace(ret, "%", "\\%");
		ret = StringUtils.replace(ret, "_", "\\_");
		return StringUtils.trim(ret);
	}
	
	public static Movie findMovie(String identifiedTitle) {
		String searchString = replaceSearchString(identifiedTitle);
		
		Log.Debug(String.format("DB :: Series search string :: %s", searchString), LogType.FIND);
		 
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Movie> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
								.addFieldName("identifiedTitle") 
								.addSearchArgument(searchString)
								.addOperator(ProtoDBSearchOperator.Like));
					
				if (result.size() > 0)
					return result.get(0);
				else 
					return null;				
			}
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.FIND, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}
	
	public static Series findSeries(String identifiedTitle) {
		String searchString = replaceSearchString(identifiedTitle) + "%";
		
		Log.Debug(String.format("DB :: Series search string :: %s", searchString), LogType.MAIN);
		 
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Series> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
								.addFieldName("identifiedTitle") 
								.addSearchArgument(searchString)
								.addOperator(ProtoDBSearchOperator.Like));

				if (result.size() > 0)
					return result.get(0);
				else 
					return null;
				
			}
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}
	
	/***
	 * Uses search method to find a specific Series by ID.
	 * Includes episodes but exludes images on series and season
	 * 
	 * @param id
	 * @return
	 */
	public static Series searchSeriesById(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Series> result = 
					db.search(SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance()) 
						.addFieldName("ID") 
						.addSearchArgument(id) 
						.addOperator(ProtoDBSearchOperator.Equals)
						.addExcludedObject("season.episode")
						.addExcludedObject("image")
						.addExcludedObject("season.image"));
					

				if (result.size() > 0)
					return result.get(0);
				else
					return null;
			}
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public static Season searchSeasonById(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Season> result = 
					db.search(SearchOptions.newBuilder(JukeboxDomain.Season.getDefaultInstance()) 
						.addFieldName("ID") 
						.addSearchArgument(id) 
						.addOperator(ProtoDBSearchOperator.Equals)
						.addExcludedObject("image")
						.addExcludedObject("episode.image")
						.addExcludedObject("episode.media.subs"));
				
					

				if (result.size() > 0)
					return result.get(0);
				else
					return null;
			}
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Get
	//---------------------------------------------------------------------------------------

	public static Movie getMovie(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.get(
					id, 
					Movie.getDefaultInstance());
			}
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			
			
		}
		
		return null;
	}

	


	public static Series getSeries(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Series> result = 
					db.search(SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance()) 
						.addFieldName("ID") 
						.addSearchArgument(id) 
						.addOperator(ProtoDBSearchOperator.Equals));

				if (result.size() > 0)
					return result.get(0);
				else
					return null;
			}
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public static Season getSeason(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Season> result =
						db.search(SearchOptions.newBuilder(JukeboxDomain.Season.getDefaultInstance()) 
								.addFieldName("ID") 
								.addSearchArgument(id) 
								.addOperator(ProtoDBSearchOperator.Equals));

				if (result.size() > 0)
					return result.get(0);
				else
					return null;
			}
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public static Episode getEpisode(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.get(id, Episode.getDefaultInstance());
			}
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Save
	//---------------------------------------------------------------------------------------

	public static Movie save(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.save(m);
			}
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}

	public static Media save(Media md) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.save(md);
			}
		}
		catch (Exception e) {
			Log.Error("Failed to store media to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}
	
	public static Episode save(Episode episode) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.save(episode);
			}
		}
		catch (Exception e) {
			Log.Error("Failed to store episode to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}	

	public static Series save(Series series) {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.save(series);
			}
		}
		catch (Exception e) {
			Log.Error("Failed to store episode to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}	

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Delete
	//---------------------------------------------------------------------------------------
	public static void delete(Movie m) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				db.delete(m);
			}
		}
		catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException e) {
			Log.Error("Failed to delete movie in DB", Log.LogType.MAIN, e);
			
			throw e;
		}		
	}
	
	public static void delete(Series s) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException  {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				db.delete(s);
			}
		}
		catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException e) {
			Log.Error("Failed to delete series in DB", Log.LogType.MAIN, e);
			
			throw e;
		}			
	}

	public static void delete(Season sn) throws ClassNotFoundException, SQLException,  DatabaseNotSupportedException  {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				db.delete(sn);
			}
		}
		catch (ClassNotFoundException | SQLException |  DatabaseNotSupportedException  e) {
			Log.Error("Failed to delete season in DB", Log.LogType.MAIN, e);
			
			throw e;
		}			
	}

	public static void delete(Episode ep) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException  {
		try {
			ProtoDB db = getProtoDBInstance();
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				db.delete(ep);
			}
		}
		catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException  e) {
			Log.Error("Failed to delete episode in DB", Log.LogType.MAIN, e);
			
			throw e;
		}				
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Watched
	//---------------------------------------------------------------------------------------
	
	public static void toggleWatched(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				db.save(Movie.newBuilder(m).setWatched(!m.getWatched()).build());
			}

		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
		}

	}		

	public static List<Media> getConverterQueue() {
		List<Media> result = new ArrayList<Media>();
       try {
           ProtoDB db = getProtoDBInstance();
               
           synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
                               
               // Restrict result to 5.
    	   	   // should only have the queue state Queued if download completed
               result = db.search(
        		   SearchOptions.newBuilder(JukeboxDomain.Media.getDefaultInstance())
        		   	.addFieldName("converterState")
        		   	.addOperator(ProtoDBSearchOperator.Equals)
        		   	.addSearchArgument("Queued")
        		   	.setNumberOfResults(5)
        		   	.setOffset(0));
     
           }
       }
       catch (Exception e) {
           Log.Error("Failed to retrieve movie listing from DB", LogType.MAIN, e);
       }
       
       return result;
	
	}
	
	public static void setDownloadCompleted(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = String.format("UPDATE Media SET downloadcomplete = 1 WHERE ID = %s", id);
				db.executeNonQuery(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to set download complete"), Log.LogType.MAIN, e);
		}				
	}

	public  static void cleanupConverterQueue() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = "UPDATE Media SET _converterstate_ID = 2 WHERE _converterstate_ID IN (4,5)";
				db.executeNonQuery(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to clean subtitle queue"), Log.LogType.MAIN, e);
		}		
	}



	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Subtitles
	//---------------------------------------------------------------------------------------

	public static Movie addMovieToSubtitleQueue(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			int id = m.hasSubtitleQueue() ? m.getSubtitleQueue().getID() : -1;
			
			m = Movie.newBuilder(m).setSubtitleQueue(
				SubtitleQueue.newBuilder()
					.setID(id)
					.setSubtitleRetreiveResult(0)
					.setSubtitleQueuedAt(getCurrentUnixTimestamp())
					.build())
				.build();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.save(m);
			}
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
		}
		return m;
	}
	
	public static Episode addEpisodeToSubtitleQueue(Episode e) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			e = Episode.newBuilder(e).setSubtitleQueue(
					SubtitleQueue.newBuilder()
						.setID(-1)
						.setSubtitleRetreiveResult(0)				
						.setSubtitleQueuedAt(DB.getCurrentUnixTimestamp())
						.build())
					.build();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.save(e);
			}
		}
		catch (Exception ex) {
			Log.Error("Failed to store epsiode to DB", Log.LogType.MAIN, ex);
		}
		return e;
	}
	
	public static long getCurrentUnixTimestamp() {
		return System.currentTimeMillis() / 1000L;
	}

	/***
	 * Retrieves the subtitle queue as a list of MovieOrSeries objects
	 * Due to the nature of the MovieOrSeries class all episodes needs
	 * to be represented by a single series and a single season object
	 * 
	 * @return
	 */
	public static List<MovieOrSeries> getSubtitleQueue() {
		List<MovieOrSeries> result = new ArrayList<MovieOrSeries>();
		List<Movie> movies = new ArrayList<Movie>();
		List<Series> series = new ArrayList<Series>();
		
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
					
				// Restrict result to 5. Since the list will be retrieved again it does not matter.
				movies =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
						.addFieldName("subtitleQueue.subtitleRetreiveResult")
						.addSearchArgument(0)
						.addOperator(ProtoDBSearchOperator.Equals)
						.setNumberOfResults(5)
						.addExcludedObject("media.subs")
						.setOffset(0));
	
				// this is a bit dangerous.
				// what if we cut a series/season in half and save the series (!)
				// So to be sure we save _only_ the episode from the SubtitleDownloader.
				series =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
						.addFieldName("season.episode.subtitleQueue.subtitleRetreiveResult")
						.addSearchArgument(0)
						.addOperator(ProtoDBSearchOperator.Equals)
						.setNumberOfResults(5)
						.addExcludedObject("season.episode.media.subs")
						.setOffset(0));
	
			}
			
			result = constructSubtitleQueue(movies, series);
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
		}
		
		return result;
	}

	public static ProtoDB getProtoDBInstance() throws DatabaseNotSupportedException {
		return getProtoDBInstance(true);
	}
	
	public static ProtoDB getProtoDBInstance(boolean populateBlobs) throws DatabaseNotSupportedException {
		String driver = Settings.get().getDatabase().getDriver();
		String connectionString = Settings.get().getDatabase().getConnectionString();

		return getProtoDBInstance(driver, connectionString, populateBlobs);
	}
	
	public static ProtoDB getProtoDBInstance(String driver, String connectionString, boolean populateBlobs) throws DatabaseNotSupportedException {
		ProtoDB db = null; 
		String logFilename = Log.getLoggerFilename(LogType.DB);
	
		if (StringUtils.isEmpty(logFilename))
			db = ProtoDBFactory.getInstance(driver, connectionString);
		else
			db = ProtoDBFactory.getInstance(driver, connectionString, logFilename);
		
		db.setPopulateBlobs(populateBlobs);
		return db;
	}

	private static List<MovieOrSeries> constructSubtitleQueue(List<Movie> movies, List<Series> series) {
		List<MovieOrSeries> moss = new ArrayList<MovieOrSeries>();
		
		for (Movie m : movies) {
			MovieOrSeries mos = new MovieOrSeries(m);
			moss.add(mos);
		}
		
		// we need to create a single Series object for every episode
		// since the MovieOrSeries object is mainly used for identifying
		moss.addAll(decoupleSeries(series));
		
		return moss;
	}

	private static List<MovieOrSeries> decoupleSeries(List<Series> series) {
		List<MovieOrSeries> moss = new ArrayList<MovieOrSeries>();
		
		for (Series s : series) {
			for (Season ss : s.getSeasonList()) {
				for (Episode e : ss.getEpisodeList()) {
					Series s2 = Series.newBuilder(s)
							.clearSeason()
							
							.addSeason(Season.newBuilder(ss)
								.clearEpisode()
								.addEpisode(e)
								.build())
					
							.build();
					
					moss.add(new MovieOrSeries(s2));
				}
			}
		}
		
		return moss;
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Blacklist
	//---------------------------------------------------------------------------------------

	public static void addToBlacklist(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			m = Movie.newBuilder(m).addBlacklist(m.getImdbId()).build();			
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				db.save(m);
			}
		}
		catch (Exception e) {
			Log.Error("Failed to add movie to blacklist in DB", Log.LogType.MAIN, e);
		}		
	}

	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Version
	//---------------------------------------------------------------------------------------
	public static Version getVersion() throws ClassNotFoundException {
		Connection conn = null;
		int minor = 0;
		int major = 0;
		
		try {
			ProtoDB db = getProtoDBInstance();
			conn = db.getConnection();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				PreparedStatement prep = conn.prepareStatement("SELECT minor, major FROM Version WHERE ID=1");
				ResultSet rs = prep.executeQuery();
				if (rs.next()) {
					minor = rs.getInt("minor");
					major = rs.getInt("major");
				}
			}
		} catch (Exception e) {
			minor = 0;
			major = 0;
		}
		
		finally {
			DB.disconnect(conn);
		}
		
		//default for pre-protodb is 0.10
		if (minor == 0 && major == 0)
			minor = 10;
		
		return new Version(major, minor);
	}
	
	public  static void setVersion(Version ver) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		Connection conn = null;
		try {
			ProtoDB db = getProtoDBInstance();
			conn = db.getConnection();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				PreparedStatement prep = conn.prepareStatement("UPDATE Version SET major = ?, minor= ? WHERE ID = 1");
				prep.setInt(1, ver.getMajor());
				prep.setInt(2, ver.getMinor());
				
				prep.execute();
			}
		}
		finally {
			DB.disconnect(conn);
		}
	}
	
	private static void insertVersion(Version ver) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		Connection conn = null;
		try {
			ProtoDB db = getProtoDBInstance();
			conn = db.getConnection();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				PreparedStatement prep = conn.prepareStatement("INSERT INTO Version (major, minor) VALUES (?, ?)");
				prep.setInt(1, ver.getMajor());
				prep.setInt(2, ver.getMinor());
				
				prep.execute();
			}
		}
		finally {
			DB.disconnect(conn);
		}
	}
	
	public static String backup() throws IOException, DatabaseNotSupportedException  {
		String backupFilename = StringUtils.EMPTY;
		
		ProtoDB db = getProtoDBInstance();
		if (db.getDBType() == DBType.Sqlite) {
			String[] splitted = StringUtils.split(db.getDatabaseBackend().getConnectionString(), ':');
			ArrayUtils.reverse(splitted);
			String databaseFilename = splitted[0];
			
			backupFilename = String.format("%s.bak", databaseFilename);
			File backup = new File(backupFilename);
			File current = new File(databaseFilename);
			
			int i = 1;
			while (backup.exists()) {
				backupFilename = String.format("%s.bak.%s", databaseFilename, i);
				backup = new File(backupFilename);
				i++;
			}
			
			System.out.println(String.format("Making backup to :: %s", backupFilename));
			FileUtils.copyFile(current, backup);
		}
		else {
			System.out.println("Database is not Sqlite. Backup not available. Send a prayer to the data gods.");
		}
		return backupFilename;
	}

	public static void restore(String backupFilename) throws IOException {
		File backup = new File(backupFilename);
		File restoreFile = new File("jukebox_proto.db");
		
		if (restoreFile.exists())
			restoreFile.delete();
		
		FileUtils.copyFile(backup, restoreFile);
	}
	

	public synchronized static boolean executeUpgradeStatements(String[] statements) {
		Connection conn = null;
		String sql = StringUtils.EMPTY;
		try {
			conn = DB.initialize();
			conn.setAutoCommit(false);

			int nrOfScripts = statements.length;
			for (int i=0; i<statements.length;i++) {
				sql = statements[i];
				System.out.println(String.format("Running script\t\t[%s/%s]", i + 1, nrOfScripts));

				PreparedStatement prep = conn.prepareStatement(sql);			
				prep.execute();
			}

			conn.commit();
			
			return true;
		}
				
		catch (Exception e) {
			Log.Error("Upgrade failed", LogType.UPGRADE, e);
			Log.Debug("Failing query was::", LogType.UPGRADE);
			Log.Debug(sql, LogType.UPGRADE);
			
			try {
				conn.rollback();
			} catch (SQLException sqlEx) {}
			
			return false;
		}
		finally {
			DB.disconnect(conn);
		}
	}
	
	private static Connection initialize() throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		return DB.getProtoDBInstance().getConnection();
	}
	
	private static void disconnect(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	public synchronized static boolean purgeDatabase() {
		try {
			DB.getProtoDBInstance().dropAllTables();
		
			setupDatabase();
			
			return true;
		}
		catch (Exception e) {
			Log.Error("Purge failed", LogType.MAIN, e);
			return false;
		}
	}

	public synchronized static boolean setupDatabase() throws ClassNotFoundException, SQLException, IDFieldNotFoundException, DatabaseNotSupportedException {
		try {
			Version ver = DB.getVersion();
			if (ver.getMajor() == 0 && ver.getMinor() == 10) {
				ProtoDB db = getProtoDBInstance();
				
				db.setupDatabase(Movie.getDefaultInstance());
				db.setupDatabase(se.qxx.jukebox.domain.JukeboxDomain.Version.getDefaultInstance());
				db.setupDatabase(Series.getDefaultInstance());
				DB.insertVersion(new Version());
				
				return false;
			}
		} catch (ClassNotFoundException | SQLException
				| IDFieldNotFoundException | DatabaseNotSupportedException e) {
			Log.Error("Failed to setup database", Log.LogType.MAIN, e);
			
			throw e;
		}
		
		return true;
	}

	public static Movie getMovieByMediaID(int mediaID) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Movie> result =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
						.addFieldName("media.ID")
						.addSearchArgument(mediaID)
						.addOperator(ProtoDBSearchOperator.Equals));
				
				if (result.size() > 0)
					return result.get(0);
				else 
					return null;
			}
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
			
	}

	
	public static Media getMediaByFilename(String filename) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Media> result =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Media.getDefaultInstance())
						.addFieldName("filename")
						.addSearchArgument(filename)
						.addOperator(ProtoDBSearchOperator.Equals)
						.setShallow(false)
						.addExcludedObject("subs"));
				
				if (result.size() > 0)
					return result.get(0);
				else 
					return null;
			}
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
			
	}
	
	public static Media getMediaById(int mediaId)  {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				return db.get(mediaId, Media.getDefaultInstance());
			}
		} catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException e) {
			Log.Error(String.format("Failed to get media %s", mediaId), Log.LogType.MAIN, e);
		}
		
		return null;		
	}

	public static Movie getMovieBySubfilename(String subsFilename) throws DatabaseNotSupportedException {
		try {
			String searchString = replaceSearchString(subsFilename) + "%";
			
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Movie> result = 
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
						.addFieldName("media.subs.filename")
						.addSearchArgument(searchString)
						.addOperator(ProtoDBSearchOperator.Like));
				
				if (result.size() > 0)
					return result.get(0);
			}
			
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException | ProtoDBParserException | SearchOptionsNotInitializedException e) {
			Log.Error(String.format("Failed to get movie with subs filename %s", subsFilename), Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public static void purgeSeries() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				List<Series> result =
					db.search(SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
							.addFieldName("title")
							.addSearchArgument("%")
							.addOperator(ProtoDBSearchOperator.Like));
				
				for (Series s : result) {
					System.out.println(String.format("Purging :: %s", s.getTitle()));
					db.delete(s);
				}
			}
			
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException | DatabaseNotSupportedException | ProtoDBParserException | SearchOptionsNotInitializedException e) {
			Log.Error(String.format("Failed to purge series"), Log.LogType.MAIN, e);
		}		
	}

	/***
	 * This purges the subtitle queue from all items that are not present in
	 * the Episode and the Movie objects any more
	 */
	public  static void cleanSubtitleQueue() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = "UPDATE SubtitleQueue SET subtitleRetreiveResult = -2 WHERE ID NOT IN (SELECT _subtitleQueue_ID FROM Movie) AND ID NOT IN (SELECT _subtitleQueue_ID FROM Episode);";
				db.executeNonQuery(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to clean subtitle queue"), Log.LogType.MAIN, e);
		}		
	}
	
	public static int getTotalNrOfMovies() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = "SELECT COUNT(1) FROM Movie;";
				return db.executeScalar(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to clean subtitle queue"), Log.LogType.MAIN, e);
		}		
		
		return 0;
	}

	public static int getTotalNrOfSeries() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = "SELECT COUNT(1) FROM Series;";
				return db.executeScalar(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to clean subtitle queue"), Log.LogType.MAIN, e);
		}		
		
		return 0;
	}
	
	public static int getTotalNrOfSeasons(int seriesID) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = String.format("SELECT COUNT(1) FROM SeriesSeason_Season SS INNER JOIN Season S ON SS._season_ID = S.ID WHERE _series_ID = %s;", seriesID);
				return db.executeScalar(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to clean subtitle queue"), Log.LogType.MAIN, e);
		}		
		
		return 0;		
	}
	
	public static int getTotalNrOfEpisodes(int seasonID) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = String.format("SELECT COUNT(1) FROM SeasonEpisode_Episode SS INNER JOIN Episode E ON SS._episode_ID = E.ID WHERE _season_ID = %s;", seasonID);
				return db.executeScalar(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to clean subtitle queue"), Log.LogType.MAIN, e);
		}		
		
		return 0;				
	}

	public static void saveConversion(int id, String newFilename, int value) {
		try {
			ProtoDB db = getProtoDBInstance();

			String filename = newFilename.replace("'", "''");
			synchronized(db.getDBType() == DBType.Sqlite ? syncObject : new Object()) {
				String sql = String.format(
					"UPDATE Media "
					+ "SET _converterState_ID = %s "
					+ (StringUtils.isEmpty(newFilename) ? "" : String.format(", convertedFileName = '%s' ", filename))
					+ "WHERE ID = %s", 
						value,
						id);
				db.executeNonQuery(sql);
			}
		} catch (Exception e) {
			Log.Error(String.format("Failed to set download complete"), Log.LogType.MAIN, e);
		}						
	}

	public static void saveConversion(int id, int completedValue) {
		saveConversion(id, StringUtils.EMPTY, completedValue);
	}
}
