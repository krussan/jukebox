package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter;

public class FilenameBuilder extends MovieBuilder {

	@Override
	public Movie extractMovie(String filepath, String filename) {
		int maxGroupMatch = 0;
		ArrayList<String> groupsToCheck = getGroupsToCheck();
		
		String 	title = "", 
				type = "",
				format = "", 
				sound = "", 
				language = "", 
				group = "";
		int year = 0;
		
		String fileNameToMatch = filename;
		
		for (Splitter splitter : Settings.get().getStringSplitters().getSplitter()) {
			//ignoring some keywords specified in xml
			String strIgnorePattern = splitter.getIgnore().trim();
			fileNameToMatch = Util.replaceIgnorePattern(fileNameToMatch, strIgnorePattern);
			
			NamedPattern p = NamedPattern.compile(splitter.getRegex().trim(), Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
			NamedMatcher m = p.matcher(fileNameToMatch);

			int matches = 0;
			for (String s : groupsToCheck) {
				if (m.matches() && p.groupNames().contains(s))
					if (m.group(s) != null)
						if (m.group(s).length() > 0) 
							matches++;
			}
			
			
			if (matches > maxGroupMatch) {
				maxGroupMatch = matches;

				if (m.group("title") != null && p.groupNames().contains("title")) 
					title = Util.parseAwaySpace(m.group("title"));
				
				if (m.group("year") != null && p.groupNames().contains("year")) {
					String yearString = m.group("year");
					if (Util.tryParseInt(yearString))
						year = Integer.parseInt(yearString);
				}
				
				if (m.group("type") != null && p.groupNames().contains("type")) 
					type = Util.parseAwaySpace(m.group("type"));
				
				if (m.group("format") != null && p.groupNames().contains("format")) 
					format = Util.parseAwaySpace(m.group("format"));
				
				if (m.group("sound") != null && p.groupNames().contains("sound")) 
					sound = Util.parseAwaySpace(m.group("sound"));
				
				if (m.group("language") != null && p.groupNames().contains("language")) 
					language = Util.parseAwaySpace(m.group("language"));
				
				if (m.group("group") != null && p.groupNames().contains("group")) 
					group = Util.parseAwaySpace(m.group("group"));
				
			}
			
		}

		//if movie ends with extension then something is wrong
		if (title.endsWith(filename.substring(filename.length() - 3)))
			return null;
		
		if (maxGroupMatch > 0) {
			Movie movie = Movie.newBuilder()
				.setID(-1)
				.setFilename(filename)
				.setFilepath(filepath)
				.setTitle(title)
				.setYear(year)
				.setType(type)
				.setFormat(format)
				.setSound(sound)
				.setLanguage(language)
				.setGroup(group)
				.setIdentifier(Identifier.Filename)
				.setIdentifierRating(Math.round(100 * maxGroupMatch / groupsToCheck.size()))
				.build();
						
			return movie;
		}
		else {
			// movie does not match
			return Movie.newBuilder().setID(-1).build();
		}
	}
}
