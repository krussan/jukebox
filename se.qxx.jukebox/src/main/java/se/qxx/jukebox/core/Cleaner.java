package se.qxx.jukebox.core;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

@Singleton
public class Cleaner extends JukeboxThread implements ICleaner {
	private IDatabase database;
	private IArguments arguments;
	
	@Inject
	public Cleaner(IDatabase database, IExecutor executor, IArguments arguments, LoggerFactory loggerFactory) {
		super("Cleaner", 30*60*1000, loggerFactory.create(LogType.FIND), executor);
		this.setDatabase(database);
		this.setArguments(arguments);
	}
	
	public IArguments getArguments() {
		return arguments;
	}

	public void setArguments(IArguments arguments) {
		this.arguments = arguments;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	@Override
	protected void initialize() {
	}

	@Override
	protected void execute() throws InterruptedException {
		this.getLog().Info("Starting up cleaner thread");
		this.getLog().Info("Cleaning up movies");
		cleanMovies();	
		
		if (!this.isRunning())
			return;
		
		Thread.sleep(15 * 60 * 1000);
		this.getLog().Info("Cleaning up episodes");
		cleanEpisodes();
		
		if (!this.isRunning())
			return;
		
		Thread.sleep(15 * 60 * 1000);
		this.getLog().Info("Cleaning up empty series");
		cleanEmptySeries();
		
	}
	
	private void cleanMovies() {
		List<Movie> movies = this.getDatabase().searchMoviesByTitle("");
		
		//TODO: check that the path is part of a listener
		for (Movie m : movies) {
			for (Media md : m.getMediaList()) {
				if (!mediaExists(md)) {
					this.getLog().Debug(String.format("#####!!!!!! Media %s was not found. Deleting .... ", md.getFilename()));
					
					try {
						if (!this.getArguments().isCleanerLogOnly())
							this.getDatabase().delete(m);
					} catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException ex) {
						this.getLog().Error("Deletion of media failed", ex);
					}
				}
				
				if (!this.isRunning())
					return;
			}
		}
		
	}

	private void cleanEpisodes() {
		List<Series> series = this.getDatabase().searchSeriesByTitle("");
		
		//TODO: check that the path is part of a listener
		for (Series s : series) {
			for (Season ss : s.getSeasonList()) {
				for (Episode e : ss.getEpisodeList()) {
					for (Media md : e.getMediaList()) {
						if (!mediaExists(md)) {
							this.getLog().Debug(String.format("#####!!!!!! Media %s was not found. Deleting .... ", md.getFilename()));
							
							try {
								if (!this.getArguments().isCleanerLogOnly())
									this.getDatabase().delete(e);
							} catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException ex) {
								this.getLog().Error("Deletion of media failed", ex);
							}
						}
						
						if (!this.isRunning())
							return;
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

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.ICleaner#getJukeboxPriority()
	 */
	@Override
	public int getJukeboxPriority() {
		return 3;
	}

	@Override
	public Runnable getRunnable() {
		return this;
	}
}
