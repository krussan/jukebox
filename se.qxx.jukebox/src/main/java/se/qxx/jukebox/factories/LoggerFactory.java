package se.qxx.jukebox.factories;

import se.qxx.jukebox.Log;

public interface LoggerFactory {
	public Log create(Log.LogType logType);
}
