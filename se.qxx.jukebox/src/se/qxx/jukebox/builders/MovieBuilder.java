package se.qxx.jukebox.builders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Builders.Builder;
import se.qxx.jukebox.settings.Settings;

public abstract class MovieBuilder {
	
	public MovieBuilder() {
	}
	
	public abstract Movie extractMovie(String filepath, String filename);
	
	protected ArrayList<String> getGroupsToCheck() {
		ArrayList<String> groupsToCheck = new ArrayList<String>();
		groupsToCheck.add("title");
		groupsToCheck.add("year");
		groupsToCheck.add("type");
		groupsToCheck.add("format");
		groupsToCheck.add("sound");
		groupsToCheck.add("language");
		groupsToCheck.add("group");
		return groupsToCheck;
	}	
	
	/**
	 * Execute all enabled builders, perform rating and return the one with the best match.
	 * @param filepath
	 * @param filename
	 * @return
	 */
	public static Movie identifyMovie(String filepath, String filename) {
		ArrayList<Movie> proposals = new ArrayList<Movie>();
		
		// Execute all enabled builders, perform rating and return the one with the best match.
		for (Builder b : Settings.get().getBuilders().getBuilder()) {
			String className = b.getClazz();
			try {
				if (b.isEnabled()) {
					Object o = Util.getInstance(className);
					Movie proposal = ((MovieBuilder)o).extractMovie(filepath, filename);
					if (proposal != null)
						proposals.add(proposal);
				}
			} catch (Exception e) {
				Log.Error(String.format("Error when loading or executing movie builder %s", className), Log.LogType.FIND, e);
			}
		}
		
		Collections.sort(proposals, new MovieBuilderComparer());

		if (proposals.size() > 0) {
			return proposals.get(proposals.size() - 1);
		}
		else {
			Log.Info(String.format("Failed to identify movie with filename %s", filename), LogType.FIND);
			return null;
		}
	}
}
