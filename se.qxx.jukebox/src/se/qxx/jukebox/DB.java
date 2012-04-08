package se.qxx.jukebox;

import java.sql.*;
import java.util.ArrayList;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.subtitles.SubFile.Rating;

public class DB {
    
	private DB() {
		
	}
	
	public synchronized static Movie getMovie(String title) {
		Connection conn = null;
		try {
			conn = DB.initialize();

			PreparedStatement prep = conn.prepareStatement(
			" select ID, filename, filepath, title, year, type, format, sound, language, groupName, imdburl" +
			" from movie where title = ?");
					
			prep.setString(1, title);
				
			ResultSet rs = prep.executeQuery();
			if (rs.next())
				return extractMovie(rs);
			else
				return null;

		} catch (Exception e) {
			Log.Error("failed to get information from database", e);
			
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
				" WHERE ID = ?"
			);
			
			addArguments(prep, m);
			prep.setInt(11, m.getID());
			
			prep.execute();
		}
		catch (Exception e) {
			Log.Error("Failed to update movie in DB", e);
		}finally {
			DB.disconnect(conn);
		}
	}

	public synchronized static Movie addMovie(Movie m) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			PreparedStatement prep = conn.prepareStatement(
					"insert into movie " +
					"(filename, filepath, title, year, type, format, sound, language, groupName, imdburl)" +
					"values" +
					"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			addArguments(prep, m);
			prep.execute();
	
			int i = getIdentity(conn);
			Movie mm = Movie.newBuilder().mergeFrom(m).setID(i).build();
					
			return mm;
		}
		catch (Exception e) {
			Log.Error("Failed to store movie to DB", e);
			return null;
		}finally {
			DB.disconnect(conn);
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
	}
	
	private static int getIdentity(Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt(1);
		else
			return -1;
	}
	
	public synchronized static void addSubtitle(Movie m, String filename, String description, Rating rating) {
		Connection conn = null;
		try {
			conn = DB.initialize();
			
			PreparedStatement prep = conn.prepareStatement(
					"insert into subtitles " +
					"(_movie_ID, filename, description, rating)" +
					"values" +
					"(?, ?, ?, ?)");
			
			prep.setInt(1, m.getID());
			prep.setString(2, filename);
			prep.setString(4, description);
			prep.setString(4, rating.toString());
			prep.execute();
						
		}
		catch (Exception e) {
			Log.Error("Failed to add subtitles to DB", e);
			
		}finally {
			DB.disconnect(conn);
		}
	}
	
	public synchronized static ArrayList<Movie> searchMovies(String searchString) {
		Connection conn = null;
		try {
			conn = DB.initialize();
	
			PreparedStatement prep = conn.prepareStatement(
					" SELECT ID, filename, filepath, title, year, type, format, sound, language, groupName, imdburl " +
					" FROM movie" +
					" WHERE title LIKE '%" + searchString + "%'"
					);
			//prep.setString(1, searchString);
			
			ResultSet rs = prep.executeQuery();
			ArrayList<Movie> result = new ArrayList<Movie>();
			while (rs.next()) {
				result.add(extractMovie(rs));
			}
					
			return result;
		}
		catch (Exception e) {
			Log.Error("Failed to retrieve movie listing from DB", e);
			
			return new ArrayList<Movie>();
		}finally {
			DB.disconnect(conn);
		}
	}
	
	private static Movie extractMovie(ResultSet rs) throws SQLException {
		Movie m = Movie.newBuilder()
				.setID(rs.getInt("ID"))
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
				.build();

		return m;
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
}
