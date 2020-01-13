package se.qxx.jukebox.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.LogsTest;

public class Log implements IJukeboxLogger {
	final int DEBUG = 4;
	final int INFO = 3;
	final int ERROR = 2;
	final int CRITICAL = 1;
	private ISettings settings;
	private LogType logType;
	
	public enum LogType {
		ALL,
		MAIN,
		SUBS,
		FIND,
		COMM,
		UPGRADE,
		VLCRESPONSE,
		IMDB,
		WEBSERVER,
		DB,
		CONVERTER,
		CHECKER,
		NONE
	}
	
	@Inject
	public Log(ISettings settings, @Assisted LogType type) {
		this.setLogType(type);
		this.setSettings(settings);
	}
	
	public LogType getLogType() {
		return logType;
	}

	public void setLogType(LogType logType) {
		this.logType = logType;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public void Critical(String msg) {
		log(msg, "CRITICAL");
	}
	
	@Override
	public void Critical(String msg, Exception e) {
		log(msg, e, "CRITICAL");
	}
	
	@Override
	public void Error(String msg, Exception e) {
		log(msg, e, "ERROR");
	}

	@Override
	public void Error(String msg) {
		log(msg, "ERROR");
	}

	@Override
	public void Debug(String msg) {
		log(msg, "DEBUG");
	}
	
	@Override
	public void Info(String msg) {
		log(msg, "INFO");
	}

	private void log(String msg, String level) {
		int msgLevel = getLevel(level);
		String logMessage = getLogString(msg, level);
		try {
			
			if (this.getLogType() == LogType.NONE)
				return;
			
			if (this.getLogType() == LogType.ALL) {
				logToAll(msg, level);
				return;
			}
			
			LogsTest l = getLogger();
			
			if (l != null) {
				int logLevel = getLevel(l);
				if (logLevel >= msgLevel) {
					if (l.getType().toLowerCase().equals("file"))
						logToFile(l.getFilename(), logMessage);
					else if (l.getType().toLowerCase().equals("console"))
						logToConsole(logMessage);
				}
			}
			
		} catch (Exception e) {
			logToConsole(logMessage, e);
		}
	}
	
	private void logToAll(String msg, String level) {

		for(LogsTest l : this.getSettings().getSettings().getLogs()) {
			LogType logType = LogType.valueOf(l.getLogs());
			
			if (logType != LogType.ALL)
				log(msg, level);
		}
	}

	@Override
	public LogsTest getLogger() {
		for(LogsTest l : this.getSettings().getSettings().getLogs()) {
			LogType logType = LogType.valueOf(l.getLogs());
			
			if (logType == this.getLogType())
				return l;
		}
		
		return null;
	}
	
	@Override
	public String getLoggerFilename() {
		LogsTest l = getLogger();
		if (l != null)
			return l.getFilename();
		
		return null;
	}
	
	
	private void log(String msg, Exception e, String level) {
		log(msg, level);
		log(e.toString(), level);
		printStackTrace(e, level);
	}
	
	private void printStackTrace(Exception e, String level) {
		for (StackTraceElement ste : e.getStackTrace()) {
			log(String.format("%s\n", ste), level);
		}
	}
	
	private void logToFile(String filename, String msg) {
		try {
			java.io.FileWriter fs = new java.io.FileWriter(filename, true);
			fs.write(String.format("%s\n", msg));
			fs.close();
		}
		catch (Exception e) {
			logToConsole("---Exception occured in logging class---", e);
		}
	}
	
	private void logToConsole(String msg) {
		System.out.println(msg);
	}
	
	private void logToConsole(String msg, Exception e) {
		System.out.println(msg);
		System.out.println(e.toString());
	}
	
	private int getLevel(LogsTest l) {
		return getLevel(l.getLevel());
	}
	
	private int getLevel(String level) {
		if (level.toUpperCase().equals("DEBUG"))
			return DEBUG;
		else if (level.toUpperCase().equals("INFO"))
			return INFO;
		else if (level.toUpperCase().equals("ERROR"))
			return ERROR;
		else if (level.toUpperCase().equals("CRITICAL"))
			return CRITICAL;
		else
			return 99;
		
	}
	
	private String getLogString(String msg, String level) {
		return String.format("%s - [%s] - %s - %s", getDateString(), Thread.currentThread().getId(), level, msg);
	}
	
	private String getDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date date = new Date();
	    return dateFormat.format(date);
	}
}