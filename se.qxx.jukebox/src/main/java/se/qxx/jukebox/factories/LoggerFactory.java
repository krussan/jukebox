package se.qxx.jukebox.factories;

import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.interfaces.IJukeboxLogger;

public interface LoggerFactory {
	public IJukeboxLogger create(Log.LogType logType);
}
