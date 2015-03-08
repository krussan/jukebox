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
	
	protected static List<Media> parseDynamicListToMedia(
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
	
	public synchronized static Movie getMovieByStartOfMediaFilename(String startOfMediaFilename) {
		String searchString = replaceSearchString(startOfMediaFilename) + "%";
		
		Log.Debug(String.format("DB :: Database search string :: %s", searchString), LogType.MAIN);
				 
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Movie> result =
				db.find(JukeboxDomain.Movie.getDefaultInstance(), 
					"media.filename", 
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
	
	public synchronized static Series findSeries(String seriesTitle) {
		String searchString = replaceSearchString(seriesTitle) + "%";
		
		Log.Debug(String.format("DB :: Series search string :: %s", searchString), LogType.MAIN);
		 
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Series> result =
				db.find(JukeboxDomain.Series.getDefaultInstance(), 
					"title", 
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

	public synchronized static Movie getMovie(int id) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			Movie m = db.get(id, Movie.getDefaultInstance());
			
			if (m!=null)
				return Movie.parseFrom(m.toByteString());
		
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			
			
		}
		
		return null;
	}


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
	//------------------------------------------------------------------------ Images
	//---------------------------------------------------------------------------------------
//	private synchronized static void addMovieImage(int movieID, ImageType imageType, byte[] data, Connection conn) throws SQLException {
//		addImage(movieID, "MovieImage", "_movie_ID", imageType, data, conn);
//	}
	
//	private synchronized static void addSeasonImage(int seasonID, ImageType imageType, byte[] data, Connection conn) throws SQLException {
//		addImage(seasonID, "SeasonImage", "_season_ID", imageType, data, conn);
//	}
	
//	private synchronized static void addImage(int ID, String tableName, String id_column, ImageType imageType, byte[] data, Connection conn) throws SQLException {
//		if (data.length > 0) {
//			PreparedStatement prep = conn.prepareStatement(
//				" INSERT INTO BlobData (data) VALUES (?)");
//			
//			prep.setBytes(1, data);
//			prep.execute();
//			
//			int id = getIdentity(conn);
//			
//			prep = conn.prepareStatement(
//				" INSERT INTO " + tableName + " (" + id_column + ", _blob_id, imageType) VALUES (?, ?, ?)");
//			
//			prep.setInt(1, ID);
//			prep.setInt(2, id);
//			prep.setString(3, imageType.toString());
//					
//			prep.execute();
//		}
//	}
	
//	private synchronized static byte[] getMovieImage(int movieID, ImageType imageType, Connection conn) throws SQLException {
//		return getImageData(movieID, "MovieImage", "_movie_ID", imageType, conn);
//	}
	
//	private synchronized static byte[] getSeasonImage(int seasonID, ImageType imageType, Connection conn) throws SQLException {
//		return getImageData(seasonID, "SeasonImage", "_season_ID", imageType, conn);
//	}
	
//	private synchronized static byte[] getImageData(int ID, String tableName, String id_column, ImageType imageType, Connection conn) throws SQLException {
//		byte[] data = null;
//		PreparedStatement prep = conn.prepareStatement(
//		   " SELECT B.data" +
//		   " FROM " + tableName + " A" +
//		   " INNER JOIN BlobData B ON A._blob_id = B.id " +
//		   " WHERE A." + id_column + " = ? AND A.imageType = ?");
//		
//		prep.setInt(1, ID);
//		prep.setString(2, imageType.toString());
//		
//		ResultSet rs = prep.executeQuery();
//		
//		if (rs.next())
//			data = rs.getBytes("data");
//		
//		return data;
//	}	

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Genres
	//---------------------------------------------------------------------------------------
	
//	private synchronized static int addGenre(String genre, Connection conn) throws SQLException {
//		PreparedStatement prep = conn.prepareStatement(
//				"INSERT INTO Genre (genreName) VALUES (?)");
//		
//		prep.setString(1, genre);
//		
//		prep.execute();
//		int i = getIdentity(conn);
//
//		return i;
//	}
//	
//	protected synchronized static void addMovieGenres(List<String> genres, int movieID, Connection conn) throws SQLException {
//		addGenres(movieID, "MovieGenre", "_movie_ID", genres, conn);
//	}
//
//	protected synchronized static void addSeasonGenres(List<String> genres, int seasonID, Connection conn) throws SQLException {
//		addGenres(seasonID, "SeasonGenre", "_season_ID", genres, conn);
//	}
//	
//	protected synchronized static void addGenres(int ID, String tableName, String id_column, List<String> genres, Connection conn) throws SQLException {
//		PreparedStatement prep;
//		for(String genre : genres) {
//			int genreID = getGenreID(genre, conn);
//			if (genreID == -1)
//				genreID = addGenre(genre, conn);
//			
//			String statement = "INSERT INTO " + tableName + " (" + id_column + ", _genre_ID) VALUES (?, ?)"; 
//			prep = conn.prepareStatement(statement);
//			prep.setInt(1, ID);
//			prep.setInt(2, genreID);
//			
//			prep.execute();
//		}
//	}	
//	
//	private synchronized static int getGenreID(String genre, Connection conn) throws SQLException {
//		PreparedStatement prep = conn.prepareStatement(
//				"SELECT ID, genreName FROM Genre WHERE genreName = ?");
//		
//		prep.setString(1, genre);
//		
//		ResultSet rs = prep.executeQuery();
//		
//		if (rs.next())
//			return rs.getInt("ID");
//		else
//			return -1;
//	}
//	
//	private synchronized static List<String> getGenres(int movieID, Connection conn) throws SQLException {
//		List<String> list = new ArrayList<String>();
//		
//		PreparedStatement prep = conn.prepareStatement(
//			" SELECT G.genreName FROM MovieGenre MG" +
//			" INNER JOIN Genre G ON MG._genre_ID = G.ID" +
//			" WHERE MG._movie_ID = ?");
//		
//		prep.setInt(1, movieID);
//		
//		ResultSet rs = prep.executeQuery();
//		
//		while (rs.next()) 
//			list.add(rs.getString("genreName"));
//		
//		return list;
//	}
	
/*	private static List<String> getSeasonGenres(int seasonid, Connection conn) throws SQLException {
		List<String> list = new ArrayList<String>();
		
		PreparedStatement prep = conn.prepareStatement(
			" SELECT G.genreName " +
		    " FROM SeasonGenre SG" +
			" INNER JOIN Genre G ON SG._genre_ID = G.ID" +
			" WHERE SG._season_ID = ?");
		
		prep.setInt(1, seasonid);
		
		ResultSet rs = prep.executeQuery();
		
		while (rs.next()) 
			list.add(rs.getString("genreName"));
		
		return list;
	}
*/	

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Media
	//---------------------------------------------------------------------------------------
//	public synchronized static Media getMediaById(int mediaID) {
//		Connection conn = null;
//		String statement = 
//				" SELECT ID, _movie_id, idx, filename, filepath, metaDuration, metaFramerate" +
//				" FROM Media" +
//				" WHERE ID = ?";
//		try {
//			conn = DB.initialize();
//
//			PreparedStatement prep = conn.prepareStatement(statement);
//			prep.setInt(1, mediaID);
//				
//			ResultSet rs = prep.executeQuery();
//			if (rs.next())
//				return extractMedia(rs, conn);
//			else
//				return null;
//
//		} catch (Exception e) {
//			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//			return null;
//		}finally {
//			DB.disconnect(conn);
//		}
//	}
//	
//	private synchronized static List<Media> getMedia(int movieID, Connection conn) throws SQLException {
//		List<Media> list = new ArrayList<Media>();
//		
//		PreparedStatement prep = conn.prepareStatement(
//			" SELECT MD.ID, MD._movie_ID, MD.filename, MD.filepath, MD.idx, MD.metaDuration, MD.metaFramerate " +
//			" FROM Media MD" +
//			" WHERE MD._movie_ID = ?" +
//			" ORDER BY idx");
//		
//		prep.setInt(1, movieID);
//		ResultSet rs = prep.executeQuery();
//				
//		while (rs.next()) {
//			Media md = extractMedia(rs, conn);
//
//			list.add(md);
//		}
//		
//		return list;
//	}
//	
//	public synchronized static int addMedia(int movieid, Media media) {
//		Connection conn = null;
//		
//		try {
//			conn = DB.initialize();
//
//			return addMedia(movieid, media, conn);
//		}
//		catch (Exception e) {
//			Log.Error("Failed to retrieve movie subtitles from DB", Log.LogType.MAIN, e);
//			
//		}finally {
//			DB.disconnect(conn);
//		}
//
//		return -1;
//	}
//
//	protected synchronized static int addMedia(int movieid, Media media, Connection conn)
//			throws SQLException {
//		String statement = "INSERT INTO Media (_movie_ID, idx, filepath, filename, metaDuration, metaFramerate) VALUES (?, ?, ?, ?, ?, ?)"; 
//		PreparedStatement prep = conn.prepareStatement(statement);
//		prep.setInt(1, movieid);
//		prep.setInt(2, media.getIndex());
//		prep.setString(3, media.getFilepath());
//		prep.setString(4, media.getFilename());
//		prep.setInt(5, media.getMetaDuration());
//		prep.setString(6, media.getMetaFramerate());
//		
//		prep.execute();
//		
//		int i = getIdentity(conn);
//		
//		return i;
//	}
//
//	protected synchronized static Media extractMedia(ResultSet rs, Connection conn)
//			throws SQLException {
//		int mediaid = rs.getInt("ID");
//		
//		List<Subtitle> subs = getSubtitles(mediaid, conn);
//					
//		Media md = se.qxx.jukebox.domain.JukeboxDomain.Media.newBuilder()
//				.setID(mediaid)
//				.setIndex(rs.getInt("idx"))
//				.setFilename(rs.getString("filename"))
//				.setFilepath(rs.getString("filepath"))
//				.setMetaDuration(rs.getInt("metaDuration"))
//				.setMetaFramerate(rs.getString("metaFramerate"))
//				.addAllSubs(subs)
//				.build();
//		return md;
//	}

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
					"subtitleRetreiveResult", 
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

	public static Media getMediaById(int mediaId) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());			
			return db.get(mediaId, Media.getDefaultInstance());
		} catch (ClassNotFoundException | SQLException e) {
			Log.Error(String.format("Failed to get media %s", mediaId), Log.LogType.MAIN, e);
		}
		
		return null;		
	}

	public static Media getMediaByStartOfFilename(String mediaFilename) {
		try {
			String searchString = replaceSearchString(mediaFilename) + "%";
			
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<Media> result =
				db.find(JukeboxDomain.Media.getDefaultInstance(), 
					"filename", 
					searchString, 
					true);
			
			if (result.size() > 0)
				return result.get(0);
			
		} catch (ClassNotFoundException | SQLException
				| SearchFieldNotFoundException e) {
			Log.Error(String.format("Failed to get media %s", mediaFilename), Log.LogType.MAIN, e);
		}
		
		return null;
	}
	
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
