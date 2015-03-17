package se.qxx.jukebox;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.WireFormat.JavaType;

public class DB {
     
//	private final static String[] COLUMNS = {"ID", "title", "year",
//			"type", "format", "sound", "language", "groupName", "imdburl", "duration",
//			"rating", "director", "story", "identifier", "identifierRating", "watched",
//			"isTvEpisode", "season", "episode", "firstAirDate", "episodeTitle"};
//    " isTvEpisode bool NOT NULL DEFAULT 0," +
//	" episode int NULL, " +
//    " firstAirDate date NULL, " +
//	" _season_ID int NULL",

	private static String databaseFilename = "jukebox_proto.db";
	
	private DB() {
		
	} 

	private static String getDatabaseFilename() {
		return databaseFilename;
	}

	private static void setDatabaseFilename(String databaseFilename) {
		DB.databaseFilename = databaseFilename;
	}

	public static void setDatabase(String databaseFilename) {
		setDatabaseFilename(databaseFilename);
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Search
	//---------------------------------------------------------------------------------------
	
	public synchronized static List<Movie> searchMoviesByTitle(String searchString) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return 
				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
					"title", 
					"%" + searchString + "%", 
					true);
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			return new ArrayList<Movie>();
		}
	}

	public synchronized static List<Series> searchSeriesByTitle(String searchString) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return 
				db.find(JukeboxDomain.Series.getDefaultInstance(), 
					"title", 
					"%" + searchString + "%", 
					true);
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve series listing from DB", Log.LogType.MAIN, e);
			return new ArrayList<Series>();
		}
	}
	
	
	protected static List<Media> __parseDynamicListToMedia(
			List<DynamicMessage> result) throws InvalidProtocolBufferException {
		List<Media> movieResult = new ArrayList<Media>();
		for (DynamicMessage m : result)
			movieResult.add(Media.parseFrom(m.toByteString()));
		return movieResult;
	}

	private static String replaceSearchString(String searchString) {
		String ret = searchString;
		ret = StringUtils.replace(ret, "%", "\\%");
		ret = StringUtils.replace(ret, "_", "\\_");
		return StringUtils.trim(ret);
	}
	
//	public synchronized static Movie getMovieByStartOfMediaFilename(String startOfMediaFilename) {
//		String searchString = replaceSearchString(startOfMediaFilename) + "%";
//		
//		Log.Debug(String.format("DB :: Database search string :: %s", searchString), LogType.MAIN);
//				 
//		try {
//			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
//			List<Movie> result =
//				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
//					"media.filename", 
//					searchString, 
//					true);
//			
//			if (result.size() > 0)
//				return result.get(0);
//			else
//				return null;
//		} catch (Exception e) {
//			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
////			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//			return null;
//		}
//	}

	public synchronized static Movie findMovie(String identifiedTitle) {
		String searchString = replaceSearchString(identifiedTitle) + "%";
		
		Log.Debug(String.format("DB :: Series search string :: %s", searchString), LogType.MAIN);
		 
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Movie> result =
				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
					"identifiedTitle", 
					searchString, 
					true);
			
			if (result.size() > 0)
				return result.get(0);
			else 
				return null;
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}
	
	public synchronized static Series findSeries(String identifiedTitle) {
		String searchString = replaceSearchString(identifiedTitle) + "%";
		
		Log.Debug(String.format("DB :: Series search string :: %s", searchString), LogType.MAIN);
		 
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Series> result =
				db.find(JukeboxDomain.Series.getDefaultInstance(), 
					"identifiedTitle", 
					searchString, 
					true);
			
			if (result.size() > 0)
				return result.get(0);
			else 
				return null;
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Get
	//---------------------------------------------------------------------------------------

	public synchronized static Movie getMovie(int id) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			return db.get(id, Movie.getDefaultInstance());
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			
			
		}
		
		return null;
	}

	public synchronized static Series getSeries(int id) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			return db.get(id, Series.getDefaultInstance());
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public synchronized static Season getSeason(int id) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			return db.get(id, Season.getDefaultInstance());
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public synchronized static Episode getEpisode(int id) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			return db.get(id, Episode.getDefaultInstance());
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
		}
		
		return null;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Save
	//---------------------------------------------------------------------------------------

	public synchronized static Movie save(Movie m) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return db.save(m);
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}

	public synchronized static Media save(Media md) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return db.save(md);
		}
		catch (Exception e) {
			Log.Error("Failed to store media to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}
	
	public synchronized static Episode save(Episode episode) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return db.save(episode);
		}
		catch (Exception e) {
			Log.Error("Failed to store episode to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}	

	public synchronized static Series save(Series series) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return db.save(series);
		}
		catch (Exception e) {
			Log.Error("Failed to store episode to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}	

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Delete
	//---------------------------------------------------------------------------------------
	public synchronized static void delete(Movie m) throws ClassNotFoundException, SQLException {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			db.delete(m);
		}
		catch (ClassNotFoundException | SQLException e) {
			Log.Error("Failed to delete movie in DB", Log.LogType.MAIN, e);
			
			throw e;
		}		
	}
	
	public synchronized static void delete(Series s) throws ClassNotFoundException, SQLException {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			db.delete(s);
		}
		catch (ClassNotFoundException | SQLException e) {
			Log.Error("Failed to delete series in DB", Log.LogType.MAIN, e);
			
			throw e;
		}			
	}

	public synchronized static void delete(Season sn) throws ClassNotFoundException, SQLException {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			db.delete(sn);
		}
		catch (ClassNotFoundException | SQLException e) {
			Log.Error("Failed to delete season in DB", Log.LogType.MAIN, e);
			
			throw e;
		}			
	}

	public synchronized static void delete(Episode ep) throws ClassNotFoundException, SQLException {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			db.delete(ep);
		}
		catch (ClassNotFoundException | SQLException e) {
			Log.Error("Failed to delete episode in DB", Log.LogType.MAIN, e);
			
			throw e;
		}				
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Watched
	//---------------------------------------------------------------------------------------
	
	public synchronized static void toggleWatched(Movie m) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());

			db.save(Movie.newBuilder(m).setWatched(!m.getWatched()).build());

		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
		}

	}		

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Subtitles
	//---------------------------------------------------------------------------------------

	public synchronized static Movie addMovieToSubtitleQueue(Movie m) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			m = Movie.newBuilder(m).setSubtitleQueue(
				SubtitleQueue.newBuilder()
					.setID(-1)
					.setSubtitleRetreiveResult(0)
					.setSubtitleQueuedAt(getCurrentUnixTimestamp())
					.build())
				.build();
			
			return db.save(m);
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
		}
		return m;
	}
			
	public static long getCurrentUnixTimestamp() {
		return System.currentTimeMillis() / 1000L;
	}

	public synchronized static List<Movie> getSubtitleQueue() {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			return
				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
					"SubtitleQueue.subtitleRetreiveResult", 
					0, 
					false);

		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			return new ArrayList<Movie>();
		}
	}

	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Blacklist
	//---------------------------------------------------------------------------------------

	public synchronized static void addToBlacklist(Movie m) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			m = Movie.newBuilder(m).addBlacklist(m.getImdbId()).build();			
			
			db.save(m);
		}
		catch (Exception e) {
			Log.Error("Failed to add movie to blacklist in DB", Log.LogType.MAIN, e);
		}		
	}

	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Version
	//---------------------------------------------------------------------------------------
	public synchronized static Version getVersion() throws ClassNotFoundException {
		Connection conn = null;
		int minor = 0;
		int major = 0;
		
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement("SELECT minor, major FROM Version WHERE ID=1");
			ResultSet rs = prep.executeQuery();
			if (rs.next()) {
				minor = rs.getInt("minor");
				major = rs.getInt("major");
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
	
	public synchronized static void setVersion(Version ver) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement("UPDATE Version SET major = ?, minor= ? WHERE ID = 1");
			prep.setInt(1, ver.getMajor());
			prep.setInt(2, ver.getMinor());
			
			prep.execute();
		}
		finally {
			DB.disconnect(conn);
		}
	}
	
	private synchronized static void insertVersion(Version ver) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement("INSERT INTO Version (major, minor) VALUES (?, ?)");
			prep.setInt(1, ver.getMajor());
			prep.setInt(2, ver.getMinor());
			
			prep.execute();
		}
		finally {
			DB.disconnect(conn);
		}
	}
	
	public static String backup() throws IOException {
		String backupFilename = String.format("%s.bak", DB.getDatabaseFilename());
		File backup = new File(backupFilename);
		File current = new File(DB.getDatabaseFilename());
		
		int i = 1;
		while (backup.exists()) {
			backupFilename = String.format("%s.bak.%s", DB.getDatabaseFilename(), i);
			backup = new File(backupFilename);
			i++;
		}
		
		System.out.println(String.format("Making backup to :: %s", backupFilename));
		FileUtils.copyFile(current, backup);
		
		return backupFilename;
	}

	public static void restore(String backupFilename) throws IOException {
		File backup = new File(backupFilename);
		File restoreFile = new File(DB.getDatabaseFilename());
		
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
	
	private static Connection initialize() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
	    return DriverManager.getConnection(String.format("jdbc:sqlite:%s", DB.databaseFilename));				
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
			File f = new File(databaseFilename);
			f.delete();
		
			setupDatabase();
			
			return true;
		}
		catch (Exception e) {
			Log.Error("Purge failed", LogType.MAIN, e);
			return false;
		}
	}

	public static boolean setupDatabase() {
		try {
			File f = new File(DB.getDatabaseFilename());
			if (!f.exists()) {
				ProtoDB db = new ProtoDB(DB.getDatabaseFilename());			
				db.setupDatabase(Movie.getDefaultInstance());
				db.setupDatabase(se.qxx.jukebox.domain.JukeboxDomain.Version.getDefaultInstance());
				db.setupDatabase(Series.getDefaultInstance());
				DB.insertVersion(new Version());
			}
			
			return true;
		} catch (ClassNotFoundException | SQLException
				| IDFieldNotFoundException e) {
			Log.Error("Failed to setup database", Log.LogType.MAIN, e);
		}
		
		return false;
	}

	public static Movie getMovieByMediaID(int mediaID) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Movie> result =
				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
					"media.ID", 
					mediaID, 
					false);
			
			if (result.size() > 0)
				return result.get(0);
			else 
				return null;
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
			
	}

	
	public static Media getMediaByFilename(String filename) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Media> result =
				db.find(JukeboxDomain.Media.getDefaultInstance(), 
					"filename", 
					filename, 
					false);
			
			if (result.size() > 0)
				return result.get(0);
			else 
				return null;
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
			
	}

	
	public static Media getMediaById(int mediaId) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());			
			return db.get(mediaId, Media.getDefaultInstance());
		} catch (ClassNotFoundException | SQLException e) {
			Log.Error(String.format("Failed to get media %s", mediaId), Log.LogType.MAIN, e);
		}
		
		return null;		
	}

//	public static Media getMediaByStartOfFilename(String mediaFilename) {
//		try {
//			String searchString = replaceSearchString(mediaFilename) + "%";
//			
//			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
//			List<Media> result =
//				db.find(JukeboxDomain.Media.getDefaultInstance(), 
//					"filename", 
//					searchString, 
//					true);
//			
//			if (result.size() > 0)
//				return result.get(0);
//			
//		} catch (ClassNotFoundException | SQLException
//				| SearchFieldNotFoundException e) {
//			Log.Error(String.format("Failed to get media %s", mediaFilename), Log.LogType.MAIN, e);
//		}
//		
//		return null;
//	}
//	
	public static Movie getMovieBySubfilename(String subsFilename) {
		try {
			String searchString = replaceSearchString(subsFilename) + "%";
			
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Movie> result = 
				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
					"media.subs.filename", 
					searchString, 
					true);
			
			if (result.size() > 0)
				return result.get(0);
			
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException e) {
			Log.Error(String.format("Failed to get movie with subs filename %s", subsFilename), Log.LogType.MAIN, e);
		}
		
		return null;
	}

	public static void purgeSeries() {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Series> result =
					db.find(JukeboxDomain.Series.getDefaultInstance(), 
						"title", 
						"%", 
						true);
			
			for (Series s : result) {
				System.out.println(String.format("Purging :: %s", s.getTitle()));
				db.delete(s);
			}
			
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException e) {
			Log.Error(String.format("Failed to purge series"), Log.LogType.MAIN, e);
		}		
	}
	
}
