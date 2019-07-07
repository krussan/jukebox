package se.qxx.jukebox.builders;

import se.qxx.jukebox.builders.exceptions.DeprecatedBuilderException;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;

public abstract class MovieBuilder {

	private ISettings settings;
	private IJukeboxLogger log;
	
	public MovieBuilder(ISettings settings, IJukeboxLogger log) {
		this.setSettings(settings);
		this.setLog(log);
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public abstract MovieOrSeries extract(String filepath, String filename) throws DeprecatedBuilderException;

	protected Media getMedia(String filepath, String filename) {
		return Media.newBuilder().setID(-1).setFilename(filename).setFilepath(filepath).setIndex(1)
				.setDownloadComplete(false).build();
	}

}
