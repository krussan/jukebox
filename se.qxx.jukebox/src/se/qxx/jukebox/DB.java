package se.qxx.jukebox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.ByteString;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.subtitles.SubFile.Rating;

public class DB {
    
	private final static String[] COLUMNS = {"ID", "title", "year",
			"type", "format", "sound", "language", "groupName", "imdburl", "duration",
			"rating", "director", "story", "identifier", "identifierRating", "watched"};
	
	private static String connectionString = "jdbc:sqlite:jukebox.db";
	
	private DB() {
		
	}

	public static void setDatabase(String databaseFilename) {
		DB.connectionString = String.format("jdbc:sqlite:%s", databaseFilename);
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Search
	//---------------------------------------------------------------------------------------
	
	public synchronized static ArrayList<Movie> searchMoviesByTitle(String searchString) {
		Connection conn = null;
		String statement = String.format("%s WHERE title LIKE ?", getSelectStatement());

		try {
			conn = DB.initialize();
	
			PreparedStatement prep = conn.prepareStatement(statement);
			prep.setString(1, "%" + searchString + "%");
			
			ResultSet rs = prep.executeQuery();
			ArrayList<Movie> result = new ArrayList<Movie>();
			while (rs.next()) {
				result.add(extractMovie(rs, conn));
			}
					
			return result;
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			return new ArrayList<Movie>();
		}finally {
			DB.disconnect(conn);
		}
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

	public synchronized static Movie getMovieByStartOfMediaFilename(String startOfMediaFilename) {
		Connection conn = null;
		String statement = String.format(
				" SELECT %s " +
				" FROM Media MD" +
				" INNER JOIN Movie M ON MD._movie_ID = M.ID" +
				" WHERE MD.filename LIKE ?"
				, getColumnList("M", true, ","));
		
		try {
			conn = DB.initialize();

			PreparedStatement prep = conn.prepareStatement(statement);
			prep.setString(1, startOfMediaFilename + "%");
				
			ResultSet rs = prep.executeQuery();
			if (rs.next())
				return extractMovie(rs, conn);
			else
				return null;

		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}finally {
			DB.disconnect(conn);
		}
	}

	public synchronized static Media getMediaByStartOfFilename(String startOfFilename) {
		Connection conn = null;
		String statement = 
				" SELECT ID, _movie_id, idx, filename, filepath, metaDuration, metaFramerate" +
				" FROM Media" +
				" WHERE filename LIKE ?";
		try {
			conn = DB.initialize();

			PreparedStatement prep = conn.prepareStatement(statement);
			prep.setString(1, startOfFilename + "%");
				
			ResultSet rs = prep.executeQuery();
			if (rs.next())
				return extractMedia(rs, conn);
			else
				return null;

		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}finally {
			DB.disconnect(conn);
		}
	}

	public synchronized static Movie getMovie(int id) {
		Connection conn = null;
		String statement = String.format("%s WHERE ID = ?", getSelectStatement());

		try {
			conn = DB.initialize();

			PreparedStatement prep = conn.prepareStatement(statement);
					
			prep.setInt(1, id);
				
			ResultSet rs = prep.executeQuery();
			if (rs.next())
				return extractMovie(rs, conn);
			else
				return null;

		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}finally {
			DB.disconnect(conn);
		}
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

	public synchronized static Movie addMovie(Movie m) {
		Connection conn = null;
		String statement = getInsertStatement();
		
		try {
			conn = DB.initialize();
			conn.setAutoCommit(false);
			
			PreparedStatement prep = conn.prepareStatement(statement);
			
			addArguments(prep, m);
			prep.execute();
			
			int i = getIdentity(conn);
	
			addMovieGenres(m.getGenreList(), i, conn);
			addMovieMedia(m.getMediaList(), i, conn);
			addImage(i, ImageType.Poster, m.getImage().toByteArray(), conn);
			
			Movie mm = Movie.newBuilder(m).setID(i).build();
			
			conn.commit();
			
			return mm;
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			try {
				conn.rollback();
			} catch (SQLException sqlEx) {}
			return null;
		}finally {
			DB.disconnect(conn);
		}
	}

	protected synchronized static void addMovieGenres(List<String> genres, int movieID, Connection conn) throws SQLException {
		PreparedStatement prep;
		for(String genre : genres) {
			int genreID = getGenreID(genre, conn);
			if (genreID == -1)
				genreID = addGenre(genre, conn);
			
			String statement = "INSERT INTO MovieGenre (_movie_ID, _genre_ID) VALUES (?, ?)"; 
			prep = conn.prepareStatement(statement);
			prep.setInt(1, movieID);
			prep.setInt(2, genreID);
			
			prep.execute();
		}
	}
	
	protected synchronized static void addMovieMedia(List<Media> medias, int movieID, Connection conn) throws SQLException {
		for(Media media : medias) {			
			addMedia(movieID, media, conn);
		}
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Images
	//---------------------------------------------------------------------------------------
	
	private synchronized static void addImage(int movieID, ImageType imageType, byte[] data, Connection conn) throws SQLException {
		if (data.length > 0) {
			PreparedStatement prep = conn.prepareStatement(
				" INSERT INTO BlobData (data) VALUES (?)");
			
			prep.setBytes(1, data);
			prep.execute();
			
			int id = getIdentity(conn);
			
			prep = conn.prepareStatement(
				" INSERT INTO MovieImage (_movie_id, _blob_id, imageType) VALUES (?, ?, ?)");
			
			prep.setInt(1, movieID);
			prep.setInt(2, id);
			prep.setString(3, imageType.toString());
					
			prep.execute();
		}
	}
	
	private synchronized static byte[] getImageData(int movieID, ImageType imageType, Connection conn) throws SQLException {
		byte[] data = null;
		PreparedStatement prep = conn.prepareStatement(
		   " SELECT B.data" +
		   " FROM MovieImage MI" +
		   " INNER JOIN BlobData B ON MI._blob_id = B.id " +
		   " WHERE MI._movie_id = ?");
		
		prep.setInt(1, movieID);
		
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
		{
			data = rs.getBytes("data");
		}
		
		return data;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Genres
	//---------------------------------------------------------------------------------------
	
	private synchronized static int addGenre(String genre, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
				"INSERT INTO Genre (genreName) VALUES (?)");
		
		prep.setString(1, genre);
		
		prep.execute();
		int i = getIdentity(conn);

		return i;
	}
	
	private synchronized static int getGenreID(String genre, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
				"SELECT ID, genreName FROM Genre WHERE genreName = ?");
		
		prep.setString(1, genre);
		
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt("ID");
		else
			return -1;
	}
	
	private synchronized static List<String> getGenres(int movieID, Connection conn) throws SQLException {
		List<String> list = new ArrayList<String>();
		
		PreparedStatement prep = conn.prepareStatement(
			" SELECT G.genreName FROM MovieGenre MG" +
			" INNER JOIN Genre G ON MG._genre_ID = G.ID" +
			" WHERE MG._movie_ID = ?");
		
		prep.setInt(1, movieID);
		
		ResultSet rs = prep.executeQuery();
		
		while (rs.next()) 
			list.add(rs.getString("genreName"));
		
		return list;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Media
	//---------------------------------------------------------------------------------------
	private synchronized static List<Media> getMedia(int movieID, Connection conn) throws SQLException {
		List<Media> list = new ArrayList<Media>();
		
		PreparedStatement prep = conn.prepareStatement(
			" SELECT MD.ID, MD._movie_ID, MD.filename, MD.filepath, MD.idx, MD.metaDuration, MD.metaFramerate " +
			" FROM Media MD" +
			" WHERE MD._movie_ID = ?" +
			" ORDER BY idx");
		
		prep.setInt(1, movieID);
		ResultSet rs = prep.executeQuery();
				
		while (rs.next()) {
			Media md = extractMedia(rs, conn);

			list.add(md);
		}
		
		return list;
	}
	
	public synchronized static int addMedia(int movieid, Media media) {
		Connection conn = null;
		
		try {
			conn = DB.initialize();

			return addMedia(movieid, media, conn);
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie subtitles from DB", Log.LogType.MAIN, e);
			
		}finally {
			DB.disconnect(conn);
		}

		return -1;
	}

	protected synchronized static int addMedia(int movieid, Media media, Connection conn)
			throws SQLException {
		String statement = "INSERT INTO Media (_movie_ID, idx, filepath, filename, metaDuration, metaFramerate) VALUES (?, ?, ?, ?, ?, ?)"; 
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, movieid);
		prep.setInt(2, media.getIndex());
		prep.setString(3, media.getFilepath());
		prep.setString(4, media.getFilename());
		prep.setInt(5, media.getMetaDuration());
		prep.setString(6, media.getMetaFramerate());
		
		prep.execute();
		
		int i = getIdentity(conn);
		
		return i;
	}

	protected synchronized static Media extractMedia(ResultSet rs, Connection conn)
			throws SQLException {
		int mediaid = rs.getInt("ID");
		
		List<Subtitle> subs = getSubtitles(mediaid, conn);
					
		Media md = se.qxx.jukebox.domain.JukeboxDomain.Media.newBuilder()
				.setID(mediaid)
				.setIndex(rs.getInt("idx"))
				.setFilename(rs.getString("filename"))
				.setFilepath(rs.getString("filepath"))
				.setMetaDuration(rs.getInt("metaDuration"))
				.setMetaFramerate(rs.getString("metaFramerate"))
				.addAllSubs(subs)
				.build();
		return md;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Languages
	//---------------------------------------------------------------------------------------
	private synchronized static int getLanguageID(String language, Connection conn) throws ClassNotFoundException, SQLException {
		String statement =
				" SELECT ID FROM language " +
				" WHERE language = ?";		
				
		PreparedStatement prep = conn.prepareStatement(statement);
		
		prep.setString(1, language);

		ResultSet rs = prep.executeQuery();
		if (rs.next()) {
			return rs.getInt("ID");
		}
		else {
			return -1;
		}
		
		
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Subtitles
	//---------------------------------------------------------------------------------------
	
	/**
	 * Returns true if the movie has a subtitle downloaded or has a subtitle in the queue
	 * @param m
	 */
	public synchronized static boolean hasSubtitles(Movie m) {
		Connection conn = null;
		String statement =
				" SELECT 1 FROM subtitles S " +
				" INNER JOIN Media MD ON S._media_ID = MD.ID " +
				" WHERE _movie_ID = ?" +
				" UNION " +
				" SELECT 1 FROM subtitleQueue " +
				" WHERE _movie_ID = ?";
		
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement(statement);
			
			prep.setInt(1, m.getID());
			prep.setInt(2, m.getID());

			ResultSet rs = prep.executeQuery();
			return rs.next();
			
		}
		catch (Exception e) {
			Log.Error("Failed to check if subtitles exist", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
		}finally {
			DB.disconnect(conn);
		}
		
		// if error occured then set to true to avoid downloading subtitles
		return true;
	}
	public synchronized static void addSubtitle(Media md, String filename, String description, Rating rating, String language) {
		Connection conn = null;
		String statement = 
				"insert into subtitles " +
				"(_media_ID, filename, description, rating, _subtitleLanguage_ID)" +
				"values" +
				"(?, ?, ?, ?, ?)";
		
		try {
			conn = DB.initialize();
			int subLanguageID = getLanguageID(language, conn);
			
			if (!subFileExist(md, filename, language, conn) && subLanguageID > 0) {
				PreparedStatement prep = conn.prepareStatement(statement);
				
				prep.setInt(1, md.getID());
				prep.setString(2, filename);
				prep.setString(3, description);
				prep.setString(4, rating.toString());
				prep.setInt(5, subLanguageID);
				prep.execute();				
			}			
						
		}
		catch (Exception e) {
			Log.Error("Failed to add subtitles to DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
		}finally {
			DB.disconnect(conn);
		}
	}
	
	private synchronized static boolean subFileExist(Media md, String filename, String language, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
				"SELECT 1 FROM subtitles S " +
				"INNER JOIN Language L ON S._subtitleLanguage_ID = L.ID " +
				"WHERE _media_ID = ? AND filename = ? AND L.language = ?");
		
		prep.setInt(1, md.getID());
		prep.setString(2, filename);
		prep.setString(3, language);

		ResultSet rs = prep.executeQuery();
		if (rs.next())
			return true;
		else
			return false;
	}

	public synchronized static void addMovieToSubtitleQueue(Movie m) {
		Connection conn = null;
		String statement = 
				"insert into subtitleQueue " +
				"(queuedAt, _movie_ID, result)" +
				"values" +
				"(datetime(), ?, 0)";				
		try {
			conn = DB.initialize();
			PreparedStatement prep = conn.prepareStatement(statement);
			
			prep.setInt(1, m.getID());
			prep.execute();
	
//			int i = getIdentity(conn);
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
		}finally {
			DB.disconnect(conn);
		}
	}

	public synchronized static void setSubtitleDownloaded(Movie m, int result) {
		Connection conn = null;
		String statement = StringUtils.EMPTY;
		
		try {
			conn = DB.initialize();
			PreparedStatement prep;
			if (result >= 0) {
				statement = 
					"update subtitleQueue " +
					" set retreivedAt = datetime() " +
					"  , result = ?" +
					"where _movie_ID = ?";
			} 
			else {
				statement = 
					"update subtitleQueue " +
					" set result = ?" +
					"where _movie_ID = ?";
			}
			prep = conn.prepareStatement(statement);
			prep.setInt(1, result);
			prep.setInt(2, m.getID());
			prep.execute();
	
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
		}finally {
			DB.disconnect(conn);
		}
	}

	
	public synchronized static ArrayList<Movie> getSubtitleQueue() {
		Connection conn = null;
		String statement = 
				String.format("SELECT %s %s %s %s",
						getColumnList("M", true, ","),
						"FROM movie AS M",
						"INNER JOIN subtitleQueue SQ ON SQ._movie_ID = M.ID",
						"WHERE retreivedAt IS NULL AND result = 0");
				
		try {
			conn = DB.initialize();
	
			PreparedStatement prep = conn.prepareStatement(statement);
			
			ResultSet rs = prep.executeQuery();
			ArrayList<Movie> result = new ArrayList<Movie>();
			while (rs.next()) {
				result.add(extractMovie(rs, conn));
			}
					
			return result;
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return new ArrayList<Movie>();
		}finally {
			DB.disconnect(conn);
		}
	}
	
	public synchronized static ArrayList<Subtitle> getSubtitles(Media media) {
		Connection conn = null;
		
		try {
			conn = DB.initialize();

			return getSubtitles(media.getID(), conn);
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie subtitles from DB", Log.LogType.MAIN, e);
			
			return new ArrayList<Subtitle>();
		}finally {
			DB.disconnect(conn);
		}
	}

	private synchronized static ArrayList<Subtitle> getSubtitles(int mediaid, Connection conn) throws SQLException {

		String statement =
				" SELECT S._media_ID, S.filename, S.description, S.rating, MD.idx, L.language " +
				" FROM subtitles S" +
				" INNER JOIN Media MD ON S._media_ID = MD.ID" +
				" INNER JOIN Language L ON S._subtitleLanguage_ID = L.ID" +
				" WHERE _media_ID = ?";

		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, mediaid);
					
		ResultSet rs = prep.executeQuery();
		ArrayList<Subtitle> result = new ArrayList<Subtitle>();
		while (rs.next()) {
			result.add(extractSubtitle(rs, conn));
		}
				
		return result;
	}	
	
	public synchronized static boolean subFileExistsInDB(String filename) {
		Connection conn = null;
		String statement =
				" SELECT 1 " +
				" FROM subtitles" +
				" WHERE filename = ?";
		
		try {
			conn = DB.initialize();
	
			PreparedStatement prep = conn.prepareStatement(statement);
			prep.setString(1, filename);
						
			ResultSet rs = prep.executeQuery();

			return rs.next();
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie subtitles from DB", Log.LogType.MAIN, e);
			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
	
			return false;
		}finally {
			DB.disconnect(conn);
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
			
			PreparedStatement prep = conn.prepareStatement("SELECT minor, major FROM dbVersion");
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
		
		return new Version(major, minor);
	}
	
	public synchronized static void setVersion(Version ver) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement("UPDATE dbVersion SET major = ?, minor= ?");
			prep.setInt(1, ver.getMajor());
			prep.setInt(2, ver.getMinor());
			
			prep.execute();
		}
		finally {
			DB.disconnect(conn);
		}
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Helpers
	//---------------------------------------------------------------------------------------

	private synchronized static Movie extractMovie(ResultSet rs, Connection conn) throws SQLException {
		int id = rs.getInt("ID");
		byte[] imageData = getImageData(id, ImageType.Poster, conn);
				
		List<String> genres = getGenres(id, conn);
		List<Media> media = getMedia(id, conn);
		
		Builder builder = Movie.newBuilder()
				.setID(id)
				.addAllMedia(media)
				.setTitle(rs.getString("title"))
				.setYear(rs.getInt("year"))
				.setType(rs.getString("type"))
				.setFormat(rs.getString("format"))
				.setSound(rs.getString("sound"))
				.setLanguage(rs.getString("language"))
				.setGroup(rs.getString("groupName"))
				.setImdbUrl(rs.getString("imdburl"))
				.setDuration(rs.getInt("duration"))
				.setRating(rs.getString("rating"))
				.setDirector(rs.getString("director"))
				.setStory(rs.getString("story"))
				.setIdentifier(Identifier.valueOf(rs.getString("identifier")))
				.setIdentifierRating(rs.getInt("identifierRating"))
				.addAllGenre(genres)
				.setWatched(rs.getBoolean("watched"));
		
		if (imageData != null)
			builder = builder.setImage(ByteString.copyFrom(imageData));
		
		return builder.build();
	}

	private synchronized static Subtitle extractSubtitle(ResultSet rs, Connection conn) throws SQLException {		
		return Subtitle.newBuilder()
				.setFilename(rs.getString("filename"))
				.setDescription(rs.getString("description"))
				.setRating(rs.getString("rating"))
				.setMediaIndex(rs.getInt("idx"))
				.setLanguage(rs.getString("language"))
				.build();				
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
	    return DriverManager.getConnection(DB.connectionString);				
	}
	
	private static void disconnect(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	private synchronized static void addArguments(PreparedStatement prep, Movie m) throws SQLException {
		prep.setString(1, m.getTitle());
		prep.setInt(2, m.getYear());
		prep.setString(3, m.getType());
		prep.setString(4, m.getFormat());
		prep.setString(5, m.getSound());
		prep.setString(6, m.getLanguage());
		prep.setString(7, m.getGroup());
		prep.setString(8, m.getImdbUrl());
		prep.setInt(9, m.getDuration());
		prep.setString(10, m.getRating());
		prep.setString(11, m.getDirector());
		prep.setString(12, m.getStory());
		prep.setString(13, m.getIdentifier().toString());
		prep.setInt(14, m.getIdentifierRating());
		prep.setBoolean(15, m.getWatched());
	}
	
	private static int getIdentity(Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt(1);
		else
			return -1;
	}
	
	private static String getSelectStatement() {
		return String.format("SELECT %s FROM Movie", getColumnList("", true, ","));
	}
	
	private static String getColumnList(String alias, boolean includeIdColumn, String separator) {
		String prefix = StringUtils.EMPTY;
		if (!StringUtils.isEmpty(alias))
			prefix = alias + ".";

		if (!includeIdColumn) {
			List<String> updateColumns = new ArrayList<String>();
			
			for (String column : COLUMNS)
				if (!StringUtils.equalsIgnoreCase("ID", column))
					updateColumns.add(column);
			
			return prefix + StringUtils.join(updateColumns, separator + prefix) ;			
		}
		else {
			return prefix + StringUtils.join(COLUMNS, separator + prefix);			
		} 
	}

	private static String getUpdateStatement() {
		return String.format("UPDATE Movie SET %s = ?", getColumnList("", false, " = ?,"));
	}	
		
	private static String getInsertStatement() {
		return String.format("INSERT INTO Movie (%s) VALUES (%s)",
				getColumnList("", false, ","),
				StringUtils.repeat("?", ",", COLUMNS.length - 1));
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Purge
	//---------------------------------------------------------------------------------------

	public synchronized static boolean purgeDatabase() {
		Connection conn = null;
		String[] statements = new String[] {
			"DELETE FROM BlobData",
			"DELETE FROM MovieImage",
			"DELETE FROM subtitles",
			"DELETE FROM subtitleQueue",
			"DELETE FROM Genre",
			"DELETE FROM MovieGenre",
			"DELETE FROM Media",
			"DELETE FROM Movie"
		};
		
		try {
			conn = DB.initialize();
		
			for (String stmt : statements) {
				PreparedStatement prep = conn.prepareStatement(stmt);
				prep.execute();				
			}
			
			return true;
		}
		catch (Exception e) {
			Log.Error("Purge failed", LogType.MAIN, e);
			return false;
		}
		finally {
			DB.disconnect(conn);
		}		
	}

	public synchronized static boolean purgeSubs() {
		Connection conn = null;
		String[] statements = new String[] {
			"DELETE FROM subtitles",
			"DELETE FROM subtitleQueue"
		};
		
		try {
			conn = DB.initialize();
		
			for (String stmt : statements) {
				PreparedStatement prep = conn.prepareStatement(stmt);
				prep.execute();				
			}
			
			return true;
		}
		catch (Exception e) {
			Log.Error("Purge of subtitles failed", LogType.MAIN, e);
			return false;
		}
		finally {
			DB.disconnect(conn);
		}		
	}
	
}
