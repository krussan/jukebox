package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter.Groups.Group;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter;

public class FilenameBuilder extends MovieBuilder {

	@Override
	public Movie extractMovie(String filepath, String filename) {
		Log.Info(String.format("FilenameBuilder filename :: %s", filename), LogType.FIND);
		int maxGroupMatch = 0;
		ArrayList<String> groupsToCheck = getGroupsToCheck();
		
		String 	title = "",  	
				type = "",
				format = "", 
				sound = "", 
				language = "", 
				group = "";
		int year = 0;

		//TODO: check that file ends with one of the listened for extensions and remove it
		// For now we remove any extension (beyond the last dot)	
		String fileNameToMatch = FilenameUtils.getBaseName(filename);
		
		for (Splitter splitter : Settings.get().getStringSplitters().getSplitter()) {
			
			//ignoring some keywords specified in xml
			String strIgnorePattern = splitter.getIgnore().trim();
			String fileNameWithoutIgnore = Util.replaceIgnorePattern(fileNameToMatch, strIgnorePattern);
						
			Log.Info(String.format("FilenameBuilder filename to match :: %s", fileNameWithoutIgnore), LogType.FIND);
			
			
			Pattern p = Pattern.compile(splitter.getRegex().trim(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(fileNameWithoutIgnore);

			int matches = 0;
			if (m.matches()) {
				for (String s : groupsToCheck) {
					if (!StringUtils.isEmpty(getProperty(splitter, m, s)))
						matches++;
				}
			}
		
			Log.Debug(String.format("FilenameBuilder :: nrOfMatches :: %s", matches), LogType.FIND);
			
			if (matches > maxGroupMatch) {
				maxGroupMatch = matches;
				
				title = getProperty(splitter, m, "title");

				String yearString = getProperty(splitter, m, "year");
				if (!StringUtils.isEmpty(yearString))
					if (Util.tryParseInt(yearString))
						year = Integer.parseInt(yearString);
				
				type = getProperty(splitter, m, "type");
				format = getProperty(splitter, m, "format");
				sound = getProperty(splitter, m, "sound");
				language = getProperty(splitter, m, "language");
				group = getProperty(splitter, m, "group");
				
			}
		}

		//if movie ends with extension then something is wrong
		if (filename.length() - 3 > 0) 
			if (title.endsWith(filename.substring(filename.length() - 3)))
				return null;
		
		if (maxGroupMatch > 0) {
			Log.Debug(String.format("FilenameBuilder :: Max match :: %s", maxGroupMatch), LogType.FIND);
			Movie movie = Movie.newBuilder()
				.setID(-1)
				.setTitle(title)
				.setYear(year)
				.setType(type)
				.setFormat(format)
				.setSound(sound)
				.setLanguage(language)
				.setGroupName(group)
				.setIdentifier(Identifier.Filename)
				.setIdentifierRating(Math.round(100 * maxGroupMatch / groupsToCheck.size()))
//				.setSubtitleRetreiveResult(0)
				.build();
						
			return movie;
		}
		else {
			// movie does not match
			Log.Info("FilenameBuilder :: No Match", LogType.FIND);
			return Movie.newBuilder().setID(-1).build();
		}
	}
	
	private String getProperty(Splitter splitter, Matcher matcher, String property) {
		int groupId = getGroupIndex(splitter, property);

		if (groupId > 0)
			if (matcher.group(groupId) != null)
				return StringUtils.trim(Util.parseAwaySpace(matcher.group(groupId)));

		return StringUtils.EMPTY;
	}

	private int getGroupIndex(Splitter s, String property) {
		for(Group g : s.getGroups().getGroup()) {
			if (StringUtils.equalsIgnoreCase(g.getProperty(), property))
				return g.getId();
		}

		return -1;
	}
}
