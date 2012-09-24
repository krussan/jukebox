package se.qxx.jukebox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.subtitles.SubFile.Rating;

public class DB {
    
	private DB() {
		
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Search
	//---------------------------------------------------------------------------------------
	
	public synchronized static ArrayList<Movie> searchMovies(String searchString) {
		Connection conn = null;
		try {
			conn = DB.initialize();
	
			PreparedStatement prep = conn.prepareStatement(
					" select ID, filename, filepath, title, year, type, format, sound, language, groupName, imdburl" +
					"      , duration, rating, director, story, identifier, identifierRating" +
					" FROM movie" +
					" WHERE title LIKE '%" + searchString + "%'"
					);
			//prep.setString(1, searchString);
			
			ResultSet rs = prep.executeQuery();
			ArrayList<Movie> result = new ArrayList<Movie>();
			while (rs.next()) {
				result.add(extractMovie(rs, conn));
			}
					
			return result;
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			
			return new ArrayList<Movie>();
		}finally {
			DB.disconnect(conn);
		}
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Movies
	//---------------------------------------------------------------------------------------
	
	public synchronized static Movie getMovieByFilename(String filename, String filepath) {
		Connection conn = null;
		try {
			conn = DB.initialize();

			PreparedStatement prep = conn.prepareStatement(
			" select ID, filename, filepath, title, year, type, format, sound, language, groupName, imdburl" +
			"      , duration, rating, director, story, identifier, identifierRating" +
			" from movie where filename = ? and filepath = ?");
					
			prep.setString(1, filename);
			prep.setString(2, filepath);
				
			ResultSet rs = prep.executeQuery();
			if (rs.next())
				return extractMovie(rs, conn);
			else
				return null;

		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			
			return null;
		}finally {
			DB.disconnect(conn);
		}
	}
		
	public synchronized static Movie getMovie(int id) {
		Connection conn = null;
		try {
			conn = DB.initialize();

			PreparedStatement prep = conn.prepareStatement(
				" select ID, filename, filepath, title, year, type, format, sound, language, groupName, imdburl" +
				"      , duration, rating, director, story, identifier, identifierRating" +
				" from movie where ID = ?");
					
			prep.setInt(1, id);
				
			ResultSet rs = prep.executeQuery();
			if (rs.next())
				return extractMovie(rs, conn);
			else
				return null;

		} catch (Exception e) {
			Log.Error("failed to get information from database", Log.LogType.MAIN, e);
			
			return null;
		}finally {
			DB.disconnect(conn);
		}
	}
	
	public synchronized static void updateMovie(Movie m) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			PreparedStatement prep = conn.prepareStatement(
				"update movie" +
				"  set filename = ?" +
				"    , filepath = ?" + 
				"    , title = ?" + 
				"    , year = ?" +
				"    , format = ?" +
				"    , sound = ?" +
				"    , language = ?" +
				"    , groupName = ?" +
				"    , imdburl = ?" +
				"    , duration = ?" +
				"    , rating = ?" +
				"    , director = ?" +
				"    , story = ?" +
				"    , identifier = ?" +
				"    , identifierRating = ?" +
				" WHERE ID = ?"
			);

			//TODO: Should we update image as well??
			addArguments(prep, m);
			prep.setInt(17, m.getID());
			
			prep.execute();
		}
		catch (Exception e) {
			Log.Error("Failed to update movie in DB", Log.LogType.MAIN, e);
		}finally {
			DB.disconnect(conn);
		}
	}

	public synchronized static Movie addMovie(Movie m) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			conn.setAutoCommit(false);
			
			PreparedStatement prep = conn.prepareStatement(
					"insert into movie " +
					"(filename, filepath, title, year, type, format, sound, language, groupName, imdburl, duration, rating, director, story, identifier, identifierRating)" +
					"values" +
					"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			addArguments(prep, m);
			prep.execute();
			
			int i = getIdentity(conn);
	
			for(String genre : m.getGenreList()) {
				int genreID = getGenreID(genre, conn);
				if (genreID == -1)
					genreID = addGenre(genre, conn);
				
				prep = conn.prepareStatement(
						"INSERT INTO MovieGenre (_movie_ID, _genre_ID) VALUES (?, ?)");
				prep.setInt(1, i);
				prep.setInt(2, genreID);
				
				prep.execute();
			}
			
			addImage(i, ImageType.Poster, m.getImage().toByteArray(), conn);
			
			Movie mm = Movie.newBuilder().mergeFrom(m).setID(i).build();
			
			conn.commit();
			
			return mm;
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
			try {
				conn.rollback();
			} catch (SQLException sqlEx) {}
			return null;
		}finally {
			DB.disconnect(conn);
		}
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Images
	//---------------------------------------------------------------------------------------
	
	private static void addImage(int movieID, ImageType imageType, byte[] data, Connection conn) throws SQLException {
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
	
	private static byte[] getImageData(int movieID, ImageType imageType, Connection conn) throws SQLException {
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
	
	private static int addGenre(String genre, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
				"INSERT INTO Genre (genreName) VALUES (?)");
		
		prep.setString(1, genre);
		
		prep.execute();
		int i = getIdentity(conn);

		return i;
	}
	
	private static int getGenreID(String genre, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
				"SELECT ID, genreName FROM Genre WHERE genreName = ?");
		
		prep.setString(1, genre);
		
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt("ID");
		else
			return -1;
	}
	
	private static List<String> getGenres(int movieID, Connection conn) throws SQLException {
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
	//------------------------------------------------------------------------ Subtitles
	//---------------------------------------------------------------------------------------
	
	/**
	 * Returns true if the movie has a subtitle downloaded or has a subtitle in the queue
	 * @param m
	 */
	public synchronized static boolean hasSubtitles(Movie m) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement(
				" SELECT 1 FROM subtitles" +
				" WHERE _movie_ID = ?" +
				" UNION " +
				" SELECT 1 FROM subtitleQueue " +
				" WHERE _movie_ID = ?");
			
			prep.setInt(1, m.getID());
			prep.setInt(2, m.getID());

			ResultSet rs = prep.executeQuery();
			return rs.next();
			
		}
		catch (Exception e) {
			Log.Error("Failed to check if subtitles exist", Log.LogType.MAIN, e);
			
		}finally {
			DB.disconnect(conn);
		}
		
		// if error occured then set to true to avoid downloading subtitles
		return true;
	}
	public synchronized static void addSubtitle(Movie m, String filename, String description, Rating rating) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			
			if (!subFileExist(m, filename, conn)) {
				PreparedStatement prep = conn.prepareStatement(
						"insert into subtitles " +
						"(_movie_ID, filename, description, rating)" +
						"values" +
						"(?, ?, ?, ?)");
				
				prep.setInt(1, m.getID());
				prep.setString(2, filename);
				prep.setString(3, description);
				prep.setString(4, rating.toString());
				prep.execute();				
			}
			else
			{
				
			}
			
						
		}
		catch (Exception e) {
			Log.Error("Failed to add subtitles to DB", Log.LogType.MAIN, e);
			
		}finally {
			DB.disconnect(conn);
		}
	}
	
	private static boolean subFileExist(Movie m, String filename, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
				"SELECT _movie_ID, filename, description, rating FROM subtitles WHERE _movie_ID = ? AND filename = ?");
		prep.setInt(1, m.getID());
		prep.setString(2, filename);

		ResultSet rs = prep.executeQuery();
		if (rs.next())
			return true;
		else
			return false;

	}

	public synchronized static void addMovieToSubtitleQueue(Movie m) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			PreparedStatement prep = conn.prepareStatement(
					"insert into subtitleQueue " +
					"(queuedAt, _movie_ID, result)" +
					"values" +
					"(datetime(), ?, 0)");
			
			prep.setInt(1, m.getID());
			prep.execute();
	
			int i = getIdentity(conn);
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
		}finally {
			DB.disconnect(conn);
		}
	}

	public synchronized static void setSubtitleDownloaded(Movie m, int result) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			PreparedStatement prep;
			if (result >= 0) {
				prep = conn.prepareStatement(
					"update subtitleQueue " +
					" set retreivedAt = datetime() " +
					"  , result = ?" +
					"where _movie_ID = ?");
			} else
			{
				prep = conn.prepareStatement(
					"update subtitleQueue " +
					" set result = ?" +
					"where _movie_ID = ?");
			}
			prep.setInt(1, result);
			prep.setInt(2, m.getID());
			prep.execute();
	
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", Log.LogType.MAIN, e);
		}finally {
			DB.disconnect(conn);
		}
	}

	
	public synchronized static ArrayList<Movie> getSubtitleQueue() {
		Connection conn = null;
		try {
			conn = DB.initialize();
	
			PreparedStatement prep = conn.prepareStatement(
					" SELECT M.ID, M.filename, M.filepath, M.title, M.year, M.type, M.format, M.sound, M.language, M.groupName, M.imdburl " +
					"      , M.duration, M.rating, M.director, M.story, M.identifier, M.identifierRating" +
					" FROM movie AS M" +
					" INNER JOIN subtitleQueue SQ ON SQ._movie_ID = M.ID" +
					" WHERE retreivedAt IS NULL AND result = 0"
					);
			//prep.setString(1, searchString);
			
			ResultSet rs = prep.executeQuery();
			ArrayList<Movie> result = new ArrayList<Movie>();
			while (rs.next()) {
				result.add(extractMovie(rs, conn));
			}
					
			return result;
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", Log.LogType.MAIN, e);
			
			return new ArrayList<Movie>();
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

	private static Movie extractMovie(ResultSet rs, Connection conn) throws SQLException {
		int id = rs.getInt("ID");
		byte[] imageData = getImageData(id, ImageType.Poster, conn);
				
		List<String> genres = getGenres(id, conn);
		
		Builder builder = Movie.newBuilder()
				.setID(id)
				.setFilename(rs.getString("filename"))
				.setFilepath(rs.getString("filepath"))
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
				.addAllGenre(genres);
		
		if (imageData != null)
			builder = builder.setImage(ByteString.copyFrom(imageData));
		
		return builder.build();
	}
	
	public static boolean executeUpgradeStatement(String sql) {
		Connection conn = null;
		try {
			conn = DB.initialize();
		
			PreparedStatement prep = conn.prepareStatement(sql);
			
			prep.execute();
			
			return true;
		}
		catch (Exception e) {
			Log.Error("Upgrade failed", LogType.UPGRADE, e);
			Log.Debug("Failing query was::", LogType.UPGRADE);
			Log.Debug(sql, LogType.UPGRADE);
			return false;
		}
		finally {
			DB.disconnect(conn);
		}
	}
	private static Connection initialize() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
	    return DriverManager.getConnection("jdbc:sqlite:jukebox.db");				
	}
	
	private static void disconnect(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	private static void addArguments(PreparedStatement prep, Movie m) throws SQLException {
		prep.setString(1, m.getFilename());
		prep.setString(2, m.getFilepath());
		prep.setString(3, m.getTitle());
		prep.setInt(4, m.getYear());
		prep.setString(5, m.getType());
		prep.setString(6, m.getFormat());
		prep.setString(7, m.getSound());
		prep.setString(8, m.getLanguage());
		prep.setString(9, m.getGroup());
		prep.setString(10, m.getImdbUrl());
		prep.setInt(11, m.getDuration());
		prep.setString(12, m.getRating());
		prep.setString(13, m.getDirector());
		prep.setString(14, m.getStory());
		prep.setString(15, m.getIdentifier().toString());
		prep.setInt(16, m.getIdentifierRating());
	}
	
	private static int getIdentity(Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt(1);
		else
			return -1;
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Purge
	//---------------------------------------------------------------------------------------

	public static boolean purgeDatabase() {
		Connection conn = null;
		String[] statements = new String[] {
			"DELETE FROM BlobData",
			"DELETE FROM MovieImage",
			"DELETE FROM subtitles",
			"DELETE FROM subtitleQueue",
			"DELETE FROM Genre",
			"DELETE FROM MovieGenre",
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

	public static boolean purgeSubs() {
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
