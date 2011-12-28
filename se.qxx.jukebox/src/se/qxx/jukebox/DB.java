package se.qxx.jukebox;

import java.sql.*;
import java.util.ArrayList;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.subtitles.SubFile.Rating;

public class DB {
    
	private DB() {
		
	}

	public static Movie addMovie(Movie m) throws SQLException, ClassNotFoundException {
		Connection conn = DB.initialize();
		PreparedStatement prep = conn.prepareStatement(
				"insert into movie " +
				"(filename, title, year, type, format, sound, language, groupName, imdburl)" +
				"values" +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		prep.setString(1, m.getFilename());
		prep.setString(2, m.getTitle());
		prep.setInt(3, m.getYear());
		prep.setString(4, m.getType());
		prep.setString(5, m.getFormat());
		prep.setString(6, m.getSound());
		prep.setString(7, m.getLanguage());
		prep.setString(8, m.getGroup());
		prep.setString(9, m.getImdbUrl());
		
		prep.execute();

		int i = getIdentity(conn);
		Movie mm = Movie.newBuilder().mergeFrom(m).setID(i).build();
		
		DB.disconnect(conn);
		
		return mm;
	}
	
	private static int getIdentity(Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
		ResultSet rs = prep.getResultSet();
		
		rs.next();
		return rs.getInt(0);
	}
	
	public static void addSubtitle(Movie m, String filename, String description, Rating rating) throws SQLException, ClassNotFoundException {
		Connection conn = DB.initialize();
		
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
		
		
		DB.disconnect(conn);
	}
	
	public static ArrayList<Movie> searchMovies(String searchString) throws ClassNotFoundException, SQLException {
		Connection conn = DB.initialize();
		PreparedStatement prep = conn.prepareStatement(
				"SELECT ID, filename, title, year, type, format, sound, language, groupName, imdburl" +
				"FROM movie" +
				"WHERE title LIKE %?%"
				);
		prep.setString(1, searchString);
		
		ResultSet rs = prep.executeQuery();
		ArrayList<Movie> result = new ArrayList<Movie>();
		while (rs.next()) {
			Movie m = Movie.newBuilder()
				.setID(rs.getInt("ID"))
				.setFilename(rs.getString("filename"))
				.setTitle(rs.getString("title"))
				.setYear(rs.getInt("year"))
				.setType(rs.getString("type"))
				.setFormat(rs.getString("format"))
				.setSound(rs.getString("sound"))
				.setLanguage(rs.getString("language"))
				.setGroup(rs.getString("groupName"))
				.setImdbUrl(rs.getString("imdburl"))
				.build();
			
			result.add(m);
		}
		
		DB.disconnect(conn);
		
		return result;
		
		
				
	}
	private static Connection initialize() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
	    return DriverManager.getConnection("jdbc:sqlite:jukebox.db");				
	}
	
	private static void disconnect(Connection conn) throws SQLException {
		conn.close();
	}
}
