package se.qxx.jukebox;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

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
	
	private static String connectionString = "jdbc:sqlite:jukebox.db";
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
	
	public synchronized static ArrayList<Movie> searchMoviesByTitle(String searchString) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<DynamicMessage> result =
				db.find(JukeboxDomain.Movie.getDescriptor(), 
					"title", 
					"%" + searchString + "%", 
					true);
			
			
			return parseDynamicListToMovie(result);
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			return new ArrayList<Movie>();
		}
	}

	protected static ArrayList<Movie> parseDynamicListToMovie(
			List<DynamicMessage> result) throws InvalidProtocolBufferException {
		ArrayList<Movie> movieResult = new ArrayList<Movie>();
		for (DynamicMessage m : result)
			movieResult.add(Movie.parseFrom(m.toByteString()));
		return movieResult;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Movies
	//---------------------------------------------------------------------------------------
//	public synchronized static Movie getMovieByFilename(String filename, String filepath) {
//		Connection conn = null;
//		String statement = String.format(
//				"SELECT %s FROM Movie M" +
//				" INNER JOIN Media MD ON MD._movie_ID = M.ID" +
//				" WHERE MD.filename = ? AND MD.filepath = ?"
//			, getColumnList("M", true, ","));
//
////		String statement = String.format("%s WHERE filename = ? and filepath = ?", getSelectStatement());
//
//		try {
//			conn = DB.initialize();
//
//			PreparedStatement prep = conn.prepareStatement(statement);
//			prep.setString(1, filename);
//			prep.setString(2, filepath);
//				
//			ResultSet rs = prep.executeQuery();
//			if (rs.next())
//				return extractMovie(rs, conn);
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

	private static String replaceSearchString(String searchString) {
		String ret = searchString;
		ret = StringUtils.replace(ret, "%", "\\%");
		ret = StringUtils.replace(ret, "_", "\\_");
		return ret;
	}
	
	public synchronized static Movie getMovieByStartOfMediaFilename(String startOfMediaFilename) {
		String searchString = replaceSearchString(startOfMediaFilename) + "%";
		
		Log.Debug(String.format("DB :: Database search string :: %s", searchString), LogType.MAIN);
				
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			List<DynamicMessage> result =
				db.find(JukeboxDomain.Movie.getDescriptor(), 
					"media.filename", 
					searchString, 
					true);
			
			if (result.size() > 0)
				return parseDynamicListToMovie(result).get(0);
			else
				return null;
			
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}

//	public synchronized static Media getMediaByStartOfFilename(String startOfFilename) {
//		Connection conn = null;
//		String searchString = replaceSearchString(startOfFilename) + "%";
//		String statement = 
//				" SELECT ID, _movie_id, idx, filename, filepath, metaDuration, metaFramerate" +
//				" FROM Media" +
//				" WHERE filename LIKE ? ESCAPE '\\'";
//		try {
//			conn = DB.initialize();
//
//			PreparedStatement prep = conn.prepareStatement(statement);
//			prep.setString(1, searchString);
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

	public synchronized static Movie getMovie(int id) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			DynamicMessage m = db.get(id, Movie.getDescriptor());
			
			if (m!=null)
				return Movie.parseFrom(m.toByteString());
		
		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			
			
		}
		
		return null;
	}
	
//	public synchronized static void updateMovie(Movie m) {
//		Connection conn = null;
//		String statement = String.format("%s WHERE ID = ?", getUpdateStatement());
//		try {
//			conn = DB.initialize();
//			PreparedStatement prep = conn.prepareStatement(statement);
//
//			//TODO: Should we update image as well??
//			addArguments(prep, m);
//			prep.setInt(COLUMNS.length, m.getID());
//			
//			prep.execute();
//		}
//		catch (Exception e) {
//			Log.Error("Failed to update movie in DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//	
//		}finally {
//			DB.disconnect(conn);
//		}
//	}

	public synchronized static Movie save(Movie m) {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());
			
			int id = db.save(m);
			Movie mm = Movie.newBuilder(m).setID(id).build();
			
			return mm;
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			
			return null;
		}
	}



//	
//	protected synchronized static void addMovieMedia(List<Media> medias, int movieID, Connection conn) throws SQLException {
//		for(Media media : medias) {			
//			addMedia(movieID, media, conn);
//		}
//	}
//	
	
//	public synchronized static boolean removeMovie(Movie m) {
//		Connection conn = null;
//		String currentStatement = StringUtils.EMPTY;
//		
//		String[] statementsMovie = new String[] {
//				"DELETE FROM MovieImage WHERE _movie_ID = ?",
//				"DELETE FROM subtitleQueue WHERE _movie_ID = ?",
//				"DELETE FROM MovieGenre WHERE _movie_ID = ?",
//				"DELETE FROM Media WHERE _movie_ID = ?",
//				"DELETE FROM Movie WHERE ID = ?"
//			};
//		String[] statementsMedia = new String[] {
//				"DELETE FROM subtitles WHERE _media_ID = ?"
//			};
//		
//		try {
//			conn = DB.initialize();
//			conn.setAutoCommit(false);
//			List<Media> media = getMedia(m.getID(), conn);
//			
//			for(String statement : statementsMedia) {
//				currentStatement = statement;
//				for (Media md : media) {
//					PreparedStatement prep = conn.prepareStatement(statement);
//					prep.setInt(1, md.getID());
//					
//					prep.execute();
//				}
//			}
//
//			for(String statement : statementsMovie) {
//				PreparedStatement prep = conn.prepareStatement(statement);
//				prep.setInt(1, m.getID());
//				
//				prep.execute();
//			}
//					
//			conn.commit();
//			
//			return true;
//		}
//		catch (Exception e) {
//			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", currentStatement), LogType.MAIN);
//			
//			try {
//				conn.rollback();
//			} catch (SQLException sqlEx) {}
//			return false;
//		}finally {
//			DB.disconnect(conn);
//		}		
//	}
	
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

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Languages
	//---------------------------------------------------------------------------------------
//	private synchronized static int getLanguageID(String language, Connection conn) throws ClassNotFoundException, SQLException {
//		String statement =
//				" SELECT ID FROM language " +
//				" WHERE language = ?";		
//				
//		PreparedStatement prep = conn.prepareStatement(statement);
//		
//		prep.setString(1, language);
//
//		ResultSet rs = prep.executeQuery();
//		if (rs.next()) {
//			return rs.getInt("ID");
//		}
//		else {
//			return -1;
//		}
//		
//		
//	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Subtitles
	//---------------------------------------------------------------------------------------
	
	/**
	 * Returns true if the movie has a subtitle downloaded or has a subtitle in the queue
	 * @param m
	 */
//	public synchronized static boolean hasSubtitles(Movie m) {
//		Connection conn = null;
//		String statement =
//				" SELECT 1 FROM subtitles S " +
//				" INNER JOIN Media MD ON S._media_ID = MD.ID " +
//				" WHERE _movie_ID = ?" +
//				" UNION " +
//				" SELECT 1 FROM subtitleQueue " +
//				" WHERE _movie_ID = ?";
//		
//		try {
//			conn = DB.initialize();
//			
//			PreparedStatement prep = conn.prepareStatement(statement);
//			
//			prep.setInt(1, m.getID());
//			prep.setInt(2, m.getID());
//
//			ResultSet rs = prep.executeQuery();
//			return rs.next();
//			
//		}
//		catch (Exception e) {
//			Log.Error("Failed to check if subtitles exist", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//		}finally {
//			DB.disconnect(conn);
//		}
//		
//		// if error occured then set to true to avoid downloading subtitles
//		return true;
//	}
//	public synchronized static void addSubtitle(Media md, String filename, String description, Rating rating, String language) {
//		Connection conn = null;
//		String statement = 
//				"insert into subtitles " +
//				"(_media_ID, filename, description, rating, _subtitleLanguage_ID)" +
//				"values" +
//				"(?, ?, ?, ?, ?)";
//		
//		try {
//			conn = DB.initialize();
//			int subLanguageID = getLanguageID(language, conn);
//			
//			if (!subFileExist(md, filename, language, conn) && subLanguageID > 0) {
//				PreparedStatement prep = conn.prepareStatement(statement);
//				
//				prep.setInt(1, md.getID());
//				prep.setString(2, filename);
//				prep.setString(3, description);
//				prep.setString(4, rating.toString());
//				prep.setInt(5, subLanguageID);
//				prep.execute();				
//			}			
//						
//		}
//		catch (Exception e) {
//			Log.Error("Failed to add subtitles to DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//		}finally {
//			DB.disconnect(conn);
//		}
//	}
//	
//	private synchronized static boolean subFileExist(Media md, String filename, String language, Connection conn) throws SQLException {
//		PreparedStatement prep = conn.prepareStatement(
//				"SELECT 1 FROM subtitles S " +
//				"INNER JOIN Language L ON S._subtitleLanguage_ID = L.ID " +
//				"WHERE _media_ID = ? AND filename = ? AND L.language = ?");
//		
//		prep.setInt(1, md.getID());
//		prep.setString(2, filename);
//		prep.setString(3, language);
//
//		ResultSet rs = prep.executeQuery();
//		if (rs.next())
//			return true;
//		else
//			return false;
//	}
//
//	public synchronized static void addMovieToSubtitleQueue(Movie m) {
//		Connection conn = null;
//		String statement = 
//				"insert into subtitleQueue " +
//				"(queuedAt, _movie_ID, result)" +
//				"values" +
//				"(datetime(), ?, 0)";				
//		try {
//			conn = DB.initialize();
//			PreparedStatement prep = conn.prepareStatement(statement);
//			
//			prep.setInt(1, m.getID());
//			prep.execute();
//	
//		}
//		catch (Exception e) {
//			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//		}finally {
//			DB.disconnect(conn);
//		}
//	}
//
//	public synchronized static void setSubtitleDownloaded(Movie m, int result) {
//		Connection conn = null;
//		String statement = StringUtils.EMPTY;
//		
//		try {
//			conn = DB.initialize();
//			PreparedStatement prep;
//			if (result >= 0) {
//				statement = 
//					"update subtitleQueue " +
//					" set retreivedAt = datetime() " +
//					"  , result = ?" +
//					"where _movie_ID = ?";
//			} 
//			else {
//				statement = 
//					"update subtitleQueue " +
//					" set result = ?" +
//					"where _movie_ID = ?";
//			}
//			prep = conn.prepareStatement(statement);
//			prep.setInt(1, result);
//			prep.setInt(2, m.getID());
//			prep.execute();
//	
//		}
//		catch (Exception e) {
//			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//		}finally {
//			DB.disconnect(conn);
//		}
//	}
//
//	
//	public synchronized static ArrayList<Movie> getSubtitleQueue() {
//		Connection conn = null;
//		String statement = 
//				String.format("SELECT %s %s %s %s",
//						getColumnList("M", true, ","),
//						"FROM movie AS M",
//						"INNER JOIN subtitleQueue SQ ON SQ._movie_ID = M.ID",
//						"WHERE retreivedAt IS NULL AND result = 0");
//				
//		try {
//			conn = DB.initialize();
//	
//			PreparedStatement prep = conn.prepareStatement(statement);
//			
//			ResultSet rs = prep.executeQuery();
//			ArrayList<Movie> result = new ArrayList<Movie>();
//			while (rs.next()) {
//				result.add(extractMovie(rs, conn));
//			}
//					
//			return result;
//		}
//		catch (Exception e) {
//			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//			
//			return new ArrayList<Movie>();
//		}finally {
//			DB.disconnect(conn);
//		}
//	}
//	
//	public synchronized static ArrayList<Subtitle> getSubtitles(Media media) {
//		Connection conn = null;
//		
//		try {
//			conn = DB.initialize();
//
//			return getSubtitles(media.getID(), conn);
//		}
//		catch (Exception e) {
//			Log.Error("Failed to retrieve movie subtitles from DB", Log.LogType.MAIN, e);
//			
//			return new ArrayList<Subtitle>();
//		}finally {
//			DB.disconnect(conn);
//		}
//	}
//
//	private synchronized static ArrayList<Subtitle> getSubtitles(int mediaid, Connection conn) throws SQLException {
//
//		String statement =
//				" SELECT S._media_ID, S.filename, S.description, S.rating, MD.idx, L.language " +
//				" FROM subtitles S" +
//				" INNER JOIN Media MD ON S._media_ID = MD.ID" +
//				" INNER JOIN Language L ON S._subtitleLanguage_ID = L.ID" +
//				" WHERE _media_ID = ?" +
//				" ORDER BY S.filename";
//
//		PreparedStatement prep = conn.prepareStatement(statement);
//		prep.setInt(1, mediaid);
//					
//		ResultSet rs = prep.executeQuery();
//		ArrayList<Subtitle> result = new ArrayList<Subtitle>();
//		while (rs.next()) {
//			result.add(extractSubtitle(rs, conn));
//		}
//				
//		return result;
//	}	
//	
//	public synchronized static boolean subFileExistsInDB(String filename) {
//		Connection conn = null;
//		String statement =
//				" SELECT 1 " +
//				" FROM subtitles" +
//				" WHERE filename = ?";
//		
//		try {
//			conn = DB.initialize();
//	
//			PreparedStatement prep = conn.prepareStatement(statement);
//			prep.setString(1, filename);
//						
//			ResultSet rs = prep.executeQuery();
//
//			return rs.next();
//		}
//		catch (Exception e) {
//			Log.Error("Failed to retrieve movie subtitles from DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//	
//			return false;
//		}finally {
//			DB.disconnect(conn);
//		}		
//	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Blacklist
	//---------------------------------------------------------------------------------------
	
//	public synchronized static void addToBlacklist(Movie m) {
//		Connection conn = null;
//		String statement =
//				" INSERT INTO Blacklist (filepath, filename, imdbid) " +
//				" VALUES (?, ?, ?)";
//		
//		try {
//			conn = DB.initialize();
//
//			for (Media md : m.getMediaList()) {
//				PreparedStatement prep = conn.prepareStatement(statement);
//				prep.setString(1, md.getFilepath());
//				prep.setString(2, md.getFilename());
//				prep.setString(3, Util.getImdbIdFromUrl(m.getImdbUrl()));
//							
//				prep.execute();
//			}
//
//		}
//		catch (Exception e) {
//			Log.Error("Failed to add movie to blacklist in DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//		}finally {
//			DB.disconnect(conn);
//		}		
//	}
//	
//	public synchronized static List<String> getBlacklist(Movie m) {
//		Connection conn = null;
//		String statement = StringUtils.EMPTY;
//		List<String> blacklistedIDs = new ArrayList<String>();
//		
//		try {
//			conn = DB.initialize();
//
//			for (Media md : m.getMediaList()) {
//				statement = 
//					" SELECT filepath, filename, imdbid " +
//					" FROM Blacklist " +
//					" WHERE filepath = ? AND filename = ?";
//				
//				PreparedStatement prep = conn.prepareStatement(statement);
//				prep.setString(1, md.getFilepath());
//				prep.setString(2, md.getFilename());
//				
//				ResultSet rs = prep.executeQuery();
//				
//				while (rs.next())
//					blacklistedIDs.add(rs.getString("imdbid"));
//			}
//			
//			return blacklistedIDs;
//		}
//		catch (Exception e) {
//			Log.Error("Failed to add movie to blacklist in DB", Log.LogType.MAIN, e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
//		}finally {
//			DB.disconnect(conn);
//		}		
//		
//		return blacklistedIDs;
//	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Version
	//---------------------------------------------------------------------------------------
	
//	public synchronized static Version getVersion() throws ClassNotFoundException {
//		Connection conn = null;
//		int minor = 0;
//		int major = 0;
//		
//		try {
//			conn = DB.initialize();
//			
//			PreparedStatement prep = conn.prepareStatement("SELECT minor, major FROM dbVersion");
//			ResultSet rs = prep.executeQuery();
//			if (rs.next()) {
//				minor = rs.getInt("minor");
//				major = rs.getInt("major");
//			}
//		} catch (Exception e) {
//			minor = 0;
//			major = 0;
//		}
//		
//		finally {
//			DB.disconnect(conn);
//		}
//		
//		return new Version(major, minor);
//	}
//	
//	public synchronized static void setVersion(Version ver) throws ClassNotFoundException, SQLException {
//		Connection conn = null;
//		try {
//			conn = DB.initialize();
//			
//			PreparedStatement prep = conn.prepareStatement("UPDATE dbVersion SET major = ?, minor= ?");
//			prep.setInt(1, ver.getMajor());
//			prep.setInt(2, ver.getMinor());
//			
//			prep.execute();
//		}
//		finally {
//			DB.disconnect(conn);
//		}
//	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Helpers
	//---------------------------------------------------------------------------------------

//	private synchronized static Movie extractMovie(ResultSet rs, Connection conn) throws SQLException {
//		int id = rs.getInt("ID");
//		byte[] imageData = getMovieImage(id, ImageType.Poster, conn);
//				
//		List<String> genres = getGenres(id, conn);
//		List<Media> media = getMedia(id, conn);
////		Season season = getSeason(id, conn);
//		
//		Builder builder = Movie.newBuilder()
//				.setID(id)
//				.addAllMedia(media)
//				.addAllGenre(genres);
//		
//		// Add simple entities
//		builder = mapResultSet(builder, rs);
//				
////				.setTitle(rs.getString("title"))
////				.setYear(rs.getInt("year"))
////				.setType(rs.getString("type"))
////				.setFormat(rs.getString("format"))
////				.setSound(rs.getString("sound"))
////				.setLanguage(rs.getString("language"))
////				.setGroup(rs.getString("groupName"))
////				.setImdbUrl(rs.getString("imdburl"))
////				.setDuration(rs.getInt("duration"))
////				.setRating(rs.getString("rating"))
////				.setDirector(rs.getString("director"))
////				.setStory(rs.getString("story"))
////				.setIdentifier(Identifier.valueOf(rs.getString("identifier")))
////				.setIdentifierRating(rs.getInt("identifierRating"))
////				.setWatched(rs.getBoolean("watched"))
////				.setIsTvEpisode(rs.getBoolean("isTvEpisode"))
////				.setEpisode(rs.getInt("episode"))
////				.setFirstAirDate(rs.getLong("firstAirDate"))
////				.setSeason(rs.getInt("season"))
////				.setEpisodeTitle(rs.getString("episodeTitle"));
////				.setSeason(season);	
//		
//
//		
//		if (imageData != null)
//			builder = builder.setImage(ByteString.copyFrom(imageData));
//		
//		return builder.build();
//	}
	
//	private synchronized static Builder mapResultSet(Builder b, ResultSet rs) throws SQLException {
//		List<FieldDescriptor> fields = Builder.getDescriptor().getFields();
//		for(FieldDescriptor field : fields) {
//			String fieldName = field.getName();
//			String fieldNameRep = fieldName.replace("_", "").replace(".","");
//
//			for (String s : COLUMNS) {
//				String sRep = s.replace("_", "").replace(".","");
//				if (StringUtils.equalsIgnoreCase(sRep, fieldNameRep)) {
//					b.setField(field, rs.getObject(fieldName));
//					break;
//				}
//					
//			}
//			
//			if (ArrayUtils.contains(COLUMNS, fieldName)) {
//				Log.Debug(String.format("DB :: Setting field %s", fieldName), LogType.MAIN);
//				
//			}
//		}		
//		
//		return b;
//	}

//	private synchronized static Subtitle extractSubtitle(ResultSet rs, Connection conn) throws SQLException {		
//		return Subtitle.newBuilder()
//				.setFilename(rs.getString("filename"))
//				.setDescription(rs.getString("description"))
//				.setRating(Rating.valueOf(rs.getString("rating")))
//				.setMediaIndex(rs.getInt("idx"))
//				.setLanguage(rs.getString("language"))
//				.build();				
//	}
//
//	public synchronized static boolean executeUpgradeStatements(String[] statements) {
//		Connection conn = null;
//		String sql = StringUtils.EMPTY;
//		try {
//			conn = DB.initialize();
//			conn.setAutoCommit(false);
//
//			int nrOfScripts = statements.length;
//			for (int i=0; i<statements.length;i++) {
//				sql = statements[i];
//				System.out.println(String.format("Running script\t\t[%s/%s]", i + 1, nrOfScripts));
//
//				PreparedStatement prep = conn.prepareStatement(sql);			
//				prep.execute();
//			}
//
//			conn.commit();
//			
//			return true;
//		}
//				
//		catch (Exception e) {
//			Log.Error("Upgrade failed", LogType.UPGRADE, e);
//			Log.Debug("Failing query was::", LogType.UPGRADE);
//			Log.Debug(sql, LogType.UPGRADE);
//			
//			try {
//				conn.rollback();
//			} catch (SQLException sqlEx) {}
//			
//			return false;
//		}
//		finally {
//			DB.disconnect(conn);
//		}
//	}
//	
//	private static Connection initialize() throws ClassNotFoundException, SQLException {
//		Class.forName("org.sqlite.JDBC");
//	    return DriverManager.getConnection(DB.connectionString);				
//	}
//	
//	private static void disconnect(Connection conn) {
//		try {
//			if (conn != null)
//				conn.close();
//		} catch (SQLException e) {
//		}
//	}
//
//	private synchronized static void addArguments(PreparedStatement prep, Movie m) throws SQLException {
//		Descriptor d = Movie.Builder.getDescriptor();
//		
//		prep.setString(1, m.getTitle());
//		prep.setInt(2, m.getYear());
//		prep.setString(3, m.getType());
//		prep.setString(4, m.getFormat());
//		prep.setString(5, m.getSound());
//		prep.setString(6, m.getLanguage());
//		prep.setString(7, m.getGroupName());
//		prep.setString(8, m.getImdbUrl());
//		prep.setInt(9, m.getDuration());
//		prep.setString(10, m.getRating());
//		prep.setString(11, m.getDirector());
//		prep.setString(12, m.getStory());
//		prep.setString(13, m.getIdentifier().toString());
//		prep.setInt(14, m.getIdentifierRating());
//		prep.setBoolean(15, m.getWatched());
//		prep.setBoolean(16, m.getIsTvEpisode());
//		prep.setInt(17, m.getEpisode());
//		prep.setLong(18, m.getFirstAirDate());
//		prep.setString(19, m.getEpisodeTitle());		
//	}
	
//	private static int getIdentity(Connection conn) throws SQLException {
//		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
//		ResultSet rs = prep.executeQuery();
//		
//		if (rs.next())
//			return rs.getInt(1);
//		else
//			return -1;
//	}
	
//	private static String getSelectStatement() {
//		return String.format("SELECT %s FROM Movie", getColumnList("", true, ","));
//	}
	
//	private static String getColumnList(String alias, boolean includeIdColumn, String separator) {
//		String prefix = StringUtils.EMPTY;
//		if (!StringUtils.isEmpty(alias))
//			prefix = alias + ".";
//
//		if (!includeIdColumn) {
//			List<String> updateColumns = new ArrayList<String>();
//			
//			for (String column : COLUMNS)
//				if (!StringUtils.equalsIgnoreCase("ID", column))
//					updateColumns.add(column);
//			
//			return prefix + StringUtils.join(updateColumns, separator + prefix) ;			
//		}
//		else {
//			return prefix + StringUtils.join(COLUMNS, separator + prefix);			
//		} 
//	}

//	private static String getUpdateStatement() {
//		return String.format("UPDATE Movie SET %s = ?", getColumnList("", false, " = ?,"));
//	}	
		
//	private static String getInsertStatement() {
//		return String.format("INSERT INTO Movie (%s) VALUES (%s)",
//				getColumnList("", false, ","),
//				StringUtils.repeat("?", ",", COLUMNS.length - 1));
//	}
//	
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Season
	//---------------------------------------------------------------------------------------
/*
	private static int addSeason(Season season, int movieid, Connection conn) throws SQLException {
		String statement = "INSERT INTO Season (seasonNumber, rating, story) VALUES (?, ?, ?)"; 
		PreparedStatement prep = conn.prepareStatement(statement);
		
		prep.setInt(1, season.getSeasonNumber());
		prep.setString(2, season.getRating());
		prep.setString(3, season.getStory());
		
		prep.execute();
		
		int seasonid = getIdentity(conn);
		
		addSeasonGenres(season.getGenreList(), seasonid, conn);
		
		statement = "UPDATE Movie SET _season_ID = ? WHERE ID = ?";
		prep = conn.prepareStatement(statement);
		
		prep.setInt(1, seasonid);
		prep.setInt(2, movieid);
	
		return seasonid;		
	}
	
	private static Season getSeason(int id, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
			" SELECT S.ID, S.rating, S.story, S.seasonNumber " +
			" FROM Season S" +
			" WHERE S.ID = ?");
		
		prep.setInt(1, id);
		ResultSet rs = prep.executeQuery();
		
		Season s = null;
		if (rs.next())
			s = extractSeason(rs, conn);
			
		return s;
	}
	
	protected synchronized static Season extractSeason(ResultSet rs, Connection conn)
			throws SQLException {
		int seasonid = rs.getInt("ID");
		
		List<String> genres = getSeasonGenres(seasonid, conn);
		byte[] imageData = getSeasonImage(seasonid, ImageType.Poster, conn);
		
		se.qxx.jukebox.domain.JukeboxDomain.Season.Builder b = se.qxx.jukebox.domain.JukeboxDomain.Season.newBuilder()
				.setID(seasonid)
				.setSeasonNumber(rs.getInt("seasonNumber"))
				.setRating(rs.getString("rating"))
				.setStory(rs.getString("story"))
				.addAllGenre(genres);
		
		if (imageData != null)
			b = b.setImage(ByteString.copyFrom(imageData));
		
		return b.build();
	}
*/	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Purge
	//---------------------------------------------------------------------------------------

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

//	public synchronized static boolean purgeSubs() {
//		Connection conn = null;
//		String[] statements = new String[] {
//			"DELETE FROM subtitles",
//			"DELETE FROM subtitleQueue"
//		};
//		
//		try {
//			conn = DB.initialize();
//		
//			for (String stmt : statements) {
//				PreparedStatement prep = conn.prepareStatement(stmt);
//				prep.execute();				
//			}
//			
//			return true;
//		}
//		catch (Exception e) {
//			Log.Error("Purge of subtitles failed", LogType.MAIN, e);
//			return false;
//		}
//		finally {
//			DB.disconnect(conn);
//		}		
//	}

	public static boolean setupDatabase() {
		try {
			ProtoDB db = new ProtoDB(DB.getDatabaseFilename());			
			db.setupDatabase(Movie.getDefaultInstance());
			
			return true;
		} catch (ClassNotFoundException | SQLException
				| IDFieldNotFoundException e) {
			Log.Error("Failed to setup database", Log.LogType.MAIN, e);
		}
		
		return false;
	}
	
}
