package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.settings.LogsTest;

public interface IJukeboxLogger {

	void Critical(String msg);

	void Critical(String msg, Exception e);

	void Error(String msg, Exception e);

	void Error(String msg);

	void Debug(String msg);

	void Info(String msg);

	LogsTest getLogger();

	String getLoggerFilename();

}
