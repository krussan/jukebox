package se.qxx.jukebox;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class Cleaner extends JukeboxThread {
	private static Cleaner _instance = null; 
	
	public Cleaner() {
		super("Cleaner", 30*60*1000, LogType.FIND);
	}
	
	public static Cleaner get() {
		if (_instance == null)
			_instance = new Cleaner();
		
		return _instance;
	}

	@Override
	protected void initialize() {
	}

	@Override
	protected void execute() throws InterruptedException {
		Log.Info("Starting up cleaner thread", LogType.FIND);
		Log.Info("Cleaning up movies", LogType.FIND);
		cleanMovies();	
		
		Thread.sleep(15 * 60 * 1000);
		Log.Info("Cleaning up episodes", LogType.FIND);
		cleanEpisodes();
		
		Thread.sleep(15 * 60 * 1000);
		Log.Info("Cleaning up empty series", LogType.FIND);
		cleanEmptySeries();
		
	}
	
	private void cleanMovies() {
		List<Movie> movies = DB.searchMoviesByTitle("");
		for (Movie m : movies) {
			for (Media md : m.getMediaList()) {
				if (!mediaExists(md)) {
					Log.Debug(String.format("#####!!!!!! Media %s was not found. Deleting .... ", md.getFilename()), LogType.FIND);
					
					try {
						if (!Arguments.get().isCleanerLogOnly())
							DB.delete(m);
					} catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException ex) {
						Log.Error("Deletion of media failed", LogType.FIND, ex);
					}
				}
			}
		}
		
	}

	private void cleanEpisodes() {
		List<Series> series = DB.searchSeriesByTitle("");
		
		for (Series s : series) {
			for (Season ss : s.getSeasonList()) {
				for (Episode e : ss.getEpisodeList()) {
					for (Media md : e.getMediaList()) {
						if (!mediaExists(md)) {
							Log.Debug(String.format("#####!!!!!! Media %s was not found. Deleting .... ", md.getFilename()), LogType.FIND);
							
							try {
								if (!Arguments.get().isCleanerLogOnly())
									DB.delete(e);
							} catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException ex) {
								Log.Error("Deletion of media failed", LogType.FIND, ex);
							}
						}
					}					
				}
			}
		}
	}

	private void cleanEmptySeries() {
	}
	
	private boolean mediaExists(Media md) {
		File f = new File(Util.getFullFilePath(md));
		return f.exists();
	}

	@Override
	public int getJukeboxPriority() {
		return 3;
	}
}
