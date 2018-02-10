package se.qxx.jukebox;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.sun.jna.platform.FileUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class Cleaner implements Runnable {
	private boolean isRunning;

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	private static Cleaner _instance = null; 
	
	public static Cleaner get() {
		if (_instance == null)
			_instance = new Cleaner();
		
		return _instance;
	}

	@Override
	public void run() {
		this.setRunning(true);
		Util.waitForSettings();
		mainLoop();
	}
	
	private void mainLoop() {
		while(this.isRunning()) {
			Log.Info("Starting up cleaner thread", LogType.FIND);
			cleanMovies();	
			cleanEpisodes();
			cleanEmptySeries();
			
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	private void cleanMovies() {
		List<Movie> movies = DB.searchMoviesByTitle("%", false, true);
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
		List<Series> series = DB.searchSeriesByTitle("%", false, true);
		
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
//		List<Series> series = DB.searchSeriesByTitle("%", false, true);
//		int countEpisodes = 0;
//		int countSeasons = 0;
//		
//		for (Series s : series) {
//			for (Season ss : s.getSeasonList()) {
//			}
//		}
	}
	
	private boolean mediaExists(Media md) {
		File f = new File(Util.getFullFilePath(md));
		return f.exists();
	}
}
