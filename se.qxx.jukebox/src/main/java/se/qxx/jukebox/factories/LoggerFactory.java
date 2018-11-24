package se.qxx.jukebox.factories;

import se.qxx.jukebox.core.Log;

public interface LoggerFactory {
	public Log create(Log.LogType logType);
}
