package se.qxx.jukebox;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;

public class Log {
	static final int DEBUG = 4;
	static final int INFO = 3;
	static final int ERROR = 2;
	static final int CRITICAL = 1;
	
	public static void Critical(String msg) {
		log(msg, "CRITICAL");
	}
	
	public static void Critical(String msg, Exception e) {
		log(msg, e, "CRITICAL");
	}
	
	public static void Error(String msg, Exception e) {
		log(msg, e, "ERROR");
	}
	
	public static void Debug(String msg) {
		log(msg, "DEBUG");
	}
	
	public static void Info(String msg) {
		log(msg, "INFO");
	}
	
	private static void log(String msg, String level) {
		int msgLevel = getLevel(level);
		String logMessage = getLogString(msg, level);
		try {
			for(JukeboxListenerSettings.Logs.Log l : Settings.get().getLogs().getLog()) {
				int logLevel = getLevel(l);
				if (logLevel >= msgLevel) {
					if (l.getType().toLowerCase().equals("file"))
						logToFile(l.getFilename(), logMessage);
					else if (l.getType().toLowerCase().equals("console"))
						logToConsole(logMessage);
				}
			}
		} catch (Exception e) {
			logToConsole(logMessage);
		}
	}
	
	private static void log(String msg, Exception e, String level) {
		log(msg, level);
		log(e.toString(), level);
		printStackTrace(e, level);
	}
	
	private static void printStackTrace(Exception e, String level) {
		for (StackTraceElement ste : e.getStackTrace()) {
			log(String.format("%s\n", ste), level);
		}
	}
	
	private static void logToFile(String filename, String msg) {
		try {
			java.io.FileWriter fs = new java.io.FileWriter(filename, true);
			fs.write(String.format("%s\n", msg));
			fs.close();
		}
		catch (Exception e) {
			logToConsole("---Exception occured in logging class---", e);
		}
	}
	
	private static void logToConsole(String msg) {
		System.out.println(msg);
	}
	
	private static void logToConsole(String msg, Exception e) {
		System.out.println(msg);
		System.out.println(e.toString());
	}
	
	private static int getLevel(JukeboxListenerSettings.Logs.Log l) {
		return getLevel(l.getLevel());
	}
	
	private static int getLevel(String level) {
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
	
	private static String getLogString(String msg, String level) {
		return String.format("%s - [%s] - %s - %s", getDateString(), Thread.currentThread().getId(), level, msg);
	}
	public static String getDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date date = new Date();
	    return dateFormat.format(date);
	}
}